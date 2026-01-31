import apiClient from './apiClient';
import type { SimulationRequest, SimulationResponse } from '../../types/api.types';

/**
 * Portfolio simulation API service.
 */
export const portfolioApi = {
  /**
   * Run investment simulation with optional timeframe parameter.
   * 
   * Timeframe controls how densely the timeline is sampled across
   * the ENTIRE investment period (not just recent days):
   * - '1D': Daily points (every trading day)
   * - '1W': Weekly points (first trading day of each week)
   * - '1M': Monthly points (first trading day of each month)
   * - '1Y': Yearly points (first trading day of January)
   * - 'ALL': Smart sampling (~500 points, default)
   * 
   * @param request Investment details (stocks, amounts, dates)
   * @param timeframe Timeline sampling granularity (default: 'ALL')
   * @returns Simulation results with timeline, metrics, and holdings
   */
  simulate: async (
    request: SimulationRequest,
    timeframe: string = 'ALL'
  ): Promise<SimulationResponse> => {
    const requestWithTimeframe = {
      ...request,
      timeframe,
    };
    
    const response = await apiClient.post<SimulationResponse>(
      '/portfolio/simulate',
      requestWithTimeframe
    );
    return response.data;
  },
};