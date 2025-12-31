import axios from 'axios';

/**
 * Cấu hình Axios Instance cho ứng dụng
 * Đã hợp nhất logic điều hướng Gateway và xử lý Token bảo mật
 */
export const http = axios.create({
  // ⭐ SỬA LẠI: Trỏ về Gateway (8888) thay vì Core (8081) để đi qua bộ lọc CORS tập trung
  baseURL: 'http://localhost:8888/api',
  headers: {
    'Content-Type': 'application/json',
  },
});



// Interceptor cho Request: Tự động gắn Token vào Header
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('student_token');

    // ⭐ QUAN TRỌNG: Chỉ đính kèm Token nếu có và KHÔNG phải là yêu cầu đăng nhập/xác thực
    // Việc gửi token cũ khi đang cố gắng login có thể gây lỗi xác thực ở phía server
    if (token && !config.url?.includes('/auth/login') && !config.url?.includes('/auth/register')) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor cho Response: Xử lý lỗi tập trung (401, 403, v.v.)
http.interceptors.response.use(
  (response) => response,
  (error) => {
    // FIX: Thay thế log.warn (không tồn tại trong JS/TS mặc định) bằng console.warn
    if (error.response?.status === 401) {
      console.warn("Unauthorized request - potential expired token. Redirecting to login...");
      
      // Tùy chọn: Tự động xóa token và đẩy về trang login
      localStorage.removeItem('student_token');
      if (!window.location.pathname.includes('/login')) {
         window.location.href = '/login';
      }
    }

    if (error.response?.status === 403) {
      console.error("Access denied - You do not have permission to access this resource.");
    }

    return Promise.reject(error);
  }
);