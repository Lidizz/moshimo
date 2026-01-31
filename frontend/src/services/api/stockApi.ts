import apiClient from './apiClient';
import type { Stock, PriceData, AssetType } from '../../types/api.types';

/**
 * Stock data API service.
 */
export const stockApi = {
  /**
   * Get all active stocks with optional filtering.
   * 
   * @param type - Optional asset type filter (STOCK, ETF, INDEX)
   * @param sector - Optional sector filter
   */
  getAllStocks: async (type?: AssetType, sector?: string): Promise<Stock[]> => {
    const params: Record<string, string> = {};
    if (type) params.type = type;
    if (sector) params.sector = sector;
    
    const response = await apiClient.get<Stock[]>('/stocks', { params });
    return response.data;
  },

  /**
   * Get stock by symbol.
   */
  getStockBySymbol: async (symbol: string): Promise<Stock> => {
    const response = await apiClient.get<Stock>(`/stocks/${symbol}`);
    return response.data;
  },

  /**
   * Get available sectors.
   */
  getSectors: async (): Promise<string[]> => {
    const response = await apiClient.get<string[]>('/stocks/sectors');
    return response.data;
  },

  /**
   * Get historical prices for a stock.
   */
  getPriceHistory: async (
    symbol: string,
    fromDate: string,
    toDate: string
  ): Promise<PriceData[]> => {
    const response = await apiClient.get<PriceData[]>(
      `/stocks/${symbol}/prices`,
      {
        params: { from: fromDate, to: toDate },
      }
    );
    return response.data;
  },
};