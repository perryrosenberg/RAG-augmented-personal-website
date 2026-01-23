"use client";

import { useState, useCallback, useRef, useEffect } from "react";
import { Bot, Sparkles } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { ChatMessage } from "@/components/assistant/chat-message";
import { ChatInput } from "@/components/assistant/chat-input";
import { SourcesPanel } from "@/components/assistant/sources-panel";
import { LoadingIndicator } from "@/components/assistant/loading-indicator";
import { queryAssistant } from "@/lib/assistant-api";
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
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading, scrollToBottom]);

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
        const response = await queryAssistant(content, conversationId);
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
          <span className="text-sm font-medium text-primary uppercase tracking-wide">
            RAG Demo
          </span>
        </div>
        <h2 className="text-base font-bold text-foreground leading-tight">
          Open Source AI/RAG Architecture Assistant
        </h2>
        <p className="text-sm text-muted-foreground mt-1">
          Ask about my experience, architecture decisions, and system design. The RAG documents are books or documentation I've read or used, and should help the model answer with something reflecting my experience.
        </p>
      </div>

      {/* Toggle between Chat and Sources */}
      <div className="flex border-b border-border shrink-0">
        <button
          type="button"
          onClick={() => setShowSources(false)}
          className={`flex-1 py-2 text-sm font-medium transition-colors ${
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
          className={`flex-1 py-2 text-sm font-medium transition-colors ${
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
          <div className="flex-1 overflow-hidden">
            <ScrollArea className="h-full">
              <div className="p-3 space-y-3">
                {messages.length === 0 ? (
                  <div className="text-center py-8">
                    <Bot className="w-10 h-10 mx-auto mb-3 text-primary opacity-50" />
                    <h3 className="text-base font-medium text-foreground mb-1">
                      Start a conversation
                    </h3>
                    <p className="text-sm text-muted-foreground mb-4">
                      Try a suggested question:
                    </p>
                    <div className="space-y-2">
                      {suggestedQuestions.map((question) => (
                        <button
                          key={question}
                          type="button"
                          onClick={() => handleSuggestedQuestion(question)}
                          disabled={isLoading}
                          className="block w-full text-left px-3 py-2 rounded-lg bg-secondary hover:bg-secondary/80 text-sm text-foreground transition-colors disabled:opacity-50"
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
                    <div ref={messagesEndRef} />
                  </>
                )}
              </div>
            </ScrollArea>
          </div>

          {/* Input */}
          <ChatInput onSubmit={handleSubmit} isLoading={isLoading} compact />
        </>
      ) : (
        <div className="flex-1 overflow-hidden">
          <ScrollArea className="h-full">
            <div className="p-3">
              <SourcesPanel sources={currentSources} compact />
            </div>
          </ScrollArea>
        </div>
      )}

      {/* Footer Note */}
      <div className="p-3 border-t border-border shrink-0">
        <p className="text-[10px] text-muted-foreground text-center">
          Powered by Java Lambda + Amazon Bedrock (Claude)
        </p>
        <a
          href="https://github.com/perryrosenberg/RAG-augmented-personal-website"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center justify-center gap-1 text-[10px] text-primary hover:underline mt-1"
        >
          View Source Code
        </a>
      </div>
    </aside>
  );
}
