/**
 * Type definitions for API responses.
 * 
 * Learning: TypeScript interfaces ensure type safety across the app.
 */

/**
 * Asset type enum - categorizes tradeable assets.
 */
export type AssetType = 'STOCK' | 'ETF' | 'INDEX';

export interface HealthResponse {
  status: string;
  timestamp: string;
  database: {
    connected: boolean;
    version: string;
    totalStocks: number;
    totalPriceRecords: number;
  };
  application: {
    name: string;
    version: string;
    environment: string;
  };
}

export interface Stock {
  id: number;
  symbol: string;
  name: string;
  assetType: AssetType;
  sector: string | null;
  industry: string | null;
  exchange: string | null;
  ipoDate: string | null;  // ISO date string
  isActive: boolean;
}

export interface PriceData {
  date: string;  // ISO date string
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number | null;
  adjustedClose: number | null;
}

export interface InvestmentItem {
  symbol: string;
  amountUsd: number;
  purchaseDate: string;  // ISO date string (YYYY-MM-DD)
}

export interface SimulationRequest {
  investments: InvestmentItem[];
  endDate?: string;  // Optional, defaults to today
}

export interface SimulationResponse {
  totalInvested: number;
  currentValue: number;
  absoluteGain: number;
  percentReturn: number;
  cagr: number;
  timeline: TimelinePoint[];
  holdings: HoldingInfo[];
  benchmarkTimeline?: TimelinePoint[];  // Optional S&P 500 benchmark data
  holdingsTimelines?: Record<string, TimelinePoint[]>;  // Individual timelines per holding
}

export interface TimelinePoint {
  date: string;
  value: number;
}

export interface HoldingInfo {
  symbol: string;
  name: string;
  invested: number;
  currentValue: number;
  shares: number;
  purchasePrice: number;
  currentPrice: number;
  absoluteGain: number;
  percentReturn: number;
}

export interface ApiError {
  code: string;
  message: string;
  timestamp: string;
  details?: Record<string, string>;
}

export interface Investment {
  id: string;  // Local ID for React keys
  symbol: string;
  amountUsd: number;
  purchaseDate: string;  // YYYY-MM-DD format
}