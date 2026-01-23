"use client";

import { memo } from "react";
import ReactMarkdown from "react-markdown";
import { User, Bot } from "lucide-react";
import { cn } from "@/lib/utils";
import type { AssistantMessage } from "@/lib/assistant-types";

interface ChatMessageProps {
  readonly message: AssistantMessage;
  readonly compact?: boolean;
}

function formatTime(date: Date): string {
  return date.toLocaleTimeString("en-US", {
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  });
}

export const ChatMessage = memo(function ChatMessage({ message, compact }: ChatMessageProps) {
  const isUser = message.role === "user";

  return (
    <div
      className={cn(
        "flex gap-2 rounded-lg",
        compact ? "p-2" : "p-4 gap-3",
        isUser ? "bg-secondary/50" : "bg-card border border-border"
      )}
    >
      <div
        className={cn(
          "flex-shrink-0 rounded-full flex items-center justify-center",
          compact ? "w-6 h-6" : "w-8 h-8",
          isUser ? "bg-primary text-primary-foreground" : "bg-accent text-accent-foreground"
        )}
      >
        {isUser ? <User className={cn(compact ? "w-3 h-3" : "w-4 h-4")} /> : <Bot className={cn(compact ? "w-3 h-3" : "w-4 h-4")} />}
      </div>
      <div className="flex-1 min-w-0">
        <div className={cn("flex items-center gap-2", compact ? "mb-0.5" : "mb-1")}>
          <span className={cn("font-medium text-foreground", compact ? "text-sm" : "text-base")}>
            {isUser ? "You" : "AI Assistant"}
          </span>
          <span className={cn("text-muted-foreground", compact ? "text-[10px]" : "text-sm")}>{formatTime(message.timestamp)}</span>
        </div>
        <div
          className={cn(
            "prose prose-sm max-w-none",
            "prose-invert",
            "prose-p:text-foreground prose-p:leading-relaxed prose-p:my-2",
            "prose-headings:text-foreground prose-headings:font-semibold prose-headings:mt-4 prose-headings:mb-2",
            "prose-h2:text-lg prose-h3:text-base",
            "prose-strong:text-foreground prose-strong:font-semibold",
            "prose-code:text-primary prose-code:bg-secondary prose-code:px-1.5 prose-code:py-0.5 prose-code:rounded prose-code:text-base prose-code:before:content-none prose-code:after:content-none",
            "prose-pre:bg-secondary prose-pre:border prose-pre:border-border prose-pre:rounded-lg prose-pre:my-3",
            "prose-ul:my-2 prose-ul:list-disc prose-ul:pl-4",
            "prose-ol:my-2 prose-ol:list-decimal prose-ol:pl-4",
            "prose-li:text-foreground prose-li:my-0.5",
            "prose-table:my-3 prose-table:text-base",
            "prose-th:bg-secondary prose-th:px-3 prose-th:py-2 prose-th:text-left prose-th:font-medium prose-th:border prose-th:border-border",
            "prose-td:px-3 prose-td:py-2 prose-td:border prose-td:border-border"
          )}
        >
          {isUser ? (
            <p className="text-foreground">{message.content}</p>
          ) : (
            <ReactMarkdown>{message.content}</ReactMarkdown>
          )}
        </div>
      </div>
    </div>
  );
});
