"use client";

import { useState } from "react";
import { ExternalLink } from "lucide-react";
import Link from "next/link";

const experiences = [
  {
    company: "Extensiv",
    title: "Principal Software Engineer, Software Architect",
    url: "https://extensiv.com",
    range: "October 2021 — December 2025",
    duties: [
      "Scaled Order Manager (OM) 40x from startup stage (<500K orders/year to 20M+, $10M to $2B+ volume). Lead technical vision through post-acquisition integration, restructuring, and growth stages",
      "Own end-to-end cloud infrastructure and SDLC with ~$50k/month AWS budget. Created KPIs driving 50% YoY reduction in user errors from 2024 to 2025",
      "Manage teams of up to 15 engineers. Review platform/cloud architecture across 60+ engineers and 8+ teams. Implemented greenfield SSO and designed shared event-sourcing strategy for 4+ lines of business",
      "Lead agentic and generative AI adoption, proving out processes and best practices for 4+ teams with 20+ developers",
      "Mentored multiple engineers—two promoted to Senior, one to Lead Engineer. Train on Domain Driven Design, Event Sourcing, Clean Code, TDD",
    ],
    technologies: ["Java", "React", "Spring Boot", "AWS", "Terraform", "DynamoDB", "OpenSearch", "Docker"],
  },
  {
    company: "Skubana",
    title: "Senior Software Engineer",
    url: "https://extensiv.com",
    range: "October 2017 — October 2021",
    duties: [
      "Led development of shipping software and cloud architecture during scaling and acquisition-seeking stage",
      "Directly impacted acquisition success by leading developer engagements with potential buyers through architectural, security, and code review processes",
      "Led move to microservices—designed and implemented distributed Dockerized architecture reducing shipping speed for 100s of orders from 15+ minutes to ~1 minute",
      "Built non-blocking architecture with Java, Spring Boot, SQS, Docker, Redis, and ECS",
    ],
    technologies: ["Java", "Spring Boot", "Docker", "Redis", "SQS", "ECS", "AWS"],
  },
  {
    company: "Tremor Video",
    title: "Software Engineer",
    url: "https://tremorvideo.com",
    range: "August 2016 — October 2017",
    duties: [
      "Engineered and deployed distributed production systems integrating AI/ML model for real-time filtering of 150,000+ JSON queries per second",
      "Built intelligent pre-filtering mechanisms to improve model efficiency, latency, and scalability while ensuring continuous, fault-tolerant operation",
      "Designed, owned, and supported Java and Scala microservices that directly affected advertising dollars spent",
    ],
    technologies: ["Java", "Scala", "Distributed Systems", "AI/ML", "Real-time Processing"],
  },
  {
    company: "Brown University",
    title: "B.S. Computer Science",
    url: "https://brown.edu",
    range: "August 2012 — May 2016",
    duties: [
      "Bachelor of Science in Computer Science",
      "Foundation in algorithms, data structures, systems programming, and software engineering",
    ],
    technologies: ["Computer Science", "Algorithms", "Systems"],
  },
];

export function ExperienceSection() {
  const [activeTab, setActiveTab] = useState(0);

  return (
    <section id="experience" className="py-8 px-6">
      <div>
        <div className="flex items-center gap-3 mb-6">
          <h2 className="text-lg font-bold text-foreground whitespace-nowrap">
            <span className="text-primary font-mono text-sm mr-1">02.</span>
            Experience
          </h2>
          <div className="h-px bg-border flex-1" />
        </div>

        <div className="flex flex-col md:flex-row gap-4">
          {/* Tab List */}
          <div className="flex md:flex-col overflow-x-auto md:overflow-visible border-b md:border-b-0 md:border-l border-border shrink-0">
            {experiences.map((exp, idx) => (
              <button
                key={exp.company}
                onClick={() => setActiveTab(idx)}
                className={`px-3 py-2 text-xs font-mono text-left whitespace-nowrap transition-colors ${
                  activeTab === idx
                    ? "text-primary bg-primary/10 border-b-2 md:border-b-0 md:border-l-2 border-primary md:-ml-px"
                    : "text-muted-foreground hover:text-primary hover:bg-primary/5"
                }`}
              >
                {exp.company}
              </button>
            ))}
          </div>

          {/* Tab Panels */}
          <div className="py-2 md:py-0 min-h-[300px]">
            <h3 className="text-base font-medium text-foreground mb-0.5">
              {experiences[activeTab].title}{" "}
              <Link
                href={experiences[activeTab].url}
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline inline-flex items-center gap-1"
              >
                @ {experiences[activeTab].company}
                <ExternalLink className="w-3 h-3" />
              </Link>
            </h3>
            <p className="text-muted-foreground font-mono text-xs mb-4">
              {experiences[activeTab].range}
            </p>

            <ul className="space-y-2">
              {experiences[activeTab].duties.map((duty, idx) => (
                <li
                  key={idx}
                  className="flex gap-2 text-muted-foreground text-sm leading-relaxed"
                >
                  <span className="text-primary mt-1 shrink-0">▹</span>
                  <span>{duty}</span>
                </li>
              ))}
            </ul>

            <div className="flex flex-wrap gap-1.5 mt-4">
              {experiences[activeTab].technologies.map((tech) => (
                <span
                  key={tech}
                  className="px-2 py-0.5 bg-primary/10 text-primary rounded-full text-[10px] font-mono"
                >
                  {tech}
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
