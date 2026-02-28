import { useContext } from 'react';
import { SimulationContext } from '../context/SimulationContext';
import type { SimulationContextValue } from '../context/SimulationContext';

/**
 * Convenience hook for consuming SimulationContext.
 * Throws if used outside <SimulationProvider>.
 */
export function useSimulationContext(): SimulationContextValue {
  const ctx = useContext(SimulationContext);
  if (!ctx) {
    throw new Error(
      'useSimulationContext must be used within a <SimulationProvider>',
    );
  }
  return ctx;
}
