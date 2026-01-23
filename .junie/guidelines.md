# Development Guidelines

> **Updated:** January 2026
> **Status:** Production-Ready

This document outlines development guidelines, coding standards, and best practices for the RAG-Augmented Personal Website project.

---

## Table of Contents

1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Project Structure](#project-structure)
4. [Code Style & Standards](#code-style--standards)
5. [Testing Guidelines](#testing-guidelines)
6. [Build & Deployment](#build--deployment)
7. [Common Tasks](#common-tasks)
8. [Troubleshooting](#troubleshooting)

---

## Overview

This project is a cloud-native RAG (Retrieval-Augmented Generation) system with:

- **Backend:** Java 17 Lambda functions with Gradle
- **Frontend:** Next.js 16 (TypeScript, React 19, Tailwind CSS)
- **Infrastructure:** Terraform for AWS deployment
- **Testing:** 70+ JUnit tests + 200+ Jest tests
- **CI/CD:** Automated testing before deployment

**Current Implementation Status:** ✅ Production-Ready

All major components are implemented and tested:
- ✅ Backend Lambda handlers (Java 17)
- ✅ RAG orchestration with Bedrock Knowledge Base
- ✅ S3 Vectors for cost-effective vector search
- ✅ Next.js frontend with chat interface
- ✅ Comprehensive test coverage (70+ backend, 200+ frontend)
- ✅ Local development setup with SAM CLI
- ✅ Automated deployment with quality gates

---

## Getting Started

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| **Java JDK** | 17+ | Backend Lambda runtime |
| **Node.js** | 20.9.0+ | Frontend (Next.js 16 requirement) |
| **Gradle** | 8.5+ | Via wrapper (`./gradlew`) |
| **AWS CLI** | 2.x | Deployment and testing |
| **Terraform** | 1.x | Infrastructure provisioning |
| **SAM CLI** | 1.100.0+ | Local Lambda testing (optional) |
| **Docker** | Latest | Required by SAM CLI (optional) |

### Quick Setup

```bash
# 1. Clone repository
git clone <repo-url>
cd RAG-augmented-personal-website

# 2. Configure AWS
aws configure
# Enter: Access Key, Secret, Region (us-east-1), Output (json)

# 3. Run tests
./test.sh

# 4. Deploy
./deploy.sh
```

---

## Project Structure

```
RAG-augmented-personal-website/
├── backend/                                    # Java 17 Lambda backend
│   ├── src/main/java/com/perryrosenberg/portfolio/
│   │   ├── constants/BedrockConstants.java    # Configuration constants
│   │   ├── dto/
│   │   │   ├── request/QueryRequest.java      # Request DTOs
│   │   │   └── response/QueryResponse.java    # Response DTOs
│   │   ├── handler/QueryHandler.java          # API Gateway handler
│   │   └── service/RagOrchestrationService.java # RAG business logic
│   ├── src/test/java/                         # JUnit 5 tests (70+ tests)
│   │   ├── handler/QueryHandlerTest.java
│   │   ├── service/RagOrchestrationServiceTest.java
│   │   └── dto/DtoSerializationTest.java
│   ├── build.gradle                           # Gradle dependencies
│   ├── template.yaml                          # SAM template for local dev
│   ├── samconfig.toml                         # SAM CLI config
│   └── env.json                               # Local environment vars
│
├── frontend/                                   # Next.js 16 frontend
│   ├── app/                                   # Next.js app directory
│   ├── components/
│   │   ├── assistant/                         # RAG chat components
│   │   │   ├── chat-message.tsx
│   │   │   ├── chat-input.tsx
│   │   │   ├── sources-panel.tsx
│   │   │   └── loading-indicator.tsx
│   │   └── ui/                                # Shadcn/UI components
│   ├── lib/
│   │   ├── assistant-api.ts                   # Backend API client
│   │   ├── assistant-types.ts                 # TypeScript interfaces
│   │   └── utils.ts                           # Utility functions
│   ├── __tests__/                             # Jest tests (200+ tests)
│   │   ├── components/                        # Component tests
│   │   └── lib/                               # API/util tests
│   ├── jest.config.js                         # Jest configuration
│   ├── jest.setup.js                          # Test environment setup
│   └── package.json                           # npm dependencies
│
├── rag-resources/                              # Knowledge base documents
│   └── [your documents here]                  # Resume, projects, etc.
│
├── main.tf                                     # Terraform main config
├── variables.tf                                # Terraform variables
├── outputs.tf                                  # Terraform outputs
├── terraform.tf                                # Provider config
│
├── deploy.sh                                   # Full deployment script
├── test.sh                                     # Test runner script
│
├── Architecture.md                             # Architecture documentation
├── LOCAL_DEVELOPMENT.md                        # Local dev guide
├── CLAUDE.md                                   # AI assistant reference
└── README.md                                   # Main documentation
```

---

## Code Style & Standards

### General Principles

1. **Functional Programming First**
   - Pure functions with minimal side effects
   - Immutable data structures where possible
   - Self-explanatory function names

2. **Descriptive Naming**
   - Use full words, no abbreviations
   - Verb-noun pattern: `retrieveDocuments()`, `calculateConfidence()`
   - Boolean names: `isValid()`, `hasPermission()`

3. **No Boolean Flags**
   - ❌ Bad: `processData(data, true, false, true)`
   - ✅ Good: Separate methods or config objects

4. **Comments Explain Why, Not What**
   - Code should be self-explanatory
   - Comments explain business logic, trade-offs, or "why" decisions

### Java (Backend)

**Package Structure:**
```
com.perryrosenberg.portfolio/
├── constants/    # Configuration constants
├── dto/          # Data Transfer Objects
│   ├── request/  # Request DTOs
│   └── response/ # Response DTOs
├── handler/      # Lambda handlers
└── service/      # Business logic
```

**Code Standards:**
```java
// ✅ GOOD: Pure function, clear intent
public List<Source> filterByConfidence(List<Source> sources, double minConfidence) {
    return sources.stream()
        .filter(source -> source.getConfidence() >= minConfidence)
        .collect(Collectors.toList());
}

// ✅ GOOD: Comprehensive JavaDoc
/**
 * Orchestrates RAG queries using Amazon Bedrock Knowledge Base.
 *
 * <p>This service retrieves relevant documents via vector search and
 * generates responses using Claude 3 Haiku with retrieved context.
 *
 * @author Perry Rosenberg
 */
public class RagOrchestrationService {
    // ...
}

// ✅ GOOD: Constants extracted to dedicated class
public final class BedrockConstants {
    public static final String LLM_MODEL_ID =
        "us.anthropic.claude-haiku-4-5-20251001-v1:0";
    public static final int MAX_TOKENS = 1024;
    // ...
}

// ✅ GOOD: SLF4J logging instead of System.out
private static final Logger logger =
    LoggerFactory.getLogger(QueryHandler.class);
logger.info("Processing query: {}", request.getQuestion());
logger.error("Error retrieving documents", exception);

// ❌ BAD: Boolean flags, side effects, unclear intent
public void processSources(List<Source> sources, boolean log, boolean cache) {
    for (Source source : sources) {
        if (log) System.out.println(source);  // BAD: System.out
        if (cache) cache.put(source);          // BAD: Side effect
    }
}
```

**Testing Standards:**
```java
// Use JUnit 5 with Arrange-Act-Assert pattern
@Test
@DisplayName("POST /api/query with valid request returns 200 with sources")
void handleRequest_validQueryRequest_returnsSuccessWithSources() {
    // Arrange
    QueryRequest request = new QueryRequest("test question", "conv-123");
    APIGatewayProxyRequestEvent event = createMockEvent(request);

    // Act
    APIGatewayProxyResponseEvent response = handler.handleRequest(event, mockContext);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getBody()).contains("\"answer\":");
    assertThat(response.getHeaders()).containsEntry("Access-Control-Allow-Origin", "*");
}
```

### TypeScript/React (Frontend)

**Component Structure:**
```typescript
// ✅ GOOD: Typed props with readonly, functional component
interface ChatMessageProps {
  readonly message: AssistantMessage;
  readonly compact?: boolean;
}

export const ChatMessage = memo(function ChatMessage({
  message,
  compact
}: ChatMessageProps) {
  const isUser = message.role === "user";

  return (
    <div className={cn("message", isUser && "user-message")}>
      {isUser ? (
        <p>{message.content}</p>
      ) : (
        <ReactMarkdown>{message.content}</ReactMarkdown>
      )}
    </div>
  );
});

// ✅ GOOD: Comprehensive JSDoc
/**
 * Sends a query to the RAG assistant backend API.
 *
 * This function makes a POST request to the backend Lambda which:
 * 1. Retrieves relevant documents from the S3 knowledge base
 * 2. Builds context from matched documents
 * 3. Generates an AI response using Amazon Bedrock (Claude)
 * 4. Returns the answer along with source documents
 *
 * @param question - The user's question
 * @param conversationId - Unique conversation identifier
 * @returns Promise resolving to the assistant's response
 */
export async function queryAssistant(
  question: string,
  conversationId: string
): Promise<AssistantQueryResponse> {
  // ...
}

// ❌ BAD: No types, side effects in render
function Component({ data }) {  // BAD: No types
  const results = [];           // BAD: Mutation
  data.forEach(item => {
    results.push(item.value);   // BAD: Side effect
  });
  return <div>{results}</div>;
}
```

**Testing Standards:**
```typescript
// Use Jest + React Testing Library
describe('ChatInput', () => {
  const mockOnSubmit = jest.fn();

  beforeEach(() => {
    mockOnSubmit.mockClear();
  });

  it('calls onSubmit when send button is clicked', async () => {
    const user = userEvent.setup();
    render(<ChatInput onSubmit={mockOnSubmit} isLoading={false} />);

    const textarea = screen.getByRole('textbox');
    await user.type(textarea, 'Test message');

    const sendButton = screen.getByLabelText('Send message');
    await user.click(sendButton);

    expect(mockOnSubmit).toHaveBeenCalledWith('Test message');
  });
});
```

---

## Testing Guidelines

### Running Tests

```bash
# Run all tests (backend + frontend)
./test.sh

# Run with coverage reports
./test.sh --coverage

# Run only backend tests
./test.sh --backend

# Run only frontend tests
./test.sh --frontend
```

### Backend Testing (Java + JUnit 5)

**Location:** `backend/src/test/java/`

**Test Coverage Goals:** 80% line coverage

**Test Suites:**
1. **QueryHandlerTest** (39 tests) - API Gateway integration, CORS, routing
2. **RagOrchestrationServiceTest** (27 tests) - RAG logic, Bedrock calls
3. **DtoSerializationTest** (27 tests) - JSON serialization

**Best Practices:**
- Use `@Nested` classes for organizing related tests
- Use `@DisplayName` for readable test descriptions
- Use Mockito for AWS SDK mocking
- Use AssertJ for fluent assertions
- Follow Arrange-Act-Assert pattern

**Example:**
```java
@Nested
@DisplayName("Query Endpoint Tests")
class QueryEndpointTests {

    @Test
    @DisplayName("Valid request returns 200 with answer and sources")
    void validRequest_returnsSuccessWithSources() {
        // Arrange
        var request = createValidRequest();

        // Act
        var response = handler.handleRequest(request, context);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"answer\":");
    }
}
```

### Frontend Testing (TypeScript + Jest)

**Location:** `frontend/__tests__/`

**Test Coverage Goals:** 70% line coverage

**Test Suites:**
1. **chat-message.test.tsx** (30+ tests) - Message rendering
2. **chat-input.test.tsx** (50+ tests) - User input handling
3. **sources-panel.test.tsx** (50+ tests) - Source display
4. **loading-indicator.test.tsx** (30+ tests) - Loading states
5. **assistant-api.test.ts** (40+ tests) - API integration

**Best Practices:**
- Use React Testing Library (not Enzyme)
- Query by accessible roles/labels
- Test user interactions, not implementation
- Mock fetch for API tests
- Test edge cases (empty states, errors, special characters)

**Example:**
```typescript
describe('ChatMessage', () => {
  it('renders user message with correct styling', () => {
    const message: AssistantMessage = {
      id: 'msg-1',
      role: 'user',
      content: 'Hello',
      timestamp: new Date(),
    };

    render(<ChatMessage message={message} />);

    expect(screen.getByText('You')).toBeInTheDocument();
    expect(screen.getByText('Hello')).toBeInTheDocument();
  });
});
```

---

## Build & Deployment

### Local Development

**Backend:**
```bash
cd backend

# Build JAR
./gradlew.bat clean jar          # Windows
./gradlew clean jar              # Unix/macOS

# Run tests
./gradlew.bat test

# Start local Lambda (SAM CLI + Docker)
sam local start-api --template template.yaml --port 3001
```

**Frontend:**
```bash
cd frontend

# Install dependencies
npm install

# Start dev server (http://localhost:3000)
npm run dev

# Run tests
npm test

# Build for production
npm run build
```

### Full Deployment

```bash
# Full deployment with tests
./deploy.sh

# Deploy with specific flags
./deploy.sh --skip-tests              # Skip tests (not recommended)
./deploy.sh --skip-frontend           # Deploy backend only
./deploy.sh --terraform-plan-only     # Plan only, no apply
```

**Deployment Steps:**
1. ✅ Verify AWS credentials
2. 🧪 Run test suite (quality gate)
3. 🔨 Build backend JAR
4. 🏗️ Deploy Terraform infrastructure
5. 📦 Build and deploy frontend
6. 📚 Upload RAG documents

### Document Ingestion

After deployment, ingest documents into Knowledge Base:

```bash
KNOWLEDGE_BASE_ID=$(terraform output -raw knowledge_base_id)
DATA_SOURCE_ID=$(terraform output -raw data_source_id)

aws bedrock-agent start-ingestion-job \
  --knowledge-base-id $KNOWLEDGE_BASE_ID \
  --data-source-id $DATA_SOURCE_ID
```

---

## Common Tasks

### Adding New Backend Endpoint

1. **Add handler method in `QueryHandler.java`:**
   ```java
   private APIGatewayProxyResponseEvent handleNewEndpoint(
       APIGatewayProxyRequestEvent event
   ) {
       // Implementation
   }
   ```

2. **Add route logic:**
   ```java
   if ("POST".equals(httpMethod) && "/api/new-endpoint".equals(path)) {
       return handleNewEndpoint(event);
   }
   ```

3. **Write tests in `QueryHandlerTest.java`:**
   ```java
   @Test
   void handleNewEndpoint_validRequest_returnsSuccess() {
       // Test implementation
   }
   ```

4. **Run tests and deploy:**
   ```bash
   ./test.sh --backend
   ./deploy.sh --skip-frontend
   ```

### Adding New Frontend Component

1. **Create component in `frontend/components/`:**
   ```typescript
   export interface MyComponentProps {
     readonly data: string;
   }

   export function MyComponent({ data }: MyComponentProps) {
       // Implementation
   }
   ```

2. **Add tests in `frontend/__tests__/components/`:**
   ```typescript
   describe('MyComponent', () => {
     it('renders correctly', () => {
       render(<MyComponent data="test" />);
       expect(screen.getByText('test')).toBeInTheDocument();
     });
   });
   ```

3. **Run tests:**
   ```bash
   cd frontend
   npm test
   ```

### Updating Knowledge Base Documents

1. **Add documents to `rag-resources/`:**
   ```bash
   cp /path/to/new-document.pdf rag-resources/
   ```

2. **Upload to S3:**
   ```bash
   BUCKET=$(terraform output -raw knowledge_bucket_name)
   aws s3 sync rag-resources/ s3://$BUCKET/documents/
   ```

3. **Trigger ingestion:**
   ```bash
   aws bedrock-agent start-ingestion-job \
     --knowledge-base-id $(terraform output -raw knowledge_base_id) \
     --data-source-id $(terraform output -raw data_source_id)
   ```

---

## Troubleshooting

### Backend Issues

**Issue:** Tests fail with `ClassNotFoundException`

**Solution:**
```bash
cd backend
./gradlew clean build
# Ensure query-handler.jar is created
```

**Issue:** Lambda cold start timeout

**Solution:**
Increase timeout in Terraform:
```hcl
resource "aws_lambda_function" "query_handler" {
  timeout = 60  # Increase from 30 to 60 seconds
}
```

### Frontend Issues

**Issue:** `Module not found` errors

**Solution:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

**Issue:** Tests fail with "Cannot find module '@testing-library/react'"

**Solution:**
```bash
cd frontend
npm install --save-dev @testing-library/react @testing-library/jest-dom
```

### Deployment Issues

**Issue:** Terraform "no changes" but Lambda code updated

**Solution:**
Force Lambda update:
```bash
cd backend && ./gradlew clean jar
aws lambda update-function-code \
  --function-name $(terraform output -raw lambda_function_name) \
  --zip-file fileb://build/libs/query-handler.jar
```

**Issue:** Frontend not updating after deploy

**Solution:**
Invalidate CloudFront cache:
```bash
aws cloudfront create-invalidation \
  --distribution-id $(terraform output -raw cloudfront_distribution_id) \
  --paths "/*"
```

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `deploy.sh` | Full deployment automation with tests |
| `test.sh` | Runs all tests (backend + frontend) |
| `Architecture.md` | System architecture and design |
| `LOCAL_DEVELOPMENT.md` | Local development setup |
| `README.md` | Main project documentation |
| `CLAUDE.md` | AI assistant reference |

---

## Additional Resources

- **AWS Lambda Java SDK:** https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html
- **Next.js 16 Docs:** https://nextjs.org/docs
- **Amazon Bedrock:** https://docs.aws.amazon.com/bedrock/
- **Terraform AWS Provider:** https://registry.terraform.io/providers/hashicorp/aws/latest/docs

---

**Last Updated:** January 2026
**Project Status:** ✅ Production-Ready
