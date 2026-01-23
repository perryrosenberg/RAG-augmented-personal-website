import type { AssistantQueryRequest, AssistantQueryResponse } from "./assistant-types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "https://perryrosenberg.com";

/**
 * Sends a query to the RAG assistant backend API.
 *
 * This function makes a POST request to the backend Lambda function which:
 * 1. Retrieves relevant documents from the S3 knowledge base
 * 2. Builds context from matched documents
 * 3. Generates an AI response using Amazon Bedrock (Claude)
 * 4. Returns the answer along with source documents
 *
 * @param question - The user's question to send to the assistant
 * @param conversationId - Unique identifier for the conversation session
 * @param page - The current page context (default: "home")
 * @returns Promise resolving to the assistant's response with answer and sources
 * @throws Error if the API request fails
 *
 * @example
 * ```ts
 * const response = await queryAssistant(
 *   "How do you design ETL pipelines?",
 *   "conv-123",
 *   "home"
 * );
 * console.log(response.answer); // AI-generated response
 * console.log(response.sources); // Source documents used
 * ```
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
 * Performs a health check on the assistant API.
 *
 * Sends a GET request to the /api/health endpoint to verify the backend
 * Lambda function is responding correctly.
 *
 * @returns Promise resolving to true if the API is healthy, false otherwise
 *
 * @example
 * ```ts
 * const isHealthy = await checkApiHealth();
 * if (isHealthy) {
 *   console.log("API is ready");
 * }
 * ```
 */
export async function checkApiHealth(): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/health`);
    return response.ok;
  } catch {
    return false;
  }
}
