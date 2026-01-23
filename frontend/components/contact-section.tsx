import { Mail, MapPin, Phone } from "lucide-react";
import Link from "next/link";

export function ContactSection() {
  return (
    <section id="contact" className="py-8 px-6 border-t border-border">
      <div className="flex items-center gap-3 mb-4">
        <h2 className="text-lg font-bold text-foreground whitespace-nowrap">
          <span className="text-primary font-mono text-base mr-1">03.</span>
          Get In Touch
        </h2>
        <div className="h-px bg-border flex-1" />
      </div>

      <p className="text-muted-foreground text-base leading-relaxed mb-4">
        I&apos;m currently exploring new opportunities where I can leverage my experience 
        in scaling systems, leading teams, and driving technical strategy.
      </p>

      <div className="flex flex-wrap items-center gap-4 mb-4 text-muted-foreground text-base">
        <span className="flex items-center gap-1.5">
          <MapPin className="w-3 h-3 text-primary" />
          San Francisco, CA
        </span>
        <span className="flex items-center gap-1.5">
          <Phone className="w-3 h-3 text-primary" />
          609-417-0080
        </span>
      </div>

      <Link
        href="mailto:perry_rosenberg@alumni.brown.edu"
        className="inline-flex items-center gap-2 px-4 py-2 border border-primary text-primary hover:bg-primary/10 transition-colors rounded-md font-mono text-sm"
      >
        <Mail className="w-3 h-3" />
        Say Hello
      </Link>
    </section>
  );
}
