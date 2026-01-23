"use client";

import { Github, Linkedin, Sparkles } from "lucide-react";
import Link from "next/link";

export function HeroCompact() {
  return (
    <section className="pb-8 px-6" style={{ paddingTop: '2rem' }}>
      {/* AI Built Badge */}
      <div className="inline-flex items-center gap-2 px-3 py-1.5 mb-6 bg-primary/10 border border-primary/30 rounded-full">
        <Sparkles className="w-3 h-3 text-primary" />
        <span className="text-primary font-mono text-sm">
          This website was built using agentic AI workflows to test and showcase the tools (v0.dev, junie, claude code) and is Open Source for transparency
        </span>
      </div>

      <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
        <div>
          <p className="text-primary font-mono text-sm mb-1">Hi, my name is</p>
          <h1 className="text-3xl md:text-4xl font-bold text-foreground mb-1">
            Perry Rosenberg
          </h1>
          <h2 className="text-xl md:text-2xl font-bold text-muted-foreground">
            I architect systems that scale.
          </h2>
        </div>

        <div className="flex flex-wrap gap-2">
          <Link
            href="https://www.linkedin.com/in/perry-rosenberg/"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 px-3 py-1.5 border border-primary text-primary hover:bg-primary/10 transition-colors rounded-md font-mono text-sm"
          >
            <Linkedin className="w-3 h-3" />
            LinkedIn
          </Link>
          <Link
            href="https://github.com/perryrosenberg"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 px-3 py-1.5 border border-border text-muted-foreground hover:border-primary hover:text-primary transition-colors rounded-md font-mono text-sm"
          >
            <Github className="w-3 h-3" />
            GitHub
          </Link>
        </div>
      </div>
    </section>
  );
}
