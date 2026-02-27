import { useState, useEffect } from 'react';

/**
 * Animates a number from 0 to the target value over a duration.
 *
 * Uses requestAnimationFrame with ease-out cubic easing for a natural
 * deceleration feel. Respects `prefers-reduced-motion` for accessibility
 * — skips animation and returns the target value immediately.
 *
 * @param target  The final number to animate towards.
 * @param duration  Animation duration in milliseconds (default 800ms).
 * @returns The current animated value (use with formatCurrency/formatPercent).
 */
export function useCountUp(target: number, duration = 800): number {
  const [current, setCurrent] = useState(0);

  useEffect(() => {
    // Accessibility: skip animation when user prefers reduced motion
    const prefersReducedMotion =
      typeof window !== 'undefined' &&
      window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;

    if (prefersReducedMotion || duration <= 0) {
      setCurrent(target);
      return;
    }

    let startTime: number | null = null;
    let rafId: number;

    const animate = (timestamp: number) => {
      if (startTime === null) startTime = timestamp;
      const elapsed = timestamp - startTime;
      const progress = Math.min(elapsed / duration, 1);

      // Ease-out cubic — fast start, gentle landing
      const eased = 1 - Math.pow(1 - progress, 3);

      setCurrent(target * eased);

      if (progress < 1) {
        rafId = requestAnimationFrame(animate);
      }
    };

    rafId = requestAnimationFrame(animate);

    return () => cancelAnimationFrame(rafId);
  }, [target, duration]);

  return current;
}
