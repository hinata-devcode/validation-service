import axios from 'axios';

// Create a custom axios instance per requirements
export const apiClient = axios.create({
  baseURL: "https://omissive-rectricial-tonita.ngrok-free.dev",
  headers: {
    "ngrok-skip-browser-warning": "69420",
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

// Intercept 401s globally to handle auth drops
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login if unauthorized
      if (window.location.pathname !== '/login' && window.location.pathname !== '/signup') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
