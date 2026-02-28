import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { usePersistedState } from '../hooks/usePersistedState';

// ── Helpers ─────────────────────────────────────────────────────────────

beforeEach(() => {
  sessionStorage.clear();
  localStorage.clear();
});

// ── usePersistedState ───────────────────────────────────────────────────

describe('usePersistedState', () => {
  it('returns the initial value when storage is empty', () => {
    const { result } = renderHook(() =>
      usePersistedState('test-key', 'initial'),
    );
    expect(result.current[0]).toBe('initial');
  });

  it('reads the stored value on mount', () => {
    sessionStorage.setItem('test-key', JSON.stringify('stored'));

    const { result } = renderHook(() =>
      usePersistedState('test-key', 'initial'),
    );
    expect(result.current[0]).toBe('stored');
  });

  it('writes to sessionStorage when the value changes', () => {
    const { result } = renderHook(() =>
      usePersistedState('test-key', 'initial'),
    );

    act(() => {
      result.current[1]('updated');
    });

    expect(result.current[0]).toBe('updated');
    expect(JSON.parse(sessionStorage.getItem('test-key')!)).toBe('updated');
  });

  it('supports the updater-function form of setState', () => {
    const { result } = renderHook(() =>
      usePersistedState('test-key', 10),
    );

    act(() => {
      result.current[1]((prev) => prev + 5);
    });

    expect(result.current[0]).toBe(15);
    expect(JSON.parse(sessionStorage.getItem('test-key')!)).toBe(15);
  });

  it('persists objects (serialisation round-trip)', () => {
    const obj = { name: 'AAPL', amount: 1000 };
    const { result } = renderHook(() =>
      usePersistedState('test-key', obj),
    );

    const updated = { name: 'MSFT', amount: 2000 };
    act(() => {
      result.current[1](updated);
    });

    expect(result.current[0]).toEqual(updated);
    expect(JSON.parse(sessionStorage.getItem('test-key')!)).toEqual(updated);
  });

  it('handles corrupt data gracefully (falls back to initial)', () => {
    sessionStorage.setItem('test-key', 'NOT-VALID-JSON{{{');

    const { result } = renderHook(() =>
      usePersistedState('test-key', 'fallback'),
    );

    expect(result.current[0]).toBe('fallback');
    // corrupt value should have been removed
    expect(sessionStorage.getItem('test-key')).toBeNull();
  });

  it('uses localStorage when storage param is "local"', () => {
    localStorage.setItem('local-key', JSON.stringify('from-local'));

    const { result } = renderHook(() =>
      usePersistedState('local-key', 'initial', 'local'),
    );

    expect(result.current[0]).toBe('from-local');

    act(() => {
      result.current[1]('new-value');
    });

    expect(JSON.parse(localStorage.getItem('local-key')!)).toBe('new-value');
    // should NOT be in sessionStorage
    expect(sessionStorage.getItem('local-key')).toBeNull();
  });

  it('defaults to sessionStorage (not localStorage)', () => {
    const { result } = renderHook(() =>
      usePersistedState('test-key', 'val'),
    );

    act(() => {
      result.current[1]('updated');
    });

    expect(sessionStorage.getItem('test-key')).not.toBeNull();
    expect(localStorage.getItem('test-key')).toBeNull();
  });
});
