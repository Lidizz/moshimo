import { createContext, useState, useCallback } from 'react';
import type { ReactNode } from 'react';
import { usePersistedState } from '../hooks/usePersistedState';
import type {
  Investment,
  SimulationRequest,
  SimulationResponse,
} from '../types/api.types';

// ── Toast type (shared with useSimulation) ──────────────────────────
export interface ToastState {
  message: string;
  type: 'success' | 'error';
}

// ── Context value ───────────────────────────────────────────────────
export interface SimulationContextValue {
  /** Current investment rows in the builder form */
  investments: Investment[];
  setInvestments: (investments: Investment[]) => void;

  /** Latest simulation response (null before first run) */
  simulationResults: SimulationResponse | null;
  setSimulationResults: (results: SimulationResponse | null) => void;

  /** The request that produced the current results */
  lastRequest: SimulationRequest | null;
  setLastRequest: (request: SimulationRequest | null) => void;

  /** Selected chart timeframe */
  timeframe: string;
  setTimeframe: (tf: string) => void;

  /** Toast notification */
  toast: ToastState | null;
  setToast: (toast: ToastState | null) => void;

  /** Convenience: clear everything for a fresh start */
  clearResults: () => void;
}

// sentinel value — overwritten by the provider
export const SimulationContext = createContext<SimulationContextValue | null>(null);

// ── Helper: create a blank investment row ───────────────────────────
function createEmptyInvestment(): Investment {
  return {
    id: crypto.randomUUID(),
    symbol: '',
    amountUsd: 0,
    purchaseDate: '',
  };
}

// ── Provider ────────────────────────────────────────────────────────
interface SimulationProviderProps {
  children: ReactNode;
}

export function SimulationProvider({ children }: SimulationProviderProps) {
  // ── Persisted state (survives navigation + page refresh) ────────
  const [investments, setInvestments] = usePersistedState<Investment[]>(
    'moshimo:investments',
    [createEmptyInvestment()],
  );
  const [simulationResults, setSimulationResults] =
    usePersistedState<SimulationResponse | null>('moshimo:results', null);
  const [lastRequest, setLastRequest] =
    usePersistedState<SimulationRequest | null>('moshimo:lastRequest', null);
  const [timeframe, setTimeframe] = usePersistedState<string>(
    'moshimo:timeframe',
    'ALL',
  );

  // ── Transient state (ephemeral — not persisted) ─────────────────
  const [toast, setToast] = useState<ToastState | null>(null);

  const clearResults = useCallback(() => {
    setSimulationResults(null);
    setLastRequest(null);
    setTimeframe('ALL');
  }, []);

  const value: SimulationContextValue = {
    investments,
    setInvestments,
    simulationResults,
    setSimulationResults,
    lastRequest,
    setLastRequest,
    timeframe,
    setTimeframe,
    toast,
    setToast,
    clearResults,
  };

  return (
    <SimulationContext.Provider value={value}>
      {children}
    </SimulationContext.Provider>
  );
}
