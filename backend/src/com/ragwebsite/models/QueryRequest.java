package com.ragwebsite.models;

/**
 * Request model for RAG queries.
 * Matches frontend AssistantQueryRequest interface.
 */
public class QueryRequest {

    private String question;
    private String conversationId;
    private QueryContext context;

    public QueryRequest() {
    }

    public QueryRequest(String question, String conversationId) {
        this.question = question;
        this.conversationId = conversationId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public QueryContext getContext() {
        return context;
    }

    public void setContext(QueryContext context) {
        this.context = context;
    }

    public static class QueryContext {
        private String page;

        public QueryContext() {
        }

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }
    }
}
