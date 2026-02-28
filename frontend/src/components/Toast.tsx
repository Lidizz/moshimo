import { useEffect } from 'react';
import styles from './Toast.module.css';

interface ToastProps {
  message: string;
  type?: 'success' | 'error' | 'info';
  duration?: number;
  onClose: () => void;
}

/**
 * Toast Component - Temporary notification messages.
 */
export function Toast({ message, type = 'success', duration = 3000, onClose }: ToastProps) {
  useEffect(() => {
    const timer = setTimeout(onClose, duration);
    return () => clearTimeout(timer);
  }, [duration, onClose]);

  const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';

  return (
    <div className={`${styles.toast} ${styles[`toast--${type}`]}`}>
      <span className={styles.toastIcon}>{icon}</span>
      <span className={styles.toastMessage}>{message}</span>
      <button className={styles.toastClose} onClick={onClose} aria-label="Close">
        ✕
      </button>
    </div>
  );
}
