import axios from 'axios';

export const http = axios.create({
  // Địa chỉ backend của bạn
  baseURL: 'http://localhost:8081/api',
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
