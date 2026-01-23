import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

/**
 * Combines multiple class names using clsx and merges Tailwind CSS classes intelligently.
 *
 * This utility function prevents Tailwind class conflicts by merging classes that target
 * the same CSS property. For example, if you pass both "px-4" and "px-2", only "px-2"
 * (the last one) will be applied.
 *
 * @param inputs - Any number of class values (strings, objects, arrays, etc.)
 * @returns A single string with merged class names
 *
 * @example
 * ```ts
 * cn("px-4 py-2", "px-6") // Returns "py-2 px-6"
 * cn("text-blue-500", isActive && "text-red-500") // Conditionally applies classes
 * cn(["flex", "items-center"], { "gap-2": hasGap }) // Mix arrays and objects
 * ```
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
