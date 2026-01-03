import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL, API_TIMEOUT, STORAGE_KEYS } from '@/constants';

// Create axios instance
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Add auth token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors globally
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError) => {
    console.log('🚨 API Error:', {
      status: error.response?.status,
      url: error.config?.url,
      method: error.config?.method,
    });

    // Handle 401 Unauthorized - CHỈ logout khi /api/auth/me thất bại
    // ⚠️ QUAN TRỌNG: KHÔNG logout khi các API khác trả về 401
    if (error.response?.status === 401) {
      const url = error.config?.url || '';
      
      // CHỈ clear storage và redirect KHI verify token (/api/auth/me) thất bại
      // Đây là dấu hiệu token thật sự expired hoặc invalid
      if (url.includes('/api/auth/me')) {
        console.log('❌ Token verification failed (401), clearing storage and redirecting to login');
        localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER_DATA);
        localStorage.removeItem('smd_user_data');
        window.location.href = '/login';
      } else {
        // Các API khác trả về 401: chỉ log, KHÔNG logout
        console.log('⚠️ API returned 401 but NOT /api/auth/me, user stays logged in');
      }
    }

    // ⚠️ KHÔNG hiện message.error ở đây nữa để tránh warning
    // Component sẽ tự handle error và hiển thị message qua App.useApp()

    return Promise.reject(error);
  }
);

// Helper function to handle API errors
export const handleAPIError = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string }>;
    return axiosError.response?.data?.message || axiosError.message || 'Có lỗi xảy ra';
  }
  return 'Có lỗi xảy ra';
};
