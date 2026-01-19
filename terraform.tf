# Copyright (c) HashiCorp, Inc.
# SPDX-License-Identifier: MPL-2.0

terraform {
  # Note: To use Terraform Cloud, uncomment the cloud block and set your organization:
  # cloud {
  #   organization = "your-organization-name"
  #   workspaces {
  #     name = "rag-personal-website"
  #   }
  # }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.17.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.7.2"
    }
    archive = {
      source  = "hashicorp/archive"
      version = "~> 2.7.1"
    }
  }

  required_version = "~> 1.2"
}
