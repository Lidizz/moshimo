import { describe, it, expect, vi, beforeEach } from 'vitest';
import { assetApi } from '../services/api/assetApi';
import apiClient from '../services/api/apiClient';
import type { Asset, AssetType } from '../types/api.types';

// Mock the apiClient
vi.mock('../services/api/apiClient', () => ({
  default: {
    get: vi.fn()
  }
}));

describe('Asset API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAllAssets', () => {
    it('fetches all assets without filters', async () => {
      const mockAssets: Asset[] = [
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

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockAssets });

      const result = await assetApi.getAllAssets();

      expect(result).toEqual(mockAssets);
      expect(apiClient.get).toHaveBeenCalledWith('/assets', { params: {} });
    });

    it('fetches assets with type filter', async () => {
      const mockAssets: Asset[] = [];
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockAssets });

      await assetApi.getAllAssets('ETF');

      expect(apiClient.get).toHaveBeenCalledWith('/assets', {
        params: { type: 'ETF' }
      });
    });

    it('fetches assets with sector filter', async () => {
      const mockAssets: Asset[] = [];
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockAssets });

      await assetApi.getAllAssets(undefined, 'Technology');

      expect(apiClient.get).toHaveBeenCalledWith('/assets', {
        params: { sector: 'Technology' }
      });
    });

    it('fetches assets with both filters', async () => {
      const mockAssets: Asset[] = [];
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockAssets });

      await assetApi.getAllAssets('STOCK', 'Technology');

      expect(apiClient.get).toHaveBeenCalledWith('/assets', {
        params: { type: 'STOCK', sector: 'Technology' }
      });
    });
  });

  describe('getAssetBySymbol', () => {
    it('fetches asset by symbol', async () => {
      const mockAsset: Asset = {
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

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockAsset });

      const result = await assetApi.getAssetBySymbol('AAPL');

      expect(result).toEqual(mockAsset);
      expect(apiClient.get).toHaveBeenCalledWith('/assets/AAPL');
    });
  });

  describe('getSectors', () => {
    it('fetches available sectors', async () => {
      const mockSectors = ['Technology', 'Healthcare', 'Financial Services'];
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockSectors });

      const result = await assetApi.getSectors();

      expect(result).toEqual(mockSectors);
      expect(apiClient.get).toHaveBeenCalledWith('/assets/sectors');
    });
  });
});
