import { describe, it, expect } from 'vitest';
import {
  formatCurrency,
  formatPercentage,
  formatPercentageChange,
  formatLargeNumber,
  formatNumber,
  calculateCAGR,
  calculateReturn
} from '../utils/financialFormatters';

describe('Financial Formatters', () => {
  describe('formatCurrency', () => {
    it('formats positive amounts', () => {
      expect(formatCurrency(1234.56)).toBe('$1,234.56');
    });

    it('formats negative amounts', () => {
      expect(formatCurrency(-500.25)).toBe('-$500.25');
    });

    it('formats zero', () => {
      expect(formatCurrency(0)).toBe('$0.00');
    });

    it('respects decimal places', () => {
      expect(formatCurrency(100, 0)).toBe('$100');
      expect(formatCurrency(100.456, 3)).toBe('$100.456');
    });

    it('formats large amounts with commas', () => {
      expect(formatCurrency(1000000)).toBe('$1,000,000.00');
    });
  });

  describe('formatPercentage', () => {
    it('formats positive percentages', () => {
      expect(formatPercentage(14.87)).toBe('14.87%');
    });

    it('formats negative percentages', () => {
      expect(formatPercentage(-5.25)).toBe('-5.25%');
    });

    it('formats zero', () => {
      expect(formatPercentage(0)).toBe('0.00%');
    });

    it('respects decimal places', () => {
      expect(formatPercentage(14.8765, 3)).toBe('14.877%');
      expect(formatPercentage(14.8765, 1)).toBe('14.9%');
    });
  });

  describe('formatPercentageChange', () => {
    it('adds + prefix for positive values', () => {
      expect(formatPercentageChange(12.5)).toBe('+12.50%');
    });

    it('keeps - prefix for negative values', () => {
      expect(formatPercentageChange(-8.3)).toBe('-8.30%');
    });

    it('adds + for zero', () => {
      expect(formatPercentageChange(0)).toBe('+0.00%');
    });
  });

  describe('formatLargeNumber', () => {
    it('formats billions', () => {
      expect(formatLargeNumber(1_500_000_000)).toBe('1.5B');
      expect(formatLargeNumber(2_300_000_000)).toBe('2.3B');
    });

    it('formats millions', () => {
      expect(formatLargeNumber(5_600_000)).toBe('5.6M');
      expect(formatLargeNumber(1_234_567)).toBe('1.2M');
    });

    it('formats thousands', () => {
      expect(formatLargeNumber(12_500)).toBe('12.5K');
      expect(formatLargeNumber(999_999)).toBe('1000.0K');
    });

    it('formats small numbers without abbreviation', () => {
      expect(formatLargeNumber(500)).toBe('500');
      expect(formatLargeNumber(42)).toBe('42');
    });
  });

  describe('formatNumber', () => {
    it('formats with commas', () => {
      expect(formatNumber(1234.56)).toBe('1,234.56');
    });

    it('respects decimal places', () => {
      expect(formatNumber(1000, 0)).toBe('1,000');
      expect(formatNumber(1000.456, 3)).toBe('1,000.456');
    });
  });

  describe('calculateCAGR', () => {
    it('calculates correct CAGR for doubling in 5 years', () => {
      const cagr = calculateCAGR(1000, 2000, 5);
      expect(cagr).toBeCloseTo(14.87, 1);
    });

    it('calculates CAGR for 50% gain in 3 years', () => {
      const cagr = calculateCAGR(1000, 1500, 3);
      expect(cagr).toBeCloseTo(14.47, 1);
    });

    it('returns 0 for zero initial value', () => {
      expect(calculateCAGR(0, 1000, 5)).toBe(0);
    });

    it('returns 0 for zero years', () => {
      expect(calculateCAGR(1000, 2000, 0)).toBe(0);
    });

    it('handles negative returns', () => {
      const cagr = calculateCAGR(1000, 500, 5);
      expect(cagr).toBeLessThan(0);
    });
  });

  describe('calculateReturn', () => {
    it('calculates 100% return for doubling', () => {
      expect(calculateReturn(1000, 2000)).toBe(100);
    });

    it('calculates 50% return', () => {
      expect(calculateReturn(1000, 1500)).toBe(50);
    });

    it('calculates negative return', () => {
      expect(calculateReturn(1000, 800)).toBe(-20);
    });

    it('returns 0 for no change', () => {
      expect(calculateReturn(1000, 1000)).toBe(0);
    });

    it('returns 0 for zero initial value', () => {
      expect(calculateReturn(0, 1000)).toBe(0);
    });
  });
});
