/**
 * Moshimo Logo Component
 * 
 * Clean, professional "M" logo with subtle financial chart motif.
 * The right leg of the M curves upward, suggesting growth.
 * Simple enough to work at any size.
 */

interface LogoProps {
  size?: number;
  className?: string;
}

export default function Logo({ size = 40, className = '' }: LogoProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 40 40"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`logo ${className}`}
      aria-label="Moshimo Logo"
    >
      {/* Clean "M" with upward-trending right leg */}
      <path
        d="M 8 32 
           L 8 12 
           L 20 24 
           L 32 12 
           L 32 24
           C 32 24, 32 8, 36 6"
        stroke="var(--accent)"
        strokeWidth="3.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
      
      {/* Small upward arrow accent */}
      <path
        d="M 33 9 L 36 6 L 36 10"
        stroke="var(--accent)"
        strokeWidth="2.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  );
}
