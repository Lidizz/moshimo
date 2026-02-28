import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { InvestmentBuilder } from '../components/InvestmentBuilder';
import { SimulationProvider } from '../context/SimulationContext';
import type { Asset } from '../types/api.types';
import type { ReactNode } from 'react';

// ── Wrapper that provides required context ─────────────────────────────

function Wrapper({ children }: { children: ReactNode }) {
  return <SimulationProvider>{children}</SimulationProvider>;
}

// ── Test fixtures ───────────────────────────────────────────────────────

const mockAssets: Asset[] = [
  {
    id: 1,
    symbol: 'AAPL',
    name: 'Apple Inc.',
    assetType: 'STOCK',
    sector: 'Technology',
    industry: 'Consumer Electronics',
    exchange: 'NASDAQ',
    ipoDate: '1980-12-12',
    isActive: true,
  },
  {
    id: 2,
    symbol: 'MSFT',
    name: 'Microsoft Corporation',
    assetType: 'STOCK',
    sector: 'Technology',
    industry: 'Software',
    exchange: 'NASDAQ',
    ipoDate: '1986-03-13',
    isActive: true,
  },
];

// ── Tests ───────────────────────────────────────────────────────────────

describe('InvestmentBuilder', () => {
  it('renders the header and one empty investment form by default', () => {
    const onSimulate = vi.fn();
    render(
      <InvestmentBuilder assets={mockAssets} onSimulate={onSimulate} isSimulating={false} />,
      { wrapper: Wrapper },
    );

    expect(screen.getByText('Build Your Portfolio')).toBeInTheDocument();
    expect(screen.getByText(/Add up to 10 investments/i)).toBeInTheDocument();
    // Should show "0 of 1 investments valid" since the form is empty
    expect(screen.getByText(/0 of 1 investments valid/i)).toBeInTheDocument();
  });

  it('shows "Add Another Investment" button', () => {
    const onSimulate = vi.fn();
    render(
      <InvestmentBuilder assets={mockAssets} onSimulate={onSimulate} isSimulating={false} />,
      { wrapper: Wrapper },
    );

    const addBtn = screen.getByText('+ Add Another Investment');
    expect(addBtn).toBeInTheDocument();
    expect(addBtn).not.toBeDisabled();
  });

  it('adds a second investment form when "Add Another Investment" is clicked', () => {
    const onSimulate = vi.fn();
    render(
      <InvestmentBuilder assets={mockAssets} onSimulate={onSimulate} isSimulating={false} />,
      { wrapper: Wrapper },
    );

    fireEvent.click(screen.getByText('+ Add Another Investment'));

    // Now should say "0 of 2 investments valid"
    expect(screen.getByText(/0 of 2 investments valid/i)).toBeInTheDocument();
  });

  it('disables simulate button when no valid investments', () => {
    const onSimulate = vi.fn();
    render(
      <InvestmentBuilder assets={mockAssets} onSimulate={onSimulate} isSimulating={false} />,
      { wrapper: Wrapper },
    );

    const simBtn = screen.getByRole('button', { name: /simulate/i });
    expect(simBtn).toBeDisabled();
  });

  it('disables simulate button while simulating', () => {
    const onSimulate = vi.fn();
    render(
      <InvestmentBuilder assets={mockAssets} onSimulate={onSimulate} isSimulating={true} />,
      { wrapper: Wrapper },
    );

    const simBtn = screen.getByRole('button', { name: /simulating/i });
    expect(simBtn).toBeDisabled();
  });

  it('shows "Simulating..." text when isSimulating is true', () => {
    const onSimulate = vi.fn();
    render(
      <InvestmentBuilder assets={mockAssets} onSimulate={onSimulate} isSimulating={true} />,
      { wrapper: Wrapper },
    );

    expect(screen.getByText(/Simulating\.\.\./i)).toBeInTheDocument();
  });

  it('does not call onSimulate when button clicked with no valid investments', () => {
    const onSimulate = vi.fn();
    render(
      <InvestmentBuilder assets={mockAssets} onSimulate={onSimulate} isSimulating={false} />,
      { wrapper: Wrapper },
    );

    const simBtn = screen.getByRole('button', { name: /simulate/i });
    fireEvent.click(simBtn);

    expect(onSimulate).not.toHaveBeenCalled();
  });
});
