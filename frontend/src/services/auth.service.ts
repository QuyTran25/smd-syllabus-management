import { LoginCredentials, AuthResponse, User, UserRole } from '@/types';
import axiosClient from '@/api/axiosClient'; // Ưu tiên axiosClient đã cấu hình interceptor
import { STORAGE_KEYS } from '@/constants';

// Helper map Role từ Team (Hỗ trợ cả "Administrator" và "ADMIN")
const mapRoleToCode = (roleName: string): UserRole => {
  const roleMap: Record<string, UserRole> = {
    'Administrator': UserRole.ADMIN,
    'Principal': UserRole.PRINCIPAL,
    'Academic Affairs': UserRole.AA,
    'Head of Department': UserRole.HOD,
    'Lecturer': UserRole.LECTURER,
    'Student': UserRole.STUDENT,
    // Hỗ trợ trường hợp backend trả về mã code trực tiếp
    'ADMIN': UserRole.ADMIN,
    'PRINCIPAL': UserRole.PRINCIPAL,
    'AA': UserRole.AA,
    'HOD': UserRole.HOD,
    'LECTURER': UserRole.LECTURER,
    'STUDENT': UserRole.STUDENT,
  };
  return roleMap[roleName] || UserRole.LECTURER;
};

// Helper map User từ Team (đã được chuẩn hóa)
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

// Real authentication service
export const authService = {
  // Login
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    // FIX PATH: Thêm /api để khớp với Gateway 8888
    const response = await axiosClient.post('/api/auth/login', credentials);
    
    // Normalize payload
    const payload = response.data?.data ? response.data.data : response.data;
    const token = payload?.accessToken ?? payload?.access_token ?? null;
    const refreshToken = payload?.refreshToken ?? payload?.refresh_token ?? null;

    // Persist tokens
    if (token) localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token);
    if (refreshToken) localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);

    // Lấy thông tin User
    let userInfo = payload?.user ?? null;
    if (!userInfo) {
      // Nếu login không trả về user, gọi thêm API /me
      const userResponse = await axiosClient.get('/api/auth/me');
      userInfo = userResponse.data?.data || userResponse.data;
    }

    return {
      token,
      refreshToken,
      user: mapUserResponse(userInfo),
    };
  },

  // Logout
  logout: async (): Promise<void> => {
    try {
      await axiosClient.post('/api/auth/logout');
    } finally {
      // Clear stored tokens
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    }
  },

  // Verify token and get current user
  getCurrentUser: async (token: string): Promise<User> => {
    // AxiosClient đã tự động gắn token qua interceptor nếu có trong localStorage
    const response = await axiosClient.get('/api/auth/me');
    const userInfo = response.data?.data || response.data;
    return mapUserResponse(userInfo);
  },

  // Refresh token
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