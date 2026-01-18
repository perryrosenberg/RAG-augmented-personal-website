"use client";

import { Github, Linkedin, Mail, FileText, ArrowDown, Sparkles } from "lucide-react";
import Link from "next/link";

export function HeroSection() {
  const scrollToAbout = () => {
    document.getElementById("about")?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <section className="min-h-screen flex flex-col justify-center px-6 md:px-12 lg:px-24 py-20">
      <div className="max-w-4xl">
        {/* AI Built Badge - Top Billing */}
        <div className="inline-flex items-center gap-2 px-4 py-2 mb-8 bg-primary/10 border border-primary/30 rounded-full">
          <Sparkles className="w-4 h-4 text-primary" />
          <span className="text-primary font-mono text-sm">
            This entire website was built with AI using v0.dev
          </span>
        </div>

        <p className="text-primary font-mono text-sm md:text-base mb-4">
          Hi, my name is
        </p>
        <h1 className="text-4xl md:text-6xl lg:text-7xl font-bold text-foreground mb-4 text-balance">
          Perry Rosenberg
        </h1>
        <h2 className="text-3xl md:text-5xl lg:text-6xl font-bold text-muted-foreground mb-6 text-balance">
          I architect systems that scale.
        </h2>
        <p className="text-muted-foreground text-lg md:text-xl max-w-2xl mb-8 leading-relaxed">
          Principal engineer and architect with a decade of experience designing and scaling 
          high-traffic, cloud-native systems. Currently based in San Francisco and open to remote opportunities.
        </p>

        <div className="flex flex-wrap gap-4 mb-12">
          <Link
            href="https://www.linkedin.com/in/perry-rosenberg/"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 px-5 py-3 border border-primary text-primary hover:bg-primary/10 transition-colors rounded-md font-mono text-sm"
          >
            <Linkedin className="w-4 h-4" />
            LinkedIn
          </Link>
          <Link
            href="https://github.com/perryrosenberg"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 px-5 py-3 border border-border text-muted-foreground hover:border-primary hover:text-primary transition-colors rounded-md font-mono text-sm"
          >
            <Github className="w-4 h-4" />
            GitHub
          </Link>
          <Link
            href="/resume.pdf"
            target="_blank"
            className="flex items-center gap-2 px-5 py-3 border border-border text-muted-foreground hover:border-primary hover:text-primary transition-colors rounded-md font-mono text-sm"
          >
            <FileText className="w-4 h-4" />
            Resume
          </Link>
          <Link
            href="mailto:perry_rosenberg@alumni.brown.edu"
            className="flex items-center gap-2 px-5 py-3 border border-border text-muted-foreground hover:border-primary hover:text-primary transition-colors rounded-md font-mono text-sm"
          >
            <Mail className="w-4 h-4" />
            Email
          </Link>
        </div>

        <button
          onClick={scrollToAbout}
          className="flex items-center gap-2 text-muted-foreground hover:text-primary transition-colors animate-bounce"
          aria-label="Scroll to about section"
        >
          <ArrowDown className="w-5 h-5" />
        </button>
      </div>
    </section>
  );
}
