import { Component, type ErrorInfo, type ReactNode } from 'react';
import './ErrorBoundary.css';

interface Props {
  children: ReactNode;
  fallbackMessage?: string;
}

interface State {
  hasError: boolean;
}

/**
 * Catches render errors in child components and displays a fallback UI
 * instead of crashing the entire page.
 */
export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // In production this would go to an error tracking service
    console.error('ErrorBoundary caught:', error, info.componentStack);
  }

  private handleRetry = () => {
    this.setState({ hasError: false });
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-boundary">
          <h2>Something went wrong</h2>
          <p>{this.props.fallbackMessage ?? 'An unexpected error occurred in this section.'}</p>
          <button onClick={this.handleRetry} className="error-boundary__retry">
            Try again
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
