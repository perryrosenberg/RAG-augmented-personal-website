package com.ragwebsite.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ragwebsite.models.QueryRequest;
import com.ragwebsite.models.QueryResponse;
import com.ragwebsite.models.QueryResponse.Source;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveResponse;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseVectorSearchConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievalResultLocation;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievalResultContent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for orchestrating RAG (Retrieval-Augmented Generation) queries.
 * Integrates with Amazon Bedrock Knowledge Base for vector-based retrieval
 * and Bedrock Runtime for LLM inference.
 */
public class RagOrchestrationService {

    private final BedrockRuntimeClient bedrockClient;
    private final BedrockAgentRuntimeClient bedrockAgentClient;
    private final Gson gson;
    private final String knowledgeBaseId;

    // Bedrock model IDs
    private static final String LLM_MODEL_ID = "us.anthropic.claude-haiku-4-5-20251001-v1:0";

    // Maximum number of retrieval results
    private static final int MAX_RETRIEVAL_RESULTS = 5;

    public RagOrchestrationService() {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .build();
        this.bedrockAgentClient = BedrockAgentRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .build();
        this.gson = new Gson();
        this.knowledgeBaseId = System.getenv("KNOWLEDGE_BASE_ID");
    }

    // Constructor for testing
    public RagOrchestrationService(BedrockRuntimeClient bedrockClient, BedrockAgentRuntimeClient bedrockAgentClient, String knowledgeBaseId) {
        this.bedrockClient = bedrockClient;
        this.bedrockAgentClient = bedrockAgentClient;
        this.gson = new Gson();
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public QueryResponse processQuery(QueryRequest request) {
        String question = request.getQuestion();
        String conversationId = request.getConversationId();

        try {
            // Step 1: Retrieve relevant documents from Bedrock Knowledge Base using vector search
            RetrievalResult retrievalResult = retrieveFromKnowledgeBase(question);

            // Step 2: Generate response using Bedrock Claude with retrieved context
            String answer = generateResponse(question, retrievalResult.context);

            return new QueryResponse(answer, retrievalResult.sources, conversationId);

        } catch (Exception e) {
            // Fallback to a helpful error response
            System.err.println("Error processing query: " + e.getMessage());
            e.printStackTrace();

            String errorAnswer = "I apologize, but I'm having trouble processing your question right now. " +
                    "Please try again in a moment.";
            return new QueryResponse(errorAnswer, new ArrayList<>(), conversationId);
        }
    }

    /**
     * Retrieves relevant documents from Bedrock Knowledge Base using vector search.
     * Uses Titan embeddings for semantic similarity matching.
     */
    private RetrievalResult retrieveFromKnowledgeBase(String query) {
        if (knowledgeBaseId == null || knowledgeBaseId.isEmpty()) {
            return new RetrievalResult("", new ArrayList<>());
        }

        try {
            // Configure retrieval with vector search
            KnowledgeBaseVectorSearchConfiguration vectorSearchConfig =
                    KnowledgeBaseVectorSearchConfiguration.builder()
                            .numberOfResults(MAX_RETRIEVAL_RESULTS)
                            .build();

            KnowledgeBaseRetrievalConfiguration retrievalConfig =
                    KnowledgeBaseRetrievalConfiguration.builder()
                            .vectorSearchConfiguration(vectorSearchConfig)
                            .build();

            // Retrieve relevant documents using Bedrock Knowledge Base
            RetrieveRequest retrieveRequest = RetrieveRequest.builder()
                    .knowledgeBaseId(knowledgeBaseId)
                    .retrievalQuery(q -> q.text(query))
                    .retrievalConfiguration(retrievalConfig)
                    .build();

            RetrieveResponse response = bedrockAgentClient.retrieve(retrieveRequest);

            // Process retrieval results
            List<Source> sources = new ArrayList<>();
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("Relevant information from the knowledge base:\n\n");

            response.retrievalResults().forEach(result -> {
                // Extract content
                RetrievalResultContent content = result.content();
                String text = content.text();

                // Extract metadata
                RetrievalResultLocation location = result.location();
                String documentId = location.s3Location() != null ?
                        location.s3Location().uri() : "unknown";
                String documentName = extractDocumentName(documentId);

                // Calculate confidence (score is between 0 and 1)
                double confidence = result.score() != null ? result.score() : 0.5;

                // Create source
                String snippet = text.length() > 200 ?
                        text.substring(0, 200) + "..." : text;

                sources.add(new Source(
                        documentId,
                        documentName,
                        inferDocumentType(documentName),
                        confidence,
                        snippet
                ));

                // Add to context
                contextBuilder.append("--- ").append(documentName).append(" ---\n");
                contextBuilder.append(text).append("\n\n");
            });

            return new RetrievalResult(contextBuilder.toString(), sources);

        } catch (Exception e) {
            System.err.println("Error retrieving from knowledge base: " + e.getMessage());
            e.printStackTrace();
            return new RetrievalResult("", new ArrayList<>());
        }
    }

    private String extractDocumentName(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "Document";
        }
        // Extract filename from S3 URI (e.g., s3://bucket/documents/file.md -> file.md)
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < uri.length() - 1) {
            return uri.substring(lastSlash + 1);
        }
        return uri;
    }

    private String inferDocumentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.contains("resume") || lower.contains("cv")) return "Resume";
        if (lower.contains("architecture") || lower.contains("design")) return "Architecture Doc";
        if (lower.contains("case") || lower.contains("study")) return "Case Study";
        if (lower.contains("blog") || lower.contains("post")) return "Technical Blog";
        return "Document";
    }

    private String generateResponse(String question, String context) {
        String systemPrompt = "You are an AI assistant for Perry Rosenberg's personal portfolio website. " +
                "You help visitors learn about the Perry's professional experience, skills, and projects, " +
                "utilize a knowledge database of sources he is familiar with to provide answers to architectural questions. " +
                "Answer questions based on the provided context. Be helpful, professional, and concise. " +
                "If the context doesn't contain relevant information, provide a general helpful response " +
                "and suggest what topics you can help with.";
        
        String userPrompt = context.isEmpty() 
                ? question 
                : "Context:\n" + context + "\n\nQuestion: " + question;
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("anthropic_version", "bedrock-2023-05-31");
        requestBody.addProperty("max_tokens", 1024);
        
        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        messages.add(userMessage);
        
        requestBody.add("messages", messages);
        requestBody.addProperty("system", systemPrompt);
        
        try {
            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                    .modelId(LLM_MODEL_ID)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(gson.toJson(requestBody)))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(invokeRequest);
            String responseBody = response.body().asUtf8String();
            
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            JsonArray contentArray = responseJson.getAsJsonArray("content");
            if (contentArray != null && contentArray.size() > 0) {
                return contentArray.get(0).getAsJsonObject().get("text").getAsString();
            }
            
            return "I received your question but couldn't generate a proper response. Please try again.";
            
        } catch (Exception e) {
            // Return a fallback response if Bedrock call fails
            System.out.println("Exception: "  +e.getMessage());
            System.out.println(Stream.of(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
            return "I received your question but an error occurred. Please try again later.";
        }
    }

    /**
     * Helper class to encapsulate retrieval results from Knowledge Base
     */
    private static class RetrievalResult {
        final String context;
        final List<Source> sources;

        RetrievalResult(String context, List<Source> sources) {
            this.context = context;
            this.sources = sources;
        }
    }
}
