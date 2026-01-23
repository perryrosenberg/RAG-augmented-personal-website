# Local Development Guide

This guide explains how to run and test the RAG-augmented personal website locally before deploying to AWS.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Backend Development](#backend-development)
3. [Frontend Development](#frontend-development)
4. [Testing Locally](#testing-locally)
5. [Debugging](#debugging)
6. [Common Issues](#common-issues)

---

## Prerequisites

### Required Tools

| Tool | Version | Purpose |
|------|---------|---------|
| **Java JDK** | 17+ | Lambda runtime environment |
| **Gradle** | 8.5+ | Backend build tool (via wrapper) |
| **Node.js** | 20.9.0+ | Frontend development (Next.js 16) |
| **AWS CLI** | 2.x | AWS service interaction |
| **SAM CLI** | 1.100.0+ | Local Lambda testing |
| **Docker** | Latest | Required by SAM CLI for Lambda containers |

### Installation

**Java JDK 17:**
```bash
# Windows (using Chocolatey)
choco install openjdk17

# macOS (using Homebrew)
brew install openjdk@17

# Linux (Ubuntu/Debian)
sudo apt install openjdk-17-jdk
```

**AWS CLI:**
```bash
# Windows
choco install awscli

# macOS
brew install awscli

# Linux
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

**SAM CLI:**
```bash
# Windows
choco install aws-sam-cli

# macOS
brew install aws-sam-cli

# Linux
pip install aws-sam-cli
```

**Docker:**
- Windows/macOS: Download [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Linux: Follow [official instructions](https://docs.docker.com/engine/install/)

### AWS Configuration

Configure AWS credentials for local testing:

```bash
aws configure

# Or set environment variables
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_DEFAULT_REGION=us-east-1
```

---

## Backend Development

### Building the Backend

```bash
cd backend

# Build JAR (Windows)
./gradlew.bat clean jar

# Build JAR (Unix/macOS)
./gradlew clean jar

# Output: backend/build/libs/query-handler.jar
```

### Running Tests

```bash
# Run all Java tests
./gradlew.bat test          # Windows
./gradlew test              # Unix/macOS

# Run tests with coverage
./gradlew.bat test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Local Lambda Execution with SAM

#### 1. Start Local API Gateway

```bash
cd backend

# Start API on port 3001
sam local start-api --template template.yaml --port 3001

# With environment variables
sam local start-api --env-vars env.json --port 3001

# With Docker network (if using LocalStack)
sam local start-api --docker-network sam-local --port 3001
```

The API will be available at `http://localhost:3001`

**Endpoints:**
- `POST http://localhost:3001/api/query` - RAG query endpoint
- `GET http://localhost:3001/api/health` - Health check

#### 2. Invoke Function Directly

```bash
# Invoke with test event
sam local invoke QueryHandlerFunction --event events/query-event.json

# With environment overrides
sam local invoke QueryHandlerFunction \
  --env-vars env.json \
  --event events/query-event.json
```

#### 3. Test with cURL

```bash
# Health check
curl http://localhost:3001/api/health

# Query endpoint
curl -X POST http://localhost:3001/api/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is your experience with AWS?",
    "conversationId": "test-123",
    "context": { "page": "home" }
  }'
```

### Creating Test Events

Create `backend/events/query-event.json`:

```json
{
  "httpMethod": "POST",
  "path": "/api/query",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": "{\"question\":\"Tell me about your experience\",\"conversationId\":\"test-123\"}"
}
```

### Hot Reloading

SAM CLI does not support hot reloading. To test changes:

1. Rebuild the JAR: `./gradlew.bat clean jar`
2. Restart SAM CLI: `sam local start-api`

**Tip:** Use an IDE debugger with remote attach instead.

---

## Frontend Development

### Installing Dependencies

```bash
cd frontend

# Using npm
npm install

# Or using pnpm (faster)
pnpm install
```

### Running Development Server

```bash
# Start Next.js dev server
npm run dev

# Server starts at http://localhost:3000
```

**Features:**
- Hot module reloading (instant updates)
- TypeScript type checking
- Fast refresh for React components

### Connecting to Local Backend

Update `frontend/lib/assistant-api.ts` to point to local backend:

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:3001";
```

Or set environment variable:

```bash
# Create frontend/.env.local
echo "NEXT_PUBLIC_API_URL=http://localhost:3001" > .env.local
```

### Running Frontend Tests

```bash
cd frontend

# Run all tests
npm test

# Run tests in watch mode (recommended for development)
npm run test:watch

# Run tests with coverage
npm run test:coverage

# View coverage report
open coverage/lcov-report/index.html
```

### Building for Production

```bash
# Build static export
npm run build

# Preview production build locally
npm run start
```

---

## Testing Locally

### Running All Tests

```bash
# From project root
./test.sh

# Run only backend tests
./test.sh --backend

# Run only frontend tests
./test.sh --frontend

# Run with coverage reports
./test.sh --coverage
```

### Manual Integration Testing

1. **Start backend API:**
   ```bash
   cd backend
   sam local start-api --port 3001
   ```

2. **Start frontend dev server:**
   ```bash
   cd frontend
   npm run dev
   ```

3. **Test in browser:**
   - Navigate to `http://localhost:3000`
   - Use the chat interface to send queries
   - Verify responses from local Lambda

### Testing with Real AWS Resources

To test with real Bedrock and S3:

1. **Update environment variables** in `backend/env.json`:
   ```json
   {
     "QueryHandlerFunction": {
       "KNOWLEDGE_BUCKET": "your-actual-bucket-name",
       "CONVERSATIONS_TABLE": "your-actual-table-name",
       "DOCUMENTS_TABLE": "your-actual-documents-table",
       "ENVIRONMENT": "dev",
       "AWS_REGION": "us-east-1"
     }
   }
   ```

2. **Ensure IAM permissions** - Your AWS profile must have:
   - `bedrock:InvokeModel`
   - `bedrock-agent-runtime:Retrieve`
   - `s3:GetObject` on knowledge bucket
   - `dynamodb:*` on tables

3. **Start SAM with environment:**
   ```bash
   sam local start-api --env-vars env.json
   ```

---

## Debugging

### Java Debugging (IntelliJ IDEA / VSCode)

1. **Start SAM in debug mode:**
   ```bash
   sam local start-api --debug-port 5858
   ```

2. **Configure IDE remote debugging:**

   **IntelliJ IDEA:**
   - Run → Edit Configurations → Add New Configuration → Remote JVM Debug
   - Host: `localhost`, Port: `5858`
   - Set breakpoints in code
   - Click Debug

   **VSCode:**
   ```json
   // .vscode/launch.json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "Attach to SAM Local",
         "request": "attach",
         "hostName": "localhost",
         "port": 5858
       }
     ]
   }
   ```

3. **Send request to trigger breakpoint:**
   ```bash
   curl -X POST http://localhost:3001/api/query \
     -H "Content-Type: application/json" \
     -d '{"question":"test","conversationId":"123"}'
   ```

### Frontend Debugging

1. **Chrome DevTools:**
   - Open Chrome → F12 → Sources tab
   - Set breakpoints in TypeScript files
   - Files are source-mapped automatically

2. **VSCode Debugging:**
   ```json
   // .vscode/launch.json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "name": "Next.js: debug server-side",
         "type": "node-terminal",
         "request": "launch",
         "command": "npm run dev"
       },
       {
         "name": "Next.js: debug client-side",
         "type": "chrome",
         "request": "launch",
         "url": "http://localhost:3000"
       }
     ]
   }
   ```

### Logs

**Backend (Lambda):**
```bash
# SAM CLI logs are printed to console
# Check for:
# - Cold start messages
# - AWS SDK calls
# - SLF4J log output
```

**Frontend (Next.js):**
```bash
# Console output shows:
# - Compilation status
# - Error messages
# - HTTP requests (in browser DevTools)
```

---

## Common Issues

### Issue: SAM CLI "No such file or directory" on Windows

**Solution:**
Use Git Bash or WSL instead of PowerShell/CMD.

```bash
# In Git Bash
sam local start-api
```

### Issue: Docker daemon not running

**Error:** `Cannot connect to the Docker daemon`

**Solution:**
Start Docker Desktop and ensure it's running:
```bash
docker ps  # Should list containers, not error
```

### Issue: Port already in use

**Error:** `Address already in use`

**Solution:**
Kill process using the port:
```bash
# Find process (Windows)
netstat -ano | findstr :3001
taskkill /PID <PID> /F

# Find process (Unix/macOS)
lsof -ti:3001 | xargs kill -9
```

### Issue: Tests fail with "Module not found"

**Solution:**
Install dependencies:
```bash
cd frontend
npm install
```

### Issue: Gradle build fails on Windows

**Error:** `Could not determine java version`

**Solution:**
Set JAVA_HOME:
```bash
export JAVA_HOME="C:\Program Files\Java\jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
```

### Issue: Lambda times out during local invocation

**Possible causes:**
1. **No AWS credentials** - Set via `aws configure`
2. **Network issues** - Check internet connection for Bedrock API calls
3. **Large payload** - Reduce context/document size
4. **Cold start** - First invocation takes longer (30+ seconds)

**Solution:**
Increase timeout in `template.yaml`:
```yaml
Globals:
  Function:
    Timeout: 60  # Increase from 30 to 60 seconds
```

### Issue: Frontend can't connect to backend

**Error:** `Failed to fetch` or `CORS error`

**Solution:**
1. Ensure SAM API is running: `curl http://localhost:3001/api/health`
2. Check CORS headers in Lambda response
3. Verify `NEXT_PUBLIC_API_URL` in `.env.local`
4. Check browser console for exact error

### Issue: Tests pass locally but fail in CI

**Common reasons:**
1. **Environment differences** - Check Node/Java versions match
2. **Missing dependencies** - Ensure all deps in package.json/build.gradle
3. **Timezone issues** - Use UTC in tests
4. **File path separators** - Use `path.join()` instead of string concatenation

---

## Next Steps

1. **Run tests:** `./test.sh`
2. **Start backend:** `cd backend && sam local start-api`
3. **Start frontend:** `cd frontend && npm run dev`
4. **Test integration:** Visit `http://localhost:3000`

For deployment to AWS, see [README.md](README.md) and run `./deploy.sh`.

---

**Questions?** Check [Architecture.md](Architecture.md) for system design details.
