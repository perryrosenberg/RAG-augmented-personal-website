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
        in scaling systems, leading teams, and driving technical strategy. If that sounds like a fit,
        please contact me on{" "}
        <Link
          href="https://www.linkedin.com/in/perry-rosenberg/"
          target="_blank"
          rel="noopener noreferrer"
          className="text-primary hover:underline"
        >
          LinkedIn
        </Link>
        !
      </p>
    </section>
  );
}
