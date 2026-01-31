import './LoadingSpinner.css';

interface LoadingSpinnerProps {
  message?: string;
}

/**
 * Loading Spinner Component - Visual feedback during async operations.
 */
export function LoadingSpinner({ message = 'Calculating portfolio performance...' }: LoadingSpinnerProps) {
  return (
    <div className="loading-spinner">
      <div className="loading-spinner__animation">
        <div className="loading-spinner__circle"></div>
        <div className="loading-spinner__circle"></div>
        <div className="loading-spinner__circle"></div>
      </div>
      <p className="loading-spinner__message">{message}</p>
    </div>
  );
}
