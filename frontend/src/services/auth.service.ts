import { LoginCredentials, AuthResponse, User } from '@/types';
import { mockUsers } from '@/mock';

// Simulate API delay
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

// Mock JWT token generator
const generateMockToken = (user: User): string => {
  // In real app, this would be a proper JWT
  const payload = {
    userId: user.id,
    role: user.role,
    email: user.email,
  };
  return btoa(JSON.stringify(payload));
};

// Mock authentication service
export const authService = {
  // Login
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    await delay(800); // Simulate network delay

    // Find user by email
    const user = mockUsers.find((u) => u.email === credentials.email);

    if (!user) {
      throw new Error('Email không tồn tại');
    }

    // In real app, validate password hash
    // For mock: accept any password for demo, or password = "123456"
    if (credentials.password !== '123456') {
      throw new Error('Mật khẩu không đúng');
    }

    if (!user.isActive) {
      throw new Error('Tài khoản đã bị khóa');
    }

    // Update last login
    user.lastLogin = new Date().toISOString();

    return {
      token: generateMockToken(user),
      refreshToken: generateMockToken(user) + '_refresh',
      user,
    };
  },

  // Logout
  logout: async (): Promise<void> => {
    await delay(300);
    // In real app, would invalidate token on server
  },

  // Verify token and get current user
  getCurrentUser: async (token: string): Promise<User> => {
    await delay(300);

    try {
      const payload = JSON.parse(atob(token));
      const user = mockUsers.find((u) => u.id === payload.userId);

      if (!user) {
        throw new Error('Invalid token');
      }

      return user;
    } catch {
      throw new Error('Invalid token');
    }
  },

  // Refresh token
  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    await delay(300);

    try {
      const payload = JSON.parse(atob(refreshToken.replace('_refresh', '')));
      const user = mockUsers.find((u) => u.id === payload.userId);

      if (!user) {
        throw new Error('Invalid refresh token');
      }

      return {
        token: generateMockToken(user),
        refreshToken: generateMockToken(user) + '_refresh',
        user,
      };
    } catch {
      throw new Error('Invalid refresh token');
    }
  },
};
