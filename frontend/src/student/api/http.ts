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
    // Tìm token trong LOCAL STORAGE
    // Ưu tiên 'student_token' hoặc 'access_token'
    const token = localStorage.getItem('student_token') || localStorage.getItem('access_token');

    // Nếu có token và không phải đang gọi API login -> Gắn vào Header
    if (token && !config.url?.includes('/auth/login')) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle 401 errors
http.interceptors.response.use(
  (response) => response,
  (error) => {
    // If 401 Unauthorized, clear token and redirect to login
    if (error.response?.status === 401) {
      localStorage.removeItem('student_token');
      localStorage.removeItem('access_token');
      
      // Only redirect if not already on login page
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
