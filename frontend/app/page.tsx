import { HeroCompact } from "@/components/hero-compact";
import { AboutCompact } from "@/components/about-compact";
import { ExperienceSection } from "@/components/experience-section";
import { StickyAssistant } from "@/components/sticky-assistant";
import { ContactSection } from "@/components/contact-section";
import { Footer } from "@/components/footer";

export default function Home() {
  return (
    <div className="min-h-screen bg-background">
      
      {/* Main layout: 2/3 content + 1/3 sticky assistant */}
      <div className="flex">
        {/* Left content area - scrollable */}
        <main className="flex-1 lg:w-2/3">
          <HeroCompact />
          <AboutCompact />
          <ExperienceSection />
          <ContactSection />
          <Footer />
        </main>

        {/* Right sidebar - sticky assistant (hidden on mobile) */}
        <div className="hidden lg:block lg:w-1/3">
          <StickyAssistant />
        </div>
      </div>

      {/* Mobile assistant link - shown at bottom on small screens */}
      <div className="lg:hidden fixed bottom-4 right-4 z-40">
        <a
          href="#assistant-mobile"
          className="flex items-center gap-2 px-4 py-3 bg-primary text-primary-foreground rounded-full shadow-lg hover:opacity-90 transition-opacity"
        >
          <span className="text-base font-medium">AI Assistant</span>
        </a>
      </div>
    </div>
  );
}
