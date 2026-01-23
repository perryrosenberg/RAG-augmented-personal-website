"use client";

import { Bot } from "lucide-react";

export function LoadingIndicator() {
  return (
    <div className="flex gap-3 p-4 rounded-lg bg-card border border-border">
      <div className="flex-shrink-0 w-8 h-8 rounded-full bg-accent text-accent-foreground flex items-center justify-center">
        <Bot className="w-4 h-4" />
      </div>
      <div className="flex-1">
        <div className="flex items-center gap-2 mb-2">
          <span className="font-medium text-base text-foreground">AI Architect Assistant</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2 h-2 bg-primary rounded-full animate-bounce [animation-delay:-0.3s]" />
          <span className="w-2 h-2 bg-primary rounded-full animate-bounce [animation-delay:-0.15s]" />
          <span className="w-2 h-2 bg-primary rounded-full animate-bounce" />
          <span className="ml-2 text-base text-muted-foreground">Searching knowledge base...</span>
        </div>
      </div>
    </div>
  );
}
