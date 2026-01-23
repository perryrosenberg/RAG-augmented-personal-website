# RAG-Augmented Personal Website Infrastructure
# Based on Architecture.md specifications

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "rag-personal-website"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

# -----------------------------------------------------------------------------
# S3 BUCKETS
# -----------------------------------------------------------------------------

# Lambda deployment bucket
resource "random_pet" "lambda_bucket_name" {
  prefix = "rag-website-lambda"
  length = 2
}

resource "aws_s3_bucket" "lambda_bucket" {
  bucket = random_pet.lambda_bucket_name.id
}

resource "aws_s3_bucket_ownership_controls" "lambda_bucket" {
  bucket = aws_s3_bucket.lambda_bucket.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_acl" "lambda_bucket" {
  depends_on = [aws_s3_bucket_ownership_controls.lambda_bucket]
  bucket     = aws_s3_bucket.lambda_bucket.id
  acl        = "private"
}

# Knowledge base bucket (for RAG documents)
resource "aws_s3_bucket" "knowledge_bucket" {
  bucket = "${var.project_name}-knowledge-${var.environment}"
}

resource "aws_s3_bucket_versioning" "knowledge_bucket" {
  bucket = aws_s3_bucket.knowledge_bucket.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Static website bucket (for Next.js export)
resource "aws_s3_bucket" "website_bucket" {
  bucket = "${var.project_name}-website-${var.environment}"
}

resource "aws_s3_bucket_website_configuration" "website_bucket" {
  bucket = aws_s3_bucket.website_bucket.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "404.html"
  }
}

resource "aws_s3_bucket_public_access_block" "website_bucket" {
  bucket = aws_s3_bucket.website_bucket.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "website_bucket" {
  depends_on = [aws_s3_bucket_public_access_block.website_bucket]
  bucket     = aws_s3_bucket.website_bucket.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObject"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.website_bucket.arn}/*"
      }
    ]
  })
}

# -----------------------------------------------------------------------------
# DYNAMODB
# -----------------------------------------------------------------------------

resource "aws_dynamodb_table" "conversations" {
  name         = "${var.project_name}-conversations-${var.environment}"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "sessionId"
  range_key    = "timestamp"

  attribute {
    name = "sessionId"
    type = "S"
  }

  attribute {
    name = "timestamp"
    type = "N"
  }

  ttl {
    attribute_name = "expiresAt"
    enabled        = true
  }

  tags = {
    Name = "RAG Conversations"
  }
}

resource "aws_dynamodb_table" "document_metadata" {
  name         = "${var.project_name}-documents-${var.environment}"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "documentId"

  attribute {
    name = "documentId"
    type = "S"
  }

  tags = {
    Name = "Document Metadata"
  }
}

# -----------------------------------------------------------------------------
# S3 VECTORS FOR VECTOR STORAGE
# -----------------------------------------------------------------------------
# COMMENTED OUT - Managed manually in AWS Console
# # S3 Vectors bucket for storing vector embeddings
# resource "aws_s3vectors_vector_bucket" "knowledge_base" {
#   vector_bucket_name = "${var.project_name}-vectors-${var.environment}"
# }
#
# # S3 Vectors index for semantic search
# resource "aws_s3vectors_index" "knowledge_base" {
#   index_name         = "${var.project_name}-kb-index-${var.environment}"
#   vector_bucket_name = aws_s3vectors_vector_bucket.knowledge_base.vector_bucket_name
#
#   data_type       = "float32"
#   dimension       = 1024
#   distance_metric = "euclidean"
# }

# -----------------------------------------------------------------------------
# IAM ROLES
# -----------------------------------------------------------------------------
# COMMENTED OUT - Bedrock KB IAM roles managed manually in AWS Console
# # IAM role for Bedrock Knowledge Base
# resource "aws_iam_role" "bedrock_kb" {
#   name = "${var.project_name}-bedrock-kb-${var.environment}"
#
#   assume_role_policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Action = "sts:AssumeRole"
#         Effect = "Allow"
#         Principal = {
#           Service = "bedrock.amazonaws.com"
#         }
#         Condition = {
#           StringEquals = {
#             "aws:SourceAccount" = data.aws_caller_identity.current.account_id
#           }
#           ArnLike = {
#             "aws:SourceArn" = "arn:aws:bedrock:${var.aws_region}:${data.aws_caller_identity.current.account_id}:knowledge-base/*"
#           }
#         }
#       }
#     ]
#   })
#
#   lifecycle {
#     ignore_changes = all
#   }
# }

# Get current AWS account ID
data "aws_caller_identity" "current" {}

# COMMENTED OUT - Bedrock KB IAM policies managed manually in AWS Console
# # IAM policy for Bedrock Knowledge Base to access S3 (knowledge bucket)
# resource "aws_iam_role_policy" "bedrock_kb_s3_knowledge" {
#   name = "${var.project_name}-bedrock-kb-s3-knowledge-policy"
#   role = aws_iam_role.bedrock_kb.id
#
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Effect = "Allow"
#         Action = [
#           "s3:GetObject",
#           "s3:ListBucket"
#         ]
#         Resource = [
#           aws_s3_bucket.knowledge_bucket.arn,
#           "${aws_s3_bucket.knowledge_bucket.arn}/*"
#         ]
#         Condition = {
#           StringEquals = {
#             "aws:PrincipalAccount" = data.aws_caller_identity.current.account_id
#           }
#         }
#       }
#     ]
#   })
#
#   lifecycle {
#     ignore_changes = all
#   }
# }
#
# # IAM policy for Bedrock Knowledge Base to access S3 Vectors
# resource "aws_iam_role_policy" "bedrock_kb_s3_vectors" {
#   name = "${var.project_name}-bedrock-kb-s3-vectors-policy"
#   role = aws_iam_role.bedrock_kb.id
#
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Effect = "Allow"
#         Action = [
#           "s3vectors:*"
#         ]
#         Resource = [
#           "arn:aws:s3vectors:${var.aws_region}:${data.aws_caller_identity.current.account_id}:bucket/${aws_s3vectors_vector_bucket.knowledge_base.vector_bucket_name}",
#           "arn:aws:s3vectors:${var.aws_region}:${data.aws_caller_identity.current.account_id}:bucket/${aws_s3vectors_vector_bucket.knowledge_base.vector_bucket_name}/index/${aws_s3vectors_index.knowledge_base.index_name}"
#         ]
#       },
#       {
#         Effect = "Allow"
#         Action = [
#           "s3:GetObject",
#           "s3:PutObject",
#           "s3:DeleteObject",
#           "s3:ListBucket"
#         ]
#         Resource = [
#           "arn:aws:s3:::${aws_s3vectors_vector_bucket.knowledge_base.vector_bucket_name}",
#           "arn:aws:s3:::${aws_s3vectors_vector_bucket.knowledge_base.vector_bucket_name}/*"
#         ]
#       }
#     ]
#   })
#
#   lifecycle {
#     ignore_changes = all
#   }
# }
#
# # IAM policy for Bedrock Knowledge Base to use embedding model
# resource "aws_iam_role_policy" "bedrock_kb_model" {
#   name = "${var.project_name}-bedrock-kb-model-policy"
#   role = aws_iam_role.bedrock_kb.id
#
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Effect = "Allow"
#         Action = [
#           "bedrock:InvokeModel"
#         ]
#         Resource = [
#           "arn:aws:bedrock:${var.aws_region}::foundation-model/amazon.titan-embed-text-v1",
#           "arn:aws:bedrock:${var.aws_region}::foundation-model/amazon.titan-embed-text-v2:0"
#         ]
#       }
#     ]
#   })
#
#   lifecycle {
#     ignore_changes = all
#   }
# }

# -----------------------------------------------------------------------------
# BEDROCK KNOWLEDGE BASE
# -----------------------------------------------------------------------------
# COMMENTED OUT - Managed manually in AWS Console
# # Bedrock Knowledge Base with S3 Vectors storage
# resource "aws_bedrockagent_knowledge_base" "main" {
#   name        = "${var.project_name}-kb-${var.environment}"
#   description = "RAG knowledge base for personal website (S3 Vectors storage)"
#   role_arn    = aws_iam_role.bedrock_kb.arn
#
#   knowledge_base_configuration {
#     type = "VECTOR"
#     vector_knowledge_base_configuration {
#       embedding_model_arn = "arn:aws:bedrock:${var.aws_region}::foundation-model/amazon.titan-embed-text-v2:0"
#       embedding_model_configuration {
#         bedrock_embedding_model_configuration {
#           dimensions          = 1024
#           embedding_data_type = "FLOAT32"
#         }
#       }
#     }
#   }
#
#   storage_configuration {
#     type = "S3_VECTORS"
#     s3_vectors_configuration {
#       index_arn = aws_s3vectors_index.knowledge_base.index_arn
#     }
#   }
#
#   depends_on = [
#     aws_iam_role_policy.bedrock_kb_s3_knowledge,
#     aws_iam_role_policy.bedrock_kb_s3_vectors,
#     aws_iam_role_policy.bedrock_kb_model,
#     aws_s3vectors_index.knowledge_base
#   ]
#
#   tags = {
#     Name = "RAG Knowledge Base"
#   }
#
#   lifecycle {
#     ignore_changes = all
#   }
# }
#
# # Bedrock Knowledge Base Data Source (connects to S3)
# resource "aws_bedrockagent_data_source" "s3_documents" {
#   knowledge_base_id = aws_bedrockagent_knowledge_base.main.id
#   name              = "s3-documents"
#   description       = "S3 bucket containing knowledge base documents (v2 with improved parser)"
#
#   data_source_configuration {
#     type = "S3"
#     s3_configuration {
#       bucket_arn = aws_s3_bucket.knowledge_bucket.arn
#       inclusion_prefixes = ["documents/"]
#     }
#   }
#
#   vector_ingestion_configuration {
#     chunking_configuration {
#       chunking_strategy = "FIXED_SIZE"
#       fixed_size_chunking_configuration {
#         max_tokens         = 300
#         overlap_percentage = 20
#       }
#     }
#   }
#
#   # Ignore all drift - this resource is manually managed in AWS Console
#   # Terraform only tracks the resource but won't modify it
#   lifecycle {
#     ignore_changes = all
#   }
#
#   depends_on = [
#     aws_bedrockagent_knowledge_base.main
#   ]
# }

# Lambda execution role
resource "aws_iam_role" "lambda_exec" {
  name = "${var.project_name}-lambda-exec-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_basic" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy" "lambda_permissions" {
  name = "${var.project_name}-lambda-permissions"
  role = aws_iam_role.lambda_exec.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:Query",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem"
        ]
        Resource = [
          aws_dynamodb_table.conversations.arn,
          aws_dynamodb_table.document_metadata.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.knowledge_bucket.arn,
          "${aws_s3_bucket.knowledge_bucket.arn}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "bedrock:InvokeModel"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "bedrock:Retrieve",
          "bedrock:RetrieveAndGenerate"
        ]
        Resource = "arn:aws:bedrock:${var.aws_region}:${data.aws_caller_identity.current.account_id}:knowledge-base/RA59IH60FE"
      },
      {
        Effect = "Allow"
        Action = [
          "aws-marketplace:ViewSubscriptions",
          "aws-marketplace:Subscribe"
        ]
        Resource = "*"
      }
    ]
  })
}

# -----------------------------------------------------------------------------
# LAMBDA FUNCTIONS
# -----------------------------------------------------------------------------

# Package Lambda code
# Note: Run 'gradlew clean jar' in the backend directory before applying Terraform
# Upload the fat JAR directly as a .zip (Lambda treats JARs as ZIPs)
# NOTE: The JAR must be built BEFORE running Terraform (see deploy.sh)
resource "aws_s3_object" "lambda_query_handler" {
  bucket = aws_s3_bucket.lambda_bucket.id
  key    = "query-handler.jar"
  source = "${path.module}/backend/build/libs/query-handler.jar"
  etag   = filemd5("${path.module}/backend/build/libs/query-handler.jar")
}

resource "aws_lambda_function" "query_handler" {
  function_name = "${var.project_name}-query-handler-${var.environment}"

  s3_bucket = aws_s3_bucket.lambda_bucket.id
  s3_key    = aws_s3_object.lambda_query_handler.key

  runtime = "java17"
  handler = "com.perryrosenberg.portfolio.handler.QueryHandler::handleRequest"

  source_code_hash = filebase64sha256("${path.module}/backend/build/libs/query-handler.jar")

  role = aws_iam_role.lambda_exec.arn

  memory_size = 512
  timeout     = 30

  environment {
    variables = {
      CONVERSATIONS_TABLE = aws_dynamodb_table.conversations.name
      DOCUMENTS_TABLE     = aws_dynamodb_table.document_metadata.name
      KNOWLEDGE_BUCKET    = aws_s3_bucket.knowledge_bucket.id
      KNOWLEDGE_BASE_ID   = "RA59IH60FE"  # Hardcoded - managed manually in AWS Console
      ENVIRONMENT         = var.environment
    }
  }
}

resource "aws_cloudwatch_log_group" "query_handler" {
  name              = "/aws/lambda/${aws_lambda_function.query_handler.function_name}"
  retention_in_days = 14
}

# -----------------------------------------------------------------------------
# BEDROCK MODEL INVOCATION LOGGING
# -----------------------------------------------------------------------------
# COMMENTED OUT - Managed manually in AWS Console
# # CloudWatch log group for Bedrock model invocations
# resource "aws_cloudwatch_log_group" "bedrock_invocations" {
#   name              = "/aws/bedrock/modelinvocations/${var.project_name}-${var.environment}"
#   retention_in_days = 14
# }
#
# # IAM role for Bedrock to write logs to CloudWatch
# resource "aws_iam_role" "bedrock_logging" {
#   name = "${var.project_name}-bedrock-logging-${var.environment}"
#
#   assume_role_policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Action = "sts:AssumeRole"
#         Effect = "Allow"
#         Principal = {
#           Service = "bedrock.amazonaws.com"
#         }
#       }
#     ]
#   })
#
#   lifecycle {
#     ignore_changes = all
#   }
# }
#
# # IAM policy for Bedrock logging role
# resource "aws_iam_role_policy" "bedrock_logging" {
#   name = "${var.project_name}-bedrock-logging-policy"
#   role = aws_iam_role.bedrock_logging.id
#
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Effect = "Allow"
#         Action = [
#           "logs:CreateLogStream",
#           "logs:PutLogEvents"
#         ]
#         Resource = "${aws_cloudwatch_log_group.bedrock_invocations.arn}:*"
#       }
#     ]
#   })
#
#   lifecycle {
#     ignore_changes = all
#   }
# }
#
# # Bedrock model invocation logging configuration
# resource "aws_bedrock_model_invocation_logging_configuration" "main" {
#   logging_config {
#     embedding_data_delivery_enabled = true
#     image_data_delivery_enabled     = true
#     text_data_delivery_enabled      = true
#
#     cloudwatch_config {
#       log_group_name = aws_cloudwatch_log_group.bedrock_invocations.name
#       role_arn       = aws_iam_role.bedrock_logging.arn
#     }
#   }
#
#   lifecycle {
#     ignore_changes = all
#   }
# }

# -----------------------------------------------------------------------------
# API GATEWAY
# -----------------------------------------------------------------------------

resource "aws_apigatewayv2_api" "main" {
  name          = "${var.project_name}-api-${var.environment}"
  protocol_type = "HTTP"

  cors_configuration {
    allow_headers = ["Content-Type", "Authorization"]
    allow_methods = ["GET", "POST", "OPTIONS"]
    allow_origins = var.cors_allowed_origins
    max_age       = 300
  }
}

resource "aws_apigatewayv2_stage" "main" {
  api_id      = aws_apigatewayv2_api.main.id
  name        = var.environment
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gateway.arn
    format = jsonencode({
      requestId      = "$context.requestId"
      ip             = "$context.identity.sourceIp"
      requestTime    = "$context.requestTime"
      httpMethod     = "$context.httpMethod"
      routeKey       = "$context.routeKey"
      status         = "$context.status"
      responseLength = "$context.responseLength"
    })
  }
}

resource "aws_cloudwatch_log_group" "api_gateway" {
  name              = "/aws/api-gateway/${var.project_name}-${var.environment}"
  retention_in_days = 14
}

resource "aws_apigatewayv2_integration" "query_handler" {
  api_id             = aws_apigatewayv2_api.main.id
  integration_type   = "AWS_PROXY"
  integration_uri    = aws_lambda_function.query_handler.invoke_arn
  integration_method = "POST"
}

resource "aws_apigatewayv2_route" "query" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "POST /api/query"
  target    = "integrations/${aws_apigatewayv2_integration.query_handler.id}"
}

resource "aws_apigatewayv2_route" "health" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "GET /api/health"
  target    = "integrations/${aws_apigatewayv2_integration.query_handler.id}"
}

resource "aws_lambda_permission" "api_gateway" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.query_handler.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.main.execution_arn}/*/*"
}

# -----------------------------------------------------------------------------
# API GATEWAY CUSTOM DOMAIN (Optional - requires ACM certificate)
# -----------------------------------------------------------------------------

resource "aws_apigatewayv2_domain_name" "api" {
  count       = var.certificate_arn != "" ? 1 : 0
  domain_name = var.domain_name

  domain_name_configuration {
    certificate_arn = var.certificate_arn
    endpoint_type   = "REGIONAL"
    security_policy = "TLS_1_2"
  }

  tags = {
    Name = "${var.project_name}-api-domain"
  }
}

resource "aws_apigatewayv2_api_mapping" "api" {
  count       = var.certificate_arn != "" ? 1 : 0
  api_id      = aws_apigatewayv2_api.main.id
  domain_name = aws_apigatewayv2_domain_name.api[0].id
  stage       = aws_apigatewayv2_stage.main.id
}

# -----------------------------------------------------------------------------
# ACM CERTIFICATE (for CloudFront - must be in us-east-1)
# -----------------------------------------------------------------------------

resource "aws_acm_certificate" "website" {
  count             = var.enable_cloudfront ? 1 : 0
  domain_name       = var.domain_name
  subject_alternative_names = ["www.${var.domain_name}"]
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name = "${var.project_name}-cert"
  }
}

# -----------------------------------------------------------------------------
# ROUTE 53 - Use existing hosted zone
# -----------------------------------------------------------------------------

data "aws_route53_zone" "main" {
  count = var.enable_cloudfront ? 1 : 0
  name  = var.domain_name
}

# DNS validation records for ACM certificate
resource "aws_route53_record" "cert_validation" {
  for_each = var.enable_cloudfront ? {
    for dvo in aws_acm_certificate.website[0].domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  } : {}

  allow_overwrite = true
  name            = each.value["name"]
  records         = [each.value["record"]]
  ttl             = 60
  type            = each.value["type"]
  zone_id         = data.aws_route53_zone.main[0].zone_id
}

resource "aws_acm_certificate_validation" "website" {
  count                   = var.enable_cloudfront ? 1 : 0
  certificate_arn         = aws_acm_certificate.website[0].arn
  validation_record_fqdns = [for record in aws_route53_record.cert_validation : record.fqdn]
}

# -----------------------------------------------------------------------------
# CLOUDFRONT (for production)
# -----------------------------------------------------------------------------

resource "aws_cloudfront_distribution" "website" {
  count = var.enable_cloudfront ? 1 : 0

  origin {
    domain_name = aws_s3_bucket_website_configuration.website_bucket.website_endpoint
    origin_id   = "S3-Website"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  # API Gateway origin for /api/* requests
  origin {
    domain_name = replace(aws_apigatewayv2_stage.main.invoke_url, "/^https?://([^/]*).*/", "$1")
    origin_id   = "API-Gateway"
    origin_path = "/${var.environment}"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "https-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"
  aliases             = [var.domain_name, "www.${var.domain_name}"]

  # Cache behavior for API requests - no caching, forward all
  ordered_cache_behavior {
    path_pattern     = "/api/*"
    allowed_methods  = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "API-Gateway"

    forwarded_values {
      query_string = true
      headers      = ["Authorization", "Origin", "Access-Control-Request-Headers", "Access-Control-Request-Method"]
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "https-only"
    min_ttl                = 0
    default_ttl            = 0
    max_ttl                = 0
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3-Website"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate_validation.website[0].certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }

  tags = {
    Name = "${var.project_name}-cdn"
  }

  depends_on = [aws_acm_certificate_validation.website]
}

# -----------------------------------------------------------------------------
# ROUTE 53 RECORDS - Point domain to CloudFront
# -----------------------------------------------------------------------------

resource "aws_route53_record" "website_a" {
  count   = var.enable_cloudfront ? 1 : 0
  zone_id = data.aws_route53_zone.main[0].zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.website[0].domain_name
    zone_id                = aws_cloudfront_distribution.website[0].hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "website_aaaa" {
  count   = var.enable_cloudfront ? 1 : 0
  zone_id = data.aws_route53_zone.main[0].zone_id
  name    = var.domain_name
  type    = "AAAA"

  alias {
    name                   = aws_cloudfront_distribution.website[0].domain_name
    zone_id                = aws_cloudfront_distribution.website[0].hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "website_www_a" {
  count   = var.enable_cloudfront ? 1 : 0
  zone_id = data.aws_route53_zone.main[0].zone_id
  name    = "www.${var.domain_name}"
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.website[0].domain_name
    zone_id                = aws_cloudfront_distribution.website[0].hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "website_www_aaaa" {
  count   = var.enable_cloudfront ? 1 : 0
  zone_id = data.aws_route53_zone.main[0].zone_id
  name    = "www.${var.domain_name}"
  type    = "AAAA"

  alias {
    name                   = aws_cloudfront_distribution.website[0].domain_name
    zone_id                = aws_cloudfront_distribution.website[0].hosted_zone_id
    evaluate_target_health = false
  }
}
