import type { HoldingInfo } from '../types/api.types';
import styles from './AllocationChart.module.css';

interface AllocationChartProps {
  holdings: HoldingInfo[];
}

/**
 * Palette of distinct colors for donut segments.
 * Matches the holding colors from PortfolioChart for consistency.
 */
const SEGMENT_COLORS = [
  'var(--accent)',     // Emerald
  'var(--warning)',    // Amber
  'var(--info)',       // Slate
  '#64b5a6',          // Teal
  '#9d7cc7',          // Purple
  '#f87171',          // Rose
  '#a3a3a3',          // Gray
  '#fb923c',          // Orange
];

/**
 * SVG donut chart showing each holding's percentage of the total portfolio value.
 * Renders as a ring with a centered total value.
 */
export function AllocationChart({ holdings }: AllocationChartProps) {
  const totalValue = holdings.reduce((sum, h) => sum + h.currentValue, 0);

  if (totalValue <= 0 || holdings.length === 0) return null;

  // Donut geometry
  const size = 200;
  const cx = size / 2;
  const cy = size / 2;
  const radius = 75;
  const strokeWidth = 28;

  // Build arc segments
  const circumference = 2 * Math.PI * radius;
  let offset = 0;

  const segments = holdings.map((holding, index) => {
    const fraction = holding.currentValue / totalValue;
    const dashLength = fraction * circumference;
    // Small gap between segments (1px visual gap)
    const gap = holdings.length > 1 ? 2 : 0;
    const segment = {
      holding,
      fraction,
      color: SEGMENT_COLORS[index % SEGMENT_COLORS.length],
      dashArray: `${Math.max(dashLength - gap, 0)} ${circumference - Math.max(dashLength - gap, 0)}`,
      dashOffset: -offset,
    };
    offset += dashLength;
    return segment;
  });

  const formatCurrency = (value: number) =>
    new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);

  const formatPercent = (fraction: number) =>
    `${(fraction * 100).toFixed(1)}%`;

  return (
    <div className={styles.allocationChart}>
      <h3 className={styles.allocationChartTitle}>Portfolio Allocation</h3>
      <div className={styles.allocationChartContent}>
        {/* SVG Donut */}
        <div className={styles.allocationChartDonut}>
          <svg viewBox={`0 0 ${size} ${size}`} className={styles.allocationChartSvg}>
            {segments.map((seg) => (
              <circle
                key={seg.holding.symbol}
                cx={cx}
                cy={cy}
                r={radius}
                fill="none"
                stroke={seg.color}
                strokeWidth={strokeWidth}
                strokeDasharray={seg.dashArray}
                strokeDashoffset={seg.dashOffset}
                strokeLinecap="butt"
                transform={`rotate(-90 ${cx} ${cy})`}
              />
            ))}
            {/* Center text */}
            <text
              x={cx}
              y={cy - 8}
              textAnchor="middle"
              dominantBaseline="central"
              className={styles.allocationChartCenterLabel}
            >
              Total
            </text>
            <text
              x={cx}
              y={cy + 14}
              textAnchor="middle"
              dominantBaseline="central"
              className={styles.allocationChartCenterValue}
            >
              {formatCurrency(totalValue)}
            </text>
          </svg>
        </div>

        {/* Legend */}
        <div className={styles.allocationChartLegend}>
          {segments.map((seg) => (
            <div key={seg.holding.symbol} className={styles.allocationChartLegendItem}>
              <span
                className={styles.allocationChartLegendDot}
                style={{ backgroundColor: seg.color }}
              />
              <span className={styles.allocationChartLegendSymbol}>{seg.holding.symbol}</span>
              <span className={styles.allocationChartLegendPct}>{formatPercent(seg.fraction)}</span>
              <span className={styles.allocationChartLegendValue}>
                {formatCurrency(seg.holding.currentValue)}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
