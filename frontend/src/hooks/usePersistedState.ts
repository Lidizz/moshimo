import { useState, useEffect, useCallback, useRef } from 'react';

type StorageType = 'session' | 'local';

/**
 * Like useState, but the value is automatically persisted to
 * sessionStorage (default) or localStorage.
 *
 * - Reads the stored value on first mount; falls back to `initial`.
 * - Writes every time the value changes.
 * - Gracefully handles corrupt / unparseable data.
 */
export function usePersistedState<T>(
  key: string,
  initial: T,
  storage: StorageType = 'session',
): [T, (value: T | ((prev: T) => T)) => void] {
  const storageObj =
    storage === 'local' ? globalThis.localStorage : globalThis.sessionStorage;

  // Read from storage only on first mount (lazy initialiser)
  const [value, setValue] = useState<T>(() => {
    try {
      const stored = storageObj.getItem(key);
      if (stored !== null) {
        return JSON.parse(stored) as T;
      }
    } catch {
      // corrupt data — remove it and fall back
      storageObj.removeItem(key);
    }
    return initial;
  });

  // Keep a ref so the effect always has the latest value without
  // re-running the effect closure on every render.
  const isFirstMount = useRef(true);

  // Persist whenever the value changes (skip initial mount because
  // the lazy initialiser already read from storage).
  useEffect(() => {
    if (isFirstMount.current) {
      isFirstMount.current = false;
      return;
    }
    try {
      storageObj.setItem(key, JSON.stringify(value));
    } catch {
      // storage full or unavailable — silently ignore
    }
  }, [key, value, storageObj]);

  // Stable setter that mirrors React's useState API
  const setPersistedValue = useCallback(
    (next: T | ((prev: T) => T)) => {
      setValue((prev) => {
        const resolved =
          typeof next === 'function' ? (next as (prev: T) => T)(prev) : next;
        // Write immediately so the storage is always in sync
        try {
          storageObj.setItem(key, JSON.stringify(resolved));
        } catch {
          // ignore
        }
        return resolved;
      });
    },
    [key, storageObj],
  );

  return [value, setPersistedValue];
}
