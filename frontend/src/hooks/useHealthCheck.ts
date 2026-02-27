import { useEffect, useState } from 'react';
import { healthApi } from '../services/api/healthApi';
import { assetApi } from '../services/api/assetApi';
import type { HealthResponse, Asset } from '../types/api.types';

interface HealthCheckResult {
  health: HealthResponse | null;
  assets: Asset[];
  loading: boolean;
  error: string | null;
}

/**
 * Fetches backend health status and asset catalogue on mount.
 * Encapsulates the initial data-loading concern so SimulatorPage
 * only deals with rendering.
 */
export function useHealthCheck(): HealthCheckResult {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);

        const [healthData, assetsData] = await Promise.all([
          healthApi.checkHealth(),
          assetApi.getAllAssets(),
        ]);

        setHealth(healthData);
        setAssets(assetsData);
      } catch (err: any) {
        setError(err.message || 'Failed to fetch data');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  return { health, assets, loading, error };
}
