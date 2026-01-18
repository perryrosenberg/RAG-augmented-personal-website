"use client";

import { useState, useCallback, useRef, useEffect } from "react";
import { Bot, Sparkles, ExternalLink } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { ChatMessage } from "@/components/assistant/chat-message";
import { ChatInput } from "@/components/assistant/chat-input";
import { SourcesPanel } from "@/components/assistant/sources-panel";
import { LoadingIndicator } from "@/components/assistant/loading-indicator";
import { mockAssistantQuery } from "@/lib/assistant-mock";
import type { AssistantMessage, AssistantSource } from "@/lib/assistant-types";

const suggestedQuestions = [
  "How would you design an ETL pipeline?",
  "Explain event sourcing tradeoffs.",
  "Distributed systems best practices?",
];

export function StickyAssistant() {
  const [messages, setMessages] = useState<AssistantMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [conversationId] = useState(() => crypto.randomUUID());
  const [currentSources, setCurrentSources] = useState<AssistantSource[]>([]);
  const [showSources, setShowSources] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, isLoading]);

  const handleSubmit = useCallback(
    async (content: string) => {
      const userMessage: AssistantMessage = {
        id: crypto.randomUUID(),
        role: "user",
        content,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, userMessage]);
      setIsLoading(true);

      try {
        const response = await mockAssistantQuery(content, conversationId);
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
        const errorMessage: AssistantMessage = {
          id: crypto.randomUUID(),
          role: "assistant",
          content: "I apologize, but I encountered an error. Please try again.",
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
    <aside
      id="assistant"
      className="hidden lg:flex flex-col w-full h-screen bg-card border-l border-border sticky top-0"
    >
      {/* Header */}
      <div className="p-4 border-b border-border shrink-0">
        <div className="flex items-center gap-2 mb-2">
          <Sparkles className="w-4 h-4 text-primary" />
          <span className="text-xs font-medium text-primary uppercase tracking-wide">
            RAG Demo
          </span>
        </div>
        <h2 className="text-sm font-bold text-foreground leading-tight">
          Open Source AI/RAG Architecture Assistant
        </h2>
        <p className="text-xs text-muted-foreground mt-1">
          Ask about my experience, architecture decisions, and system design.
        </p>
      </div>

      {/* Toggle between Chat and Sources */}
      <div className="flex border-b border-border shrink-0">
        <button
          type="button"
          onClick={() => setShowSources(false)}
          className={`flex-1 py-2 text-xs font-medium transition-colors ${
            !showSources
              ? "text-primary border-b-2 border-primary"
              : "text-muted-foreground hover:text-foreground"
          }`}
        >
          Chat
        </button>
        <button
          type="button"
          onClick={() => setShowSources(true)}
          className={`flex-1 py-2 text-xs font-medium transition-colors ${
            showSources
              ? "text-primary border-b-2 border-primary"
              : "text-muted-foreground hover:text-foreground"
          }`}
        >
          Sources ({currentSources.length})
        </button>
      </div>

      {/* Content Area */}
      {!showSources ? (
        <>
          {/* Messages */}
          <ScrollArea className="flex-1" ref={scrollRef}>
            <div className="p-3 space-y-3">
              {messages.length === 0 ? (
                <div className="text-center py-8">
                  <Bot className="w-10 h-10 mx-auto mb-3 text-primary opacity-50" />
                  <h3 className="text-sm font-medium text-foreground mb-1">
                    Start a conversation
                  </h3>
                  <p className="text-xs text-muted-foreground mb-4">
                    Try a suggested question:
                  </p>
                  <div className="space-y-2">
                    {suggestedQuestions.map((question) => (
                      <button
                        key={question}
                        type="button"
                        onClick={() => handleSuggestedQuestion(question)}
                        disabled={isLoading}
                        className="block w-full text-left px-3 py-2 rounded-lg bg-secondary hover:bg-secondary/80 text-xs text-foreground transition-colors disabled:opacity-50"
                      >
                        {question}
                      </button>
                    ))}
                  </div>
                </div>
              ) : (
                <>
                  {messages.map((message) => (
                    <ChatMessage key={message.id} message={message} compact />
                  ))}
                  {isLoading && <LoadingIndicator />}
                </>
              )}
            </div>
          </ScrollArea>

          {/* Input */}
          <ChatInput onSubmit={handleSubmit} isLoading={isLoading} compact />
        </>
      ) : (
        <ScrollArea className="flex-1">
          <div className="p-3">
            <SourcesPanel sources={currentSources} compact />
          </div>
        </ScrollArea>
      )}

      {/* Footer Note */}
      <div className="p-3 border-t border-border shrink-0">
        <p className="text-[10px] text-muted-foreground text-center">
          Mock responses. Production: Java/Spring Boot + Amazon Bedrock
        </p>
        <a
          href="https://github.com/perryrosenberg"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center justify-center gap-1 text-[10px] text-primary hover:underline mt-1"
        >
          <ExternalLink className="w-3 h-3" />
          View Source Code
        </a>
      </div>
    </aside>
  );
}
