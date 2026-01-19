import type { AssistantQueryResponse, AssistantSource } from "./assistant-types";

const mockSources: Record<string, AssistantSource[]> = {
  etl: [
    {
      id: "doc-001",
      title: "Orders Search ETL Architecture",
      type: "Architecture Doc",
      confidence: 0.94,
      excerpt:
        "The ETL pipeline was designed to handle 20M+ orders using AWS DMS for change data capture, DynamoDB streams for real-time updates, and OpenSearch for full-text search. Key considerations included idempotency, exactly-once processing semantics, and graceful degradation during peak load...",
    },
    {
      id: "doc-002",
      title: "Resume – Principal Engineer Experience",
      type: "Resume",
      confidence: 0.89,
      excerpt:
        "Led architecture and execution of an ETL pipeline processing 500k daily records, recovering a failing legacy system while maintaining zero customer-facing downtime. Designed for horizontal scalability using event-driven patterns...",
    },
    {
      id: "doc-003",
      title: "Scaling from 500K to 20M Orders",
      type: "Case Study",
      confidence: 0.85,
      excerpt:
        "When order volume grew 40x over 18 months, the existing batch processing approach couldn't keep up. We transitioned to a streaming architecture using Kafka and implemented backpressure mechanisms...",
    },
  ],
  distributed: [
    {
      id: "doc-004",
      title: "Distributed Systems Tradeoffs",
      type: "System Design",
      confidence: 0.92,
      excerpt:
        "When designing distributed systems, I prioritize: 1) Understanding the CAP theorem implications for each component, 2) Choosing appropriate consistency models based on business requirements, 3) Designing for failure with circuit breakers and bulkheads...",
    },
    {
      id: "doc-005",
      title: "Microservices Migration Strategy",
      type: "Architecture Doc",
      confidence: 0.87,
      excerpt:
        "The monolith-to-microservices transition was executed using the strangler fig pattern. We identified bounded contexts, established service boundaries based on domain-driven design principles, and implemented an API gateway for traffic management...",
    },
  ],
  event: [
    {
      id: "doc-006",
      title: "Event Sourcing Implementation",
      type: "Architecture Doc",
      confidence: 0.91,
      excerpt:
        "Event sourcing captures all changes to application state as a sequence of events. Benefits include complete audit trails, temporal queries, and the ability to rebuild state. We implemented this pattern for order lifecycle management...",
    },
    {
      id: "doc-007",
      title: "CQRS and Event-Driven Architecture",
      type: "Technical Blog",
      confidence: 0.84,
      excerpt:
        "Combining CQRS with event sourcing allows optimizing read and write models independently. The write model focuses on business invariants while read models can be denormalized for query performance...",
    },
  ],
  default: [
    {
      id: "doc-008",
      title: "Resume – Technical Leadership",
      type: "Resume",
      confidence: 0.88,
      excerpt:
        "10+ years of experience architecting cloud-native systems at scale. Expert in AWS services including Lambda, ECS, DynamoDB, and OpenSearch. Strong focus on observability, reliability engineering, and developer experience...",
    },
    {
      id: "doc-009",
      title: "Engineering Philosophy",
      type: "Technical Blog",
      confidence: 0.82,
      excerpt:
        "My approach to software architecture centers on: simplicity over complexity, measuring everything, building for change, and empowering teams through clear abstractions and well-documented decisions...",
    },
  ],
};

const mockResponses: Record<string, string> = {
  etl: `## ETL Pipeline Design for Large-Scale Order Processing

When designing an ETL pipeline for hundreds of millions of orders, I focus on several key architectural principles:

### 1. Change Data Capture (CDC)
Rather than periodic batch exports, I use CDC to capture changes in real-time:
- **AWS DMS** for database-level change capture
- **DynamoDB Streams** for NoSQL sources
- **Debezium** for Kafka-based CDC

### 2. Streaming Architecture
\`\`\`
Source DB → CDC → Kafka → Transform → Load → Target
              ↓
         Dead Letter Queue
\`\`\`

### 3. Key Design Decisions
- **Idempotency**: Every operation must be safely repeatable
- **Ordering**: Maintain event ordering within partition keys
- **Backpressure**: Implement flow control to prevent overwhelm
- **Schema Evolution**: Use Avro/Protobuf with schema registry

### 4. Scaling Considerations
- Partition by tenant/order ID for parallelism
- Use spot instances for cost-effective compute
- Implement checkpointing for exactly-once semantics

This approach successfully scaled our system from 500K to 20M+ daily orders while maintaining sub-second latency for critical paths.`,

  distributed: `## Distributed Systems Design Tradeoffs

When designing distributed systems, I systematically evaluate tradeoffs across several dimensions:

### CAP Theorem Considerations
- **Consistency vs Availability**: For financial data, I lean toward consistency (CP systems)
- **Partition Tolerance**: Always design assuming network partitions will occur

### Key Tradeoffs I Consider

| Dimension | Option A | Option B | When to Choose A |
|-----------|----------|----------|------------------|
| Consistency | Strong | Eventual | Financial transactions, inventory |
| Coupling | Synchronous | Async/Event-driven | Simple flows, immediate feedback needed |
| Data | Centralized | Distributed | Strong consistency requirements |

### Patterns I Frequently Use
1. **Circuit Breakers** - Prevent cascade failures
2. **Bulkheads** - Isolate failure domains
3. **Saga Pattern** - Distributed transactions
4. **CQRS** - Separate read/write optimization

### Example: Order Processing
\`\`\`java
@CircuitBreaker(name = "inventory", fallbackMethod = "fallback")
public InventoryResponse checkInventory(OrderRequest request) {
    return inventoryClient.check(request.getItems());
}
\`\`\`

The goal is always to **fail gracefully** and **recover automatically**.`,

  event: `## Event Sourcing Approach

Event sourcing is a pattern I've successfully applied to order lifecycle management. Here's my approach:

### Core Principles
1. **Events are immutable facts** - Never update, only append
2. **State is derived** - Replay events to rebuild current state
3. **Audit trail built-in** - Complete history by design

### Implementation Pattern
\`\`\`typescript
interface OrderEvent {
  eventId: string;
  orderId: string;
  eventType: 'CREATED' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED';
  timestamp: Date;
  payload: Record<string, unknown>;
}

// Rebuild state by folding events
function rebuildOrder(events: OrderEvent[]): Order {
  return events.reduce((state, event) => {
    switch (event.eventType) {
      case 'CREATED': return { ...state, status: 'pending' };
      case 'CONFIRMED': return { ...state, status: 'confirmed' };
      // ... more cases
    }
  }, initialState);
}
\`\`\`

### Benefits Realized
- **Temporal queries**: "What was the order status at 3pm yesterday?"
- **Debugging**: Complete replay of any issue
- **Compliance**: Built-in audit logging

### Challenges & Mitigations
- **Event schema evolution** → Use versioned schemas
- **Performance at scale** → Implement snapshots
- **Eventual consistency** → Design idempotent consumers`,

  default: `## About My Engineering Experience

I'm a Principal Software Engineer with over a decade of experience designing and scaling cloud-native systems. Here's an overview of my expertise:

### Technical Leadership
- Led architecture for systems processing **20M+ orders monthly**
- Designed migration strategies from monolith to microservices
- Built observability platforms with **99.9% uptime SLAs**

### Core Competencies
- **Cloud Architecture**: AWS (Lambda, ECS, DynamoDB, OpenSearch)
- **System Design**: Event-driven architectures, CQRS, microservices
- **Data Engineering**: ETL pipelines, streaming systems, data modeling
- **DevOps**: CI/CD, infrastructure as code, observability

### Philosophy
I believe in:
1. **Measuring everything** - Decisions backed by data
2. **Simplicity over cleverness** - Maintainable > impressive
3. **Building for change** - Systems that evolve gracefully
4. **Empowering teams** - Clear abstractions and documentation

Feel free to ask me about specific architecture decisions, system design patterns, or scaling challenges I've tackled.`,
};

function getResponseKey(question: string): string {
  const lowerQuestion = question.toLowerCase();
  if (lowerQuestion.includes("etl") || lowerQuestion.includes("pipeline") || lowerQuestion.includes("orders")) {
    return "etl";
  }
  if (lowerQuestion.includes("distributed") || lowerQuestion.includes("tradeoff") || lowerQuestion.includes("scale")) {
    return "distributed";
  }
  if (lowerQuestion.includes("event") || lowerQuestion.includes("sourcing") || lowerQuestion.includes("cqrs")) {
    return "event";
  }
  return "default";
}

export async function mockAssistantQuery(question: string, conversationId: string): Promise<AssistantQueryResponse> {
  // Simulate network latency (1-2 seconds)
  await new Promise((resolve) => setTimeout(resolve, 1000 + Math.random() * 1000));

  const responseKey = getResponseKey(question);

  return {
    answer: mockResponses[responseKey],
    sources: mockSources[responseKey],
    conversationId,
  };
}
