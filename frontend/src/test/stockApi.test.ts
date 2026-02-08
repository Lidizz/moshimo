import { describe, it, expect, vi, beforeEach } from 'vitest';
import { stockApi } from '../services/api/stockApi';
import apiClient from '../services/api/apiClient';
import type { Stock, AssetType } from '../types/api.types';

// Mock the apiClient
vi.mock('../services/api/apiClient', () => ({
  default: {
    get: vi.fn()
  }
}));

describe('Stock API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAllStocks', () => {
    it('fetches all stocks without filters', async () => {
      const mockStocks: Stock[] = [
        {
          id: 1,
          symbol: 'AAPL',
          name: 'Apple Inc.',
          assetType: 'STOCK' as AssetType,
          sector: 'Technology',
          industry: null,
          exchange: 'NASDAQ',
          ipoDate: '1980-12-12',
          isActive: true
        }
      ];

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockStocks });

      const result = await stockApi.getAllStocks();

      expect(result).toEqual(mockStocks);
      expect(apiClient.get).toHaveBeenCalledWith('/stocks', { params: {} });
    });

    it('fetches stocks with type filter', async () => {
      const mockStocks: Stock[] = [];
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockStocks });

      await stockApi.getAllStocks('ETF');

      expect(apiClient.get).toHaveBeenCalledWith('/stocks', {
        params: { type: 'ETF' }
      });
    });

    it('fetches stocks with sector filter', async () => {
      const mockStocks: Stock[] = [];
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockStocks });

      await stockApi.getAllStocks(undefined, 'Technology');

      expect(apiClient.get).toHaveBeenCalledWith('/stocks', {
        params: { sector: 'Technology' }
      });
    });

    it('fetches stocks with both filters', async () => {
      const mockStocks: Stock[] = [];
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockStocks });

      await stockApi.getAllStocks('STOCK', 'Technology');

      expect(apiClient.get).toHaveBeenCalledWith('/stocks', {
        params: { type: 'STOCK', sector: 'Technology' }
      });
    });
  });

  describe('getStockBySymbol', () => {
    it('fetches stock by symbol', async () => {
      const mockStock: Stock = {
        id: 1,
        symbol: 'AAPL',
        name: 'Apple Inc.',
        assetType: 'STOCK' as AssetType,
        sector: 'Technology',
        industry: null,
        exchange: 'NASDAQ',
        ipoDate: '1980-12-12',
        isActive: true
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockStock });

      const result = await stockApi.getStockBySymbol('AAPL');

      expect(result).toEqual(mockStock);
      expect(apiClient.get).toHaveBeenCalledWith('/stocks/AAPL');
    });
  });

  describe('getSectors', () => {
    it('fetches available sectors', async () => {
      const mockSectors = ['Technology', 'Healthcare', 'Financial Services'];
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockSectors });

      const result = await stockApi.getSectors();

      expect(result).toEqual(mockSectors);
      expect(apiClient.get).toHaveBeenCalledWith('/stocks/sectors');
    });
  });
});
