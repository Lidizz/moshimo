import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { SimulationProvider } from '../context/SimulationContext';
import { useSimulationContext } from '../hooks/useSimulationContext';
import type { SimulationResponse, SimulationRequest } from '../types/api.types';
import type { ReactNode } from 'react';

// ── Wrapper ─────────────────────────────────────────────────────────────

function wrapper({ children }: { children: ReactNode }) {
  return <SimulationProvider>{children}</SimulationProvider>;
}

// ── Fixtures ────────────────────────────────────────────────────────────

const mockResults: SimulationResponse = {
  totalInvested: 10000,
  currentValue: 15000,
  absoluteGain: 5000,
  percentReturn: 50,
  cagr: 12.5,
  timeline: [
    { date: '2023-01-01', value: 10000 },
    { date: '2024-01-01', value: 15000 },
  ],
  holdings: [
    {
      symbol: 'AAPL',
      name: 'Apple Inc.',
      invested: 10000,
      currentValue: 15000,
      shares: 50,
      purchasePrice: 200,
      currentPrice: 300,
      absoluteGain: 5000,
      percentReturn: 50,
    },
  ],
};

const mockRequest: SimulationRequest = {
  investments: [
    { symbol: 'AAPL', amountUsd: 10000, purchaseDate: '2023-01-01' },
  ],
};

// ── Tests ───────────────────────────────────────────────────────────────

beforeEach(() => {
  sessionStorage.clear();
});

describe('SimulationContext', () => {
  it('provides default state', () => {
    const { result } = renderHook(() => useSimulationContext(), { wrapper });

    expect(result.current.investments).toHaveLength(1);
    expect(result.current.investments[0].symbol).toBe('');
    expect(result.current.simulationResults).toBeNull();
    expect(result.current.lastRequest).toBeNull();
    expect(result.current.timeframe).toBe('ALL');
    expect(result.current.toast).toBeNull();
  });

  it('throws when used outside SimulationProvider', () => {
    expect(() => {
      renderHook(() => useSimulationContext());
    }).toThrow('useSimulationContext must be used within a <SimulationProvider>');
  });

  it('updates simulationResults', () => {
    const { result } = renderHook(() => useSimulationContext(), { wrapper });

    act(() => {
      result.current.setSimulationResults(mockResults);
    });

    expect(result.current.simulationResults).toEqual(mockResults);
  });

  it('updates lastRequest', () => {
    const { result } = renderHook(() => useSimulationContext(), { wrapper });

    act(() => {
      result.current.setLastRequest(mockRequest);
    });

    expect(result.current.lastRequest).toEqual(mockRequest);
  });

  it('updates timeframe', () => {
    const { result } = renderHook(() => useSimulationContext(), { wrapper });

    act(() => {
      result.current.setTimeframe('1Y');
    });

    expect(result.current.timeframe).toBe('1Y');
  });

  it('updates investments', () => {
    const { result } = renderHook(() => useSimulationContext(), { wrapper });

    const newInvestments = [
      { id: 'test-id', symbol: 'MSFT', amountUsd: 5000, purchaseDate: '2023-06-01' },
    ];
    act(() => {
      result.current.setInvestments(newInvestments);
    });

    expect(result.current.investments).toEqual(newInvestments);
  });

  it('clearResults resets results, lastRequest, and timeframe', () => {
    const { result } = renderHook(() => useSimulationContext(), { wrapper });

    // Set some state first
    act(() => {
      result.current.setSimulationResults(mockResults);
      result.current.setLastRequest(mockRequest);
      result.current.setTimeframe('1Y');
    });

    // Clear
    act(() => {
      result.current.clearResults();
    });

    expect(result.current.simulationResults).toBeNull();
    expect(result.current.lastRequest).toBeNull();
    expect(result.current.timeframe).toBe('ALL');
  });

  it('persists state to sessionStorage', () => {
    const { result } = renderHook(() => useSimulationContext(), { wrapper });

    act(() => {
      result.current.setSimulationResults(mockResults);
      result.current.setTimeframe('5Y');
    });

    // Verify sessionStorage has the values
    expect(JSON.parse(sessionStorage.getItem('moshimo:results')!)).toEqual(mockResults);
    expect(JSON.parse(sessionStorage.getItem('moshimo:timeframe')!)).toBe('5Y');
  });

  it('restores state from sessionStorage on remount', () => {
    // Pre-populate sessionStorage as if user navigated away
    sessionStorage.setItem('moshimo:results', JSON.stringify(mockResults));
    sessionStorage.setItem('moshimo:lastRequest', JSON.stringify(mockRequest));
    sessionStorage.setItem('moshimo:timeframe', JSON.stringify('1Y'));

    const { result } = renderHook(() => useSimulationContext(), { wrapper });

    expect(result.current.simulationResults).toEqual(mockResults);
    expect(result.current.lastRequest).toEqual(mockRequest);
    expect(result.current.timeframe).toBe('1Y');
  });
});
