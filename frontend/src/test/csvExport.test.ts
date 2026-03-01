import { describe, it, expect } from 'vitest';
import { generatePortfolioCSV, buildExportFilename } from '../utils/csvExport';
import type { SimulationResponse } from '../types/api.types';

// ── Test fixtures ───────────────────────────────────────────────────────

const minimalResults: SimulationResponse = {
  totalInvested: 10000,
  currentValue: 15000,
  absoluteGain: 5000,
  percentReturn: 50,
  cagr: 14.87,
  timeline: [
    { date: '2020-01-01', value: 10000 },
    { date: '2024-01-01', value: 15000 },
  ],
  holdings: [
    {
      symbol: 'AAPL',
      name: 'Apple Inc.',
      invested: 10000,
      currentValue: 15000,
      shares: 66.6667,
      purchasePrice: 150,
      currentPrice: 225,
      absoluteGain: 5000,
      percentReturn: 50,
    },
  ],
};

const multiHoldingResults: SimulationResponse = {
  ...minimalResults,
  holdings: [
    ...minimalResults.holdings,
    {
      symbol: 'MSFT',
      name: 'Microsoft Corporation',
      invested: 5000,
      currentValue: 7000,
      shares: 16.6667,
      purchasePrice: 300,
      currentPrice: 420,
      absoluteGain: 2000,
      percentReturn: 40,
    },
  ],
  benchmarkTimeline: [
    { date: '2020-01-01', value: 10000 },
    { date: '2024-01-01', value: 13000 },
  ],
  holdingsTimelines: {
    AAPL: [
      { date: '2020-01-01', value: 5000 },
      { date: '2024-01-01', value: 8000 },
    ],
    MSFT: [
      { date: '2020-01-01', value: 5000 },
      { date: '2024-01-01', value: 7000 },
    ],
  },
};

// ── Tests ───────────────────────────────────────────────────────────────

describe('generatePortfolioCSV', () => {
  it('starts with UTF-8 BOM', () => {
    const csv = generatePortfolioCSV(minimalResults);
    expect(csv.charCodeAt(0)).toBe(0xFEFF);
  });

  it('contains summary section with correct values', () => {
    const csv = generatePortfolioCSV(minimalResults);
    expect(csv).toContain('Summary');
    expect(csv).toContain('Total Invested,10000.00');
    expect(csv).toContain('Current Value,15000.00');
    expect(csv).toContain('Absolute Gain,5000.00');
    expect(csv).toContain('Percent Return,50.00%');
    expect(csv).toContain('CAGR,14.87%');
  });

  it('contains holdings section with all columns', () => {
    const csv = generatePortfolioCSV(minimalResults);
    expect(csv).toContain('Holdings');
    expect(csv).toContain('Symbol,Name,Invested,Current Value,Shares');
    expect(csv).toContain('AAPL,Apple Inc.,10000.00,15000.00,66.6667,150.00,225.00,5000.00,50.00%');
  });

  it('contains timeline section', () => {
    const csv = generatePortfolioCSV(minimalResults);
    expect(csv).toContain('Timeline');
    expect(csv).toContain('Date,Portfolio Value');
    expect(csv).toContain('2020-01-01,10000.00');
    expect(csv).toContain('2024-01-01,15000.00');
  });

  it('includes benchmark column when benchmarkTimeline is present', () => {
    const csv = generatePortfolioCSV(multiHoldingResults);
    expect(csv).toContain('Date,Portfolio Value,S&P 500 Benchmark');
    expect(csv).toContain('2020-01-01,10000.00,10000.00');
    expect(csv).toContain('2024-01-01,15000.00,13000.00');
  });

  it('includes per-holding columns when holdingsTimelines is present', () => {
    const csv = generatePortfolioCSV(multiHoldingResults);
    expect(csv).toContain('Date,Portfolio Value,S&P 500 Benchmark,AAPL,MSFT');
    // Check first row has per-holding values
    const lines = csv.split('\r\n');
    const firstDataLine = lines.find(l => l.startsWith('2020-01-01'));
    expect(firstDataLine).toContain('5000.00');
  });

  it('handles multiple holdings in the holdings section', () => {
    const csv = generatePortfolioCSV(multiHoldingResults);
    expect(csv).toContain('AAPL');
    expect(csv).toContain('MSFT');
    expect(csv).toContain('Microsoft Corporation');
  });

  it('escapes fields containing commas', () => {
    const results: SimulationResponse = {
      ...minimalResults,
      holdings: [{
        ...minimalResults.holdings[0],
        name: 'Berkshire Hathaway, Inc.',
      }],
    };
    const csv = generatePortfolioCSV(results);
    expect(csv).toContain('"Berkshire Hathaway, Inc."');
  });

  it('escapes fields containing double quotes', () => {
    const results: SimulationResponse = {
      ...minimalResults,
      holdings: [{
        ...minimalResults.holdings[0],
        name: 'Company "X" Ltd',
      }],
    };
    const csv = generatePortfolioCSV(results);
    expect(csv).toContain('"Company ""X"" Ltd"');
  });

  it('uses CRLF line endings', () => {
    const csv = generatePortfolioCSV(minimalResults);
    // Remove BOM, check that lines end with \r\n
    const content = csv.slice(1);
    const lines = content.split('\r\n');
    expect(lines.length).toBeGreaterThan(5);
    // Should not have bare \n without \r
    expect(content.replace(/\r\n/g, '')).not.toContain('\n');
  });

  it('handles empty holdings gracefully', () => {
    const results: SimulationResponse = {
      ...minimalResults,
      holdings: [],
    };
    const csv = generatePortfolioCSV(results);
    expect(csv).toContain('Summary');
    expect(csv).toContain('Timeline');
    // Holdings header should NOT appear
    expect(csv).not.toContain('Symbol,Name');
  });

  it('handles results without benchmark or holdingsTimelines', () => {
    const results: SimulationResponse = {
      ...minimalResults,
      benchmarkTimeline: undefined,
      holdingsTimelines: undefined,
    };
    const csv = generatePortfolioCSV(results);
    expect(csv).toContain('Date,Portfolio Value');
    expect(csv).not.toContain('S&P 500 Benchmark');
    expect(csv).not.toContain('AAPL,MSFT');
  });
});

describe('buildExportFilename', () => {
  it('returns a filename with today\'s date', () => {
    const filename = buildExportFilename();
    const today = new Date().toISOString().split('T')[0];
    expect(filename).toBe(`moshimo-simulation-${today}.csv`);
  });

  it('ends with .csv extension', () => {
    const filename = buildExportFilename();
    expect(filename).toMatch(/\.csv$/);
  });
});
