import type { SimulationResponse } from '../types/api.types';

/**
 * Generate a CSV string from simulation results.
 *
 * Columns:
 *  - Date, Portfolio Value (for each timeline point)
 *  - Summary section: per-holding breakdown
 *
 * Includes UTF-8 BOM for Excel compatibility and proper escaping.
 */
export function generatePortfolioCSV(results: SimulationResponse): string {
  const BOM = '\uFEFF';
  const rows: string[] = [];

  // --- Summary Section ---
  rows.push('Moshimo Portfolio Simulation');
  rows.push('');
  rows.push('Summary');
  rows.push(csvRow(['Total Invested', formatNum(results.totalInvested)]));
  rows.push(csvRow(['Current Value', formatNum(results.currentValue)]));
  rows.push(csvRow(['Absolute Gain', formatNum(results.absoluteGain)]));
  rows.push(csvRow(['Percent Return', formatPct(results.percentReturn)]));
  rows.push(csvRow(['CAGR', formatPct(results.cagr)]));
  rows.push('');

  // --- Holdings Section ---
  if (results.holdings.length > 0) {
    rows.push('Holdings');
    rows.push(csvRow([
      'Symbol', 'Name', 'Invested', 'Current Value', 'Shares',
      'Purchase Price', 'Current Price', 'Gain/Loss', 'Return %',
    ]));
    for (const h of results.holdings) {
      rows.push(csvRow([
        h.symbol,
        h.name,
        formatNum(h.invested),
        formatNum(h.currentValue),
        h.shares.toFixed(4),
        formatNum(h.purchasePrice),
        formatNum(h.currentPrice),
        formatNum(h.absoluteGain),
        formatPct(h.percentReturn),
      ]));
    }
    rows.push('');
  }

  // --- Timeline Section ---
  const hasHoldingsTimelines =
    results.holdingsTimelines && Object.keys(results.holdingsTimelines).length > 0;
  const holdingSymbols = hasHoldingsTimelines
    ? Object.keys(results.holdingsTimelines!)
    : [];

  const timelineHeaders = ['Date', 'Portfolio Value'];
  if (results.benchmarkTimeline && results.benchmarkTimeline.length > 0) {
    timelineHeaders.push('S&P 500 Benchmark');
  }
  for (const sym of holdingSymbols) {
    timelineHeaders.push(sym);
  }

  rows.push('Timeline');
  rows.push(csvRow(timelineHeaders));

  // Build a lookup map for benchmark and per-holding data keyed by date
  const benchmarkMap = new Map<string, number>();
  if (results.benchmarkTimeline) {
    for (const pt of results.benchmarkTimeline) {
      benchmarkMap.set(pt.date, pt.value);
    }
  }

  const holdingMaps = new Map<string, Map<string, number>>();
  if (hasHoldingsTimelines) {
    for (const [sym, points] of Object.entries(results.holdingsTimelines!)) {
      const m = new Map<string, number>();
      for (const pt of points) {
        m.set(pt.date, pt.value);
      }
      holdingMaps.set(sym, m);
    }
  }

  for (const pt of results.timeline) {
    const cols: string[] = [pt.date, formatNum(pt.value)];
    if (results.benchmarkTimeline && results.benchmarkTimeline.length > 0) {
      const bv = benchmarkMap.get(pt.date);
      cols.push(bv !== undefined ? formatNum(bv) : '');
    }
    for (const sym of holdingSymbols) {
      const hv = holdingMaps.get(sym)?.get(pt.date);
      cols.push(hv !== undefined ? formatNum(hv) : '');
    }
    rows.push(csvRow(cols));
  }

  return BOM + rows.join('\r\n') + '\r\n';
}

/**
 * Trigger a file download in the browser from a string.
 */
export function downloadCSV(csvContent: string, filename: string): void {
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.style.display = 'none';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

/**
 * Build a default filename: moshimo-simulation-YYYY-MM-DD.csv
 */
export function buildExportFilename(): string {
  const today = new Date().toISOString().split('T')[0];
  return `moshimo-simulation-${today}.csv`;
}

// --- Helpers ---

/** Escape a CSV field: wrap in quotes if it contains commas, quotes, or newlines. */
function escapeField(value: string): string {
  if (value.includes(',') || value.includes('"') || value.includes('\n') || value.includes('\r')) {
    return '"' + value.replace(/"/g, '""') + '"';
  }
  return value;
}

/** Join an array of strings into a CSV row with proper escaping. */
function csvRow(fields: string[]): string {
  return fields.map(escapeField).join(',');
}

/** Format a number to 2 decimal places. */
function formatNum(n: number): string {
  return n.toFixed(2);
}

/** Format a percentage value. */
function formatPct(n: number): string {
  return n.toFixed(2) + '%';
}
