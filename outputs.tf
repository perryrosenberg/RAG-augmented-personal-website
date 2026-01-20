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

output "knowledge_base_id" {
  description = "ID of the Bedrock Knowledge Base."
  value       = aws_bedrockagent_knowledge_base.main.id
}

output "knowledge_base_arn" {
  description = "ARN of the Bedrock Knowledge Base."
  value       = aws_bedrockagent_knowledge_base.main.arn
}

output "vector_store_bucket_name" {
  description = "Name of the S3 Vectors bucket used for vector storage."
  value       = aws_s3vectors_vector_bucket.knowledge_base.vector_bucket_name
}

output "vector_index_name" {
  description = "Name of the S3 Vectors index."
  value       = aws_s3vectors_index.knowledge_base.index_name
}

output "vector_index_arn" {
  description = "ARN of the S3 Vectors index."
  value       = aws_s3vectors_index.knowledge_base.index_arn
}

output "data_source_id" {
  description = "ID of the Knowledge Base data source."
  value       = aws_bedrockagent_data_source.s3_documents.data_source_id
}
