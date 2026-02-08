import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Layout from './components/Layout/Layout';
import HomePage from './pages/Home/HomePage';
import SimulatorPage from './pages/Simulator/SimulatorPage';
import AboutPage from './pages/About/AboutPage';
import './App.css';

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
        element: <SimulatorPage /> 
      },
      { 
        path: '/about', 
        element: <AboutPage /> 
      }
    ]
  }
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;