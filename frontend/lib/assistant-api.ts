import type { AssistantQueryRequest, AssistantQueryResponse } from "./assistant-types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "https://perryrosenberg.com";

/**
 * Sends a query to the RAG assistant API.
 * Falls back to mock if API is unavailable during development.
 */
export async function queryAssistant(
  question: string,
  conversationId: string,
  page: string = "home"
): Promise<AssistantQueryResponse> {
  const request: AssistantQueryRequest = {
    question,
    conversationId,
    context: { page },
  };

  try {
    const response = await fetch(`${API_BASE_URL}/api/query`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Assistant API error:", error);
    // Re-throw to let the component handle the error
    throw error;
  }
}

/**
 * Health check for the assistant API.
 */
export async function checkApiHealth(): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/health`);
    return response.ok;
  } catch {
    return false;
  }
}
