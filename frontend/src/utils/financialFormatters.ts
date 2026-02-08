/**
 * Financial formatting utilities for currency, percentages, and numbers.
 */

/**
 * Format number as USD currency.
 * @param amount Amount to format
 * @param decimals Number of decimal places (default: 2)
 * @returns Formatted currency string
 */
export function formatCurrency(amount: number, decimals: number = 2): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(amount);
}

/**
 * Format number as percentage.
 * @param value Decimal value (e.g., 0.1487 for 14.87%)
 * @param decimals Number of decimal places (default: 2)
 * @returns Formatted percentage string
 */
export function formatPercentage(value: number, decimals: number = 2): string {
  return `${(value).toFixed(decimals)}%`;
}

/**
 * Format number as percentage with + or - prefix.
 * @param value Decimal value
 * @param decimals Number of decimal places
 * @returns Formatted percentage with sign
 */
export function formatPercentageChange(value: number, decimals: number = 2): string {
  const sign = value >= 0 ? '+' : '';
  return `${sign}${value.toFixed(decimals)}%`;
}

/**
 * Format large numbers with abbreviations (K, M, B).
 * @param num Number to format
 * @returns Abbreviated string (e.g., "1.5M")
 */
export function formatLargeNumber(num: number): string {
  if (num >= 1_000_000_000) {
    return `${(num / 1_000_000_000).toFixed(1)}B`;
  }
  if (num >= 1_000_000) {
    return `${(num / 1_000_000).toFixed(1)}M`;
  }
  if (num >= 1_000) {
    return `${(num / 1_000).toFixed(1)}K`;
  }
  return num.toFixed(0);
}

/**
 * Format number with commas (e.g., 1,234.56).
 * @param num Number to format
 * @param decimals Number of decimal places
 * @returns Formatted number string
 */
export function formatNumber(num: number, decimals: number = 2): string {
  return num.toLocaleString('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  });
}

/**
 * Calculate CAGR (Compound Annual Growth Rate) percentage.
 * @param initialValue Initial investment amount
 * @param finalValue Final value
 * @param years Number of years
 * @returns CAGR percentage
 */
export function calculateCAGR(initialValue: number, finalValue: number, years: number): number {
  if (initialValue <= 0 || years <= 0) return 0;
  return ((Math.pow(finalValue / initialValue, 1 / years) - 1) * 100);
}

/**
 * Calculate percentage return.
 * @param initialValue Initial amount
 * @param finalValue Final amount
 * @returns Percentage return
 */
export function calculateReturn(initialValue: number, finalValue: number): number {
  if (initialValue === 0) return 0;
  return ((finalValue - initialValue) / initialValue) * 100;
}
