import { LoginCredentials, AuthResponse, User, UserRole } from '@/types';
import { apiClient } from '@/config/api-config';

// Map backend role name to frontend role code
const mapRoleToCode = (roleName: string): UserRole => {
  const roleMap: Record<string, UserRole> = {
    'Administrator': UserRole.ADMIN,
    'Principal': UserRole.PRINCIPAL,
    'Academic Affairs': UserRole.AA,
    'Head of Department': UserRole.HOD,
    'Lecturer': UserRole.LECTURER,
    'Student': UserRole.STUDENT,
    // Also support if backend sends code directly
    'ADMIN': UserRole.ADMIN,
    'PRINCIPAL': UserRole.PRINCIPAL,
    'AA': UserRole.AA,
    'HOD': UserRole.HOD,
    'LECTURER': UserRole.LECTURER,
    'STUDENT': UserRole.STUDENT,
  };
  return roleMap[roleName] || UserRole.LECTURER;
};

// Real authentication service using backend API
export const authService = {
  // Login
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await apiClient.post('/api/auth/login', credentials);
    // Backend returns accessToken, not token
    const { accessToken: token, refreshToken } = response.data.data;
    
    // Get user info using the token
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    const userResponse = await apiClient.get('/api/auth/me');
    const userInfo = userResponse.data.data;
    
    // Map backend user info to frontend User type
    const rawRole = userInfo.roles && userInfo.roles.length > 0 ? userInfo.roles[0] : 'Lecturer';
    const user: User = {
      id: userInfo.id,
      email: userInfo.email,
      fullName: userInfo.fullName,
      role: mapRoleToCode(rawRole),
      phone: userInfo.phoneNumber,
      isActive: userInfo.status === 'ACTIVE',
      createdAt: new Date().toISOString(),
    };

    return {
      token,
      refreshToken,
      user,
    };
  },

  // Logout
  logout: async (): Promise<void> => {
    await apiClient.post('/api/auth/logout');
    delete apiClient.defaults.headers.common['Authorization'];
  },

  // Verify token and get current user
  getCurrentUser: async (token: string): Promise<User> => {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    const response = await apiClient.get('/api/auth/me');
    const userInfo = response.data.data;
    
    // Map backend user info to frontend User type
    const rawRole = userInfo.roles && userInfo.roles.length > 0 ? userInfo.roles[0] : 'Lecturer';
    const user: User = {
      id: userInfo.id,
      email: userInfo.email,
      fullName: userInfo.fullName,
      role: mapRoleToCode(rawRole),
      phone: userInfo.phoneNumber,
      isActive: userInfo.status === 'ACTIVE',
      createdAt: new Date().toISOString(),
    };

    return user;
  },

  // Refresh token
  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await apiClient.post('/api/auth/refresh-token', { refreshToken });
    const { accessToken: newToken, refreshToken: newRefreshToken } = response.data.data;
    
    // Get updated user info
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
    const userResponse = await apiClient.get('/api/auth/me');
    const userInfo = userResponse.data.data;
    
    // Map backend user info to frontend User type
    const rawRole = userInfo.roles && userInfo.roles.length > 0 ? userInfo.roles[0] : 'Lecturer';
    const user: User = {
      id: userInfo.id,
      email: userInfo.email,
      fullName: userInfo.fullName,
      role: mapRoleToCode(rawRole),
      phone: userInfo.phoneNumber,
      isActive: userInfo.status === 'ACTIVE',
      createdAt: new Date().toISOString(),
    };

    return {
      token: newToken,
      refreshToken: newRefreshToken,
      user,
    };
  },
};
