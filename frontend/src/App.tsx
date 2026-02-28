import React, { Suspense } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Layout from './components/Layout/Layout';
import HomePage from './pages/Home/HomePage';
import { LoadingSpinner } from './components/LoadingSpinner';
import { ErrorBoundary } from './components/ErrorBoundary';
import { SimulationProvider } from './context/SimulationContext';

const SimulatorPage = React.lazy(() => import('./pages/Simulator/SimulatorPage'));
const AboutPage = React.lazy(() => import('./pages/About/AboutPage'));
const NotFoundPage = React.lazy(() => import('./pages/NotFound/NotFoundPage'));

const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { 
        path: '/', 
        element: <HomePage /> 
      },
      { 
        path: '/simulator', 
        element: (
          <ErrorBoundary fallbackMessage="The simulator encountered an error. Please try again.">
            <Suspense fallback={<LoadingSpinner message="Loading simulator..." />}>
              <SimulatorPage />
            </Suspense>
          </ErrorBoundary>
        )
      },
      { 
        path: '/about', 
        element: (
          <Suspense fallback={<LoadingSpinner message="Loading..." />}>
            <AboutPage />
          </Suspense>
        )
      },
      {
        path: '*',
        element: (
          <Suspense fallback={<LoadingSpinner message="Loading..." />}>
            <NotFoundPage />
          </Suspense>
        )
      }
    ]
  }
]);

function App() {
  return (
    <SimulationProvider>
      <RouterProvider router={router} />
    </SimulationProvider>
  );
}

export default App;