# RAG-Augmented Personal Website - Architecture & Design Document

## Overview

This document describes the architecture, design principles, and functional specifications for a cloud-native personal portfolio website with RAG (Retrieval-Augmented Generation) capabilities. The system is designed for **AWS Free Tier** usage optimization.

---

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Infrastructure Components](#infrastructure-components)
3. [Code Style & Design Principles](#code-style--design-principles)
4. [Current Project State](#current-project-state)
5. [Testing Strategy](#testing-strategy)
6. [Documentation Standards](#documentation-standards)

---

## System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND LAYER                                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────────────────┐  │
│  │  CloudFront │───▶│     S3      │    │  React/TypeScript Application   │  │
│  │    (CDN)    │    │  (Static)   │    │  - Portfolio/Resume UI          │  │
│  └─────────────┘    └─────────────┘    │  - RAG Chat Interface           │  │
│                                         └─────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              BACKEND LAYER                                   │
│  ┌─────────────┐    ┌─────────────────────────────────────────────────────┐ │
│  │ API Gateway │───▶│              AWS Lambda (Java)                      │ │
│  │   (REST)    │    │  - Query Handler                                    │ │
│  └─────────────┘    │  - RAG Orchestration                                │ │
│                      └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         RAG ORCHESTRATION LAYER                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐  │
│  │ Amazon Bedrock  │  │   OpenSearch    │  │      S3 Knowledge           │  │
│  │ (Claude/Titan)  │  │  Vector Store   │  │        Buckets              │  │
│  │                 │  │                 │  │                             │  │
│  │ - LLM Inference │  │ - Embeddings    │  │ - Resume/CV Documents      │  │
│  │ - Embeddings    │  │ - Similarity    │  │ - Project Descriptions     │  │
│  └─────────────────┘  │   Search        │  │ - Knowledge Base           │  │
│                       └─────────────────┘  └─────────────────────────────┘  │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                    DynamoDB Metadata Store                              ││
│  │  - Conversation History (minimal retention)                             ││
│  │  - Document Metadata                                                    ││
│  │  - User Session Data                                                    ││
│  └─────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Infrastructure Components

### Frontend

| Component | Technology | Purpose | Free Tier Considerations |
|-----------|------------|---------|--------------------------|
| **CDN** | CloudFront | Global content delivery, HTTPS | 1TB/month data transfer, 10M requests |
| **Static Hosting** | S3 | Host React build artifacts | 5GB storage, 20K GET requests |
| **Framework** | React/TypeScript | SPA with portfolio UI and chat interface | N/A (client-side) |

#### Frontend Structure
- **Source**: Generated from Vercel v0.app
- **Location**: `/frontend` (contains `/app`, `/components`, `/lib`, `/styles`)
- **Build**: Next.js with static export capability

### Backend

| Component | Technology | Purpose | Free Tier Considerations |
|-----------|------------|---------|--------------------------|
| **API** | API Gateway | REST endpoints for RAG queries | 1M API calls/month |
| **Compute** | Lambda (Java) | Serverless request handling | 1M requests, 400K GB-seconds |
| **IaC** | Terraform | Infrastructure provisioning | N/A |

#### Backend Structure
- **Source**: HashiCorp Terraform tutorial (example code)
- **Location**: `/*.tf`, `/backend`, `/hello-world`
- **Runtime**: Java (Gradle build)

### RAG Orchestration Layer

| Component | Technology | Purpose | Free Tier Considerations |
|-----------|------------|---------|--------------------------|
| **LLM** | Amazon Bedrock (Claude/Titan) | Text generation, embeddings | Pay-per-use (minimize calls) |
| **Vector Store** | OpenSearch Serverless | Semantic search on embeddings | Limited free tier - use sparingly |
| **Knowledge Store** | S3 | Document storage for RAG context | 5GB storage |
| **Metadata** | DynamoDB | Session/conversation tracking | 25GB storage, 25 RCU/WCU |

#### RAG Flow
1. User submits query via chat interface
2. Lambda receives request via API Gateway
3. Query is embedded using Bedrock Titan
4. OpenSearch performs similarity search
5. Relevant documents retrieved from S3
6. Context + query sent to Bedrock Claude
7. Response returned to user

---

## Code Style & Design Principles

### Functional Programming Style

All code must adhere to functional programming principles:

```java
// ✅ GOOD: Pure function, descriptive name, no side effects
public List<Document> filterRelevantDocuments(List<Document> documents, double threshold) {
    return documents.stream()
        .filter(doc -> doc.getRelevanceScore() >= threshold)
        .collect(Collectors.toList());
}

// ❌ BAD: Side effects, unclear purpose, uses flags
public void processDocuments(List<Document> documents, boolean shouldLog, boolean shouldCache) {
    for (Document doc : documents) {
        if (shouldLog) logger.info(doc);
        if (shouldCache) cache.put(doc);
        // mutating external state
    }
}
```

### Core Principles

1. **Minimal Side Effects**
   - Functions should be pure where possible
   - State changes should be explicit and isolated
   - Prefer immutable data structures

2. **Self-Explanatory Method Names**
   - Method names describe what they do
   - No abbreviations unless universally understood
   - Verb-noun pattern for actions: `retrieveDocuments()`, `calculateSimilarity()`

3. **No Flags/Switches in Method Parameters**
   ```java
   // ❌ BAD
   public Response handleRequest(Request req, boolean useCache, boolean verbose) { }
   
   // ✅ GOOD: Separate methods or configuration objects
   public Response handleRequest(Request req, RequestConfig config) { }
   // OR
   public Response handleCachedRequest(Request req) { }
   public Response handleDirectRequest(Request req) { }
   ```

4. **Professional Quality Code**
   - Comments explain "why", not "what"
   - Consistent formatting and naming conventions
   - Error handling with meaningful messages

### TypeScript/React Guidelines

```typescript
// ✅ GOOD: Functional component, typed props, no side effects in render
interface ChatMessageProps {
  readonly message: Message;
  readonly onRetry: (messageId: string) => void;
}

const ChatMessage: React.FC<ChatMessageProps> = ({ message, onRetry }) => {
  const formattedTime = formatTimestamp(message.timestamp);
  
  return (
    <div className="chat-message">
      <span>{message.content}</span>
      <time>{formattedTime}</time>
    </div>
  );
};
```

---

## Current Project State

### What Exists

| Component | Status | Location | Notes |
|-----------|--------|----------|-------|
| Frontend UI | ✅ Imported | `/frontend` | v0.app generated, needs integration |
| Terraform Base | ✅ Imported | `/*.tf` | HashiCorp tutorial example |
| Backend Skeleton | ⚠️ Partial | `/backend` | Only build.gradle exists |
| RAG System | ❌ Not Started | N/A | Needs full implementation |
| Lambda Functions | ❌ Not Started | N/A | Only hello-world example |
| Infrastructure | ❌ Not Started | N/A | Terraform needs customization |

### What Does NOT Work

1. **Backend Logic** - No Java Lambda handlers implemented
2. **RAG Pipeline** - No Bedrock/OpenSearch integration
3. **API Gateway** - Not configured in Terraform
4. **Frontend-Backend Integration** - Chat uses mock data (`/lib/assistant-mock.ts`)
5. **Deployment** - No CI/CD or deployment scripts

### Known Gaps

- No authentication/authorization
- No rate limiting for API calls
- No cost monitoring/alerts
- No error tracking/logging infrastructure
- Frontend chat interface uses mock responses

---

## Testing Strategy

### Unit Testing Requirements

All code changes must include appropriate unit tests:

#### Java/Lambda Testing
```java
// Example test structure
@Test
void retrieveDocuments_withValidQuery_returnsRelevantDocuments() {
    // Arrange
    var query = "experience with AWS";
    var expectedDocs = List.of(mockDocument("aws-experience.md"));
    
    // Act
    var result = documentService.retrieveDocuments(query);
    
    // Assert
    assertThat(result).containsExactlyElementsOf(expectedDocs);
}
```

#### TypeScript/React Testing
```typescript
// Example test structure
describe('ChatMessage', () => {
  it('renders message content correctly', () => {
    const message = { id: '1', content: 'Hello', timestamp: Date.now() };
    render(<ChatMessage message={message} onRetry={jest.fn()} />);
    expect(screen.getByText('Hello')).toBeInTheDocument();
  });
});
```

### Test Coverage Goals

| Layer | Minimum Coverage | Focus Areas |
|-------|------------------|-------------|
| Lambda Handlers | 80% | Input validation, error handling |
| RAG Orchestration | 90% | Document retrieval, LLM integration |
| Frontend Components | 70% | User interactions, state management |
| Utility Functions | 95% | Edge cases, type safety |

---

## Documentation Standards

### Documentation.md Updates

For every code change, `Documentation.md` must be updated with:

1. **What Changed** - Brief description of the modification
2. **Why It Changed** - Business/technical rationale
3. **How It Works** - Technical explanation for developers
4. **What Doesn't Work** - Known limitations or TODOs
5. **Dependencies** - New libraries or services added

### Code Comments

```java
// Comments explain WHY, not WHAT
// ✅ GOOD: Explains business logic
// Using 0.7 threshold based on empirical testing with resume documents
private static final double RELEVANCE_THRESHOLD = 0.7;

// ❌ BAD: States the obvious
// Set threshold to 0.7
private static final double RELEVANCE_THRESHOLD = 0.7;
```

---

## Free Tier Optimization Strategies

### Cost Control Measures

1. **Bedrock Usage**
   - Cache common queries and responses
   - Use smaller context windows where possible
   - Batch embedding requests

2. **OpenSearch**
   - Consider alternatives (e.g., in-memory for small datasets)
   - Limit index size and query frequency

3. **Lambda**
   - Optimize cold start times (minimize dependencies)
   - Use appropriate memory allocation
   - Implement request batching where possible

4. **DynamoDB**
   - Use on-demand capacity mode
   - Implement TTL for conversation history
   - Minimize attribute storage

5. **S3/CloudFront**
   - Enable compression
   - Set appropriate cache headers
   - Use lifecycle policies for old data

---

## Next Steps (Implementation Roadmap)

1. [ ] Customize Terraform for actual infrastructure needs
2. [ ] Implement Java Lambda handlers with proper structure
3. [ ] Set up API Gateway with proper routes
4. [ ] Integrate Bedrock for embeddings and LLM calls
5. [ ] Configure OpenSearch (or alternative) for vector storage
6. [ ] Connect frontend to real backend APIs
7. [ ] Implement proper error handling and logging
8. [ ] Add authentication layer
9. [ ] Set up CI/CD pipeline
10. [ ] Create cost monitoring dashboards

---

*Document Version: 1.0*  
*Last Updated: 2026-01-18*  
*Status: Initial Draft*
