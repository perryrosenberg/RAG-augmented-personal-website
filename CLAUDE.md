# CLAUDE.md - AI Assistant Reference Document

**Purpose:** Quick reference for Claude Code to understand the RAG-augmented personal website project.

---

## Project Overview

A cloud-native personal portfolio website with RAG (Retrieval-Augmented Generation) capabilities, optimized for AWS Free Tier usage.

**Key Features:**
- Personal portfolio/resume display
- Interactive RAG-powered chat assistant
- Serverless architecture (AWS Lambda + API Gateway)
- Static frontend hosting (S3 + CloudFront)
- Knowledge base retrieval from S3
- AI responses powered by Amazon Bedrock (Claude)

---

## Technology Stack

### Frontend
- **Framework:** Next.js 16 (requires Node.js 20.9.0+)
- **Language:** TypeScript/React
- **Location:** `/frontend`
- **Deployment:** S3 + CloudFront CDN
- **Build:** Static export to S3

### Backend
- **Runtime:** AWS Lambda (Java 17)
- **Language:** Java
- **Build Tool:** Gradle 8.5
- **Location:** `/backend`
- **Package Structure:**
  - `com.perryrosenberg.portfolio.handler` - Lambda request handlers
  - `com.perryrosenberg.portfolio.dto.request` - Request DTOs
  - `com.perryrosenberg.portfolio.dto.response` - Response DTOs
  - `com.perryrosenberg.portfolio.service` - Business logic (RAG orchestration)
  - `com.perryrosenberg.portfolio.constants` - Configuration constants

### Infrastructure
- **IaC:** Terraform
- **Location:** `*.tf` files in project root
- **Resources:**
  - API Gateway (REST endpoints)
  - Lambda Functions (Java 17)
  - S3 Buckets (lambda deployment, knowledge base, static website)
  - DynamoDB Tables (conversations, document metadata)
  - CloudFront Distribution (CDN)
  - IAM Roles & Policies

### AWS Services
- **Amazon Bedrock:** LLM inference (Claude 3 Haiku) and embeddings (Titan)
- **S3:** Document storage, static hosting, Lambda deployment
- **DynamoDB:** Conversation history and metadata
- **API Gateway:** REST API endpoints
- **Lambda:** Serverless compute (Java 17)
- **CloudFront:** CDN for static assets

---

## Current Implementation Status

### ✅ Implemented
1. **Frontend UI** - Next.js portfolio with chat interface (generated from v0.app)
2. **Backend Lambda Handler** - `QueryHandler.java` handles API Gateway requests
3. **RAG Orchestration Service** - `RagOrchestrationService.java` implements:
   - Document retrieval from S3
   - Keyword-based relevance matching
   - Context building for LLM
   - Bedrock Claude integration for responses
   - Fallback responses for errors
4. **Data Models** - `QueryRequest`, `QueryResponse` with nested classes
5. **Terraform Infrastructure** - Complete IaC for all AWS resources
6. **Gradle Build** - Fat JAR packaging with all dependencies

### ⚠️ In Progress
1. **Deployment Issue** - Fixing Windows compatibility for Terraform's local-exec provisioner
2. **Lambda Deployment** - Ensuring JAR is correctly uploaded and Lambda can find classes

### ❌ Not Implemented / Known Limitations
1. **Vector Search** - Currently uses simple keyword matching instead of embeddings + OpenSearch
2. **Authentication** - No auth layer implemented
3. **Rate Limiting** - No API throttling configured
4. **Cost Monitoring** - No CloudWatch alerts for cost overruns
5. **CI/CD Pipeline** - Manual deployment via `deploy.sh`
6. **Error Tracking** - Basic CloudWatch logs only
7. **Frontend-Backend Integration** - May still use mock data in some places

---

## Project Structure

```
/
├── frontend/                    # Next.js application
│   ├── app/                     # Next.js 16 app directory
│   ├── components/              # React components
│   ├── lib/                     # Utilities (including assistant-mock.ts)
│   └── styles/                  # CSS styles
├── backend/                     # Java Lambda functions
│   ├── src/main/java/com/ragwebsite/
│   │   ├── handlers/           # QueryHandler.java
│   │   ├── models/             # QueryRequest.java, QueryResponse.java
│   │   └── services/           # RagOrchestrationService.java
│   ├── build.gradle            # Gradle build configuration
│   ├── gradlew                 # Gradle wrapper (Unix)
│   └── gradlew.bat             # Gradle wrapper (Windows)
├── rag-resources/              # Knowledge base documents (uploaded to S3)
├── main.tf                     # Main Terraform configuration
├── variables.tf                # Terraform variables
├── outputs.tf                  # Terraform outputs
├── deploy.sh                   # Deployment script
├── Architecture.md             # System architecture documentation
└── .junie/guidelines.md        # Development guidelines
```

---

## Key Code Components

### Lambda Handler
**File:** `backend/src/main/java/com/perryrosenberg/portfolio/handler/QueryHandler.java`
- Implements `RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>`
- Handles CORS preflight (OPTIONS)
- Health check endpoint (GET /api/health)
- Query endpoint (POST /api/query)
- Returns JSON responses with proper CORS headers

### RAG Service
**File:** `backend/src/main/java/com/perryrosenberg/portfolio/service/RagOrchestrationService.java`
- Retrieves documents from Bedrock Knowledge Base using vector search
- Performs semantic similarity matching with Titan embeddings
- Builds context from relevant documents
- Calls Bedrock Claude for AI-generated responses
- Includes fallback responses for error cases
- Uses environment variable: `KNOWLEDGE_BASE_ID`

### Data Models
**Files:** `backend/src/main/java/com/perryrosenberg/portfolio/dto/`
- `request.QueryRequest` - User question + conversationId + optional context
- `response.QueryResponse` - AI answer + sources + conversationId
- `response.QueryResponse.Source` - Document metadata (id, title, type, confidence, excerpt)

### Constants
**File:** `backend/src/main/java/com/perryrosenberg/portfolio/constants/BedrockConstants.java`
- Model IDs for Bedrock services
- API configuration constants
- System prompts and error messages
- Document type classifications

---

## Development Environment

### Platform: Windows
- Using Git Bash for Unix-like commands
- Gradle wrapper: `gradlew.bat` (not `./gradlew`)
- Terraform local-exec must use Windows-compatible commands

### Required Tools
- **Java 17** - Lambda runtime and local development
- **Gradle 8.5** - Build tool (via wrapper)
- **Node.js 20.9.0+** - Frontend development (Next.js 16 requirement)
- **Terraform** - Infrastructure provisioning
- **AWS CLI** - Deployment and testing

### Build Commands

**Backend (Java):**
```bash
cd backend
./gradlew.bat clean jar          # Windows
./gradlew clean jar              # Unix/Mac

# Output: backend/build/libs/query-handler.jar (fat JAR ~17MB)
```

**Frontend (Next.js):**
```bash
cd frontend
npm install                      # or pnpm install
npm run dev                      # Development server
npm run build                    # Production build
```

**Infrastructure (Terraform):**
```bash
terraform init
terraform plan
terraform apply
```

**Full Deployment:**
```bash
./deploy.sh                      # Runs Terraform + builds + uploads
```

---

## Coding Guidelines

### Functional Programming Principles
1. **Pure Functions** - Minimize side effects, prefer immutability
2. **Self-Explanatory Names** - `retrieveDocuments()` not `getDocs()`
3. **No Boolean Flags** - Use separate methods or config objects
4. **Comments Explain Why** - Not what the code does

### Java Style
```java
// ✅ Good: Pure function, descriptive name
public List<Document> filterRelevantDocuments(List<Document> docs, double threshold) {
    return docs.stream()
        .filter(doc -> doc.getScore() >= threshold)
        .collect(Collectors.toList());
}

// ❌ Bad: Side effects, unclear, uses flags
public void processDocs(List<Document> docs, boolean log, boolean cache) {
    for (Document doc : docs) {
        if (log) logger.info(doc);
        if (cache) cache.put(doc);
    }
}
```

### TypeScript/React Style
```typescript
// ✅ Good: Functional component, typed props
interface Props {
  readonly message: Message;
  readonly onAction: (id: string) => void;
}

const Component: React.FC<Props> = ({ message, onAction }) => {
  return <div>{message.content}</div>;
};
```

---

## Current Issue Being Fixed

### ClassNotFoundException Problem
**Error:** Lambda couldn't find `com.perryrosenberg.portfolio.handler.QueryHandler`

**Root Cause:** Terraform was double-zipping the JAR file:
1. Gradle creates fat JAR: `query-handler.jar`
2. Terraform was zipping it again: `query-handler.zip`
3. Lambda couldn't find classes in double-wrapped archive

**Solution Applied:**
1. Updated Terraform to upload JAR directly (Lambda treats JARs as ZIPs natively)
2. Fixed `build.gradle` to create proper fat JAR with all dependencies
3. Added manifest with Main-Class attribute
4. Changed Terraform local-exec to use `gradlew.bat` on Windows

**Current Sub-Issue:** Windows compatibility in Terraform local-exec provisioner
- Error: `'.' is not recognized as an internal or external command`
- Fix: Using `bash -c './gradlew.bat clean jar'` instead of direct command

---

## Important Configuration

### Environment Variables (Lambda)
- `CONVERSATIONS_TABLE` - DynamoDB table for conversation history
- `DOCUMENTS_TABLE` - DynamoDB table for document metadata
- `KNOWLEDGE_BUCKET` - S3 bucket name for knowledge base documents
- `ENVIRONMENT` - Deployment environment (dev/prod)

### Terraform Variables
See `variables.tf` for full list. Key variables:
- `aws_region` - AWS region for deployment
- `project_name` - Project name prefix for resources
- `environment` - Environment name (dev/prod)
- `cors_allowed_origins` - CORS origins for API Gateway
- `enable_cloudfront` - Enable CloudFront distribution
- `domain_name` - Custom domain (optional)
- `certificate_arn` - ACM certificate ARN (optional)

### API Endpoints
- `POST /api/query` - Submit RAG query
- `GET /api/health` - Health check
- `OPTIONS /*` - CORS preflight

### Request/Response Format
**Request:**
```json
{
  "question": "What is your experience with AWS?",
  "conversationId": "uuid-here",
  "context": { "page": "about" }
}
```

**Response:**
```json
{
  "answer": "I have extensive experience with AWS...",
  "conversationId": "uuid-here",
  "sources": [
    {
      "id": "documents/resume.md",
      "title": "resume.md",
      "type": "Resume",
      "confidence": 0.85,
      "excerpt": "...relevant snippet..."
    }
  ]
}
```

---

## Free Tier Optimization

### Cost Control Strategies
1. **Bedrock** - Cache responses, use smaller models (Haiku vs Sonnet)
2. **Lambda** - Optimize cold starts, appropriate memory allocation
3. **DynamoDB** - Use on-demand mode, implement TTL for old conversations
4. **S3/CloudFront** - Enable compression, set cache headers
5. **API Gateway** - Consider caching for repeated queries

### Resource Limits
- Lambda: 1M requests/month, 400K GB-seconds
- API Gateway: 1M requests/month
- S3: 5GB storage, 20K GET requests
- DynamoDB: 25GB storage, 25 RCU/WCU
- CloudFront: 1TB data transfer, 10M requests

---

## Testing

### Unit Testing (Java)
```java
@Test
void processQuery_withValidRequest_returnsResponse() {
    // Arrange
    var request = new QueryRequest("test question", "conv-123");

    // Act
    var response = service.processQuery(request);

    // Assert
    assertThat(response.getAnswer()).isNotEmpty();
    assertThat(response.getConversationId()).isEqualTo("conv-123");
}
```

### Testing Tools
- **Java:** JUnit 5, Mockito, AssertJ
- **Frontend:** Jest (configured in `jest.config.js`)
- **Coverage Goals:** Lambda 80%, RAG 90%, Frontend 70%

---

## Quick Reference Commands

### Build & Deploy
```bash
# Build backend
cd backend && ./gradlew.bat clean jar

# Full deployment (Windows/Git Bash)
./deploy.sh

# Skip specific steps
./deploy.sh --skip-frontend
./deploy.sh --skip-backend
./deploy.sh --terraform-plan-only
```

### Testing Lambda
```bash
# Test health endpoint
curl https://your-api-gateway-url/api/health

# Test query endpoint
curl -X POST https://your-api-gateway-url/api/query \
  -H "Content-Type: application/json" \
  -d '{"question":"Hello","conversationId":"test-123"}'
```

### Viewing Logs
```bash
# CloudWatch Logs
aws logs tail /aws/lambda/rag-website-query-handler-dev --follow

# Terraform output
terraform output
```

---

**Last Updated:** 2026-01-18
**Document Purpose:** AI Assistant context and quick reference
**Maintained By:** Claude Code
