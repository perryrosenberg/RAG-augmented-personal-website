"use client";

import { useState, useCallback, useRef, useEffect } from "react";
import { Bot, Sparkles, Database, MessageSquare } from "lucide-react";
import { ChatMessage } from "@/components/assistant/chat-message";
import { ChatInput } from "@/components/assistant/chat-input";
import { SourcesPanel } from "@/components/assistant/sources-panel";
import { LoadingIndicator } from "@/components/assistant/loading-indicator";
import { queryAssistant } from "@/lib/assistant-api";
import type { AssistantMessage, AssistantSource } from "@/lib/assistant-types";

const suggestedQuestions = [
  "How would you design an ETL pipeline for billions of records?",
  "Explain how you approach event sourcing.",
  "What tradeoffs do you consider when designing distributed systems?",
];

export function AssistantSection() {
  const [messages, setMessages] = useState<AssistantMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [conversationId] = useState(() => crypto.randomUUID());
  const [currentSources, setCurrentSources] = useState<AssistantSource[]>([]);
  const scrollRef = useRef<HTMLDivElement>(null);

  // Scroll to bottom when new messages arrive
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, isLoading]);

  const handleSubmit = useCallback(
    async (content: string) => {
      // Add user message
      const userMessage: AssistantMessage = {
        id: crypto.randomUUID(),
        role: "user",
        content,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, userMessage]);
      setIsLoading(true);

      try {
        // Call RAG assistant API
        const response = await queryAssistant(content, conversationId);

        // Add assistant message
        const assistantMessage: AssistantMessage = {
          id: crypto.randomUUID(),
          role: "assistant",
          content: response.answer,
          timestamp: new Date(),
          sources: response.sources,
        };
        setMessages((prev) => [...prev, assistantMessage]);
        setCurrentSources(response.sources);
      } catch {
        // Handle error - add error message
        const errorMessage: AssistantMessage = {
          id: crypto.randomUUID(),
          role: "assistant",
          content: "I apologize, but I encountered an error processing your request. Please try again.",
          timestamp: new Date(),
        };
        setMessages((prev) => [...prev, errorMessage]);
      } finally {
        setIsLoading(false);
      }
    },
    [conversationId]
  );

  const handleSuggestedQuestion = useCallback(
    (question: string) => {
      if (!isLoading) {
        handleSubmit(question);
      }
    },
    [handleSubmit, isLoading]
  );

  return (
    <section id="assistant" className="py-24 px-6 md:px-12 lg:px-24">
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="text-center mb-12">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 border border-primary/20 mb-6">
            <Sparkles className="w-4 h-4 text-primary" />
            <span className="text-base font-medium text-primary">RAG-Powered Demo</span>
          </div>
          <h2 className="text-3xl md:text-4xl font-bold text-foreground mb-4 text-balance">
            <span className="text-primary">04.</span> AI Architect Assistant
          </h2>
          <p className="text-muted-foreground max-w-2xl mx-auto leading-relaxed text-balance">
            Ask questions about my engineering experience, architecture decisions, and system design
            philosophy. This demo showcases a RAG system that will connect to a Java/Spring Boot
            backend with Amazon Bedrock + vector search.
          </p>
        </div>

        {/* Architecture Info */}
        <div className="flex flex-wrap justify-center gap-4 mb-8">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-secondary text-base">
            <Database className="w-4 h-4 text-primary" />
            <span className="text-muted-foreground">Vector Search</span>
          </div>
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-secondary text-base">
            <Bot className="w-4 h-4 text-primary" />
            <span className="text-muted-foreground">Amazon Bedrock</span>
          </div>
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-secondary text-base">
            <MessageSquare className="w-4 h-4 text-primary" />
            <span className="text-muted-foreground">Spring Boot Backend</span>
          </div>
        </div>

        {/* Chat Interface */}
        <div className="grid lg:grid-cols-3 gap-6">
          {/* Chat Panel */}
          <div className="lg:col-span-2 bg-card border border-border rounded-xl flex flex-col h-[600px]">
            {/* Messages Area */}
            <div className="flex-1 overflow-y-auto overflow-x-hidden" ref={scrollRef}>
              <div className="p-4 space-y-4">
                {messages.length === 0 ? (
                  <div className="text-center py-12">
                    <Bot className="w-12 h-12 mx-auto mb-4 text-primary opacity-50" />
                    <h3 className="text-lg font-medium text-foreground mb-2">
                      Start a conversation
                    </h3>
                    <p className="text-base text-muted-foreground mb-6">
                      Ask me about architecture, system design, or scaling challenges.
                    </p>
                    <div className="space-y-2">
                      <p className="text-sm text-muted-foreground uppercase tracking-wide mb-3">
                        Suggested questions
                      </p>
                      {suggestedQuestions.map((question) => (
                        <button
                          key={question}
                          type="button"
                          onClick={() => handleSuggestedQuestion(question)}
                          disabled={isLoading}
                          className="block w-full text-left px-4 py-3 rounded-lg bg-secondary hover:bg-secondary/80 text-base text-foreground transition-colors disabled:opacity-50"
                        >
                          {question}
                        </button>
                      ))}
                    </div>
                  </div>
                ) : (
                  <>
                    {messages.map((message) => (
                      <ChatMessage key={message.id} message={message} />
                    ))}
                    {isLoading && <LoadingIndicator />}
                  </>
                )}
              </div>
            </div>

            {/* Input Area */}
            <div className="border-t border-border">
              <ChatInput onSubmit={handleSubmit} isLoading={isLoading} />
            </div>
          </div>

          {/* Sources Panel */}
          <div className="bg-card border border-border rounded-xl h-[600px] flex flex-col">
            <div className="flex-1 overflow-y-auto overflow-x-hidden p-4">
              <SourcesPanel sources={currentSources} />
            </div>
          </div>
        </div>

        {/* Technical Note */}
        <p className="text-center text-sm text-muted-foreground mt-6">
          Powered by AWS Lambda with Amazon Bedrock (Claude 3 Haiku) for LLM inference and S3 Vectors for semantic document retrieval.
        </p>
      </div>
    </section>
  );
}
