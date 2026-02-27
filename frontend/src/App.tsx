import React, { Suspense } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Layout from './components/Layout/Layout';
import HomePage from './pages/Home/HomePage';
import { LoadingSpinner } from './components/LoadingSpinner';
import './App.css';

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
          <Suspense fallback={<LoadingSpinner message="Loading simulator..." />}>
            <SimulatorPage />
          </Suspense>
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
  return <RouterProvider router={router} />;
}

export default App;