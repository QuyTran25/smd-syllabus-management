import axios from 'axios';

export const http = axios.create({
  baseURL: 'http://localhost:8888/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('student_token');

    // Chỉ đính kèm Token nếu có và không phải là yêu cầu đăng nhập
    if (token && !config.url?.includes('/auth/login')) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);