import { useEffect, useRef, useState } from 'react';
import { createChart, type IChartApi, type ISeriesApi, type LineData, AreaSeries, LineSeries } from 'lightweight-charts';
import type { TimelinePoint, HoldingInfo } from '../types/api.types';
import './PortfolioChart.css';

// Color palette for individual holdings (teal-inspired, distinct)
const HOLDING_COLORS = [
  '#5c9c9c', // Primary teal
  '#e07941', // Warm orange
  '#7c6daf', // Soft purple  
  '#4a9b8f', // Sage green
  '#d4785c', // Coral
  '#5d8aa8', // Steel blue
  '#9b7653', // Warm brown
  '#6b8e6b', // Forest green
];

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

  // Check if individual view is available
  const hasIndividualData = holdingsTimelines && Object.keys(holdingsTimelines).length > 0;
  
  // Show toggle if there are multiple holdings (even if individual data not yet loaded)
  const hasMultipleHoldings = holdings && holdings.length > 1;

  useEffect(() => {
    if (!chartContainerRef.current) return;

    const isDark = currentTheme === 'dark';

    // Create chart instance
    const chart = createChart(chartContainerRef.current, {
      layout: {
        background: { color: isDark ? '#1e293b' : '#ffffff' },
        textColor: isDark ? '#f1f5f9' : '#333',
      },
      grid: {
        vertLines: { color: isDark ? '#334155' : '#f0f0f0' },
        horzLines: { color: isDark ? '#334155' : '#f0f0f0' },
      },
      width: chartContainerRef.current.clientWidth,
      height: 400,
      timeScale: {
        timeVisible: true,
        secondsVisible: false,
        borderColor: isDark ? '#475569' : '#D1D5DB',
      },
      rightPriceScale: {
        borderColor: isDark ? '#475569' : '#D1D5DB',
      },
    });

    chartRef.current = chart;

    // Clear previous series refs
    holdingSeriesRefs.current.clear();

    if (viewMode === 'combined') {
      // Combined view: Single teal area chart
      const series = chart.addSeries(AreaSeries, {
        topColor: 'rgba(92, 156, 156, 0.5)',      // Teal gradient top
        bottomColor: 'rgba(92, 156, 156, 0.05)',  // Faded teal bottom
        lineColor: '#5c9c9c',                      // Solid teal line
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
          color: '#10b981',
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

        const color = HOLDING_COLORS[index % HOLDING_COLORS.length];
        
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
        color: '#9ca3af',
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
  }, [timeline, totalInvested, investments, showBenchmark, benchmarkTimeline, holdingsTimelines, viewMode, currentTheme]);

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
              title={!hasIndividualData ? 'Individual view loading...' : 'View each stock separately'}
            >
              Individual
            </button>
          </div>
        )}
      </div>
      <div ref={chartContainerRef} className="portfolio-chart__container" />
      
      {/* Legend for individual view */}
      {viewMode === 'individual' && legendSymbols.length > 0 && (
        <div className="portfolio-chart__legend">
          {legendSymbols.map((symbol, index) => (
            <div key={symbol} className="portfolio-chart__legend-item">
              <span 
                className="portfolio-chart__legend-color" 
                style={{ backgroundColor: HOLDING_COLORS[index % HOLDING_COLORS.length] }}
              />
              <span>{symbol}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}