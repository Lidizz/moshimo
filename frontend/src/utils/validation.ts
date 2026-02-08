/**
 * Validation utilities for form inputs.
 */

/**
 * Validate investment amount.
 * @param amount Amount in USD
 * @returns Validation result
 */
export interface ValidationResult {
  valid: boolean;
  error?: string;
}

export function validateInvestmentAmount(amount: number): ValidationResult {
  if (amount <= 0) {
    return { valid: false, error: 'Amount must be greater than $0' };
  }
  if (amount > 1_000_000) {
    return { valid: false, error: 'Amount cannot exceed $1,000,000' };
  }
  return { valid: true };
}

/**
 * Validate purchase date.
 * @param date Purchase date
 * @param ipoDate Optional IPO date for stock
 * @returns Validation result
 */
export function validatePurchaseDate(date: string, ipoDate?: string): ValidationResult {
  const purchaseDate = new Date(date);
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  if (purchaseDate > today) {
    return { valid: false, error: 'Date cannot be in the future' };
  }

  if (ipoDate) {
    const ipo = new Date(ipoDate);
    if (purchaseDate < ipo) {
      return { valid: false, error: `Date must be after IPO (${ipoDate})` };
    }
  }

  return { valid: true };
}

/**
 * Validate stock symbol.
 * @param symbol Stock symbol
 * @returns Validation result
 */
export function validateSymbol(symbol: string): ValidationResult {
  if (!symbol || symbol.trim() === '') {
    return { valid: false, error: 'Stock symbol is required' };
  }
  if (symbol.length > 10) {
    return { valid: false, error: 'Symbol too long' };
  }
  return { valid: true };
}

/**
 * Check if all investments are valid.
 * @param investments Array of investment data
 * @returns True if all valid
 */
export function allInvestmentsValid(investments: Array<{ symbol: string; amountUsd: number; purchaseDate: string }>): boolean {
  return investments.every(inv => {
    const symbolValid = validateSymbol(inv.symbol).valid;
    const amountValid = validateInvestmentAmount(inv.amountUsd).valid;
    const dateValid = validatePurchaseDate(inv.purchaseDate).valid;
    return symbolValid && amountValid && dateValid;
  });
}
