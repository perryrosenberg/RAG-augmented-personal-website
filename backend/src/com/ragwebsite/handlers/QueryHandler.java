package com.ragwebsite.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ragwebsite.models.QueryRequest;
import com.ragwebsite.models.QueryResponse;
import com.ragwebsite.services.RagOrchestrationService;

import java.util.HashMap;
import java.util.Map;

/**
 * Lambda handler for RAG query requests.
 * Receives user queries via API Gateway and returns AI-generated responses
 * augmented with relevant context from the knowledge base.
 */
public class QueryHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final RagOrchestrationService ragService;
    private final Gson gson;

    public QueryHandler() {
        this.ragService = new RagOrchestrationService();
        this.gson = new GsonBuilder().create();
    }

    public QueryHandler(RagOrchestrationService ragService) {
        this.ragService = ragService;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        context.getLogger().log("Received request: " + request.getHttpMethod() + " " + request.getPath());

        Map<String, String> headers = createCorsHeaders();

        // Handle CORS preflight
        if ("OPTIONS".equals(request.getHttpMethod())) {
            return createResponse(200, "", headers);
        }

        // Handle health check endpoint
        if ("GET".equals(request.getHttpMethod()) && request.getPath().endsWith("/health")) {
            return createResponse(200, "{\"status\":\"healthy\"}", headers);
        }

        // Handle query endpoint
        if ("POST".equals(request.getHttpMethod()) && request.getPath().endsWith("/query")) {
            return handleQueryRequest(request, context, headers);
        }

        return createResponse(404, "{\"error\":\"Not found\"}", headers);
    }

    private APIGatewayProxyResponseEvent handleQueryRequest(
            APIGatewayProxyRequestEvent request,
            Context context,
            Map<String, String> headers) {
        try {
            String body = request.getBody();
            if (body == null || body.isEmpty()) {
                return createResponse(400, "{\"error\":\"Request body is required\"}", headers);
            }

            context.getLogger().log("Processing query: " + body);

            QueryRequest queryRequest = gson.fromJson(body, QueryRequest.class);
            
            if (queryRequest.getQuestion() == null || queryRequest.getQuestion().isEmpty()) {
                return createResponse(400, "{\"error\":\"Question is required\"}", headers);
            }

            QueryResponse response = ragService.processQuery(queryRequest);
            String responseJson = gson.toJson(response);

            context.getLogger().log("Returning response for conversationId: " + response.getConversationId());

            return createResponse(200, responseJson, headers);

        } catch (Exception e) {
            context.getLogger().log("Error processing query: " + e.getMessage());
            return createResponse(500, "{\"error\":\"Internal server error: " + e.getMessage() + "\"}", headers);
        }
    }

    private Map<String, String> createCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
        return headers;
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body, Map<String, String> headers) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body);
    }
}
