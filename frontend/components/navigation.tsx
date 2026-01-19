"use client";

import { useState, useEffect } from "react";
import { Menu, X } from "lucide-react";
import Link from "next/link";

const navItems = [
  { name: "About", href: "#about" },
  { name: "Experience", href: "#experience" },
  { name: "Contact", href: "#contact" },
];

export function Navigation() {
  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 50);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <>
      <header
        className={`fixed top-0 left-0 z-50 transition-all duration-300 w-full lg:w-2/3 ${
          isScrolled
            ? "bg-background/90 backdrop-blur-md shadow-lg py-4"
            : "bg-transparent py-6"
        }`}
      >
        <nav className="flex items-center justify-between px-6 md:px-12 lg:px-24">
          <Link
            href="/"
            className="text-primary font-bold text-2xl hover:opacity-80 transition-opacity"
            aria-label="Home"
          >
            {"<PR />"}
          </Link>

          {/* Desktop Navigation */}
          <ul className="hidden md:flex items-center gap-8">
            {navItems.map((item, idx) => (
              <li key={item.name}>
                <Link
                  href={item.href}
                  className="text-foreground hover:text-primary transition-colors font-mono text-sm"
                >
                  <span className="text-primary">0{idx + 1}.</span> {item.name}
                </Link>
              </li>
            ))}
            <li>
              <Link
                href="/resume.pdf"
                target="_blank"
                className="px-4 py-2 border border-primary text-primary hover:bg-primary/10 transition-colors rounded-md font-mono text-sm"
              >
                Resume
              </Link>
            </li>
          </ul>

          {/* Mobile Menu Button */}
          <button
            onClick={() => setIsMobileMenuOpen(true)}
            className="md:hidden text-primary p-2"
            aria-label="Open menu"
          >
            <Menu className="w-6 h-6" />
          </button>
        </nav>
      </header>

      {/* Mobile Menu Overlay */}
      {isMobileMenuOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div
            className="absolute inset-0 bg-background/80 backdrop-blur-sm"
            onClick={() => setIsMobileMenuOpen(false)}
          />
          <aside className="absolute right-0 top-0 bottom-0 w-3/4 max-w-sm bg-card shadow-2xl flex flex-col items-center justify-center p-8">
            <button
              onClick={() => setIsMobileMenuOpen(false)}
              className="absolute top-6 right-6 text-foreground hover:text-primary transition-colors"
              aria-label="Close menu"
            >
              <X className="w-6 h-6" />
            </button>
            <ul className="flex flex-col items-center gap-8">
              {navItems.map((item, idx) => (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="text-foreground hover:text-primary transition-colors font-mono text-lg"
                  >
                    <span className="text-primary block text-center text-sm mb-1">
                      0{idx + 1}.
                    </span>
                    {item.name}
                  </Link>
                </li>
              ))}
              <li>
                <Link
                  href="/resume.pdf"
                  target="_blank"
                  onClick={() => setIsMobileMenuOpen(false)}
                  className="px-8 py-3 border border-primary text-primary hover:bg-primary/10 transition-colors rounded-md font-mono text-sm"
                >
                  Resume
                </Link>
              </li>
            </ul>
          </aside>
        </div>
      )}
    </>
  );
}
