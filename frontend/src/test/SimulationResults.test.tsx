import { describe, it, expect, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import { SimulationResults } from '../components/SimulationResults';
import type { SimulationResponse } from '../types/api.types';

// Mock PortfolioChart — uses canvas-based lightweight-charts that won't work in jsdom
vi.mock('../components/PortfolioChart', () => ({
  PortfolioChart: () => <div data-testid="portfolio-chart">Chart</div>,
}));

// Mock PortfolioHeader — keeps test focused on SimulationResults logic
vi.mock('../components/PortfolioHeader', () => ({
  PortfolioHeader: () => <div data-testid="portfolio-header">Header</div>,
}));

// ── Test fixtures ───────────────────────────────────────────────────────

const profitResults: SimulationResponse = {
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
      invested: 5000,
      currentValue: 8000,
      shares: 33.3333,
      purchasePrice: 150,
      currentPrice: 240,
      absoluteGain: 3000,
      percentReturn: 60,
    },
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
    { date: '2024-01-01', value: 14000 },
  ],
};

const lossResults: SimulationResponse = {
  totalInvested: 10000,
  currentValue: 8000,
  absoluteGain: -2000,
  percentReturn: -20,
  cagr: -10.56,
  timeline: [
    { date: '2023-01-01', value: 10000 },
    { date: '2024-01-01', value: 8000 },
  ],
  holdings: [
    {
      symbol: 'INTC',
      name: 'Intel Corporation',
      invested: 10000,
      currentValue: 8000,
      shares: 200,
      purchasePrice: 50,
      currentPrice: 40,
      absoluteGain: -2000,
      percentReturn: -20,
    },
  ],
};

const investments = [
  { symbol: 'AAPL', amountUsd: 5000, purchaseDate: '2020-01-01' },
  { symbol: 'MSFT', amountUsd: 5000, purchaseDate: '2020-01-01' },
];

// ── Tests ───────────────────────────────────────────────────────────────

describe('SimulationResults', () => {
  describe('Metric cards', () => {
    it('renders all 5 metric cards for profitable results', () => {
      render(<SimulationResults results={profitResults} investments={investments} />);

      const metrics = document.querySelector('.simulation-results__metrics')!;
      const metricsEl = within(metrics as HTMLElement);

      expect(metricsEl.getByText('Total Invested')).toBeInTheDocument();
      expect(metricsEl.getByText('Current Value')).toBeInTheDocument();
      expect(metricsEl.getByText('Absolute Gain')).toBeInTheDocument();
      expect(metricsEl.getByText('Percent Return')).toBeInTheDocument();
      expect(metricsEl.getByText('CAGR')).toBeInTheDocument();
    });

    it('displays formatted currency values', () => {
      render(<SimulationResults results={profitResults} investments={investments} />);

      const metrics = document.querySelector('.simulation-results__metrics')!;
      const metricsEl = within(metrics as HTMLElement);

      // Total Invested: $10,000.00
      expect(metricsEl.getByText('$10,000.00')).toBeInTheDocument();
      // Current Value: $15,000.00
      expect(metricsEl.getByText('$15,000.00')).toBeInTheDocument();
      // Absolute Gain: $5,000.00
      expect(metricsEl.getByText('$5,000.00')).toBeInTheDocument();
    });

    it('displays formatted percentage values', () => {
      render(<SimulationResults results={profitResults} investments={investments} />);

      const metrics = document.querySelector('.simulation-results__metrics')!;
      const metricsEl = within(metrics as HTMLElement);

      // Percent Return: +50.00%
      expect(metricsEl.getByText('+50.00%')).toBeInTheDocument();
      // CAGR: +14.87%
      expect(metricsEl.getByText('+14.87%')).toBeInTheDocument();
    });
  });

  describe('Holdings table', () => {
    it('renders holdings table with correct headers', () => {
      render(<SimulationResults results={profitResults} investments={investments} />);

      const table = document.querySelector('.holdings-table')!;
      const tableEl = within(table as HTMLElement);

      expect(tableEl.getByText('Holdings Breakdown')).toBeInTheDocument();
      expect(tableEl.getByText('Asset')).toBeInTheDocument();
      expect(tableEl.getByText('Invested')).toBeInTheDocument();
      expect(tableEl.getByText('Shares')).toBeInTheDocument();
      expect(tableEl.getByText('Purchase Price')).toBeInTheDocument();
      expect(tableEl.getByText('Current Price')).toBeInTheDocument();
      expect(tableEl.getByText('Current Value')).toBeInTheDocument();
      expect(tableEl.getByText('Gain/Loss')).toBeInTheDocument();
      expect(tableEl.getByText('Return %')).toBeInTheDocument();
    });

    it('renders a row for each holding', () => {
      render(<SimulationResults results={profitResults} investments={investments} />);

      expect(screen.getByText('AAPL')).toBeInTheDocument();
      expect(screen.getByText('Apple Inc.')).toBeInTheDocument();
      expect(screen.getByText('MSFT')).toBeInTheDocument();
      expect(screen.getByText('Microsoft Corporation')).toBeInTheDocument();
    });

    it('formats share counts with 4 decimal places', () => {
      render(<SimulationResults results={profitResults} investments={investments} />);

      expect(screen.getByText('33.3333')).toBeInTheDocument();
      expect(screen.getByText('16.6667')).toBeInTheDocument();
    });
  });

  describe('Loss scenario', () => {
    it('renders negative gain and return correctly', () => {
      render(<SimulationResults results={lossResults} />);

      // -$2,000.00 appears in both metric card and holdings row
      const allLossValues = screen.getAllByText('-$2,000.00');
      expect(allLossValues.length).toBe(2); // metric card + table row
    });

    it('renders single holding with loss data', () => {
      render(<SimulationResults results={lossResults} />);

      expect(screen.getByText('INTC')).toBeInTheDocument();
      expect(screen.getByText('Intel Corporation')).toBeInTheDocument();
    });
  });

  describe('Chart and header integration', () => {
    it('renders the mocked PortfolioChart', () => {
      render(<SimulationResults results={profitResults} investments={investments} />);

      expect(screen.getByTestId('portfolio-chart')).toBeInTheDocument();
    });

    it('renders the mocked PortfolioHeader when investments provided', () => {
      render(<SimulationResults results={profitResults} investments={investments} />);

      expect(screen.getByTestId('portfolio-header')).toBeInTheDocument();
    });
  });

  describe('Data disclaimer', () => {
    it('shows the data freshness disclaimer', () => {
      render(<SimulationResults results={profitResults} investments={investments} />);

      expect(screen.getByText(/Historical price data updated monthly/i)).toBeInTheDocument();
    });
  });
});
