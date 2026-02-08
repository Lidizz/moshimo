import { describe, it, expect } from 'vitest';
import {
  formatDateForInput,
  formatDateHuman,
  daysBetween,
  yearsBetween,
  isPast,
  isFuture
} from '../utils/dateHelpers';

describe('Date Helpers', () => {
  describe('formatDateForInput', () => {
    it('formats Date object to YYYY-MM-DD', () => {
      const date = new Date('2024-01-15');
      expect(formatDateForInput(date)).toBe('2024-01-15');
    });

    it('formats ISO string to YYYY-MM-DD', () => {
      expect(formatDateForInput('2024-03-25')).toBe('2024-03-25');
    });

    it('pads single-digit months and days', () => {
      const date = new Date('2024-01-05');
      expect(formatDateForInput(date)).toBe('2024-01-05');
    });
  });

  describe('formatDateHuman', () => {
    it('formats date to human-readable format', () => {
      const date = new Date('2024-01-15');
      expect(formatDateHuman(date)).toBe('Jan 15, 2024');
    });

    it('formats ISO string to human-readable format', () => {
      expect(formatDateHuman('2024-12-25')).toBe('Dec 25, 2024');
    });
  });

  describe('daysBetween', () => {
    it('calculates days between two dates', () => {
      const start = new Date('2024-01-01');
      const end = new Date('2024-01-11');
      expect(daysBetween(start, end)).toBe(10);
    });

    it('calculates days between ISO strings', () => {
      expect(daysBetween('2024-01-01', '2024-01-31')).toBe(30);
    });

    it('returns positive value regardless of order', () => {
      expect(daysBetween('2024-01-31', '2024-01-01')).toBe(30);
    });

    it('returns 0 for same date', () => {
      expect(daysBetween('2024-01-15', '2024-01-15')).toBe(0);
    });
  });

  describe('yearsBetween', () => {
    it('calculates years between dates', () => {
      const start = new Date('2020-01-01');
      const end = new Date('2025-01-01');
      expect(yearsBetween(start, end)).toBeCloseTo(5.0, 1);
    });

    it('returns decimal years', () => {
      const start = new Date('2023-01-01');
      const end = new Date('2023-07-01');
      expect(yearsBetween(start, end)).toBeCloseTo(0.5, 1);
    });
  });

  describe('isPast', () => {
    it('returns true for past dates', () => {
      const pastDate = new Date('2020-01-01');
      expect(isPast(pastDate)).toBe(true);
    });

    it('returns false for future dates', () => {
      const futureDate = new Date();
      futureDate.setFullYear(futureDate.getFullYear() + 1);
      expect(isPast(futureDate)).toBe(false);
    });

    it('returns false for today', () => {
      const today = new Date();
      expect(isPast(today)).toBe(false);
    });
  });

  describe('isFuture', () => {
    it('returns true for future dates', () => {
      const futureDate = new Date();
      futureDate.setFullYear(futureDate.getFullYear() + 1);
      expect(isFuture(futureDate)).toBe(true);
    });

    it('returns false for past dates', () => {
      const pastDate = new Date('2020-01-01');
      expect(isFuture(pastDate)).toBe(false);
    });

    it('returns false for today', () => {
      // Use date string to avoid time component issues
      const today = new Date().toISOString().split('T')[0];
      expect(isFuture(today)).toBe(false);
    });
  });
});
