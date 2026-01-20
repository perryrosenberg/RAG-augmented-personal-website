# Input variable definitions for RAG Personal Website

variable "aws_region" {
  description = "AWS region for all resources."
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "rag-personal-website"
}

variable "cors_allowed_origins" {
  description = "List of allowed origins for CORS"
  type        = list(string)
  default     = ["https://perryrosenberg.com", "https://www.perryrosenberg.com"]
}

variable "enable_cloudfront" {
  description = "Enable CloudFront distribution for the website"
  type        = bool
  default     = true
}

variable "domain_name" {
  description = "Custom domain name for the API (e.g., perryrosenberg.com)"
  type        = string
  default     = "perryrosenberg.com"
}

variable "certificate_arn" {
  description = "ARN of ACM certificate for custom domain (must be in us-east-1 for API Gateway)"
  type        = string
  default     = ""
}
