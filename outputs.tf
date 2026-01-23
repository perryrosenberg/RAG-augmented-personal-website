# Output value definitions for RAG Personal Website

output "lambda_bucket_name" {
  description = "Name of the S3 bucket used to store Lambda function code."
  value       = aws_s3_bucket.lambda_bucket.id
}

output "knowledge_bucket_name" {
  description = "Name of the S3 bucket for RAG knowledge documents."
  value       = aws_s3_bucket.knowledge_bucket.id
}

output "website_bucket_name" {
  description = "Name of the S3 bucket for static website hosting."
  value       = aws_s3_bucket.website_bucket.id
}

output "website_url" {
  description = "URL of the static website."
  value       = aws_s3_bucket_website_configuration.website_bucket.website_endpoint
}

output "api_gateway_url" {
  description = "Base URL for the API Gateway."
  value       = aws_apigatewayv2_stage.main.invoke_url
}

output "lambda_function_name" {
  description = "Name of the query handler Lambda function."
  value       = aws_lambda_function.query_handler.function_name
}

output "conversations_table_name" {
  description = "Name of the DynamoDB conversations table."
  value       = aws_dynamodb_table.conversations.name
}

output "documents_table_name" {
  description = "Name of the DynamoDB document metadata table."
  value       = aws_dynamodb_table.document_metadata.name
}

output "cloudfront_domain" {
  description = "CloudFront distribution domain name (if enabled)."
  value       = var.enable_cloudfront ? aws_cloudfront_distribution.website[0].domain_name : null
}

output "api_custom_domain" {
  description = "Custom domain name for the API (if configured)."
  value       = var.certificate_arn != "" ? var.domain_name : null
}

output "api_custom_domain_target" {
  description = "Target domain name for DNS CNAME record (if custom domain configured)."
  value       = var.certificate_arn != "" ? aws_apigatewayv2_domain_name.api[0].domain_name_configuration[0].target_domain_name : null
}

output "website_domain" {
  description = "Primary website domain (if CloudFront enabled)."
  value       = var.enable_cloudfront ? var.domain_name : null
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID (for cache invalidation)."
  value       = var.enable_cloudfront ? aws_cloudfront_distribution.website[0].id : null
}

# COMMENTED OUT - Bedrock resources managed manually in AWS Console
# output "knowledge_base_id" {
#   description = "ID of the Bedrock Knowledge Base."
#   value       = "RA59IH60FE"  # Hardcoded
# }
#
# output "knowledge_base_arn" {
#   description = "ARN of the Bedrock Knowledge Base."
#   value       = "arn:aws:bedrock:us-east-1:${data.aws_caller_identity.current.account_id}:knowledge-base/RA59IH60FE"
# }
#
# output "vector_store_bucket_name" {
#   description = "Name of the S3 Vectors bucket used for vector storage."
#   value       = "rag-personal-website-vectors-dev"  # Managed manually
# }
#
# output "vector_index_name" {
#   description = "Name of the S3 Vectors index."
#   value       = "rag-personal-website-kb-index-dev"  # Managed manually
# }
#
# output "vector_index_arn" {
#   description = "ARN of the S3 Vectors index."
#   value       = "Managed manually"  # Get from AWS Console if needed
# }
#
# output "data_source_id" {
#   description = "ID of the Knowledge Base data source."
#   value       = "Managed manually"  # Get from AWS Console if needed
# }
