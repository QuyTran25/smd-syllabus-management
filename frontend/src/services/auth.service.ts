import { LoginCredentials, AuthResponse, User } from '@/types';
import axiosClient from '@/api/axiosClient';
import { STORAGE_KEYS } from '@/constants';

// Helper: map backend user object to frontend `User`
const mapUserResponse = (userInfo: any): User => {
  const rawRole = userInfo?.roles && userInfo.roles.length > 0 ? userInfo.roles[0] : 'LECTURER';
  return {
    id: userInfo.id,
    email: userInfo.email,
    fullName: userInfo.fullName,
    role: rawRole.toUpperCase() as User['role'],
    phone: userInfo.phoneNumber,
    isActive: userInfo.status === 'ACTIVE',
    createdAt: userInfo.createdAt || new Date().toISOString(),
  };
};

// Real authentication service using backend API
export const authService = {
  // Login
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await axiosClient.post('/auth/login', credentials);
    // Normalize payload (some responses wrap under data.data)
    const payload = response.data?.data ? response.data.data : response.data;
    const token = payload?.accessToken ?? payload?.access_token ?? null;
    const refreshToken = payload?.refreshToken ?? payload?.refresh_token ?? null;

    // Persist tokens so axiosClient interceptor will pick them up
    if (token) localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token);
    if (refreshToken) localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);

    // Use user returned in login response if present, otherwise fetch /me
    let userInfo = payload?.user ?? null;
    if (!userInfo) {
      const userResponse = await axiosClient.get('/auth/me');
      userInfo = userResponse.data?.data;
    }

    const user: User = mapUserResponse(userInfo);

    return {
      token,
      refreshToken,
      user,
    };
  },

  // Logout
  logout: async (): Promise<void> => {
    try {
      await axiosClient.post('/auth/logout');
    } finally {
      // Clear stored tokens; interceptor will stop attaching header
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    }
  },

  // Verify token and get current user
  getCurrentUser: async (token: string): Promise<User> => {
    // If token provided but not persisted, attach per-request header
    const headers = token ? { Authorization: `Bearer ${token}` } : undefined;
    const response = await axiosClient.get('/auth/me', { headers });
    const userInfo = response.data?.data;
    return mapUserResponse(userInfo);
  },

  // Refresh token
  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await axiosClient.post('/auth/refresh-token', { refreshToken });
    const payload = response.data?.data ? response.data.data : response.data;
    const newToken = payload?.accessToken ?? payload?.access_token ?? null;
    const newRefreshToken = payload?.refreshToken ?? payload?.refresh_token ?? null;

    // Persist refreshed tokens
    if (newToken) localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, newToken);
    if (newRefreshToken) localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, newRefreshToken);

    // Use returned user if present, otherwise fetch /me
    let userInfo = payload?.user ?? null;
    if (!userInfo) {
      const userResponse = await axiosClient.get('/auth/me');
      userInfo = userResponse.data?.data;
    }

    return {
      token: newToken,
      refreshToken: newRefreshToken,
      user: mapUserResponse(userInfo),
    };
  },
};
