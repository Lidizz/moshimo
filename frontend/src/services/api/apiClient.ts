import axios, { type AxiosInstance } from 'axios';

/**
 * Axios instance configured for backend API communication.
 * 
 * Learning Notes:
 * - Vite proxy forwards /api/* to http://localhost:8080
 * - Interceptors handle common error scenarios
 * - Timeout prevents hanging requests
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: '/api',  // Vite proxy handles forwarding to backend
  timeout: 30000,   // 30 seconds
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor - logs nothing in production.
 */
apiClient.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor - Handles common error scenarios.
 */
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Re-throw so individual API call sites can handle errors in context
    return Promise.reject(error);
  }
);

export default apiClient;