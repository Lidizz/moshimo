import apiClient from './apiClient';
import type { HealthResponse } from '../../types/api.types';

/**
 * Health check API service.
 */
export const healthApi = {
  /**
   * Check backend health status.
   */
  checkHealth: async (): Promise<HealthResponse> => {
    const response = await apiClient.get<HealthResponse>('/health');
    return response.data;
  },
};