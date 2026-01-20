export function AboutSection() {
  const languages = [
    "Java",
    "JavaScript/React",
    "Python",
    "TypeScript",
    "SQL",
    "Scala",
    "Go",
    "C#",
  ];

  const aws = [
    "RDS Aurora",
    "OpenSearch",
    "DynamoDB",
    "ECS/Fargate",
    "Lambda",
    "Kinesis",
    "SQS/SNS",
    "Cognito",
  ];

  const tooling = [
    "Spring Boot",
    "Docker",
    "Terraform",
    "Jenkins",
    "Git",
    "DataDog",
    "PagerDuty",
  ];

  const ai = [
    "Claude",
    "ChatGPT",
    "Copilot",
    "Amazon Q",
    "Gemini",
    "Junie",
  ];

  return (
    <section id="about" className="py-24 px-6 md:px-12 lg:px-24">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center gap-4 mb-10">
          <h2 className="text-2xl md:text-3xl font-bold text-foreground whitespace-nowrap">
            <span className="text-primary font-mono text-xl mr-2">01.</span>
            About Me
          </h2>
          <div className="h-px bg-border flex-1" />
        </div>

        <div className="space-y-6 text-muted-foreground leading-relaxed">
          <p>
            I'm a principal engineer and architect with a decade of experience designing and 
            scaling high-traffic, cloud-native systems. Known for end-to-end ownership of 
            complex initiatives and driving transformative technical outcomes.
          </p>
          <p>
            I'm equally confident driving solutions hands-on with code, leading engineering teams, 
            and mentoring talent. I deliver lasting product success by creating alignment across 
            executives, product/technology, and customers.
          </p>
          <p>
            I hold a B.S. in Computer Science from Brown University and am based in San Francisco, 
            available for remote opportunities.
          </p>

          <div className="grid md:grid-cols-2 gap-8 mt-8">
            <div>
              <h3 className="text-foreground font-semibold mb-3">Languages</h3>
              <ul className="grid grid-cols-2 gap-2 font-mono text-sm">
                {languages.map((skill) => (
                  <li key={skill} className="flex items-center gap-2">
                    <span className="text-primary">▹</span>
                    {skill}
                  </li>
                ))}
              </ul>
            </div>
            
            <div>
              <h3 className="text-foreground font-semibold mb-3">AWS Services</h3>
              <ul className="grid grid-cols-2 gap-2 font-mono text-sm">
                {aws.map((skill) => (
                  <li key={skill} className="flex items-center gap-2">
                    <span className="text-primary">▹</span>
                    {skill}
                  </li>
                ))}
              </ul>
            </div>

            <div>
              <h3 className="text-foreground font-semibold mb-3">Tooling & DevOps</h3>
              <ul className="grid grid-cols-2 gap-2 font-mono text-sm">
                {tooling.map((skill) => (
                  <li key={skill} className="flex items-center gap-2">
                    <span className="text-primary">▹</span>
                    {skill}
                  </li>
                ))}
              </ul>
            </div>

            <div>
              <h3 className="text-foreground font-semibold mb-3">AI Tools</h3>
              <ul className="grid grid-cols-2 gap-2 font-mono text-sm">
                {ai.map((skill) => (
                  <li key={skill} className="flex items-center gap-2">
                    <span className="text-primary">▹</span>
                    {skill}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
