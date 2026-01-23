import { Zap, Database, Shield, Users, Gauge, TestTube } from "lucide-react";

const highlights = [
  {
    title: "ETL Orders Search Pipeline Rescue",
    icon: Database,
    description:
      "Rescued a failed ETL project mired in re-work for over a year. Wrote PoC using DMS, DynamoDB, Kinesis, Docker/Fargate, and Elasticsearch, then led senior engineers to implement it. Distributed architecture synced 100+ million orders in two days. Improved p75/p90 response times from minutes to seconds, reduced average load times from ~400ms to ~100ms, and cut Aurora RDS CPU usage by 25%.",
    impact: "Saved multiple $10k/month ARR contracts",
    technologies: ["DMS", "DynamoDB", "Kinesis", "Fargate", "Elasticsearch"],
  },
  {
    title: "MySQL 5.7 to 8 Database Upgrade",
    icon: Zap,
    description:
      "Led cross-team effort of 12+ engineers, architects, QA, devops, and product owners to upgrade the main Order Manager database from MySQL 5.7 to MySQL 8. Delivered estimated 2-month project 3 weeks ahead of time with zero customer impact.",
    impact: "Reduced AWS spend by 37%",
    technologies: ["MySQL", "Aurora RDS", "Cross-functional Leadership"],
  },
  {
    title: "Single Sign-On Architecture",
    icon: Shield,
    description:
      "Led transition of OM Product to Extensiv's SSO by architecting application and schema architecture for federated access and permissions across multiple accounts. Designed customer UX and managed implementation across API, security, and access features.",
    impact: "Unified access for 4+ product lines",
    technologies: ["Cognito", "IAM", "OAuth", "Security Architecture"],
  },
  {
    title: "Infrastructure & Monitoring Excellence",
    icon: Gauge,
    description:
      "Defined and created infrastructure for releases and monitoring. Integrated CloudWatch, DataDog, Sumologic, and PagerDuty. Defined key metrics for observability and trained entire devops team on tooling.",
    impact: "Improved uptime from 98% to 99.98% (~$40M less delayed order volume)",
    technologies: ["CloudWatch", "DataDog", "Sumologic", "PagerDuty"],
  },
  {
    title: "Partner & Stakeholder Management",
    icon: Users,
    description:
      "Managed multiple engagements with high-value customers ($100k+ yearly contracts) and partners (Amazon, FedEx, DHL Express). Implemented FedEx compatible partnership program and defined partnership goals enabling visibility directly on partner sites.",
    impact: "Prevented churn on $100k+ contracts",
    technologies: ["Amazon MWS", "FedEx API", "DHL Express"],
  },
  {
    title: "QA Automation Transformation",
    icon: TestTube,
    description:
      "Directed QA Engineers in defining a key suite of core tests, transforming QA efforts from a manual process for cron jobs to a Selenium-based approach that is automated, repeatable, and integrated into CI/CD.",
    impact: "Automated testing pipeline",
    technologies: ["Selenium", "CI/CD", "Jenkins", "Automated Testing"],
  },
];

export function ProjectsSection() {
  return (
    <section id="projects" className="py-24 px-6 md:px-12 lg:px-24">
      <div className="max-w-5xl mx-auto">
        <div className="flex items-center gap-4 mb-10">
          <h2 className="text-2xl md:text-3xl font-bold text-foreground whitespace-nowrap">
            <span className="text-primary font-mono text-xl mr-2">03.</span>
            Highlighted Impact
          </h2>
          <div className="h-px bg-border flex-1" />
        </div>

        <p className="text-muted-foreground text-lg mb-12 max-w-3xl">
          Key initiatives and technical achievements demonstrating architecture, leadership, 
          and measurable business impact.
        </p>

        <div className="grid md:grid-cols-2 gap-6">
          {highlights.map((project) => (
            <div
              key={project.title}
              className="bg-card p-6 rounded-lg border border-border hover:border-primary/50 transition-colors group"
            >
              <div className="flex items-start gap-4 mb-4">
                <div className="p-3 bg-primary/10 rounded-lg group-hover:bg-primary/20 transition-colors">
                  <project.icon className="w-6 h-6 text-primary" />
                </div>
                <div className="flex-1">
                  <h3 className="text-lg font-bold text-foreground mb-1 group-hover:text-primary transition-colors">
                    {project.title}
                  </h3>
                  <p className="text-primary font-mono text-base">
                    {project.impact}
                  </p>
                </div>
              </div>
              
              <p className="text-muted-foreground text-base leading-relaxed mb-4">
                {project.description}
              </p>

              <div className="flex flex-wrap gap-2">
                {project.technologies.map((tech) => (
                  <span
                    key={tech}
                    className="px-2 py-1 bg-secondary text-muted-foreground rounded text-sm font-mono"
                  >
                    {tech}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
