package com.perryrosenberg.portfolio.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.perryrosenberg.portfolio.constants.BedrockConstants;
import com.perryrosenberg.portfolio.dto.request.QueryRequest;
import com.perryrosenberg.portfolio.dto.response.QueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalResult;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievalResultContent;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievalResultLocation;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievalResultS3Location;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveResponse;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Essential test suite for RagOrchestrationService.
 * Tests critical RAG orchestration, Knowledge Base retrieval, and LLM integration.
 *
 * @author Perry Rosenberg
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RagOrchestrationService Tests")
class RagOrchestrationServiceTest {

    @Mock
    private BedrockRuntimeClient mockBedrockClient;

    @Mock
    private BedrockAgentRuntimeClient mockBedrockAgentClient;

    private RagOrchestrationService service;
    private Gson gson;

    private static final String TEST_KNOWLEDGE_BASE_ID = "test-kb-id-123";
    private static final String TEST_CONVERSATION_ID = "conv-test-123";

    @BeforeEach
    void setUp() {
        service = new RagOrchestrationService(mockBedrockClient, mockBedrockAgentClient, TEST_KNOWLEDGE_BASE_ID);
        gson = new Gson();
    }

    @Test
    @DisplayName("Valid query returns successful response with sources")
    void validQuery_returnsSuccessfulResponseWithSources() {
        QueryRequest request = new QueryRequest("What is your experience?", TEST_CONVERSATION_ID);

        mockRetrieveResponse("Experience with AWS and Java for 5 years.", "resume.md");
        mockBedrockInvokeResponse("I have 5 years of experience with AWS and Java.");

        QueryResponse response = service.processQuery(request);

        assertThat(response).isNotNull();
        assertThat(response.getAnswer()).isEqualTo("I have 5 years of experience with AWS and Java.");
        assertThat(response.getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
        assertThat(response.getSources()).hasSize(1);

        verify(mockBedrockAgentClient).retrieve(any(RetrieveRequest.class));
        verify(mockBedrockClient).invokeModel(any(InvokeModelRequest.class));
    }

    @Test
    @DisplayName("Retrieval failure returns response without sources")
    void retrievalFailure_returnsResponseWithoutSources() {
        QueryRequest request = new QueryRequest("What is your experience?", TEST_CONVERSATION_ID);

        when(mockBedrockAgentClient.retrieve(any(RetrieveRequest.class)))
                .thenThrow(new RuntimeException("Knowledge Base error"));
        mockBedrockInvokeResponse("I don't have specific information.");

        QueryResponse response = service.processQuery(request);

        assertThat(response).isNotNull();
        assertThat(response.getAnswer()).isEqualTo("I don't have specific information.");
        assertThat(response.getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
        assertThat(response.getSources()).isEmpty();
    }

    @Test
    @DisplayName("LLM invocation failure returns error message")
    void llmInvocationFailure_returnsErrorMessage() {
        QueryRequest request = new QueryRequest("What is your experience?", TEST_CONVERSATION_ID);

        mockRetrieveResponse("Some context", "resume.md");
        when(mockBedrockClient.invokeModel(any(InvokeModelRequest.class)))
                .thenThrow(new RuntimeException("Bedrock invocation error"));

        QueryResponse response = service.processQuery(request);

        assertThat(response).isNotNull();
        assertThat(response.getAnswer()).isEqualTo(BedrockConstants.ERROR_BEDROCK_INVOCATION);
        assertThat(response.getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
    }

    @Test
    @DisplayName("Document classification correctly identifies resume")
    void documentClassification_identifiesResume() {
        QueryRequest request = new QueryRequest("What is your background?", TEST_CONVERSATION_ID);
        mockRetrieveResponse("Professional experience...", "Perry_Resume.pdf");
        mockBedrockInvokeResponse("Test answer");

        QueryResponse response = service.processQuery(request);

        assertThat(response.getSources().get(0).getType()).isEqualTo(BedrockConstants.DOC_TYPE_RESUME);
    }

    @Test
    @DisplayName("S3 URI extraction correctly parses document name")
    void s3UriExtraction_parsesDocumentName() {
        QueryRequest request = new QueryRequest("Test question", TEST_CONVERSATION_ID);
        String s3Uri = "s3://my-bucket/documents/subfolder/resume.pdf";
        mockRetrieveResponseWithS3Uri("Resume content", s3Uri);
        mockBedrockInvokeResponse("Test answer");

        QueryResponse response = service.processQuery(request);

        assertThat(response.getSources().get(0).getId()).isEqualTo(s3Uri);
        assertThat(response.getSources().get(0).getTitle()).isEqualTo("resume.pdf");
    }

    @Test
    @DisplayName("Null Knowledge Base ID skips retrieval")
    void nullKnowledgeBaseId_skipsRetrieval() {
        RagOrchestrationService serviceWithNullKb = new RagOrchestrationService(
                mockBedrockClient,
                mockBedrockAgentClient,
                null
        );
        QueryRequest request = new QueryRequest("Test question", TEST_CONVERSATION_ID);
        mockBedrockInvokeResponse("Test response");

        QueryResponse response = serviceWithNullKb.processQuery(request);

        verifyNoInteractions(mockBedrockAgentClient);
        verify(mockBedrockClient).invokeModel(any(InvokeModelRequest.class));
        assertThat(response.getSources()).isEmpty();
    }

    // ========== Helper Methods ==========

    private void mockRetrieveResponse(String content, String fileName) {
        KnowledgeBaseRetrievalResult mockReference = createMockKnowledgeBaseRetrievalResult(content, fileName);
        RetrieveResponse mockResponse = RetrieveResponse.builder()
                .retrievalResults(Collections.singletonList(mockReference))
                .build();
        when(mockBedrockAgentClient.retrieve(any(RetrieveRequest.class))).thenReturn(mockResponse);
    }

    private void mockRetrieveResponseWithS3Uri(String content, String s3Uri) {
        RetrievalResultContent mockContent = RetrievalResultContent.builder()
                .text(content)
                .build();
        RetrievalResultS3Location s3Location = RetrievalResultS3Location.builder()
                .uri(s3Uri)
                .build();
        RetrievalResultLocation mockLocation = RetrievalResultLocation.builder()
                .s3Location(s3Location)
                .build();
        KnowledgeBaseRetrievalResult mockReference = KnowledgeBaseRetrievalResult.builder()
                .content(mockContent)
                .location(mockLocation)
                .build();

        RetrieveResponse mockResponse = RetrieveResponse.builder()
                .retrievalResults(Collections.singletonList(mockReference))
                .build();
        when(mockBedrockAgentClient.retrieve(any(RetrieveRequest.class))).thenReturn(mockResponse);
    }

    private KnowledgeBaseRetrievalResult createMockKnowledgeBaseRetrievalResult(String content, String fileName) {
        RetrievalResultContent mockContent = RetrievalResultContent.builder()
                .text(content)
                .build();
        RetrievalResultS3Location s3Location = RetrievalResultS3Location.builder()
                .uri("s3://test-bucket/documents/" + fileName)
                .build();
        RetrievalResultLocation mockLocation = RetrievalResultLocation.builder()
                .s3Location(s3Location)
                .build();

        return KnowledgeBaseRetrievalResult.builder()
                .content(mockContent)
                .location(mockLocation)
                .build();
    }

    private void mockBedrockInvokeResponse(String generatedText) {
        JsonObject contentItem = new JsonObject();
        contentItem.addProperty("text", generatedText);

        JsonArray contentArray = new JsonArray();
        contentArray.add(contentItem);

        JsonObject responseJson = new JsonObject();
        responseJson.add("content", contentArray);

        String responseBody = gson.toJson(responseJson);
        InvokeModelResponse mockResponse = InvokeModelResponse.builder()
                .body(SdkBytes.fromUtf8String(responseBody))
                .build();
        when(mockBedrockClient.invokeModel(any(InvokeModelRequest.class))).thenReturn(mockResponse);
    }
}
