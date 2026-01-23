# RAG-Augmented Personal Website

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue?style=flat-square&logo=typescript)](https://www.typescriptlang.org/)
[![Next.js](https://img.shields.io/badge/Next.js-16-black?style=flat-square&logo=next.js)](https://nextjs.org/)
[![AWS](https://img.shields.io/badge/AWS-Lambda%20%7C%20Bedrock%20%7C%20S3-orange?style=flat-square&logo=amazon-aws)](https://aws.amazon.com/)
[![Terraform](https://img.shields.io/badge/Terraform-1.x-purple?style=flat-square&logo=terraform)](https://www.terraform.io/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

> A cloud-native personal portfolio website with production-grade RAG (Retrieval-Augmented Generation) capabilities, optimized for AWS Free Tier deployment.

**Live At:** [perryrosenberg.com](https://perryrosenberg.com)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Project Structure](#project-structure)
- [Documentation](#documentation)
- [Cost Analysis](#cost-analysis)
- [Contributing](#contributing)
- [License](#license)
- [Author](#author)

---

## 🎯 Overview

This project demonstrates enterprise-grade software engineering practices in a **cloud-native RAG system** built with modern AWS services. It serves as both a personal portfolio website and a technical showcase of:

- **Serverless Architecture**: AWS Lambda + API Gateway for zero-ops backend
- **RAG Implementation**: Amazon Bedrock Knowledge Base with S3 Vectors
- **Infrastructure as Code**: Complete Terraform configuration for reproducible deployments
- **Cost Optimization**: ~30,000x cost reduction vs OpenSearch Serverless (~$1-5/month vs $700/month)
- **Professional Code Quality**: Enterprise-grade Java and TypeScript with 70+ unit tests
- **Modern Frontend**: Next.js 16 with TypeScript, React 19, and Tailwind CSS

**Use Cases:**
- Personal portfolio websites with AI-powered Q&A
- Serverless RAG architecture reference implementation
- AWS Free Tier optimization strategies
- Modern full-stack development patterns

---

## ✨ Key Features

### 🤖 AI-Powered Chat Assistant

- **Conversational Interface**: Natural language Q&A about professional experience
- **Context-Aware Responses**: Retrieves relevant information from knowledge base documents
- **Source Attribution**: Displays source documents with confidence scores and excerpts
- **Streaming Responses**: Real-time answer generation with Amazon Bedrock (Claude 3 Haiku)

### 🏗️ Production-Grade Architecture

- **Serverless Backend**: Java 17 Lambda functions with fat JAR deployment
- **Vector Search**: S3 Vectors with Titan Embed Text v2 (1024 dimensions)
- **API Gateway**: HTTP API with CORS support and health checks
- **Static Frontend**: Next.js SSG hosted on S3 + CloudFront CDN
- **DynamoDB**: Conversation history with TTL for automatic cleanup
- **CloudWatch**: Comprehensive logging for Lambda and Bedrock invocations

### 💰 Cost-Optimized Infrastructure

- **S3 Vectors**: ~$0.023/GB + query costs (vs $700/month for OpenSearch Serverless)
- **AWS Free Tier**: Targets free tier limits for Lambda, API Gateway, S3, and DynamoDB
- **On-Demand Pricing**: No minimum charges or reserved capacity
- **Estimated Monthly Cost**: $1-5 for typical personal website traffic

### 🧪 Comprehensive Testing

- **Java Tests**: JUnit 5 tests with Mockito and AssertJ
  - Handler tests (API Gateway integration, CORS, routing)
  - Service tests (RAG orchestration, Bedrock integration)
  - DTO tests (JSON serialization, round-trip integrity)

- **TypeScript Tests**: Jest tests with React Testing Library
  - Component tests (rendering, user interactions, accessibility)
  - API tests (request/response handling, error cases)
  - Edge cases (special characters, long content, empty states)

### 🚀 Developer Experience

- **Local Development**: SAM CLI for testing Lambda functions locally with Docker
- **Automated Testing**: `test.sh` script runs all tests (Java + TypeScript)
- **CI/CD Integration**: `deploy.sh` runs tests before deployment (quality gate)
- **Hot Reload**: Next.js dev server with instant feedback
- **Comprehensive Docs**: Architecture diagrams, API docs, deployment guides

---

## 🏛️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND LAYER                          │
│  ┌──────────────┐   ┌────────────────────────────────────────┐ │
│  │  CloudFront  │──▶│  S3 Static Website                     │ │
│  │     (CDN)    │   │  - Next.js 16 (SSG)                   │ │
│  └──────────────┘   │  - TypeScript + React 19              │ │
│                     │  - Tailwind CSS + Shadcn/UI           │ │
│                     └────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                             │ HTTPS/REST
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                          API LAYER                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  API Gateway (HTTP API)                                  │  │
│  │  - POST /api/query  (RAG queries)                        │  │
│  │  - GET  /api/health (Health check)                       │  │
│  │  - OPTIONS /* (CORS preflight)                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    COMPUTE LAYER (Lambda)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  AWS Lambda (Java 17)                                    │  │
│  │  - QueryHandler: API Gateway event handler              │  │
│  │  - RagOrchestrationService: RAG business logic          │  │
│  │  - BedrockConstants: Configuration management           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    RAG ORCHESTRATION LAYER                      │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │  Amazon Bedrock Knowledge Base                         │   │
│  │  ┌──────────────┐         ┌────────────────────────┐  │   │
│  │  │  Data Source │────────▶│  S3 Vectors (Index)    │  │   │
│  │  │  (S3 Bucket) │         │  - 1024-dim vectors    │  │   │
│  │  │              │         │  - Float32 data type   │  │   │
│  │  │  documents/  │         │  - Euclidean distance  │  │   │
│  │  │  - resume    │         └────────────────────────┘  │   │
│  │  │  - projects  │         Embedding: Titan v2         │   │
│  │  └──────────────┘                                      │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │  Amazon Bedrock Runtime                                │   │
│  │  - LLM: Claude 3 Haiku (cost-optimized)               │   │
│  │  - Embeddings: Titan Embed Text v2                    │   │
│  │  - Model Invocation Logging: CloudWatch               │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │  DynamoDB                                              │   │
│  │  - conversations: Session tracking (TTL-enabled)       │   │
│  │  - document_metadata: Document indexing               │   │
│  └────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

**Data Flow:**
1. User asks question in frontend chat interface
2. Request sent to API Gateway → Lambda
3. Lambda retrieves relevant documents from Bedrock Knowledge Base
4. Knowledge Base uses S3 Vectors for semantic similarity search
5. Lambda sends context + question to Bedrock Claude
6. Claude generates answer based on retrieved documents
7. Response returned to frontend with sources

For detailed architecture documentation, see [Architecture.md](Architecture.md).

---

## 🛠️ Technology Stack

### Backend

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Runtime** | Java 17 | AWS Lambda execution environment |
| **Build Tool** | Gradle 8.5 | Dependency management, fat JAR packaging |
| **Framework** | AWS SDK v2 | Bedrock, S3, DynamoDB, Lambda clients |
| **Logging** | SLF4J + Log4j2 | Structured logging to CloudWatch |
| **Testing** | JUnit 5 + Mockito | Unit testing with mocking |

**Key Dependencies:**
- `aws-lambda-java-core` - Lambda handler interfaces
- `aws-lambda-java-events` - API Gateway event types
- `software.amazon.awssdk:bedrockagent` - Knowledge Base API
- `software.amazon.awssdk:bedrockruntime` - LLM invocation
- `com.fasterxml.jackson.core` - JSON serialization

### Frontend

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Framework** | Next.js 16 | React framework with SSG |
| **Language** | TypeScript 5 | Type-safe JavaScript |
| **UI Library** | Shadcn/UI | Component library (Radix + Tailwind) |
| **Styling** | Tailwind CSS 4 | Utility-first CSS |
| **Testing** | Jest + React Testing Library | Unit and integration tests |

**Key Dependencies:**
- `react` 19.2.0 - UI framework
- `next` 16.0.10 - SSR/SSG framework
- `lucide-react` - Icon library
- `react-markdown` - Markdown rendering for AI responses

### Infrastructure

| Component | Technology | Purpose |
|-----------|------------|---------|
| **IaC** | Terraform 1.x | Declarative infrastructure provisioning |
| **Cloud Provider** | AWS | Hosting and managed services |
| **Vector Store** | S3 Vectors | Cost-effective vector database |
| **LLM** | Amazon Bedrock | Managed AI service (Claude 3 Haiku) |
| **Embeddings** | Titan Embed Text v2 | 1024-dimensional vectors |

---

## 📦 Prerequisites

### Required Software

| Tool | Version | Installation |
|------|---------|--------------|
| **Java JDK** | 17+ | [Download](https://adoptium.net/) |
| **Node.js** | 20.9.0+ | [Download](https://nodejs.org/) (Next.js 16 requirement) |
| **Gradle** | 8.5+ | Via wrapper (`./gradlew`) |
| **AWS CLI** | 2.x | [Install Guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) |
| **Terraform** | 1.x | [Download](https://www.terraform.io/downloads) |
| **Git** | Latest | [Download](https://git-scm.com/) |

### Optional (for local development)

| Tool | Purpose |
|------|---------|
| **SAM CLI** | Local Lambda testing with Docker |
| **Docker** | Required by SAM CLI |
| **pnpm** | Faster npm alternative |

### AWS Account Setup

1. **Create AWS Account**: [aws.amazon.com](https://aws.amazon.com)
2. **Configure AWS CLI**:
   ```bash
   aws configure
   # Enter: Access Key ID, Secret Access Key, Region (us-east-1), Output (json)
   ```
3. **Enable Bedrock Models**:
   - Navigate to AWS Console → Bedrock → Model Access
   - Enable: `Claude 3 Haiku`, `Titan Embed Text v2`

---

## 🚀 Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/yourusername/RAG-augmented-personal-website.git
cd RAG-augmented-personal-website
```

### 2. Configure Variables

Edit `variables.tf` to set your preferences:

```hcl
variable "project_name" {
  default = "rag-website"  # Change to your project name
}

variable "aws_region" {
  default = "us-east-1"
}

variable "environment" {
  default = "dev"
}
```

### 3. Add Knowledge Base Documents

Place your documents in `rag-resources/`:

```bash
mkdir -p rag-resources
# Add your resume, project descriptions, case studies, etc.
cp /path/to/resume.pdf rag-resources/
cp /path/to/projects.md rag-resources/
```

### 4. Deploy Infrastructure

```bash
# Full deployment (backend + frontend + infrastructure)
./deploy.sh

# The script will:
# 1. Verify AWS credentials
# 2. Run test suite (can skip with --skip-tests)
# 3. Build backend JAR
# 4. Deploy infrastructure with Terraform
# 5. Build and deploy Next.js frontend
# 6. Upload RAG documents to S3
```

### 5. Ingest Documents

After deployment, trigger document ingestion:

```bash
# Get resource IDs from Terraform
KNOWLEDGE_BASE_ID=$(terraform output -raw knowledge_base_id)
DATA_SOURCE_ID=$(terraform output -raw data_source_id)

# Start ingestion job
aws bedrock-agent start-ingestion-job \
  --knowledge-base-id $KNOWLEDGE_BASE_ID \
  --data-source-id $DATA_SOURCE_ID

# Monitor status
aws bedrock-agent list-ingestion-jobs \
  --knowledge-base-id $KNOWLEDGE_BASE_ID \
  --data-source-id $DATA_SOURCE_ID
```

### 6. Access Your Website

```bash
# Get CloudFront URL
terraform output website_url

# Or test API directly
curl $(terraform output -raw api_gateway_url)/api/health
```

**Expected Output:**
```json
{
  "status": "healthy",
  "timestamp": "2026-01-19T12:00:00Z"
}
```

---

## 💻 Development

### Backend Development

```bash
cd backend

# Build JAR
./gradlew.bat clean jar          # Windows
./gradlew clean jar              # Unix/macOS

# Run tests
./gradlew.bat test

# Run tests with coverage
./gradlew.bat test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Start dev server (http://localhost:3000)
npm run dev

# Run tests
npm test

# Run tests in watch mode
npm run test:watch

# Build for production
npm run build
```

### Local Lambda Testing (SAM CLI)

```bash
cd backend

# Start local API Gateway (port 3001)
sam local start-api --template template.yaml --port 3001

# Test endpoints
curl http://localhost:3001/api/health

curl -X POST http://localhost:3001/api/query \
  -H "Content-Type: application/json" \
  -d '{"question":"What is your experience?","conversationId":"test-123"}'

# Invoke function directly
sam local invoke QueryHandlerFunction --event events/query-event.json
```

For detailed local development instructions, see [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md).

---

## 🧪 Testing

### Run All Tests

```bash
# Run both backend (Java) and frontend (TypeScript) tests
./test.sh

# Run with coverage reports
./test.sh --coverage

# Run only backend tests
./test.sh --backend

# Run only frontend tests
./test.sh --frontend
```

**Backend Test Suites:**
- `QueryHandlerTest` - API Gateway integration, CORS, routing
- `RagOrchestrationServiceTest` - RAG logic, Bedrock integration
- `DtoSerializationTest` - JSON serialization, data integrity

**Frontend Test Suites:**
- `chat-message.test.tsx` - Message rendering, markdown support
- `chat-input.test.tsx` - User input, form validation
- `sources-panel.test.tsx` - Source display, expand/collapse
- `loading-indicator.test.tsx` - Loading states
- `assistant-api.test.ts` - API integration, error handling

### Continuous Integration

Tests run automatically before deployment via `deploy.sh`:

```bash
# Deploy with tests (default)
./deploy.sh

# Skip tests (not recommended)
./deploy.sh --skip-tests
```

---

## 🚢 Deployment

### Full Deployment

```bash
./deploy.sh
```

**Deployment Steps:**
1. ✅ Verify AWS credentials
2. 🧪 Run test suite (backend + frontend)
3. 🔨 Build backend JAR
4. 🏗️ Deploy Terraform infrastructure
5. 📦 Build and deploy frontend
6. 📚 Upload RAG documents

### Partial Deployment

```bash
# Deploy only backend
./deploy.sh --skip-frontend --skip-rag-resources

# Deploy only frontend
./deploy.sh --skip-backend --skip-terraform --skip-rag-resources

# Terraform plan only (no apply)
./deploy.sh --terraform-plan-only

# Skip tests (not recommended)
./deploy.sh --skip-tests
```

### Manual Steps

**Update Lambda Code Only:**
```bash
# Build JAR
cd backend && ./gradlew.bat clean jar

# Upload to S3
aws s3 cp build/libs/query-handler.jar \
  s3://$(terraform output -raw lambda_bucket_name)/query-handler.jar

# Update Lambda function
aws lambda update-function-code \
  --function-name $(terraform output -raw lambda_function_name) \
  --s3-bucket $(terraform output -raw lambda_bucket_name) \
  --s3-key query-handler.jar
```

**Update Frontend Only:**
```bash
cd frontend
npm run build
aws s3 sync out s3://$(terraform output -raw website_bucket_name) --delete
aws cloudfront create-invalidation --distribution-id $(terraform output -raw cloudfront_distribution_id) --paths "/*"
```

---

## 📁 Project Structure

```
.
├── backend/                        # Java Lambda backend
│   ├── src/main/java/
│   │   └── com/perryrosenberg/portfolio/
│   │       ├── constants/          # Configuration constants
│   │       ├── dto/                # Data Transfer Objects
│   │       │   ├── request/        # Request DTOs
│   │       │   └── response/       # Response DTOs
│   │       ├── handler/            # Lambda handlers
│   │       └── service/            # Business logic
│   ├── src/test/java/              # JUnit tests
│   ├── build.gradle                # Gradle build config
│   ├── template.yaml               # SAM template for local dev
│   ├── samconfig.toml              # SAM CLI configuration
│   └── env.json                    # Local environment variables
│
├── frontend/                       # Next.js frontend
│   ├── app/                        # Next.js 16 app directory
│   ├── components/                 # React components
│   │   ├── assistant/              # RAG chat components
│   │   └── ui/                     # Shadcn/UI components
│   ├── lib/                        # Utilities and API clients
│   ├── __tests__/                  # Jest tests
│   ├── jest.config.js              # Jest configuration
│   ├── jest.setup.js               # Test environment setup
│   ├── package.json                # npm dependencies
│   └── tsconfig.json               # TypeScript configuration
│
├── rag-resources/                  # Knowledge base documents
│   ├── resume.pdf                  # (Upload your resume)
│   ├── projects.md                 # (Upload your projects)
│   └── case-studies/               # (Upload case studies)
│
├── main.tf                         # Terraform main configuration
├── variables.tf                    # Terraform variables
├── outputs.tf                      # Terraform outputs
├── terraform.tf                    # Provider configuration
│
├── deploy.sh                       # Deployment script
├── test.sh                         # Test runner script
│
├── Architecture.md                 # System architecture docs
├── LOCAL_DEVELOPMENT.md            # Local development guide
├── CLAUDE.md                       # AI assistant reference
└── README.md                       # This file
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [Architecture.md](Architecture.md) | System architecture, cost analysis, implementation status |
| [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md) | Local development setup, debugging, common issues |
| [CLAUDE.md](CLAUDE.md) | AI assistant reference for understanding the codebase |

---

## 💰 Cost Analysis

### Monthly Cost Estimate (Low Traffic)

| Service | Free Tier | Typical Usage | Overage Cost | Estimated Cost |
|---------|-----------|---------------|--------------|----------------|
| **Lambda** | 1M requests, 400K GB-sec | 10K requests | $0.20/1M requests | **$0.00** |
| **API Gateway** | 1M requests | 10K requests | $1.00/1M requests | **$0.00** |
| **S3** | 5GB storage | 500MB | $0.023/GB/month | **$0.00** |
| **DynamoDB** | 25GB storage | 100MB | $0.25/GB/month | **$0.00** |
| **CloudFront** | 1TB transfer | 10GB | $0.085/GB | **$0.00** |
| **Bedrock Claude Haiku** | Pay-per-use | 5K queries | $0.25/1M input tokens | **~$1-2** |
| **Bedrock Titan Embeddings** | Pay-per-use | 5K embeddings | $0.0001/1K tokens | **~$0.50** |
| **S3 Vectors** | S3 storage | 100MB vectors | $0.023/GB/month | **~$0.01** |
| **Total** | - | - | - | **$1-5/month** |

### Cost Comparison: S3 Vectors vs OpenSearch Serverless

| Service | Minimum Monthly Cost | Notes |
|---------|---------------------|-------|
| **S3 Vectors** | ~$1 | Storage + query costs only |
| **OpenSearch Serverless** | ~$700 | 2 OCU minimum (0.5 indexing + 1.5 search) |
| **Savings** | **~30,000x cheaper** | For small workloads (<1M queries/month) |

**Why S3 Vectors?**
- No minimum capacity requirements
- Pay only for storage and queries
- Ideal for personal websites and small projects
- Scales automatically without provisioning

---

### Development Workflow

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Make your changes**
4. **Run tests**: `./test.sh`
5. **Commit your changes**: `git commit -m 'Add amazing feature'`
6. **Push to the branch**: `git push origin feature/amazing-feature`
7. **Open a Pull Request**

### Code Style

- **Java**: Follow Google Java Style Guide
- **TypeScript**: Use Prettier + ESLint
- **Commits**: Use conventional commits format
- **Tests**: Maintain >70% coverage

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Perry Rosenberg**

- Portfolio: [perryrosenberg.com](https://perryrosenberg.com)
- GitHub: [@perryrosenberg](https://github.com/perryrosenberg)
- LinkedIn: [Your LinkedIn](https://www.linkedin.com/in/perry-rosenberg/)

---

## 🙏 Acknowledgments

- **Claude Code/Junie** - Heavily utilized agents for code
- **Next.js Team** - Amazing React framework
- **AWS Bedrock** - Managed AI infrastructure
- **Shadcn** - Beautiful UI components
- **Terraform** - Infrastructure as Code
- **v0.app** - Initial frontend design

---

**⭐ Star this repo if you find it useful!**

Built with ❤️ using modern cloud-native technologies.
