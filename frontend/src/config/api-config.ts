import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';

// 🟢 Cấu hình cứng URL Gateway (Port 8888)
const API_BASE_URL = 'http://localhost:8888/api';
const API_TIMEOUT = 20000;

// Các Key lưu trữ Token
const STORAGE_KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER_DATA: 'user_data',
  STUDENT_TOKEN: 'student_token',
};

// Tạo Axios Instance
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

// --- 1. Request Interceptor: Gắn Token ---
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Tìm token ở cả 2 key phổ biến (Ưu tiên logic hiện tại của bạn)
    const token =
      localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN) ||
      localStorage.getItem(STORAGE_KEYS.STUDENT_TOKEN);

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// --- 2. Response Interceptor: Bắt lỗi ---
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config;

    // Log lỗi gọn gàng để debug nếu cần (nhưng không hiện alert làm phiền)
    if (error.response) {
      console.error(
        `🚨 API Error [${error.response.status}] ${originalRequest?.url}:`,
        error.response.data
      );
    }

    // 🔴 XỬ LÝ LỖI 401 (UNAUTHORIZED) - Token hết hạn hoặc không hợp lệ
    if (error.response?.status === 401 && originalRequest && !(originalRequest as any)._retry) {
      console.warn('❌ Phiên đăng nhập hết hạn. Đang đăng xuất...');

      // 1. Xóa sạch token để tránh vòng lặp vô tận
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.STUDENT_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER_DATA);

      // 2. Chuyển hướng về trang Login (nếu chưa ở đó)
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }

      return Promise.reject(error);
    }

    // 🟠 XỬ LÝ LỖI 403 (FORBIDDEN) - Không có quyền truy cập
    if (error.response?.status === 403) {
      console.error('🚫 Lỗi 403: Bạn không có quyền thực hiện thao tác này.');
      // Không logout, chỉ báo lỗi để UI hiển thị thông báo (ví dụ: message.error)
    }

    return Promise.reject(error);
  }
);

// Hàm helper để hiển thị lỗi ra UI (giữ nguyên)
export const handleAPIError = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string }>;
    return axiosError.response?.data?.message || axiosError.message || 'Có lỗi xảy ra';
  }
  return 'Có lỗi xảy ra';
};
