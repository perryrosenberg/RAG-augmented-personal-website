"use client";

import React from "react"

import { useState } from "react";
import { ChevronDown, ChevronRight, FileText, FileCode, Briefcase, BookOpen, Layers } from "lucide-react";
import { cn } from "@/lib/utils";
import type { AssistantSource } from "@/lib/assistant-types";

interface SourcesPanelProps {
  sources: AssistantSource[];
  compact?: boolean;
}

const typeIcons: Record<string, React.ComponentType<{ className?: string }>> = {
  "Architecture Doc": FileCode,
  Resume: Briefcase,
  "Case Study": BookOpen,
  "Technical Blog": FileText,
  "System Design": Layers,
};

function SourceItem({ source }: { source: AssistantSource }) {
  const [isExpanded, setIsExpanded] = useState(false);
  const Icon = typeIcons[source.type] || FileText;
  const confidencePercent = Math.round(source.confidence * 100);

  return (
    <div className="border border-border rounded-lg overflow-hidden">
      <button
        type="button"
        onClick={() => setIsExpanded(!isExpanded)}
        className="w-full flex items-start gap-3 p-3 text-left hover:bg-secondary/50 transition-colors"
        aria-expanded={isExpanded}
        aria-label={`${source.title}, ${source.type}, ${confidencePercent}% relevance. Click to ${isExpanded ? "collapse" : "expand"} excerpt.`}
      >
        <div className="flex-shrink-0 mt-0.5">
          {isExpanded ? (
            <ChevronDown className="w-4 h-4 text-muted-foreground" />
          ) : (
            <ChevronRight className="w-4 h-4 text-muted-foreground" />
          )}
        </div>
        <div className="flex-shrink-0 w-8 h-8 rounded bg-secondary flex items-center justify-center">
          <Icon className="w-4 h-4 text-primary" />
        </div>
        <div className="flex-1 min-w-0">
          <div className="font-medium text-sm text-foreground truncate">{source.title}</div>
          <div className="flex items-center gap-2 mt-1">
            <span className="text-xs text-muted-foreground">{source.type}</span>
            <span className="text-xs text-muted-foreground">|</span>
            <div className="flex items-center gap-1.5">
              <div className="w-16 h-1.5 bg-secondary rounded-full overflow-hidden">
                <div
                  className={cn(
                    "h-full rounded-full transition-all",
                    confidencePercent >= 90
                      ? "bg-green-500"
                      : confidencePercent >= 80
                        ? "bg-primary"
                        : "bg-yellow-500"
                  )}
                  style={{ width: `${confidencePercent}%` }}
                />
              </div>
              <span className="text-xs font-medium text-muted-foreground">{confidencePercent}%</span>
            </div>
          </div>
        </div>
      </button>
      {isExpanded && (
        <div className="px-3 pb-3 pl-14">
          <p className="text-sm text-muted-foreground leading-relaxed">{source.excerpt}</p>
        </div>
      )}
    </div>
  );
}

export function SourcesPanel({ sources, compact }: SourcesPanelProps) {
  if (sources.length === 0) {
    return (
      <div className={`text-center text-muted-foreground ${compact ? "py-4" : "py-8"}`}>
        <FileText className={`mx-auto mb-2 opacity-50 ${compact ? "w-6 h-6" : "w-8 h-8"}`} />
        <p className={compact ? "text-xs" : "text-sm"}>No sources to display yet.</p>
        <p className={`mt-1 ${compact ? "text-[10px]" : "text-xs"}`}>Ask a question to see relevant documents.</p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {!compact && (
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-semibold text-foreground">Sources Used</h3>
          <span className="text-xs text-muted-foreground">{sources.length} documents</span>
        </div>
      )}
      {sources.map((source) => (
        <SourceItem key={source.id} source={source} />
      ))}
    </div>
  );
}
