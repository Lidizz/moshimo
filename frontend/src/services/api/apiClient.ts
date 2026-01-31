import axios, { type AxiosInstance, type AxiosError } from 'axios';

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
 * Request interceptor - Logs outgoing requests in development.
 */
apiClient.interceptors.request.use(
  (config) => {
    console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('[API Request Error]', error);
    return Promise.reject(error);
  }
);

/**
 * Response interceptor - Handles common error scenarios.
 */
apiClient.interceptors.response.use(
  (response) => {
    console.log(`[API Response] ${response.status} ${response.config.url}`);
    return response;
  },
  (error: AxiosError) => {
    if (error.response) {
      // Server responded with error status
      console.error(`[API Error] ${error.response.status}:`, error.response.data);
    } else if (error.request) {
      // Request made but no response
      console.error('[API Error] No response from server:', error.message);
    } else {
      // Error setting up request
      console.error('[API Error]', error.message);
    }
    return Promise.reject(error);
  }
);

export default apiClient;