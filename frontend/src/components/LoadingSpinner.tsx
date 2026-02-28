import styles from './LoadingSpinner.module.css';

interface LoadingSpinnerProps {
  message?: string;
}

/**
 * Loading Spinner Component - Visual feedback during async operations.
 */
export function LoadingSpinner({ message = 'Calculating portfolio performance...' }: LoadingSpinnerProps) {
  return (
    <div className={styles.loadingSpinner}>
      <div className={styles.loadingSpinnerAnimation}>
        <div className={styles.loadingSpinnerCircle}></div>
        <div className={styles.loadingSpinnerCircle}></div>
        <div className={styles.loadingSpinnerCircle}></div>
      </div>
      <p className={styles.loadingSpinnerMessage}>{message}</p>
    </div>
  );
}
