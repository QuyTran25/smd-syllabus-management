import axios from 'axios';

export const http = axios.create({
  // ⭐ ĐÃ CHỐT: Trỏ về Gateway (8888) theo cấu hình Main Branch
  baseURL: 'http://localhost:8888/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('student_token');

    // ⭐ QUAN TRỌNG: Nếu đang login thì ĐỪNG gửi token
    if (token && !config.url?.includes('/auth/login')) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);      