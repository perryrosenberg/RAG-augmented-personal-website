package com.perryrosenberg.portfolio.constants;

/**
 * Constants for Amazon Bedrock service integration.
 *
 * <p>This class contains configuration constants for interacting with Amazon Bedrock,
 * including model identifiers, API versions, and operational limits for the RAG-powered
 * portfolio assistant.
 *
 * <p>Model Selection Rationale:
 * <ul>
 *   <li><b>Claude Haiku 4.5:</b> Selected for LLM inference due to optimal balance of
 *       cost-efficiency, speed, and quality for conversational AI responses. Suitable
 *       for AWS Free Tier usage constraints.</li>
 *   <li><b>Titan Embeddings:</b> Used implicitly by Bedrock Knowledge Base for vector
 *       search and semantic similarity matching.</li>
 * </ul>
 *
 * @author Perry Rosenberg
 * @since 1.0.0
 */
public final class BedrockConstants {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private BedrockConstants() {
        throw new UnsupportedOperationException("BedrockConstants is a utility class and cannot be instantiated");
    }

    // ========== Model Identifiers ==========

    /**
     * Amazon Bedrock model ID for Claude Haiku 4.5 in US East region.
     *
     * <p>This model is used for generating conversational responses based on
     * retrieved context from the knowledge base. Claude Haiku offers fast
     * inference times and cost-effective token pricing while maintaining
     * high response quality.
     *
     * @see <a href="https://docs.aws.amazon.com/bedrock/latest/userguide/model-ids.html">Bedrock Model IDs</a>
     */
    public static final String LLM_MODEL_ID = "us.anthropic.claude-haiku-4-5-20251001-v1:0";

    // ========== API Configuration ==========

    /**
     * Anthropic API version for Bedrock runtime requests.
     *
     * <p>This version identifier must be included in all Claude model invocations
     * to ensure API compatibility and proper request formatting.
     */
    public static final String ANTHROPIC_API_VERSION = "bedrock-2023-05-31";

    /**
     * Maximum number of tokens allowed in Claude model responses.
     *
     * <p>This limit balances response completeness with latency and cost.
     * A value of 1024 tokens typically allows for detailed yet concise answers
     * suitable for portfolio assistant queries.
     */
    public static final int MAX_TOKENS = 1024;

    // ========== Retrieval Configuration ==========

    /**
     * Maximum number of documents to retrieve from Knowledge Base for each query.
     *
     * <p>This value controls the retrieval scope for vector search operations.
     * Retrieving 5 documents provides sufficient context for accurate responses
     * while minimizing latency and token consumption.
     */
    public static final int MAX_RETRIEVAL_RESULTS = 5;

    /**
     * Maximum length (in characters) for document excerpts in response sources.
     *
     * <p>Excerpts longer than this limit are truncated with an ellipsis.
     * This ensures response payloads remain reasonably sized while providing
     * enough context for users to understand source relevance.
     */
    public static final int EXCERPT_MAX_LENGTH = 200;

    // ========== System Prompts ==========

    /**
     * System prompt for Claude model that defines assistant behavior and persona.
     *
     * <p>This prompt instructs the model to:
     * <ul>
     *   <li>Act as Perry Rosenberg's portfolio assistant</li>
     *   <li>Provide information about professional experience, skills, and projects</li>
     *   <li>Answer architectural questions using the knowledge base</li>
     *   <li>Maintain a professional and helpful tone</li>
     *   <li>Handle queries gracefully when context is insufficient</li>
     * </ul>
     */
    public static final String SYSTEM_PROMPT =
            "You are an AI assistant for Perry Rosenberg's personal resume website. " +
            "You help visitors learn about Perry's resume experience and the documentation he's used " +
            "by utilizing a knowledge database of sources he is familiar with to provide answers to architectural questions. " +
            "Answer questions based on the provided context. Be helpful, professional, and concise. " +
            "If the context doesn't contain relevant information, provide a general helpful response " +
            "and suggest what topics you can help with. Please don't refer to: projects, portfolio or anything other than " +
            "his \"resume\" or the documentation when referring to Perry's experience, since you don't have that stuff in your database. ";

    // ========== Error Messages ==========

    /**
     * User-facing error message when query processing fails.
     */
    public static final String ERROR_PROCESSING_QUERY =
            "I apologize, but I'm having trouble processing your question right now. " +
            "Please try again in a moment.";

    /**
     * User-facing error message when LLM response generation fails.
     */
    public static final String ERROR_GENERATING_RESPONSE =
            "I received your question but couldn't generate a proper response. Please try again.";

    /**
     * User-facing error message when Bedrock invocation fails.
     */
    public static final String ERROR_BEDROCK_INVOCATION =
            "I received your question but an error occurred. Please try again later.";

    // ========== Document Type Classification ==========

    /**
     * Document type label for resume/CV files.
     */
    public static final String DOC_TYPE_RESUME = "Resume";

    /**
     * Document type label for architecture documentation.
     */
    public static final String DOC_TYPE_ARCHITECTURE = "Architecture Doc";

    /**
     * Document type label for case studies.
     */
    public static final String DOC_TYPE_CASE_STUDY = "Case Study";

    /**
     * Document type label for technical blog posts.
     */
    public static final String DOC_TYPE_BLOG = "Technical Blog";

    /**
     * Default document type label for unclassified documents.
     */
    public static final String DOC_TYPE_DEFAULT = "Document";
}
