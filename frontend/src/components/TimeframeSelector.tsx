import React from 'react';
import './TimeframeSelector.css';

interface TimeframeSelectorProps {
  selectedTimeframe: string;
  onTimeframeChange: (timeframe: string) => void;
  disabled?: boolean;
}

/**
 * Timeframe Selector Component
 * 
 * Educational Purpose:
 * This component teaches students that the same investment looks VERY different
 * depending on the timeframe. It's a powerful lesson in perspective:
 * - Daily: Scary volatility, lots of red days
 * - Weekly: Smoother, trends emerge
 * - Monthly: Clear patterns, less noise
 * - Yearly: Compounding magic, crashes become tiny dips
 * - ALL: Balanced view optimized for performance
 * 
 * Key Learning: Long-term investors should zoom OUT to avoid panic.
 */
const TIMEFRAMES = [
  { 
    code: '1D', 
    label: '1D', 
    tooltip: 'Daily — Every trading day across full period\nReveals volatility and short-term noise' 
  },
  { 
    code: '1W', 
    label: '1W', 
    tooltip: 'Weekly — First trading day of each week\nSmoothes daily fluctuations, shows trends' 
  },
  { 
    code: '1M', 
    label: '1M', 
    tooltip: 'Monthly — First trading day of each month\nClear long-term patterns, reduced noise' 
  },
  { 
    code: '1Y', 
    label: '1Y', 
    tooltip: 'Yearly — First trading day of January\nMaximum zoom-out, decade+ perspective' 
  },
  { 
    code: 'ALL', 
    label: 'ALL', 
    tooltip: 'Smart Sampling — Auto-optimized (~500 points)\nBalances detail with performance' 
  },
];

export const TimeframeSelector: React.FC<TimeframeSelectorProps> = ({
  selectedTimeframe,
  onTimeframeChange,
  disabled = false,
}) => {
  return (
    <div className="timeframe-selector">
      <span className="timeframe-label">Timeframe:</span>
      <div className="timeframe-buttons">
        {TIMEFRAMES.map((tf) => (
          <button
            key={tf.code}
            className={`timeframe-btn ${selectedTimeframe === tf.code ? 'active' : ''}`}
            onClick={() => onTimeframeChange(tf.code)}
            title={tf.tooltip}
            disabled={disabled}
            aria-label={`${tf.label} timeframe`}
          >
            {tf.label}
          </button>
        ))}
      </div>
      <span className="timeframe-hint">
        Sampling across entire investment period
      </span>
    </div>
  );
};
