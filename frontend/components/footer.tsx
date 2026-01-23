import { Github, Linkedin, Mail, Sparkles } from "lucide-react";
import Link from "next/link";

const socialLinks = [
  {
    name: "GitHub",
    href: "https://github.com/perryrosenberg",
    icon: Github,
  },
  {
    name: "LinkedIn",
    href: "https://www.linkedin.com/in/perry-rosenberg/",
    icon: Linkedin,
  },
  {
    name: "Email",
    href: "mailto:perry_rosenberg@alumni.brown.edu",
    icon: Mail,
  },
];

export function Footer() {
  return (
    <footer className="py-6 px-6 border-t border-border">
      {/* Mobile Social Links */}
      <div className="lg:hidden flex justify-center gap-4 mb-4">
        {socialLinks.map((link) => (
          <Link
            key={link.name}
            href={link.href}
            target="_blank"
            rel="noopener noreferrer"
            className="text-muted-foreground hover:text-primary transition-colors"
            aria-label={link.name}
          >
            <link.icon className="w-4 h-4" />
          </Link>
        ))}
      </div>

      {/* Footer Text */}
      <div className="text-center space-y-1">
        <div className="inline-flex items-center gap-2 text-muted-foreground font-mono text-[10px]">
          <Sparkles className="w-3 h-3 text-primary" />
          <span>This website was built using agentic AI workflows to test and showcase the tools (v0.dev, junie, claude code) and is Open Source for transparency</span>
        </div>
        <p className="text-muted-foreground font-mono text-[10px]">
          {new Date().getFullYear()} Perry Rosenberg
        </p>
      </div>
    </footer>
  );
}
