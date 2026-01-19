export interface AssistantSource {
  id: string;
  title: string;
  type: "Architecture Doc" | "Resume" | "Case Study" | "Technical Blog" | "System Design";
  confidence: number;
  excerpt: string;
}

export interface AssistantMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  timestamp: Date;
  sources?: AssistantSource[];
}

export interface AssistantQueryRequest {
  question: string;
  conversationId: string;
  context: {
    page: string;
  };
}

export interface AssistantQueryResponse {
  answer: string;
  sources: AssistantSource[];
  conversationId: string;
}
