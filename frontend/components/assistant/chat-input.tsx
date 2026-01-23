"use client";

import { useState, useRef, useCallback } from "react";
import { Send } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";

interface ChatInputProps {
  readonly onSubmit: (message: string) => void;
  readonly isLoading: boolean;
  readonly compact?: boolean;
}

export function ChatInput({ onSubmit, isLoading, compact }: ChatInputProps) {
  const [input, setInput] = useState("");
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const handleSubmit = useCallback(() => {
    const trimmed = input.trim();
    if (trimmed && !isLoading) {
      onSubmit(trimmed);
      setInput("");
      // Reset textarea height
      if (textareaRef.current) {
        textareaRef.current.style.height = "auto";
      }
    }
  }, [input, isLoading, onSubmit]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        handleSubmit();
      }
    },
    [handleSubmit]
  );

  const handleChange = useCallback((e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInput(e.target.value);
    // Auto-resize textarea
    e.target.style.height = "auto";
    e.target.style.height = `${Math.min(e.target.scrollHeight, 200)}px`;
  }, []);

  return (
    <div className={`border-t border-border bg-card ${compact ? "p-2" : "p-4"}`}>
      <div className={compact ? "flex gap-2" : "flex gap-3"}>
        <Textarea
          ref={textareaRef}
          value={input}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          placeholder={compact ? "Ask a question..." : "Ask about my architecture experience, system design decisions, or scaling challenges..."}
          disabled={isLoading}
          className={`resize-none bg-secondary border-border focus-visible:ring-primary ${compact ? "min-h-[36px] max-h-[100px] text-sm" : "min-h-[44px] max-h-[200px]"}`}
          rows={1}
          aria-label="Message input"
        />
        <Button
          onClick={handleSubmit}
          disabled={!input.trim() || isLoading}
          size="icon"
          className={compact ? "flex-shrink-0 h-9 w-9" : "flex-shrink-0 h-11 w-11"}
          aria-label="Send message"
        >
          <Send className={compact ? "w-3 h-3" : "w-4 h-4"} />
        </Button>
      </div>
      {!compact && (
        <p className="text-sm text-muted-foreground mt-2">
          Press Enter to send, Shift+Enter for new line
        </p>
      )}
    </div>
  );
}
