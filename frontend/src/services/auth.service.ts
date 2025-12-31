import { LoginCredentials, AuthResponse, User, UserRole } from '@/types';
import axiosClient from '@/api/axiosClient';
import { STORAGE_KEYS } from '@/constants';

/**
 * Map backend role name to frontend role code
 * Hỗ trợ cả tên hiển thị (Administrator) và mã code (ADMIN) từ Backend
 */
const mapRoleToCode = (roleName: string): UserRole => {
  const roleMap: Record<string, UserRole> = {
    'Administrator': UserRole.ADMIN,
    'Principal': UserRole.PRINCIPAL,
    'Academic Affairs': UserRole.AA,
    'Head of Department': UserRole.HOD,
    'Lecturer': UserRole.LECTURER,
    'Student': UserRole.STUDENT,
    'ADMIN': UserRole.ADMIN,
    'PRINCIPAL': UserRole.PRINCIPAL,
    'AA': UserRole.AA,
    'HOD': UserRole.HOD,
    'LECTURER': UserRole.LECTURER,
    'STUDENT': UserRole.STUDENT,
  };
  return roleMap[roleName] || UserRole.LECTURER;
};

/**
 * Chuẩn hóa dữ liệu User từ Backend trả về để khớp với Interface Frontend
 */
const mapUserResponse = (userInfo: any): User => {
  const rawRole = userInfo?.roles && userInfo.roles.length > 0 ? userInfo.roles[0] : 'LECTURER';
  return {
    id: userInfo.id,
    email: userInfo.email,
    fullName: userInfo.fullName,
    role: mapRoleToCode(rawRole),
    phone: userInfo.phoneNumber,
    isActive: userInfo.status === 'ACTIVE',
    createdAt: userInfo.createdAt || new Date().toISOString(),
  };
};

export const authService = {
  // Đăng nhập
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    // Gọi qua Gateway 8888 (đã bọc trong axiosClient)
    const response = await axiosClient.post('/api/auth/login', credentials);
    
    // Xử lý dữ liệu linh hoạt (Hỗ trợ cả object lồng 'data' hoặc object phẳng)
    const payload = response.data?.data ? response.data.data : response.data;
    const token = payload?.accessToken ?? payload?.access_token ?? null;
    const refreshToken = payload?.refreshToken ?? payload?.refresh_token ?? null;

    // Lưu Token vào LocalStorage để Interceptor có thể sử dụng cho các request sau
    if (token) localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token);
    if (refreshToken) localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);

    let userInfo = payload?.user ?? null;
    
    // Nếu login chưa trả về user, gọi API /me để lấy thông tin chi tiết
    if (!userInfo) {
      const userResponse = await axiosClient.get('/api/auth/me');
      userInfo = userResponse.data?.data || userResponse.data;
    }

    return {
      token,
      refreshToken,
      user: mapUserResponse(userInfo),
    };
  },

  // Đăng xuất
  logout: async (): Promise<void> => {
    try {
      await axiosClient.post('/api/auth/logout');
    } finally {
      // Luôn xóa token ở client kể cả khi API logout gặp lỗi
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER_DATA);
    }
  },

  // Verify token và lấy thông tin người dùng hiện tại
  getCurrentUser: async (): Promise<User> => {
    // axiosClient tự động gắn Bearer Token từ interceptor (Không cần truyền token thủ công)
    const response = await axiosClient.get('/api/auth/me');
    const userInfo = response.data?.data || response.data;
    return mapUserResponse(userInfo);
  },

  // Làm mới Token
  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await axiosClient.post('/api/auth/refresh-token', { refreshToken });
    const payload = response.data?.data ? response.data.data : response.data;
    
    const newToken = payload?.accessToken ?? payload?.access_token ?? null;
    const newRefreshToken = payload?.refreshToken ?? payload?.refresh_token ?? null;

    if (newToken) localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, newToken);
    if (newRefreshToken) localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, newRefreshToken);

    let userInfo = payload?.user ?? null;
    if (!userInfo) {
      const userResponse = await axiosClient.get('/api/auth/me');
      userInfo = userResponse.data?.data || userResponse.data;
    }

    return {
      token: newToken,
      refreshToken: newRefreshToken,
      user: mapUserResponse(userInfo),
    };
  },
};