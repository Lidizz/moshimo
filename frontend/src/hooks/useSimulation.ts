import { useState } from 'react';
import { portfolioApi } from '../services/api/portfolioApi';
import type { SimulationRequest, SimulationResponse } from '../types/api.types';
import { useSimulationContext } from './useSimulationContext';
import type { ToastState } from '../context/SimulationContext';

interface SimulationHookResult {
  isSimulating: boolean;
  simulationResults: SimulationResponse | null;
  simulationError: string | null;
  timeframe: string;
  lastRequest: SimulationRequest | null;
  toast: ToastState | null;
  clearToast: () => void;
  handleSimulate: (request: SimulationRequest) => Promise<void>;
  handleTimeframeChange: (newTimeframe: string) => Promise<void>;
}

/**
 * Manages the full simulation lifecycle: running a simulation,
 * changing timeframes, and tracking toast / error state.
 *
 * Persistent state (results, request, timeframe) is stored in
 * SimulationContext and survives navigation / page refresh.
 * Transient state (isSimulating, simulationError) stays local.
 */
export function useSimulation(): SimulationHookResult {
  const ctx = useSimulationContext();

  // ── Transient (local) state ─────────────────────────────────────
  const [isSimulating, setIsSimulating] = useState(false);
  const [simulationError, setSimulationError] = useState<string | null>(null);

  const handleSimulate = async (request: SimulationRequest) => {
    try {
      setIsSimulating(true);
      setSimulationError(null);
      ctx.setLastRequest(request);

      const results = await portfolioApi.simulate(request, ctx.timeframe);
      ctx.setSimulationResults(results);

      ctx.setToast({
        message: '🎉 Simulation complete! Check out your results below.',
        type: 'success',
      });

      // Scroll to results
      setTimeout(() => {
        document.getElementById('results')?.scrollIntoView({
          behavior: 'smooth',
          block: 'start',
        });
      }, 100);
    } catch (err: any) {
      setSimulationError(
        err.response?.data?.message ||
          err.message ||
          'Simulation failed. Please check your inputs and try again.',
      );
      ctx.setSimulationResults(null);
    } finally {
      setIsSimulating(false);
    }
  };

  const handleTimeframeChange = async (newTimeframe: string) => {
    ctx.setTimeframe(newTimeframe);

    if (ctx.lastRequest) {
      try {
        setIsSimulating(true);
        setSimulationError(null);
        // Clear stale results so the loading spinner shows immediately
        ctx.setSimulationResults(null);

        const results = await portfolioApi.simulate(ctx.lastRequest, newTimeframe);
        ctx.setSimulationResults(results);
      } catch (err: any) {
        setSimulationError(
          err.response?.data?.message ||
            err.message ||
            'Failed to update timeframe',
        );
      } finally {
        setIsSimulating(false);
      }
    }
  };

  const clearToast = () => ctx.setToast(null);

  return {
    isSimulating,
    simulationResults: ctx.simulationResults,
    simulationError,
    timeframe: ctx.timeframe,
    lastRequest: ctx.lastRequest,
    toast: ctx.toast,
    clearToast,
    handleSimulate,
    handleTimeframeChange,
  };
}
