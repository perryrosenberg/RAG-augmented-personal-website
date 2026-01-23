package com.perryrosenberg.portfolio.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perryrosenberg.portfolio.dto.request.QueryRequest;
import com.perryrosenberg.portfolio.dto.response.QueryResponse;
import com.perryrosenberg.portfolio.service.RagOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * AWS Lambda handler for RAG-powered query requests.
 *
 * <p>This handler serves as the entry point for the portfolio website's AI assistant,
 * processing HTTP requests via API Gateway and orchestrating RAG-based responses.
 * It implements the {@link RequestHandler} interface for Lambda integration.
 *
 * <p><b>Supported Endpoints:</b>
 * <ul>
 *   <li><b>OPTIONS /*:</b> CORS preflight requests (returns 200 with CORS headers)</li>
 *   <li><b>GET /api/health:</b> Health check endpoint (returns {"status":"healthy"})</li>
 *   <li><b>POST /api/query:</b> RAG query endpoint (processes user questions)</li>
 * </ul>
 *
 * <p><b>Request/Response Flow:</b>
 * <pre>
 * API Gateway → QueryHandler → RagOrchestrationService → Bedrock
 *                  ↓
 *            JSON Response
 * </pre>
 *
 * <p><b>CORS Configuration:</b> All responses include CORS headers to allow
 * frontend access from any origin. In production, this should be restricted
 * to specific domains.
 *
 * <p><b>Error Handling:</b> All exceptions are caught and converted to
 * HTTP 500 responses with user-friendly error messages. Detailed error
 * information is logged to CloudWatch for debugging.
 *
 * @author Perry Rosenberg
 * @since 1.0.0
 * @see RagOrchestrationService
 * @see QueryRequest
 * @see QueryResponse
 */
public class QueryHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(QueryHandler.class);

    private final RagOrchestrationService ragService;
    private final Gson gson;

    /**
     * Default constructor initializing the RAG service and JSON serializer.
     *
     * <p>This constructor is used by AWS Lambda when instantiating the handler.
     * The handler instance is reused across invocations (Lambda container reuse),
     * so the RAG service and Gson instances are created once and cached.
     */
    public QueryHandler() {
        this.ragService = new RagOrchestrationService();
        this.gson = new GsonBuilder().create();
        logger.info("QueryHandler initialized");
    }

    /**
     * Constructor for dependency injection (primarily for testing).
     *
     * <p>Allows injection of a mock or custom-configured RAG service for
     * unit testing and integration testing scenarios.
     *
     * @param ragService the RAG orchestration service to use for query processing
     */
    public QueryHandler(RagOrchestrationService ragService) {
        this.ragService = ragService;
        this.gson = new GsonBuilder().create();
        logger.info("QueryHandler initialized with injected RagOrchestrationService");
    }

    /**
     * Handles incoming API Gateway requests and routes them to appropriate handlers.
     *
     * <p>This method is invoked by AWS Lambda for each API Gateway request. It performs
     * request routing based on HTTP method and path, handling CORS preflight, health
     * checks, and query processing.
     *
     * <p><b>Routing Logic:</b>
     * <ol>
     *   <li>OPTIONS requests → CORS preflight (200 with headers)</li>
     *   <li>GET /api/health → Health check (200 with status JSON)</li>
     *   <li>POST /api/query → Query processing (delegates to {@link #handleQueryRequest})</li>
     *   <li>All other requests → 404 Not Found</li>
     * </ol>
     *
     * @param request the API Gateway proxy request event containing HTTP details
     * @param context the Lambda execution context providing runtime information
     * @return an API Gateway proxy response event with status code, headers, and body
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        logger.info("Received request: {} {}", request.getHttpMethod(), request.getPath());

        Map<String, String> headers = createCorsHeaders();

        // Handle CORS preflight
        if ("OPTIONS".equals(request.getHttpMethod())) {
            logger.debug("Handling CORS preflight request");
            return createResponse(200, "", headers);
        }

        // Handle health check endpoint
        if ("GET".equals(request.getHttpMethod()) && request.getPath().endsWith("/health")) {
            logger.debug("Handling health check request");
            return createResponse(200, "{\"status\":\"healthy\"}", headers);
        }

        // Handle query endpoint
        if ("POST".equals(request.getHttpMethod()) && request.getPath().endsWith("/query")) {
            return handleQueryRequest(request, context, headers);
        }

        logger.warn("Route not found: {} {}", request.getHttpMethod(), request.getPath());
        return createResponse(404, "{\"error\":\"Not found\"}", headers);
    }

    /**
     * Processes RAG query requests through validation, orchestration, and response formatting.
     *
     * <p>This method implements the core query processing pipeline:
     * <ol>
     *   <li><b>Request Validation:</b> Ensures body and question field are present</li>
     *   <li><b>Deserialization:</b> Converts JSON to {@link QueryRequest} DTO</li>
     *   <li><b>RAG Processing:</b> Delegates to {@link RagOrchestrationService}</li>
     *   <li><b>Serialization:</b> Converts {@link QueryResponse} to JSON</li>
     *   <li><b>Response Creation:</b> Returns HTTP 200 with CORS headers</li>
     * </ol>
     *
     * <p><b>Validation Rules:</b>
     * <ul>
     *   <li>Request body must not be null or empty → 400 Bad Request</li>
     *   <li>Question field must not be null or empty → 400 Bad Request</li>
     * </ul>
     *
     * <p><b>Error Handling:</b> Any exception during processing results in HTTP 500
     * with a JSON error message. All errors are logged to CloudWatch.
     *
     * @param request the API Gateway proxy request containing the query JSON
     * @param context the Lambda execution context for logging
     * @param headers the CORS headers to include in the response
     * @return an API Gateway response with the query result or error message
     */
    private APIGatewayProxyResponseEvent handleQueryRequest(
            APIGatewayProxyRequestEvent request,
            Context context,
            Map<String, String> headers) {
        try {
            String body = request.getBody();
            if (body == null || body.isEmpty()) {
                logger.warn("Received query request with empty body");
                return createResponse(400, "{\"error\":\"Request body is required\"}", headers);
            }

            logger.debug("Processing query request body: {}", body);

            QueryRequest queryRequest = gson.fromJson(body, QueryRequest.class);

            if (queryRequest.getQuestion() == null || queryRequest.getQuestion().isEmpty()) {
                logger.warn("Received query request with missing question field");
                return createResponse(400, "{\"error\":\"Question is required\"}", headers);
            }

            logger.info("Processing query for conversation ID: {}", queryRequest.getConversationId());

            QueryResponse response = ragService.processQuery(queryRequest);
            String responseJson = gson.toJson(response);

            logger.info("Successfully processed query for conversation ID: {}", response.getConversationId());

            return createResponse(200, responseJson, headers);

        } catch (Exception e) {
            logger.error("Error processing query request: {}", e.getMessage(), e);
            return createResponse(
                    500,
                    "{\"error\":\"Internal server error: " + e.getMessage() + "\"}",
                    headers
            );
        }
    }

    /**
     * Creates CORS headers for API Gateway responses.
     *
     * <p>Configures Cross-Origin Resource Sharing (CORS) to allow frontend access
     * from any origin. The headers enable preflight requests and specify allowed
     * methods and headers.
     *
     * <p><b>Security Note:</b> The current configuration uses a wildcard origin (*),
     * which allows access from any domain. In production, this should be restricted
     * to specific trusted origins using the {@code CORS_ALLOWED_ORIGINS} environment
     * variable.
     *
     * <p><b>Headers Configured:</b>
     * <ul>
     *   <li><b>Content-Type:</b> application/json</li>
     *   <li><b>Access-Control-Allow-Origin:</b> * (should be restricted in production)</li>
     *   <li><b>Access-Control-Allow-Methods:</b> GET, POST, OPTIONS</li>
     *   <li><b>Access-Control-Allow-Headers:</b> Content-Type, Authorization</li>
     * </ul>
     *
     * @return a map of HTTP header names to values including CORS configuration
     */
    private Map<String, String> createCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*"); // TODO: Restrict in production
        headers.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
        return headers;
    }

    /**
     * Creates an API Gateway proxy response event with specified parameters.
     *
     * <p>This utility method constructs the Lambda response object that API Gateway
     * will translate into an HTTP response sent to the client.
     *
     * @param statusCode the HTTP status code (e.g., 200, 400, 500)
     * @param body the response body (typically JSON string)
     * @param headers the HTTP headers including CORS configuration
     * @return a configured API Gateway proxy response event
     */
    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body, Map<String, String> headers) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body);
    }
}
