import { useEffect, useRef, useState, useMemo } from 'react';
import { createChart, type IChartApi, type ISeriesApi, type LineData, AreaSeries, LineSeries } from 'lightweight-charts';
import type { TimelinePoint, HoldingInfo } from '../types/api.types';
import './PortfolioChart.css';

/**
 * Helper function to read CSS variable value
 */
const getCSSVariable = (varName: string): string => {
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim();
};

/**
 * Holding colors palette - uses theme colors for consistency
 */
const getHoldingColors = (): string[] => {
  return [
    getCSSVariable('--accent'),      // Emerald
    getCSSVariable('--warning'),     // Amber
    getCSSVariable('--info'),        // Slate
    '#64b5a6',                       // Teal (complementary)
    '#9d7cc7',                       // Purple (complementary)
    '#f87171',                       // Rose (complementary)
    '#a3a3a3',                       // Gray (complementary)
    '#fb923c',                       // Orange (complementary)
  ];
};

interface PortfolioChartProps {
  timeline: TimelinePoint[];
  totalInvested: number;
  investments?: Array<{ symbol: string; amountUsd: number; purchaseDate: string }>;
  showBenchmark?: boolean;
  benchmarkTimeline?: TimelinePoint[];
  holdings?: HoldingInfo[];
  holdingsTimelines?: Record<string, TimelinePoint[]>;
}

type ViewMode = 'combined' | 'individual';

/**
 * Portfolio Chart Component - Visualizes portfolio value over time.
 * 
 * Features:
 * - Combined view: Shows total portfolio value as teal area chart
 * - Individual view: Shows each holding as separate colored line
 * - Theme-aware with light/dark mode support
 */
export function PortfolioChart({ 
  timeline, 
  totalInvested, 
  investments, 
  showBenchmark = false, 
  benchmarkTimeline,
  holdings,
  holdingsTimelines 
}: PortfolioChartProps) {
  const chartContainerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<'Area'> | null>(null);
  const benchmarkSeriesRef = useRef<ISeriesApi<'Line'> | null>(null);
  const holdingSeriesRefs = useRef<Map<string, ISeriesApi<'Line'>>>(new Map());
  
  const [viewMode, setViewMode] = useState<ViewMode>('combined');
  const [currentTheme, setCurrentTheme] = useState<'light' | 'dark'>(() => {
    return (document.documentElement.getAttribute('data-theme') as 'light' | 'dark') || 'light';
  });
  const [tooltipData, setTooltipData] = useState<{
    visible: boolean;
    entries: Array<{ symbol: string; value: string; color: string }>;
    date: string;
    x: number;
    y: number;
  }>({
    visible: false,
    entries: [],
    date: '',
    x: 0,
    y: 0,
  });

  // Get theme colors from CSS variables
  const themeColors = useMemo(() => {
    return {
      bgPrimary: getCSSVariable('--bg-primary'),
      bgSecondary: getCSSVariable('--bg-secondary'),
      textPrimary: getCSSVariable('--text-primary'),
      textSecondary: getCSSVariable('--text-secondary'),
      borderColor: getCSSVariable('--border-color'),
      accent: getCSSVariable('--accent'),
      success: getCSSVariable('--success'),
      info: getCSSVariable('--info'),
    };
  }, [currentTheme]);

  // Get holding colors based on current theme
  const holdingColors = useMemo(() => getHoldingColors(), [currentTheme]);

  // Check if individual view is available
  const hasIndividualData = holdingsTimelines && Object.keys(holdingsTimelines).length > 0;
  
  // Show toggle if there are multiple holdings (even if individual data not yet loaded)
  const hasMultipleHoldings = holdings && holdings.length > 1;

  useEffect(() => {
    if (!chartContainerRef.current) return;

    // Create chart instance with theme colors
    const chart = createChart(chartContainerRef.current, {
      layout: {
        background: { color: themeColors.bgPrimary },
        textColor: themeColors.textPrimary,
      },
      grid: {
        vertLines: { color: themeColors.borderColor },
        horzLines: { color: themeColors.borderColor },
      },
      width: chartContainerRef.current.clientWidth,
      height: 400,
      crosshair: {
        mode: 0, // Normal (free) — crosshair follows mouse smoothly
        vertLine: {
          width: 1,
          color: themeColors.textSecondary,
          style: 3, // Dashed line
          labelBackgroundColor: themeColors.accent,
        },
        horzLine: {
          width: 1,
          color: themeColors.textSecondary,
          style: 3, // Dashed line
          labelBackgroundColor: themeColors.accent,
        },
      },
      timeScale: {
        timeVisible: true,
        secondsVisible: false,
        borderColor: themeColors.borderColor,
        rightOffset: 5,
        barSpacing: 10,
        fixLeftEdge: true,
        fixRightEdge: true,
      },
      rightPriceScale: {
        borderColor: themeColors.borderColor,
        scaleMargins: {
          top: 0.1,
          bottom: 0.1,
        },
      },
      localization: {
        priceFormatter: (price: number) => {
          return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
          }).format(price);
        },
      },
      handleScale: {
        axisPressedMouseMove: {
          time: true,
          price: true,
        },
        mouseWheel: true,
        pinch: true,
      },
      handleScroll: {
        mouseWheel: true,
        pressedMouseMove: true,
        horzTouchDrag: true,
        vertTouchDrag: true,
      },
    });

    chartRef.current = chart;

    // Clear previous series refs
    holdingSeriesRefs.current.clear();

    if (viewMode === 'combined') {
      // Combined view: Emerald area chart (accent color)
      const accentRgb = getCSSVariable('--accent-rgb');
      
      const series = chart.addSeries(AreaSeries, {
        topColor: `rgba(${accentRgb}, 0.5)`,       // Accent gradient top
        bottomColor: `rgba(${accentRgb}, 0.05)`,   // Faded accent bottom
        lineColor: themeColors.accent,             // Solid accent line
        lineWidth: 2,
        priceLineVisible: false,
        lastValueVisible: true,
      });

      seriesRef.current = series;

      const chartData: LineData[] = timeline.map((point) => ({
        time: point.date as any,
        value: point.value,
      }));

      series.setData(chartData);

      // Add baseline for total invested
      if (totalInvested > 0 && timeline.length > 0) {
        series.createPriceLine({
          price: totalInvested,
          color: themeColors.success,
          lineWidth: 2,
          lineStyle: 2,
          axisLabelVisible: true,
          title: 'Total Invested',
        });
      }
    } else if (viewMode === 'individual' && holdingsTimelines) {
      // Individual view: Separate colored lines per holding
      const symbols = Object.keys(holdingsTimelines);
      
      symbols.forEach((symbol, index) => {
        const timelineData = holdingsTimelines[symbol];
        if (!timelineData || timelineData.length === 0) return;

        const color = holdingColors[index % holdingColors.length];
        
        const series = chart.addSeries(LineSeries, {
          color,
          lineWidth: 2,
          priceLineVisible: false,
          lastValueVisible: true,
        });

        const chartData: LineData[] = timelineData.map((point) => ({
          time: point.date as any,
          value: point.value,
        }));

        series.setData(chartData);
        holdingSeriesRefs.current.set(symbol, series);
      });
    }

    // Add benchmark line if enabled
    if (showBenchmark && benchmarkTimeline && benchmarkTimeline.length > 0) {
      const benchmarkSeries = chart.addSeries(LineSeries, {
        color: themeColors.info,
        lineWidth: 2,
        lineStyle: 2,
        priceLineVisible: false,
        lastValueVisible: false,
      });

      benchmarkSeriesRef.current = benchmarkSeries;

      const benchmarkData: LineData[] = benchmarkTimeline.map((point) => ({
        time: point.date as any,
        value: point.value,
      }));

      benchmarkSeries.setData(benchmarkData);
    }

    chart.timeScale().fitContent();

    // Subscribe to crosshair move for tooltip
    // Track last known values so the tooltip stays stable between data gaps
    const lastKnownValues = new Map<string, number>();

    chart.subscribeCrosshairMove((param) => {
      if (
        param.point === undefined ||
        !param.time ||
        param.point.x < 0 ||
        param.point.y < 0
      ) {
        setTooltipData({ visible: false, entries: [], date: '', x: 0, y: 0 });
        return;
      }

      const formatUSD = (price: number) =>
        new Intl.NumberFormat('en-US', {
          style: 'currency',
          currency: 'USD',
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        }).format(price);

      let entries: Array<{ symbol: string; value: string; color: string }> = [];

      if (viewMode === 'combined' && seriesRef.current) {
        const data = param.seriesData.get(seriesRef.current);
        const price = (data as any)?.value;
        if (price != null) {
          lastKnownValues.set('Portfolio', price);
          entries = [{ symbol: 'Portfolio', value: formatUSD(price), color: themeColors.accent }];
        } else {
          const last = lastKnownValues.get('Portfolio');
          if (last != null) {
            entries = [{ symbol: 'Portfolio', value: formatUSD(last), color: themeColors.accent }];
          }
        }
      } else if (viewMode === 'individual') {
        // Show ALL holdings — use last known value when no data at this point
        const symbols = [...holdingSeriesRefs.current.keys()];
        symbols.forEach((symbol, index) => {
          const series = holdingSeriesRefs.current.get(symbol);
          if (!series) return;
          const data = param.seriesData.get(series);
          const price = (data as any)?.value;
          const color = holdingColors[index % holdingColors.length];

          if (price != null) {
            lastKnownValues.set(symbol, price);
            entries.push({ symbol, value: formatUSD(price), color });
          } else {
            const last = lastKnownValues.get(symbol);
            if (last != null) {
              entries.push({ symbol, value: formatUSD(last), color });
            }
          }
        });
      }

      const dateStr = new Date(param.time as string).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      });

      setTooltipData({
        visible: entries.length > 0,
        entries,
        date: dateStr,
        x: param.point.x,
        y: param.point.y,
      });
    });

    // Handle window resize
    const handleResize = () => {
      if (chartContainerRef.current) {
        chart.applyOptions({
          width: chartContainerRef.current.clientWidth,
        });
      }
    };

    window.addEventListener('resize', handleResize);

    // Cleanup on unmount
    return () => {
      window.removeEventListener('resize', handleResize);
      chart.remove();
    };
  }, [timeline, totalInvested, investments, showBenchmark, benchmarkTimeline, holdingsTimelines, viewMode, currentTheme, themeColors, holdingColors]);

  // Listen for theme changes
  useEffect(() => {
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.type === 'attributes' && mutation.attributeName === 'data-theme') {
          const newTheme = (document.documentElement.getAttribute('data-theme') as 'light' | 'dark') || 'light';
          setCurrentTheme(newTheme);
        }
      });
    });

    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme'],
    });

    return () => {
      observer.disconnect();
    };
  }, []);

  // Get symbols for legend
  const legendSymbols = holdingsTimelines ? Object.keys(holdingsTimelines) : [];

  return (
    <div className="portfolio-chart">
      <div className="portfolio-chart__header">
        <div className="portfolio-chart__header-left">
          <h3 className="portfolio-chart__title">Portfolio Value Over Time</h3>
          <p className="portfolio-chart__subtitle">
            {viewMode === 'combined' 
              ? 'Track your total investment growth from purchase to present'
              : 'Compare individual holding performance over time'
            }
          </p>
        </div>
        {hasMultipleHoldings && (
          <div className="portfolio-chart__view-toggle">
            <button
              className={`portfolio-chart__view-btn ${viewMode === 'combined' ? 'portfolio-chart__view-btn--active' : ''}`}
              onClick={() => setViewMode('combined')}
            >
              Combined
            </button>
            <button
              className={`portfolio-chart__view-btn ${viewMode === 'individual' ? 'portfolio-chart__view-btn--active' : ''}`}
              onClick={() => hasIndividualData && setViewMode('individual')}
              disabled={!hasIndividualData}
              title={!hasIndividualData ? 'Individual view loading...' : 'View each asset separately'}
            >
              Individual
            </button>
          </div>
        )}
      </div>
      <div className="portfolio-chart__chart-wrapper">
        <div className="portfolio-chart__y-axis-label">
          Portfolio Value (USD)
        </div>
        <div ref={chartContainerRef} className="portfolio-chart__container">
          <div className="portfolio-chart__watermark">Moshimo</div>
        </div>
        
        {/* Custom Tooltip */}
        {tooltipData.visible && (
          <div 
            className="portfolio-chart__tooltip"
            style={{
              left: `${tooltipData.x}px`,
              top: `${tooltipData.y}px`,
            }}
          >
            {tooltipData.entries.map((entry) => (
              <div key={entry.symbol} className="portfolio-chart__tooltip-entry">
                <span
                  className="portfolio-chart__tooltip-dot"
                  style={{ backgroundColor: entry.color }}
                />
                <span className="portfolio-chart__tooltip-symbol">{entry.symbol}</span>
                <span className="portfolio-chart__tooltip-value">{entry.value}</span>
              </div>
            ))}
            <div className="portfolio-chart__tooltip-date">{tooltipData.date}</div>
          </div>
        )}
        
        <div className="portfolio-chart__x-axis-label">
          Date / Time
        </div>
      </div>
      
      {/* Legend for individual view */}
      {viewMode === 'individual' && legendSymbols.length > 0 && (
        <div className="portfolio-chart__legend">
          {legendSymbols.map((symbol, index) => (
            <div key={symbol} className="portfolio-chart__legend-item">
              <span 
                className="portfolio-chart__legend-color" 
                style={{ backgroundColor: holdingColors[index % holdingColors.length] }}
              />
              <span>{symbol}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}