import { Link } from 'react-router-dom';
import './NotFoundPage.css';

export default function NotFoundPage() {
  return (
    <div className="not-found-page">
      <h1 className="not-found-page__code">404</h1>
      <p className="not-found-page__message">
        もしも… this page existed? It doesn't.
      </p>
      <Link to="/" className="not-found-page__link">
        Back to Home
      </Link>
    </div>
  );
}
