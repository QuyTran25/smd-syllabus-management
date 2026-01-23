  import axios from 'axios';
  import { API_BASE_URL } from '@/constants';

  export const http = axios.create({

    // Use centralized Gateway URL
    baseURL: `${API_BASE_URL}/api`,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  http.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('student_token') || localStorage.getItem('access_token');
      if (token && !config.url?.includes('/auth/login')) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  http.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        localStorage.removeItem('student_token');
        localStorage.removeItem('access_token');
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
      }
      return Promise.reject(error);
    }
  );
