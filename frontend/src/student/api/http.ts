import axios from 'axios';

export const http = axios.create({
  // Đảm bảo port 8081 khớp với Backend Java của bạn
  baseURL: 'http://localhost:8081/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor xử lý gắn Token vào Header cho các request sau
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('student_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
