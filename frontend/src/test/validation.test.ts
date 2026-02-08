import { describe, it, expect } from 'vitest';
import {
  validateInvestmentAmount,
  validatePurchaseDate,
  validateSymbol,
  allInvestmentsValid
} from '../utils/validation';

describe('Validation Utilities', () => {
  describe('validateInvestmentAmount', () => {
    it('accepts valid amounts', () => {
      expect(validateInvestmentAmount(1000).valid).toBe(true);
      expect(validateInvestmentAmount(100.50).valid).toBe(true);
      expect(validateInvestmentAmount(999999).valid).toBe(true);
    });

    it('rejects zero', () => {
      const result = validateInvestmentAmount(0);
      expect(result.valid).toBe(false);
      expect(result.error).toContain('greater than $0');
    });

    it('rejects negative amounts', () => {
      const result = validateInvestmentAmount(-100);
      expect(result.valid).toBe(false);
      expect(result.error).toContain('greater than $0');
    });

    it('rejects amounts over $1,000,000', () => {
      const result = validateInvestmentAmount(1_000_001);
      expect(result.valid).toBe(false);
      expect(result.error).toContain('cannot exceed');
    });

    it('accepts exactly $1,000,000', () => {
      expect(validateInvestmentAmount(1_000_000).valid).toBe(true);
    });
  });

  describe('validatePurchaseDate', () => {
    it('accepts past dates', () => {
      expect(validatePurchaseDate('2020-01-01').valid).toBe(true);
    });

    it('accepts today', () => {
      const today = new Date().toISOString().split('T')[0];
      expect(validatePurchaseDate(today).valid).toBe(true);
    });

    it('rejects future dates', () => {
      const futureDate = new Date();
      futureDate.setFullYear(futureDate.getFullYear() + 1);
      const futureDateStr = futureDate.toISOString().split('T')[0];
      const result = validatePurchaseDate(futureDateStr);
      expect(result.valid).toBe(false);
      expect(result.error).toContain('cannot be in the future');
    });

    it('accepts dates after IPO', () => {
      const result = validatePurchaseDate('2020-01-01', '2015-01-01');
      expect(result.valid).toBe(true);
    });

    it('rejects dates before IPO', () => {
      const result = validatePurchaseDate('2010-01-01', '2015-01-01');
      expect(result.valid).toBe(false);
      expect(result.error).toContain('after IPO');
    });

    it('accepts date on IPO day', () => {
      const result = validatePurchaseDate('2015-01-01', '2015-01-01');
      expect(result.valid).toBe(true);
    });
  });

  describe('validateSymbol', () => {
    it('accepts valid symbols', () => {
      expect(validateSymbol('AAPL').valid).toBe(true);
      expect(validateSymbol('MSFT').valid).toBe(true);
      expect(validateSymbol('BRK.B').valid).toBe(true);
    });

    it('rejects empty string', () => {
      const result = validateSymbol('');
      expect(result.valid).toBe(false);
      expect(result.error).toContain('required');
    });

    it('rejects whitespace-only string', () => {
      const result = validateSymbol('   ');
      expect(result.valid).toBe(false);
      expect(result.error).toContain('required');
    });

    it('rejects symbols longer than 10 characters', () => {
      const result = validateSymbol('VERYLONGSTOCK');
      expect(result.valid).toBe(false);
      expect(result.error).toContain('too long');
    });

    it('accepts exactly 10 characters', () => {
      expect(validateSymbol('TENLETTERS').valid).toBe(true);
    });
  });

  describe('allInvestmentsValid', () => {
    it('returns true for all valid investments', () => {
      const investments = [
        { symbol: 'AAPL', amountUsd: 1000, purchaseDate: '2020-01-01' },
        { symbol: 'MSFT', amountUsd: 2000, purchaseDate: '2021-01-01' }
      ];
      expect(allInvestmentsValid(investments)).toBe(true);
    });

    it('returns false if any investment has invalid symbol', () => {
      const investments = [
        { symbol: 'AAPL', amountUsd: 1000, purchaseDate: '2020-01-01' },
        { symbol: '', amountUsd: 2000, purchaseDate: '2021-01-01' }
      ];
      expect(allInvestmentsValid(investments)).toBe(false);
    });

    it('returns false if any investment has invalid amount', () => {
      const investments = [
        { symbol: 'AAPL', amountUsd: 1000, purchaseDate: '2020-01-01' },
        { symbol: 'MSFT', amountUsd: 0, purchaseDate: '2021-01-01' }
      ];
      expect(allInvestmentsValid(investments)).toBe(false);
    });

    it('returns false if any investment has future date', () => {
      const futureDate = new Date();
      futureDate.setFullYear(futureDate.getFullYear() + 1);
      const futureDateStr = futureDate.toISOString().split('T')[0];
      
      const investments = [
        { symbol: 'AAPL', amountUsd: 1000, purchaseDate: '2020-01-01' },
        { symbol: 'MSFT', amountUsd: 2000, purchaseDate: futureDateStr }
      ];
      expect(allInvestmentsValid(investments)).toBe(false);
    });

    it('returns true for empty array', () => {
      expect(allInvestmentsValid([])).toBe(true);
    });
  });
});
