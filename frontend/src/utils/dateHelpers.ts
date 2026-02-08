/**
 * Date formatting utilities for the application.
 */

/**
 * Format date to YYYY-MM-DD format (for date inputs).
 * @param date Date object or ISO string
 * @returns Formatted date string
 */
export function formatDateForInput(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Format date to human-readable format (e.g., "Jan 15, 2024").
 * @param date Date object or ISO string
 * @returns Formatted date string
 */
export function formatDateHuman(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

/**
 * Calculate days between two dates.
 * @param startDate Start date
 * @param endDate End date
 * @returns Number of days
 */
export function daysBetween(startDate: Date | string, endDate: Date | string): number {
  const start = typeof startDate === 'string' ? new Date(startDate) : startDate;
  const end = typeof endDate === 'string' ? new Date(endDate) : endDate;
  const diffTime = Math.abs(end.getTime() - start.getTime());
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

/**
 * Calculate years between two dates (with decimals).
 * @param startDate Start date
 * @param endDate End date
 * @returns Number of years (decimal)
 */
export function yearsBetween(startDate: Date | string, endDate: Date | string): number {
  const days = daysBetween(startDate, endDate);
  return Number((days / 365.25).toFixed(2));
}

/**
 * Check if a date is in the past.
 * @param date Date to check
 * @returns True if in the past
 */
export function isPast(date: Date | string): boolean {
  const d = typeof date === 'string' ? new Date(date) : date;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return d < today;
}

/**
 * Check if a date is in the future.
 * @param date Date to check
 * @returns True if in the future
 */
export function isFuture(date: Date | string): boolean {
  const d = typeof date === 'string' ? new Date(date) : date;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return d > today;
}
