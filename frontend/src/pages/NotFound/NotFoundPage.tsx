import { Link } from 'react-router-dom';
import styles from './NotFoundPage.module.css';

export default function NotFoundPage() {
  return (
    <div className={styles.notFoundPage}>
      <h1 className={styles.notFoundPageCode}>404</h1>
      <p className={styles.notFoundPageMessage}>
        もしも… this page existed? It doesn't.
      </p>
      <Link to="/" className={styles.notFoundPageLink}>
        Back to Home
      </Link>
    </div>
  );
}
