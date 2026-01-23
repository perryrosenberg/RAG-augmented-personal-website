/**
 * Represents a source document returned by the RAG system.
 */
export interface AssistantSource {
  /** Unique identifier for the source document */
  readonly id: string;
  /** Human-readable title of the document */
  readonly title: string;
  /** Type/category of the document */
  readonly type: "Architecture Doc" | "Resume" | "Case Study" | "Technical Blog" | "System Design";
  /** Confidence score for relevance (0.0 to 1.0) */
  readonly confidence: number;
  /** Short excerpt from the document showing relevant content */
  readonly excerpt: string;
}

/**
 * Represents a single message in the chat conversation.
 */
export interface AssistantMessage {
  /** Unique identifier for the message */
  readonly id: string;
  /** Role of the message sender */
  readonly role: "user" | "assistant";
  /** Content of the message */
  readonly content: string;
  /** When the message was sent */
  readonly timestamp: Date;
  /** Source documents (only for assistant responses) */
  readonly sources?: AssistantSource[];
}

/**
 * Request payload sent to the RAG assistant API.
 */
export interface AssistantQueryRequest {
  /** The user's question */
  readonly question: string;
  /** Unique conversation identifier for tracking context */
  readonly conversationId: string;
  /** Additional context about where the question originated */
  readonly context: {
    readonly page: string;
  };
}

/**
 * Response payload from the RAG assistant API.
 */
export interface AssistantQueryResponse {
  /** The AI-generated answer to the user's question */
  readonly answer: string;
  /** Source documents used to generate the answer */
  readonly sources: AssistantSource[];
  /** The conversation identifier (same as request) */
  readonly conversationId: string;
}
