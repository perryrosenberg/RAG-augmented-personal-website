package com.ragwebsite.models;

import java.util.List;

/**
 * Response model for RAG queries.
 * Matches frontend AssistantQueryResponse interface.
 */
public class QueryResponse {

    private String answer;
    private List<Source> sources;
    private String conversationId;

    public QueryResponse() {
    }

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
     * Source document referenced in the response.
     * Matches frontend AssistantSource interface.
     */
    public static class Source {
        private String id;
        private String title;
        private String type;
        private double confidence;
        private String excerpt;

        public Source() {
        }

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
