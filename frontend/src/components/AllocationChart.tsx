import type { HoldingInfo } from '../types/api.types';
import './AllocationChart.css';

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
    <div className="allocation-chart">
      <h3 className="allocation-chart__title">Portfolio Allocation</h3>
      <div className="allocation-chart__content">
        {/* SVG Donut */}
        <div className="allocation-chart__donut">
          <svg viewBox={`0 0 ${size} ${size}`} className="allocation-chart__svg">
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
              className="allocation-chart__center-label"
            >
              Total
            </text>
            <text
              x={cx}
              y={cy + 14}
              textAnchor="middle"
              dominantBaseline="central"
              className="allocation-chart__center-value"
            >
              {formatCurrency(totalValue)}
            </text>
          </svg>
        </div>

        {/* Legend */}
        <div className="allocation-chart__legend">
          {segments.map((seg) => (
            <div key={seg.holding.symbol} className="allocation-chart__legend-item">
              <span
                className="allocation-chart__legend-dot"
                style={{ backgroundColor: seg.color }}
              />
              <span className="allocation-chart__legend-symbol">{seg.holding.symbol}</span>
              <span className="allocation-chart__legend-pct">{formatPercent(seg.fraction)}</span>
              <span className="allocation-chart__legend-value">
                {formatCurrency(seg.holding.currentValue)}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
