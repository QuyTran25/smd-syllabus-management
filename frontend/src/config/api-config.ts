import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { message } from 'antd';
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
    // Handle 401 Unauthorized
    if (error.response?.status === 401) {
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER_DATA);
      window.location.href = '/login';
      message.error('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');
    }

    // Handle 403 Forbidden
    if (error.response?.status === 403) {
      message.error('Bạn không có quyền truy cập tài nguyên này.');
    }

    // Handle 404 Not Found
    if (error.response?.status === 404) {
      message.error('Không tìm thấy tài nguyên.');
    }

    // Handle 500 Internal Server Error
    if (error.response?.status === 500) {
      message.error('Lỗi hệ thống. Vui lòng thử lại sau.');
    }

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
