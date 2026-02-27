import { useState } from 'react';
import { portfolioApi } from '../services/api/portfolioApi';
import type { SimulationRequest, SimulationResponse } from '../types/api.types';

interface ToastState {
  message: string;
  type: 'success' | 'error';
}

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
 */
export function useSimulation(): SimulationHookResult {
  const [isSimulating, setIsSimulating] = useState(false);
  const [simulationResults, setSimulationResults] = useState<SimulationResponse | null>(null);
  const [simulationError, setSimulationError] = useState<string | null>(null);
  const [timeframe, setTimeframe] = useState<string>('ALL');
  const [lastRequest, setLastRequest] = useState<SimulationRequest | null>(null);
  const [toast, setToast] = useState<ToastState | null>(null);

  const handleSimulate = async (request: SimulationRequest) => {
    try {
      setIsSimulating(true);
      setSimulationError(null);
      setLastRequest(request);

      const results = await portfolioApi.simulate(request, timeframe);
      setSimulationResults(results);

      setToast({
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
      setSimulationResults(null);
    } finally {
      setIsSimulating(false);
    }
  };

  const handleTimeframeChange = async (newTimeframe: string) => {
    setTimeframe(newTimeframe);

    if (lastRequest) {
      try {
        setIsSimulating(true);
        setSimulationError(null);
        // Clear stale results so the loading spinner shows immediately
        setSimulationResults(null);

        const results = await portfolioApi.simulate(lastRequest, newTimeframe);
        setSimulationResults(results);
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

  const clearToast = () => setToast(null);

  return {
    isSimulating,
    simulationResults,
    simulationError,
    timeframe,
    lastRequest,
    toast,
    clearToast,
    handleSimulate,
    handleTimeframeChange,
  };
}
