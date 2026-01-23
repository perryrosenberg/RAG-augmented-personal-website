package com.perryrosenberg.portfolio.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.perryrosenberg.portfolio.dto.request.QueryRequest;
import com.perryrosenberg.portfolio.dto.response.QueryResponse;
import com.perryrosenberg.portfolio.service.RagOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Essential test suite for QueryHandler Lambda function.
 * Tests critical API Gateway integration, CORS, validation, and error handling.
 *
 * @author Perry Rosenberg
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QueryHandler Tests")
class QueryHandlerTest {

    @Mock
    private RagOrchestrationService mockRagService;

    @Mock
    private Context mockContext;

    private QueryHandler handler;
    private Gson gson;

    @BeforeEach
    void setUp() {
        handler = new QueryHandler(mockRagService);
        gson = new Gson();
    }

    @Test
    @DisplayName("OPTIONS request returns CORS headers")
    void optionsRequest_returnsCorsHeaders() {
        APIGatewayProxyRequestEvent request = createRequest("OPTIONS", "/api/query", null);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders())
                .containsEntry("Access-Control-Allow-Origin", "*")
                .containsEntry("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        verifyNoInteractions(mockRagService);
    }

    @Test
    @DisplayName("Health check returns healthy status")
    void healthCheck_returnsHealthyStatus() {
        APIGatewayProxyRequestEvent request = createRequest("GET", "/api/health", null);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("{\"status\":\"healthy\"}");
        verifyNoInteractions(mockRagService);
    }

    @Test
    @DisplayName("Valid query returns success response")
    void validQuery_returnsSuccessResponse() {
        QueryRequest queryRequest = new QueryRequest("What is your experience?", "conv-123");
        String requestBody = gson.toJson(queryRequest);
        APIGatewayProxyRequestEvent request = createRequest("POST", "/api/query", requestBody);

        QueryResponse mockResponse = new QueryResponse(
                "I have 5 years of experience in cloud architecture.",
                new ArrayList<>(),
                "conv-123"
        );
        when(mockRagService.processQuery(any(QueryRequest.class))).thenReturn(mockResponse);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");

        QueryResponse actualResponse = gson.fromJson(response.getBody(), QueryResponse.class);
        assertThat(actualResponse.getAnswer()).isEqualTo("I have 5 years of experience in cloud architecture.");
        assertThat(actualResponse.getConversationId()).isEqualTo("conv-123");

        verify(mockRagService).processQuery(any(QueryRequest.class));
    }

    @Test
    @DisplayName("Null request body returns 400 Bad Request")
    void nullBody_returns400() {
        APIGatewayProxyRequestEvent request = createRequest("POST", "/api/query", null);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).contains("Request body is required");
        verifyNoInteractions(mockRagService);
    }

    @Test
    @DisplayName("Invalid JSON returns 500 Internal Server Error")
    void invalidJson_returns500() {
        APIGatewayProxyRequestEvent request = createRequest("POST", "/api/query", "{invalid json");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getBody()).contains("error");
        verifyNoInteractions(mockRagService);
    }

    @Test
    @DisplayName("Service exception returns 500 Internal Server Error")
    void serviceException_returns500() {
        QueryRequest queryRequest = new QueryRequest("test question", "conv-123");
        String requestBody = gson.toJson(queryRequest);
        APIGatewayProxyRequestEvent request = createRequest("POST", "/api/query", requestBody);

        when(mockRagService.processQuery(any(QueryRequest.class)))
                .thenThrow(new RuntimeException("Bedrock service unavailable"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getBody()).contains("error");
        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Origin");
    }

    @Test
    @DisplayName("Unknown path returns 404 Not Found")
    void unknownPath_returns404() {
        APIGatewayProxyRequestEvent request = createRequest("GET", "/api/unknown", null);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getBody()).contains("Not found");
        verifyNoInteractions(mockRagService);
    }

    @Test
    @DisplayName("End-to-end query flow with sources completes successfully")
    void endToEndQueryFlow_completesSuccessfully() {
        QueryRequest queryRequest = new QueryRequest("Tell me about your AWS experience", "conv-456");
        String requestBody = gson.toJson(queryRequest);
        APIGatewayProxyRequestEvent request = createRequest("POST", "/api/query", requestBody);

        List<QueryResponse.Source> sources = List.of(
                new QueryResponse.Source("doc-1", "Resume", "resume", 0.92, "AWS experience excerpt")
        );
        QueryResponse mockResponse = new QueryResponse(
                "I have extensive AWS experience including Lambda, S3, and DynamoDB.",
                sources,
                "conv-456"
        );
        when(mockRagService.processQuery(any(QueryRequest.class))).thenReturn(mockResponse);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertThat(response.getStatusCode()).isEqualTo(200);
        QueryResponse actualResponse = gson.fromJson(response.getBody(), QueryResponse.class);
        assertThat(actualResponse.getAnswer()).contains("AWS experience");
        assertThat(actualResponse.getConversationId()).isEqualTo("conv-456");
        assertThat(actualResponse.getSources()).hasSize(1);
        assertThat(actualResponse.getSources().get(0).getConfidence()).isEqualTo(0.92);
    }

    private APIGatewayProxyRequestEvent createRequest(String method, String path, String body) {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod(method);
        request.setPath(path);
        request.setBody(body);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        request.setHeaders(headers);

        return request;
    }
}
