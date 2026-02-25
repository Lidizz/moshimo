import apiClient from './apiClient';
import type { Asset, PriceData, AssetType } from '../../types/api.types';

/**
 * Asset data API service.
 */
export const assetApi = {
  /**
   * Get all active assets with optional filtering.
   * 
   * @param type - Optional asset type filter (STOCK, ETF, INDEX, CRYPTO)
   * @param sector - Optional sector filter
   */
  getAllAssets: async (type?: AssetType, sector?: string): Promise<Asset[]> => {
    const params: Record<string, string> = {};
    if (type) params.type = type;
    if (sector) params.sector = sector;
    
    const response = await apiClient.get<Asset[]>('/assets', { params });
    return response.data;
  },

  /**
   * Get asset by symbol.
   */
  getAssetBySymbol: async (symbol: string): Promise<Asset> => {
    const response = await apiClient.get<Asset>(`/assets/${symbol}`);
    return response.data;
  },

  /**
   * Get available sectors.
   */
  getSectors: async (): Promise<string[]> => {
    const response = await apiClient.get<string[]>('/assets/sectors');
    return response.data;
  },

  /**
   * Get historical prices for an asset.
   */
  getPriceHistory: async (
    symbol: string,
    fromDate: string,
    toDate: string
  ): Promise<PriceData[]> => {
    const response = await apiClient.get<PriceData[]>(
      `/assets/${symbol}/prices`,
      {
        params: { from: fromDate, to: toDate },
      }
    );
    return response.data;
  },
};