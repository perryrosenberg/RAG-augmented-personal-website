package com.perryrosenberg.portfolio.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.perryrosenberg.portfolio.constants.BedrockConstants;
import com.perryrosenberg.portfolio.dto.request.QueryRequest;
import com.perryrosenberg.portfolio.dto.response.QueryResponse;
import com.perryrosenberg.portfolio.dto.response.QueryResponse.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseVectorSearchConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievalResultContent;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievalResultLocation;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveResponse;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for orchestrating RAG (Retrieval-Augmented Generation) queries.
 *
 * <p>This service integrates Amazon Bedrock Knowledge Base for vector-based document retrieval
 * with Amazon Bedrock Runtime for LLM inference. It implements a two-stage RAG pipeline:
 * <ol>
 *   <li><b>Retrieval:</b> Semantic search using Titan embeddings to find relevant documents
 *       from the knowledge base that match the user's query.</li>
 *   <li><b>Generation:</b> Claude LLM generates contextually-aware responses using the
 *       retrieved documents as grounding context.</li>
 * </ol>
 *
 * <p><b>Architecture:</b>
 * <pre>
 * User Query
 *     |
 *     v
 * [Vector Embeddings]
 *     |
 *     v
 * [Knowledge Base Search] --> Retrieved Documents
 *     |
 *     v
 * [Context Building]
 *     |
 *     v
 * [Claude LLM] --> AI-Generated Response
 * </pre>
 *
 * <p><b>Error Handling:</b> All exceptions are caught and logged with fallback responses
 * to ensure graceful degradation. Errors during retrieval result in empty context, while
 * errors during generation return user-friendly error messages.
 *
 * @author Perry Rosenberg
 * @since 1.0.0
 * @see BedrockConstants
 * @see QueryRequest
 * @see QueryResponse
 */
public class RagOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(RagOrchestrationService.class);

    private final BedrockRuntimeClient bedrockClient;
    private final BedrockAgentRuntimeClient bedrockAgentClient;
    private final Gson gson;
    private final String knowledgeBaseId;

    /**
     * Default constructor initializing AWS Bedrock clients with US East 1 region.
     *
     * <p>Reads the Knowledge Base ID from the {@code KNOWLEDGE_BASE_ID} environment variable,
     * which is injected by AWS Lambda at runtime via Terraform configuration.
     *
     * <p><b>AWS Services Initialized:</b>
     * <ul>
     *   <li><b>BedrockRuntimeClient:</b> For invoking Claude LLM models</li>
     *   <li><b>BedrockAgentRuntimeClient:</b> For querying Knowledge Base with vector search</li>
     * </ul>
     */
    public RagOrchestrationService() {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .build();
        this.bedrockAgentClient = BedrockAgentRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .build();
        this.gson = new Gson();
        this.knowledgeBaseId = System.getenv("KNOWLEDGE_BASE_ID");

        logger.info("Initialized RagOrchestrationService with Knowledge Base ID: {}",
                knowledgeBaseId != null ? knowledgeBaseId : "NOT_CONFIGURED");
    }

    /**
     * Constructor for dependency injection (primarily for testing).
     *
     * <p>Allows injection of mock or custom-configured Bedrock clients for unit testing
     * and integration testing scenarios.
     *
     * @param bedrockClient the Bedrock Runtime client for LLM invocations
     * @param bedrockAgentClient the Bedrock Agent Runtime client for Knowledge Base retrieval
     * @param knowledgeBaseId the ID of the Bedrock Knowledge Base to query
     */
    public RagOrchestrationService(
            BedrockRuntimeClient bedrockClient,
            BedrockAgentRuntimeClient bedrockAgentClient,
            String knowledgeBaseId) {
        this.bedrockClient = bedrockClient;
        this.bedrockAgentClient = bedrockAgentClient;
        this.gson = new Gson();
        this.knowledgeBaseId = knowledgeBaseId;

        logger.info("Initialized RagOrchestrationService (injected clients) with Knowledge Base ID: {}",
                knowledgeBaseId);
    }

    /**
     * Processes a user query through the complete RAG pipeline.
     *
     * <p>This method orchestrates the two-stage RAG process:
     * <ol>
     *   <li>Retrieves relevant documents from the Knowledge Base using vector search</li>
     *   <li>Generates an AI response using Claude with the retrieved context</li>
     * </ol>
     *
     * <p><b>Error Resilience:</b> If any stage fails, the method returns a graceful error
     * response rather than propagating exceptions. All errors are logged for debugging.
     *
     * @param request the user's query request containing the question and conversation context
     * @return a {@link QueryResponse} containing the AI-generated answer and source documents
     */
    public QueryResponse processQuery(QueryRequest request) {
        String question = request.getQuestion();
        String conversationId = request.getConversationId();

        logger.info("Processing query for conversation {}: {}", conversationId, question);

        try {
            // Step 1: Retrieve relevant documents from Bedrock Knowledge Base using vector search
            RetrievalResult retrievalResult = retrieveFromKnowledgeBase(question);
            logger.debug("Retrieved {} sources for query", retrievalResult.sources.size());

            // Step 2: Generate response using Bedrock Claude with retrieved context
            String answer = generateResponse(question, retrievalResult.context);
            logger.info("Successfully generated response for conversation {}", conversationId);

            return new QueryResponse(answer, retrievalResult.sources, conversationId);

        } catch (Exception e) {
            // Fallback to a helpful error response
            logger.error("Error processing query for conversation {}: {}", conversationId, e.getMessage(), e);

            return new QueryResponse(
                    BedrockConstants.ERROR_PROCESSING_QUERY,
                    new ArrayList<>(),
                    conversationId
            );
        }
    }

    /**
     * Retrieves relevant documents from Bedrock Knowledge Base using vector search.
     *
     * <p>This method performs semantic similarity search using Titan embeddings to find
     * the most relevant documents from the knowledge base. The retrieval process:
     * <ol>
     *   <li>Converts the query to a vector embedding (handled by Bedrock)</li>
     *   <li>Performs similarity search against stored document embeddings</li>
     *   <li>Returns the top N most relevant documents with confidence scores</li>
     * </ol>
     *
     * <p><b>Configuration:</b>
     * <ul>
     *   <li>Maximum results: {@link BedrockConstants#MAX_RETRIEVAL_RESULTS}</li>
     *   <li>Embedding model: Amazon Titan (managed by Knowledge Base)</li>
     *   <li>Search type: Vector similarity (cosine distance)</li>
     * </ul>
     *
     * <p><b>Response Processing:</b> Retrieved documents are processed to extract:
     * <ul>
     *   <li>Document content text for LLM context</li>
     *   <li>S3 location metadata for source attribution</li>
     *   <li>Confidence scores for relevance indication</li>
     *   <li>Excerpts for user-facing source display</li>
     * </ul>
     *
     * @param query the user's natural language question
     * @return a {@link RetrievalResult} containing formatted context and source metadata
     */
    private RetrievalResult retrieveFromKnowledgeBase(String query) {
        if (knowledgeBaseId == null || knowledgeBaseId.isEmpty()) {
            logger.warn("Knowledge Base ID not configured, returning empty results");
            return new RetrievalResult("", new ArrayList<>());
        }

        try {
            // Configure retrieval with vector search
            KnowledgeBaseVectorSearchConfiguration vectorSearchConfig =
                    KnowledgeBaseVectorSearchConfiguration.builder()
                            .numberOfResults(BedrockConstants.MAX_RETRIEVAL_RESULTS)
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

            logger.debug("Executing Knowledge Base retrieval for query: {}", query);
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

                // Create source with excerpt
                String snippet = text.length() > BedrockConstants.EXCERPT_MAX_LENGTH ?
                        text.substring(0, BedrockConstants.EXCERPT_MAX_LENGTH) + "..." : text;

                sources.add(new Source(
                        documentId,
                        documentName,
                        inferDocumentType(documentName),
                        confidence,
                        snippet
                ));

                // Add to context for LLM
                contextBuilder.append("--- ").append(documentName).append(" ---\n");
                contextBuilder.append(text).append("\n\n");

                logger.debug("Retrieved document: {} (confidence: {})", documentName, confidence);
            });

            logger.info("Successfully retrieved {} documents from Knowledge Base", sources.size());
            return new RetrievalResult(contextBuilder.toString(), sources);

        } catch (Exception e) {
            logger.error("Error retrieving from Knowledge Base: {}", e.getMessage(), e);
            return new RetrievalResult("", new ArrayList<>());
        }
    }

    /**
     * Extracts the document filename from an S3 URI.
     *
     * <p>Parses S3 URIs in the format {@code s3://bucket/path/to/document.ext} and
     * returns the final filename component.
     *
     * @param uri the S3 URI of the document
     * @return the filename extracted from the URI, or the full URI if parsing fails
     */
    private String extractDocumentName(String uri) {
        if (uri == null || uri.isEmpty()) {
            return BedrockConstants.DOC_TYPE_DEFAULT;
        }
        // Extract filename from S3 URI (e.g., s3://bucket/documents/file.md -> file.md)
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < uri.length() - 1) {
            return uri.substring(lastSlash + 1);
        }
        return uri;
    }

    /**
     * Infers the document type based on filename patterns.
     *
     * <p>Uses heuristic keyword matching to classify documents into categories
     * relevant to the portfolio context. This classification helps users understand
     * the nature of source documents.
     *
     * <p><b>Classification Rules:</b>
     * <ul>
     *   <li>Contains "resume" or "cv" → Resume</li>
     *   <li>Contains "architecture" or "design" → Architecture Doc</li>
     *   <li>Contains "case" or "study" → Case Study</li>
     *   <li>Contains "blog" or "post" → Technical Blog</li>
     *   <li>Default → Document</li>
     * </ul>
     *
     * @param fileName the name of the document file
     * @return the inferred document type label
     */
    private String inferDocumentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.contains("resume") || lower.contains("cv")) {
            return BedrockConstants.DOC_TYPE_RESUME;
        }
        if (lower.contains("architecture") || lower.contains("design")) {
            return BedrockConstants.DOC_TYPE_ARCHITECTURE;
        }
        if (lower.contains("case") || lower.contains("study")) {
            return BedrockConstants.DOC_TYPE_CASE_STUDY;
        }
        if (lower.contains("blog") || lower.contains("post")) {
            return BedrockConstants.DOC_TYPE_BLOG;
        }
        return BedrockConstants.DOC_TYPE_DEFAULT;
    }

    /**
     * Generates an AI response using Claude model with retrieved context.
     *
     * <p>This method invokes the Claude Haiku model via Bedrock Runtime to generate
     * a contextually-aware response. The generation process:
     * <ol>
     *   <li>Formats the system prompt defining assistant behavior</li>
     *   <li>Constructs the user prompt with retrieved context</li>
     *   <li>Invokes Claude model with proper message structure</li>
     *   <li>Extracts and returns the generated text response</li>
     * </ol>
     *
     * <p><b>Prompt Structure:</b>
     * <ul>
     *   <li><b>System:</b> Defines assistant role and behavior guidelines</li>
     *   <li><b>User:</b> Contains retrieved context followed by the user's question</li>
     * </ul>
     *
     * <p><b>Model Configuration:</b>
     * <ul>
     *   <li>Model: {@link BedrockConstants#LLM_MODEL_ID}</li>
     *   <li>Max tokens: {@link BedrockConstants#MAX_TOKENS}</li>
     *   <li>API version: {@link BedrockConstants#ANTHROPIC_API_VERSION}</li>
     * </ul>
     *
     * <p><b>Error Handling:</b> If the Bedrock invocation fails, a fallback error
     * message is returned rather than propagating the exception.
     *
     * @param question the user's original question
     * @param context the retrieved document context for grounding the response
     * @return the AI-generated response text
     */
    private String generateResponse(String question, String context) {
        String userPrompt = context.isEmpty()
                ? question
                : "Context:\n" + context + "\n\nQuestion: " + question;

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("anthropic_version", BedrockConstants.ANTHROPIC_API_VERSION);
        requestBody.addProperty("max_tokens", BedrockConstants.MAX_TOKENS);

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);
        requestBody.addProperty("system", BedrockConstants.SYSTEM_PROMPT);

        try {
            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                    .modelId(BedrockConstants.LLM_MODEL_ID)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(gson.toJson(requestBody)))
                    .build();

            logger.debug("Invoking Claude model: {}", BedrockConstants.LLM_MODEL_ID);
            InvokeModelResponse response = bedrockClient.invokeModel(invokeRequest);
            String responseBody = response.body().asUtf8String();

            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            JsonArray contentArray = responseJson.getAsJsonArray("content");
            if (contentArray != null && contentArray.size() > 0) {
                String generatedText = contentArray.get(0).getAsJsonObject().get("text").getAsString();
                logger.info("Successfully generated response ({} characters)", generatedText.length());
                return generatedText;
            }

            logger.warn("Bedrock response missing content array, returning fallback message");
            return BedrockConstants.ERROR_GENERATING_RESPONSE;

        } catch (Exception e) {
            // Return a fallback response if Bedrock call fails
            logger.error("Bedrock invocation failed: {}", e.getMessage());
            logger.debug("Stack trace: {}",
                    Stream.of(e.getStackTrace())
                            .map(StackTraceElement::toString)
                            .collect(Collectors.joining("\n")));
            return BedrockConstants.ERROR_BEDROCK_INVOCATION;
        }
    }

    /**
     * Helper class to encapsulate retrieval results from Knowledge Base.
     *
     * <p>This internal data structure holds both the formatted context string
     * (used for LLM prompting) and the structured source metadata (used for
     * user-facing source attribution).
     */
    private static class RetrievalResult {
        final String context;
        final List<Source> sources;

        /**
         * Constructs a retrieval result.
         *
         * @param context formatted text context for LLM prompt
         * @param sources list of source document metadata
         */
        RetrievalResult(String context, List<Source> sources) {
            this.context = context;
            this.sources = sources;
        }
    }
}
