# Development Guidelines

## Build & Configuration

### Frontend (Next.js)

**Location:** `/frontend`

**Requirements:** Node.js 20.9.0+ (required by Next.js 16)

```bash
cd frontend

# Install dependencies (pnpm preferred, npm works)
pnpm install
# or
npm install

# Development server
npm run dev

# Production build
npm run build

# Start production server
npm start

# Linting
npm run lint
```

### Backend (Java/Gradle)

The backend is located in `/backend` with an empty `build.gradle`. When implementing:

- Use Gradle for dependency management
- Target Java 17+ for Lambda compatibility
- Structure: handlers, services, models packages

### Infrastructure (Terraform)

Terraform files are in the project root (`*.tf`). Initialize with:

```bash
terraform init
terraform plan
terraform apply
```

---

## Testing

### Frontend Testing with Jest

**Configuration:** `jest.config.js` in project root

```bash
# Run all tests
npm test

# Watch mode
npm run test:watch
```

**Adding new tests:**

1. Create test files with `.test.js` suffix (e.g., `myModule.test.js`)
2. Place tests alongside the code they test in the same directory
3. Use Jest's built-in `describe`, `it`, and `expect`

**Example test structure:**

```javascript
describe('Feature name', () => {
  it('should do something specific', () => {
    const result = myFunction('input')
    expect(result).toBe('expected output')
  })
})
```

**TypeScript Testing:** Requires Node.js 18+ for `ts-jest`. Add to jest.config.js:

```javascript
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/*.test.ts', '**/*.test.tsx'],
}
```

### Backend Testing (Java)

When implementing backend tests, use JUnit 5:

```java
@Test
void methodName_condition_expectedResult() {
    // Arrange
    var input = "test";
    
    // Act
    var result = service.process(input);
    
    // Assert
    assertThat(result).isEqualTo(expected);
}
```

---

## Code Style

### Functional Programming Principles

- **Pure functions:** Minimize side effects, prefer immutable data
- **Self-explanatory names:** `retrieveDocuments()` not `getDocs()`
- **No boolean flags:** Use separate methods or config objects instead

### TypeScript/React

- Use functional components with typed props
- Mark props as `readonly` when appropriate
- Keep side effects in hooks, not render logic

```typescript
interface Props {
  readonly message: Message;
  readonly onAction: (id: string) => void;
}

const Component: React.FC<Props> = ({ message, onAction }) => {
  // ...
};
```

### Java

- Follow verb-noun pattern: `calculateSimilarity()`, `filterDocuments()`
- Comments explain "why", not "what"
- Use meaningful error messages

---

## Project Structure

```
/frontend                  - Next.js frontend (v0.app generated)
  /app                     - Next.js app directory
  /components              - React components
  /lib                     - Utility libraries
  /styles                  - Global CSS
  /public                  - Static assets
/backend                   - Java Lambda handlers (skeleton)
/*.tf                      - Terraform infrastructure
```

---

## Current Limitations

- Frontend chat uses mock data (`/frontend/lib/assistant-mock.ts`)
- Backend has no implemented handlers
- RAG pipeline not yet integrated
- No CI/CD pipeline configured

See `Architecture.md` for full system design and implementation roadmap.
