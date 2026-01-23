# RAG-Augmented Personal Website - Architecture & Design Document

## Overview

This document describes the architecture, design principles, and implementation details for a production-ready, cloud-native personal portfolio website with RAG (Retrieval-Augmented Generation) capabilities. The system is designed for **AWS Free Tier** optimization while maintaining enterprise-grade code quality.

---

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Infrastructure Components](#infrastructure-components)
3. [Code Style & Design Principles](#code-style--design-principles)
4. [Current Implementation Status](#current-implementation-status)
5. [Testing Strategy](#testing-strategy)
6. [Local Development](#local-development)
7. [Deployment](#deployment)

---

## System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND LAYER                                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────────────────┐  │
│  │  CloudFront │───▶│     S3      │    │  Next.js 16 Application         │  │
│  │    (CDN)    │    │  (Static)   │    │  - Portfolio/Resume UI          │  │
│  └─────────────┘    └─────────────┘    │  - RAG Chat Interface           │  │
│                                         │  - TypeScript + React           │  │
│                                         └─────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ HTTPS/REST
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API LAYER                                       │
│  ┌─────────────┐                                                             │
│  │ API Gateway │    Endpoints:                                               │
│  │  (HTTP API) │    - POST /api/query  (RAG queries)                        │
│  │             │    - GET  /api/health (Health check)                       │
│  └─────────────┘                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          COMPUTE LAYER (Lambda)                              │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │              AWS Lambda (Java 17)                                       │ │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │ │
│  │  │  QueryHandler                                                     │  │ │
│  │  │  - Handles API Gateway events                                    │  │ │
│  │  │  - CORS management                                               │  │ │
│  │  │  - Request validation                                            │  │ │
│  │  └──────────────────────────────────────────────────────────────────┘  │ │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │ │
│  │  │  RagOrchestrationService                                         │  │ │
│  │  │  - Knowledge Base retrieval via Bedrock Agent Runtime           │  │ │
│  │  │  - LLM response generation via Bedrock Runtime                  │  │ │
│  │  │  - Source attribution and confidence scoring                    │  │ │
│  │  └──────────────────────────────────────────────────────────────────┘  │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         RAG ORCHESTRATION LAYER                              │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                   Amazon Bedrock Knowledge Base                     │   │
│  │  ┌────────────────────┐         ┌────────────────────┐             │   │
│  │  │   Data Source      │         │  Vector Storage    │             │   │
│  │  │   (S3 Bucket)      │────────▶│   (S3 Vectors)     │             │   │
│  │  │                    │         │                    │             │   │
│  │  │ documents/         │         │ - 1024-dim vectors │             │   │
│  │  │ - resume.pdf       │         │ - Euclidean metric │             │   │
│  │  │ - projects.md      │         │ - Float32 data     │             │   │
│  │  │ - case-studies/    │         │                    │             │   │
│  │  └────────────────────┘         └────────────────────┘             │   │
│  │                                                                      │   │
│  │  Embedding Model: Titan Embed Text v2 (1024 dimensions)            │   │
│  │  Chunking: Fixed-size (300 tokens, 20% overlap)                    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                   Amazon Bedrock Runtime                            │   │
│  │  - LLM: Claude 3 Haiku (cost-optimized)                             │   │
│  │  - Embeddings: Titan Embed Text v2                                  │   │
│  │  - Model Invocation Logging: CloudWatch                             │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                   DynamoDB Metadata Store                           │   │
│  │  - conversations: Session tracking (TTL-enabled)                    │   │
│  │  - document_metadata: Document indexing metadata                    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Infrastructure Components

### Frontend

| Component | Technology | Purpose | Free Tier |
|-----------|------------|---------|-----------|
| **Framework** | Next.js 16 | Static site generation | N/A |
| **Language** | TypeScript | Type-safe React development | N/A |
| **UI Library** | Shadcn/UI | Component library | N/A |
| **Hosting** | S3 + CloudFront | Static website hosting with CDN | 5GB storage, 1TB transfer/month |

**Build Output**: `/frontend/out` → S3 bucket

### Backend (Lambda)

| Component | Technology | Purpose | Free Tier |
|-----------|------------|---------|-----------|
| **Runtime** | Java 17 | Lambda execution environment | 1M requests, 400K GB-seconds/month |
| **Build Tool** | Gradle 8.5 | Dependency management, fat JAR packaging | N/A |
| **SDK** | AWS SDK v2 (2.41.10) | Bedrock, S3, DynamoDB clients | N/A |

**Key Classes**:
- `QueryHandler.java` - API Gateway request handler
- `RagOrchestrationService.java` - RAG business logic
- `QueryRequest.java` / `QueryResponse.java` - DTOs

### RAG Infrastructure

| Component | Technology | Purpose | Free Tier / Cost |
|-----------|------------|---------|------------------|
| **Knowledge Base** | Amazon Bedrock KB | Managed RAG service | Pay-per-query (~$0.00025/query) |
| **Vector Store** | S3 Vectors | Serverless vector database | S3 storage costs (~$0.023/GB) |
| **Embedding Model** | Titan Embed Text v2 | 1024-dim embeddings | $0.0001/1K tokens |
| **LLM** | Claude 3 Haiku | Fast, cost-optimized inference | $0.25/1M input tokens |
| **Document Storage** | S3 | Raw documents for ingestion | 5GB free, then $0.023/GB |
| **Metadata** | DynamoDB | Conversations, metadata | 25GB free, 25 RCU/WCU |

**Cost Comparison**: S3 Vectors vs OpenSearch Serverless
- S3 Vectors: ~$0.023/GB + query costs (pennies/month for small workloads)
- OpenSearch Serverless: ~$700/month minimum (2 OCUs)
- **Savings**: ~30,000x cheaper for small-scale deployments

### API Layer

| Component | Technology | Purpose | Free Tier |
|-----------|------------|---------|-----------|
| **API Gateway** | HTTP API (v2) | RESTful endpoints | 1M requests/month |
| **CORS** | Built-in | Cross-origin support | N/A |
| **Logging** | CloudWatch Logs | Request/response logs | 5GB ingestion/month |

---

## Code Style & Design Principles

### Functional Programming Principles

All code adheres to functional programming best practices:

```java
// ✅ GOOD: Pure function, clear intent, no side effects
public List<Source> filterByConfidence(List<Source> sources, double minConfidence) {
    return sources.stream()
        .filter(source -> source.getConfidence() >= minConfidence)
        .collect(Collectors.toList());
}

// ❌ BAD: Side effects, boolean flags, unclear intent
public void processSources(List<Source> sources, boolean filter, boolean log) {
    for (Source source : sources) {
        if (filter && source.getConfidence() < 0.5) continue;
        if (log) logger.info(source);
        cache.put(source); // Side effect!
    }
}
```

### Core Principles

1. **Self-Explanatory Names** - No abbreviations, verb-noun patterns
2. **Minimal Side Effects** - Pure functions where possible
3. **No Boolean Flags** - Use separate methods or config objects
4. **Comments Explain Why** - Not what the code does
5. **Professional Quality** - Enterprise-grade error handling

### TypeScript/React Guidelines

```typescript
// ✅ GOOD: Typed props, functional component
interface ChatMessageProps {
  readonly message: Message;
  readonly onAction: (id: string) => void;
}

export const ChatMessage: React.FC<ChatMessageProps> = ({ message, onAction }) => {
  return <div className="message">{message.content}</div>;
};
```

---

## Current Implementation Status

### ✅ Fully Implemented

| Component | Status | Notes |
|-----------|--------|-------|
| Frontend UI | ✅ Complete | Next.js 16, TypeScript, Shadcn/UI |
| API Gateway | ✅ Complete | HTTP API with CORS, health checks |
| Lambda Handler | ✅ Complete | Java 17, handles POST /api/query |
| RAG Service | ✅ Complete | Bedrock KB integration with vector search |
| Knowledge Base | ✅ Complete | S3 Vectors backend, Titan embeddings |
| Infrastructure | ✅ Complete | Terraform with S3, Lambda, Bedrock, DynamoDB |
| Model Logging | ✅ Complete | CloudWatch logs for Bedrock invocations |
| Deployment | ✅ Complete | deploy.sh with Terraform + Gradle |

### ⚠️ Partially Implemented

| Component | Status | Next Steps |
|-----------|--------|-----------|
| Document Ingestion | ⚠️ Manual | Automate via deploy.sh or scheduled Lambda |
| Error Handling | ⚠️ Basic | Add retry logic, circuit breakers |
| Monitoring | ⚠️ Basic | Add custom CloudWatch metrics, alarms |

### ❌ Not Implemented

| Component | Status | Priority |
|-----------|--------|----------|
| Authentication | ❌ None | Low (portfolio site) |
| Rate Limiting | ❌ None | Medium (cost control) |
| CI/CD Pipeline | ❌ None | High (automation) |
| Unit Tests | ❌ None | High (code quality) |
| Integration Tests | ❌ None | Medium (reliability) |
| Cost Alerts | ❌ None | Medium (budget protection) |

---

## Testing Strategy

### Unit Testing

**Java (JUnit 5 + Mockito)**
```java
@Test
void processQuery_withValidRequest_returnsResponse() {
    // Arrange
    var request = new QueryRequest("test question", "conv-123");
    var mockBedrockClient = mock(BedrockAgentRuntimeClient.class);
    var service = new RagOrchestrationService(mockBedrockClient, "kb-id");

    // Act
    var response = service.processQuery(request);

    // Assert
    assertThat(response.getAnswer()).isNotEmpty();
    assertThat(response.getConversationId()).isEqualTo("conv-123");
}
```

**TypeScript (Jest + React Testing Library)**
```typescript
describe('ChatMessage', () => {
  it('renders user message correctly', () => {
    const message = { role: 'user', content: 'Hello', timestamp: new Date() };
    render(<ChatMessage message={message} />);
    expect(screen.getByText('Hello')).toBeInTheDocument();
  });
});
```

### Coverage Goals

| Layer | Target | Focus |
|-------|--------|-------|
| Lambda Handlers | 80% | Input validation, CORS, routing |
| RAG Service | 90% | Retrieval logic, LLM calls |
| Frontend Components | 70% | User interactions, state |
| Utility Functions | 95% | Edge cases, type safety |

---

## Local Development

### Prerequisites

- **Java**: JDK 17+
- **Gradle**: 8.5+ (via wrapper)
- **Node.js**: 20.9.0+ (Next.js 16 requirement)
- **AWS CLI**: Configured with credentials
- **SAM CLI**: For local Lambda testing
- **Docker**: For SAM local invoke

### Running Locally

**Frontend (Next.js)**
```bash
cd frontend
npm install
npm run dev  # http://localhost:3000
```

**Backend (SAM Local)**
```bash
# Build JAR
cd backend
./gradlew.bat clean jar

# Start local API
sam local start-api --template-file template.yaml
```

**Tests**
```bash
./test.sh  # Runs all tests (Java + TypeScript)
```

---

## Deployment

### Full Deployment

```bash
./deploy.sh
```

**Steps**:
1. Builds backend JAR (`gradlew clean jar`)
2. Runs Terraform (init → plan → apply)
3. Uploads Lambda code to S3
4. Builds and deploys frontend to S3
5. Uploads RAG documents to knowledge bucket
6. Outputs API Gateway URL and resource IDs

### Document Ingestion

After deploying, ingest documents into Knowledge Base:

```bash
aws bedrock-agent start-ingestion-job \
  --knowledge-base-id $(terraform output -raw knowledge_base_id) \
  --data-source-id $(terraform output -raw data_source_id)
```

Monitor ingestion:
```bash
aws bedrock-agent list-ingestion-jobs \
  --knowledge-base-id $(terraform output -raw knowledge_base_id) \
  --data-source-id $(terraform output -raw data_source_id)
```

---

## Cost Optimization

### Free Tier Utilization

| Service | Free Tier | Typical Usage | Overage Cost |
|---------|-----------|---------------|--------------|
| Lambda | 1M requests | ~10K/month | $0.20/1M requests |
| API Gateway | 1M requests | ~10K/month | $1.00/1M requests |
| S3 | 5GB storage | ~500MB | $0.023/GB/month |
| DynamoDB | 25GB storage | ~100MB | $0.25/GB/month |
| Bedrock Claude | Pay-per-use | ~5K queries | $0.25/1M input tokens |
| Bedrock Titan | Pay-per-use | ~5K embeddings | $0.0001/1K tokens |
| S3 Vectors | S3 storage | ~100MB | $0.023/GB/month |

**Estimated Monthly Cost** (Low Traffic): $1-5/month

### Cost Control Strategies

1. **Cache LLM Responses** - Reduce duplicate Bedrock calls
2. **Limit Context Window** - Use only relevant chunks
3. **DynamoDB TTL** - Auto-delete old conversations
4. **CloudWatch Alarms** - Alert on budget thresholds
5. **Use Haiku** - Cheapest Claude model for demos

---

*Document Version: 2.0*
*Last Updated: 2026-01-19*
*Status: Production-Ready*
