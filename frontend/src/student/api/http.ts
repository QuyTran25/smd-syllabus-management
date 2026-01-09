import axios from 'axios';

export const http = axios.create({
  // ⭐ Gọi trực tiếp Core Service (8081) để test, khi deploy sẽ dùng Gateway
  baseURL: 'http://localhost:8081/api',
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
