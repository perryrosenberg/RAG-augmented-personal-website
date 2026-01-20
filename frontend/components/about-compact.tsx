export function AboutCompact() {
  const skills = [
    { category: "Languages", items: ["Java", "JavaScript/React", "Python", "TypeScript", "SQL", "Scala", "Go"] },
    { category: "AWS", items: ["RDS Aurora", "OpenSearch", "DynamoDB", "ECS/Fargate", "Lambda", "Kinesis", "SQS/SNS"] },
    { category: "Tools", items: ["Spring Boot", "Docker", "Terraform", "Jenkins", "DataDog", "PagerDuty"] },
    { category: "AI", items: ["Claude", "ChatGPT", "Copilot", "Amazon Q", "Gemini"] },
  ];

  return (
    <section id="about" className="py-8 px-6 border-b border-border">
      <div className="flex items-center gap-3 mb-4">
        <h2 className="text-lg font-bold text-foreground whitespace-nowrap">
          <span className="text-primary font-mono text-sm mr-1">01.</span>
          About Me
        </h2>
        <div className="h-px bg-border flex-1" />
      </div>

      <p className="text-muted-foreground text-sm leading-relaxed mb-4">
        Principal engineer and architect with a decade of experience designing and scaling 
        high-traffic, cloud-native systems. Known for end-to-end ownership of complex initiatives. 
        B.S. Computer Science from Brown University. Based in San Francisco.
      </p>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {skills.map((group) => (
          <div key={group.category}>
            <h3 className="text-foreground font-semibold text-xs mb-2">{group.category}</h3>
            <ul className="space-y-0.5 font-mono text-xs text-muted-foreground">
              {group.items.map((skill) => (
                <li key={skill} className="flex items-center gap-1">
                  <span className="text-primary">▹</span>
                  {skill}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </section>
  );
}
