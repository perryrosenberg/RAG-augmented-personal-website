package com.perryrosenberg.portfolio.dto.response;

import java.util.List;

/**
 * Response DTO for RAG-powered queries.
 *
 * <p>Contains the AI-generated answer along with source documents that were
 * retrieved from the knowledge base and used to generate the response.
 * Each source includes confidence scoring and relevant excerpts.
 *
 * @author Perry Rosenberg
 */
public class QueryResponse {

    private String answer;
    private List<Source> sources;
    private String conversationId;

    /**
     * Default constructor for Jackson serialization.
     */
    public QueryResponse() {
    }

    /**
     * Constructs a QueryResponse with answer, sources, and conversation ID.
     *
     * @param answer the AI-generated response
     * @param sources list of source documents used for context
     * @param conversationId unique identifier for the conversation session
     */
    public QueryResponse(String answer, List<Source> sources, String conversationId) {
        this.answer = answer;
        this.sources = sources;
        this.conversationId = conversationId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Represents a source document retrieved from the knowledge base.
     *
     * <p>Includes metadata about the source and a confidence score indicating
     * how relevant the document is to the user's query. The excerpt provides
     * a preview of the relevant content from the source.
     */
    public static class Source {
        private String id;
        private String title;
        private String type;
        private double confidence;
        private String excerpt;

        public Source() {
        }

        /**
         * Constructs a Source with all metadata.
         *
         * @param id unique identifier (typically S3 URI)
         * @param title human-readable document name
         * @param type document type (e.g., "Resume", "Case Study")
         * @param confidence relevance score (0.0 to 1.0)
         * @param excerpt relevant snippet from the document
         */
        public Source(String id, String title, String type, double confidence, String excerpt) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.confidence = confidence;
            this.excerpt = excerpt;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public String getExcerpt() {
            return excerpt;
        }

        public void setExcerpt(String excerpt) {
            this.excerpt = excerpt;
        }
    }
}
