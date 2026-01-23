package com.perryrosenberg.portfolio.dto.request;

/**
 * Request DTO for RAG-powered queries from the chat interface.
 *
 * <p>This class represents user queries submitted through the portfolio website's
 * AI assistant interface. It includes the user's question, conversation context,
 * and optional metadata for enhancing retrieval relevance.
 *
 * @author Perry Rosenberg
 */
public class QueryRequest {

    private String question;
    private String conversationId;
    private QueryContext context;

    /**
     * Default constructor for Jackson deserialization.
     */
    public QueryRequest() {
    }

    /**
     * Constructs a QueryRequest with question and conversation ID.
     *
     * @param question the user's question
     * @param conversationId unique identifier for the conversation session
     */
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

    /**
     * Optional context information about the user's current page/state.
     * Used to enhance retrieval relevance based on where the user is in the site.
     */
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
