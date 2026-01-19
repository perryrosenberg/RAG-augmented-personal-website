package com.ragwebsite.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ragwebsite.models.QueryRequest;
import com.ragwebsite.models.QueryResponse;
import com.ragwebsite.models.QueryResponse.Source;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for orchestrating RAG (Retrieval-Augmented Generation) queries.
 * Integrates with Amazon Bedrock for embeddings and LLM inference,
 * and S3 for document retrieval from the knowledge base.
 */
public class RagOrchestrationService {

    private final BedrockRuntimeClient bedrockClient;
    private final S3Client s3Client;
    private final Gson gson;
    private final String knowledgeBucket;
    
    // Bedrock model IDs
    private static final String EMBEDDING_MODEL_ID = "amazon.titan-embed-text-v1";
    private static final String LLM_MODEL_ID = "anthropic.claude-3-haiku-20240307-v1:0";
    
    // Relevance threshold for document retrieval
    private static final double RELEVANCE_THRESHOLD = 0.5;

    public RagOrchestrationService() {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .build();
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .build();
        this.gson = new Gson();
        this.knowledgeBucket = System.getenv("KNOWLEDGE_BUCKET");
    }

    // Constructor for testing
    public RagOrchestrationService(BedrockRuntimeClient bedrockClient, S3Client s3Client, String knowledgeBucket) {
        this.bedrockClient = bedrockClient;
        this.s3Client = s3Client;
        this.gson = new Gson();
        this.knowledgeBucket = knowledgeBucket;
    }

    public QueryResponse processQuery(QueryRequest request) {
        String question = request.getQuestion();
        String conversationId = request.getConversationId();
        
        try {
            // Step 1: Retrieve relevant documents from S3 knowledge base
            List<DocumentWithContent> documents = retrieveDocuments();
            
            // Step 2: Find relevant documents using simple keyword matching
            // (In production, use embeddings + vector search)
            List<Source> relevantSources = findRelevantDocuments(question, documents);
            
            // Step 3: Build context from relevant documents
            String context = buildContext(relevantSources, documents);
            
            // Step 4: Generate response using Bedrock Claude
            String answer = generateResponse(question, context);
            
            return new QueryResponse(answer, relevantSources, conversationId);
            
        } catch (Exception e) {
            // Fallback to a helpful error response
            String errorAnswer = "I apologize, but I'm having trouble processing your question right now. " +
                    "Please try again in a moment. Error: " + e.getMessage();
            return new QueryResponse(errorAnswer, new ArrayList<>(), conversationId);
        }
    }

    private List<DocumentWithContent> retrieveDocuments() {
        List<DocumentWithContent> documents = new ArrayList<>();
        
        if (knowledgeBucket == null || knowledgeBucket.isEmpty()) {
            return documents;
        }
        
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(knowledgeBucket)
                    .prefix("documents/")
                    .build();
            
            List<S3Object> objects = s3Client.listObjectsV2(listRequest).contents();
            
            for (S3Object obj : objects) {
                if (obj.key().endsWith("/")) continue; // Skip directories
                
                try {
                    GetObjectRequest getRequest = GetObjectRequest.builder()
                            .bucket(knowledgeBucket)
                            .key(obj.key())
                            .build();
                    
                    String content = new BufferedReader(
                            new InputStreamReader(s3Client.getObject(getRequest), StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    
                    String fileName = obj.key().substring(obj.key().lastIndexOf('/') + 1);
                    String docType = inferDocumentType(fileName);
                    
                    documents.add(new DocumentWithContent(
                            obj.key(),
                            fileName,
                            docType,
                            content
                    ));
                } catch (Exception e) {
                    // Skip documents that can't be read
                }
            }
        } catch (Exception e) {
            // Return empty list if bucket access fails
        }
        
        return documents;
    }

    private String inferDocumentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.contains("resume") || lower.contains("cv")) return "Resume";
        if (lower.contains("architecture") || lower.contains("design")) return "Architecture Doc";
        if (lower.contains("case") || lower.contains("study")) return "Case Study";
        if (lower.contains("blog") || lower.contains("post")) return "Technical Blog";
        return "Document";
    }

    private List<Source> findRelevantDocuments(String question, List<DocumentWithContent> documents) {
        List<Source> sources = new ArrayList<>();
        String lowerQuestion = question.toLowerCase();
        String[] keywords = lowerQuestion.split("\\s+");
        
        for (DocumentWithContent doc : documents) {
            String lowerContent = doc.content.toLowerCase();
            int matchCount = 0;
            
            for (String keyword : keywords) {
                if (keyword.length() > 3 && lowerContent.contains(keyword)) {
                    matchCount++;
                }
            }
            
            if (matchCount > 0) {
                double relevance = Math.min(0.95, 0.5 + (matchCount * 0.1));
                String snippet = extractSnippet(doc.content, keywords);
                
                sources.add(new Source(
                        doc.id,
                        doc.title,
                        doc.type,
                        relevance,
                        snippet
                ));
            }
        }
        
        // Sort by relevance and limit to top 5
        sources.sort((a, b) -> Double.compare(b.getRelevance(), a.getRelevance()));
        return sources.stream().limit(5).collect(Collectors.toList());
    }

    private String extractSnippet(String content, String[] keywords) {
        // Find the first occurrence of any keyword and extract surrounding context
        String lowerContent = content.toLowerCase();
        int bestIndex = -1;
        
        for (String keyword : keywords) {
            if (keyword.length() > 3) {
                int index = lowerContent.indexOf(keyword);
                if (index != -1 && (bestIndex == -1 || index < bestIndex)) {
                    bestIndex = index;
                }
            }
        }
        
        if (bestIndex == -1) {
            return content.substring(0, Math.min(200, content.length())) + "...";
        }
        
        int start = Math.max(0, bestIndex - 50);
        int end = Math.min(content.length(), bestIndex + 150);
        
        String snippet = content.substring(start, end);
        if (start > 0) snippet = "..." + snippet;
        if (end < content.length()) snippet = snippet + "...";
        
        return snippet;
    }

    private String buildContext(List<Source> sources, List<DocumentWithContent> documents) {
        StringBuilder context = new StringBuilder();
        context.append("Relevant information from the knowledge base:\n\n");
        
        for (Source source : sources) {
            // Find full document content
            for (DocumentWithContent doc : documents) {
                if (doc.id.equals(source.getDocumentId())) {
                    context.append("--- ").append(source.getTitle()).append(" (").append(source.getType()).append(") ---\n");
                    // Limit content to avoid token limits
                    String content = doc.content;
                    if (content.length() > 2000) {
                        content = content.substring(0, 2000) + "...";
                    }
                    context.append(content).append("\n\n");
                    break;
                }
            }
        }
        
        return context.toString();
    }

    private String generateResponse(String question, String context) {
        String systemPrompt = "You are an AI assistant for a personal portfolio website. " +
                "You help visitors learn about the site owner's professional experience, skills, and projects. " +
                "Answer questions based on the provided context. Be helpful, professional, and concise. " +
                "If the context doesn't contain relevant information, provide a general helpful response " +
                "and suggest what topics you can help with.";
        
        String userPrompt = context.isEmpty() 
                ? question 
                : "Context:\n" + context + "\n\nQuestion: " + question;
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("anthropic_version", "bedrock-2023-05-31");
        requestBody.addProperty("max_tokens", 1024);
        
        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        messages.add(userMessage);
        
        requestBody.add("messages", messages);
        requestBody.addProperty("system", systemPrompt);
        
        try {
            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                    .modelId(LLM_MODEL_ID)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(gson.toJson(requestBody)))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(invokeRequest);
            String responseBody = response.body().asUtf8String();
            
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            JsonArray contentArray = responseJson.getAsJsonArray("content");
            if (contentArray != null && contentArray.size() > 0) {
                return contentArray.get(0).getAsJsonObject().get("text").getAsString();
            }
            
            return "I received your question but couldn't generate a proper response. Please try again.";
            
        } catch (Exception e) {
            // Return a fallback response if Bedrock call fails
            return generateFallbackResponse(question);
        }
    }

    private String generateFallbackResponse(String question) {
        String lowerQuestion = question.toLowerCase();
        
        if (lowerQuestion.contains("experience") || lowerQuestion.contains("background")) {
            return "I'm a Principal Software Engineer with extensive experience in cloud-native systems, " +
                    "distributed architectures, and data engineering. Feel free to ask about specific " +
                    "technologies or projects I've worked on.";
        }
        
        if (lowerQuestion.contains("skill") || lowerQuestion.contains("technology")) {
            return "My core skills include AWS services (Lambda, ECS, DynamoDB), system design, " +
                    "event-driven architectures, and building scalable data pipelines. " +
                    "What specific area would you like to know more about?";
        }
        
        return "Thank you for your question! I'm here to help you learn about my professional " +
                "experience and technical expertise. Feel free to ask about my projects, " +
                "skills, or approach to software architecture.";
    }

    // Helper class for document storage
    private static class DocumentWithContent {
        final String id;
        final String title;
        final String type;
        final String content;

        DocumentWithContent(String id, String title, String type, String content) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.content = content;
        }
    }
}
