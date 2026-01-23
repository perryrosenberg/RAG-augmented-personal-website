package com.perryrosenberg.portfolio.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perryrosenberg.portfolio.dto.request.QueryRequest;
import com.perryrosenberg.portfolio.dto.response.QueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Essential test suite for DTO JSON serialization and deserialization.
 * Tests critical JSON handling for API Gateway integration.
 *
 * @author Perry Rosenberg
 */
@DisplayName("DTO Serialization Tests")
class DtoSerializationTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder().create();
    }

    @Test
    @DisplayName("QueryRequest round-trip preserves all data")
    void queryRequestRoundTrip_preservesAllData() {
        QueryRequest original = new QueryRequest("What are your skills?", "conv-round-trip");
        QueryRequest.QueryContext context = new QueryRequest.QueryContext();
        context.setPage("skills");
        original.setContext(context);

        String json = gson.toJson(original);
        QueryRequest deserialized = gson.fromJson(json, QueryRequest.class);

        assertThat(deserialized.getQuestion()).isEqualTo(original.getQuestion());
        assertThat(deserialized.getConversationId()).isEqualTo(original.getConversationId());
        assertThat(deserialized.getContext().getPage()).isEqualTo(original.getContext().getPage());
    }

    @Test
    @DisplayName("QueryResponse round-trip preserves all data including sources")
    void queryResponseRoundTrip_preservesAllData() {
        QueryResponse.Source source1 = new QueryResponse.Source(
                "doc-id-1", "Doc 1", "Resume", 0.92, "Excerpt 1"
        );
        QueryResponse.Source source2 = new QueryResponse.Source(
                "doc-id-2", "Doc 2", "Blog", 0.88, "Excerpt 2"
        );
        List<QueryResponse.Source> sources = List.of(source1, source2);

        QueryResponse original = new QueryResponse(
                "Detailed answer based on multiple sources.",
                sources,
                "conv-round-trip"
        );

        String json = gson.toJson(original);
        QueryResponse deserialized = gson.fromJson(json, QueryResponse.class);

        assertThat(deserialized.getAnswer()).isEqualTo(original.getAnswer());
        assertThat(deserialized.getConversationId()).isEqualTo(original.getConversationId());
        assertThat(deserialized.getSources()).hasSize(2);
        assertThat(deserialized.getSources().get(0).getId()).isEqualTo("doc-id-1");
        assertThat(deserialized.getSources().get(1).getId()).isEqualTo("doc-id-2");
    }

    @Test
    @DisplayName("QueryResponse with sources serializes all source fields")
    void queryResponseWithSources_serializesAllFields() {
        QueryResponse.Source source = new QueryResponse.Source(
                "s3://bucket/resume.pdf",
                "resume.pdf",
                "Resume",
                0.95,
                "Experience with AWS and Java..."
        );
        List<QueryResponse.Source> sources = new ArrayList<>();
        sources.add(source);

        QueryResponse response = new QueryResponse(
                "Based on my resume...",
                sources,
                "conv-789"
        );

        String json = gson.toJson(response);

        assertThat(json).contains("\"id\":\"s3://bucket/resume.pdf\"");
        assertThat(json).contains("\"title\":\"resume.pdf\"");
        assertThat(json).contains("\"type\":\"Resume\"");
        assertThat(json).contains("\"confidence\":0.95");
        assertThat(json).contains("\"excerpt\":\"Experience with AWS and Java...\"");
    }

    @Test
    @DisplayName("Source nested class round-trip preserves all fields")
    void sourceRoundTrip_preservesAllFields() {
        QueryResponse.Source original = new QueryResponse.Source(
                "s3://bucket/path/file.md",
                "file.md",
                "Architecture Doc",
                0.99,
                "This is the excerpt text with details."
        );

        String json = gson.toJson(original);
        QueryResponse.Source deserialized = gson.fromJson(json, QueryResponse.Source.class);

        assertThat(deserialized.getId()).isEqualTo(original.getId());
        assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
        assertThat(deserialized.getType()).isEqualTo(original.getType());
        assertThat(deserialized.getConfidence()).isEqualTo(original.getConfidence());
        assertThat(deserialized.getExcerpt()).isEqualTo(original.getExcerpt());
    }

    @Test
    @DisplayName("Deserialization handles null and missing fields gracefully")
    void deserialization_handlesNullAndMissingFields() {
        String jsonWithNulls = "{\"question\":\"Test\",\"conversationId\":null,\"context\":null}";
        QueryRequest requestWithNulls = gson.fromJson(jsonWithNulls, QueryRequest.class);

        assertThat(requestWithNulls).isNotNull();
        assertThat(requestWithNulls.getQuestion()).isEqualTo("Test");
        assertThat(requestWithNulls.getConversationId()).isNull();
        assertThat(requestWithNulls.getContext()).isNull();

        String jsonMissingFields = "{\"question\":\"Test\"}";
        QueryRequest requestMissingFields = gson.fromJson(jsonMissingFields, QueryRequest.class);

        assertThat(requestMissingFields).isNotNull();
        assertThat(requestMissingFields.getQuestion()).isEqualTo("Test");
        assertThat(requestMissingFields.getConversationId()).isNull();
    }
}
