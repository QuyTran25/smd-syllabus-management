import axios from 'axios';
import axiosClient from '@/api/axiosClient';
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, AuthState } from '@/types';
import { authService } from '@/services/auth.service';
import { message } from 'antd';
import { STORAGE_KEYS } from '@/constants';

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<User>;
  logout: () => Promise<void>;
  setUser: (user: User | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_KEY = STORAGE_KEYS.ACCESS_TOKEN;
const REFRESH_TOKEN_KEY = STORAGE_KEYS.REFRESH_TOKEN;
const USER_KEY = STORAGE_KEYS.USER_DATA; // --- use centralized storage keys ---

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize auth state from localStorage
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem(TOKEN_KEY);
      const storedUser = localStorage.getItem(USER_KEY); // --- Đọc user từ bộ nhớ ---

      if (storedToken) {
        setToken(storedToken);
        // Nếu có sẵn thông tin user trong LocalStorage thì lấy ra dùng luôn cho nhanh
        if (storedUser) {
           try {
             setUser(JSON.parse(storedUser));
           } catch (e) {
             console.error("Lỗi parse user json", e);
           }
        } else {
           // Nếu không có user (trường hợp hiếm), gọi API lấy lại
           try {
             const currentUser = await authService.getCurrentUser(storedToken);
             setUser(currentUser);
           } catch (error) {
             localStorage.removeItem(TOKEN_KEY);
             localStorage.removeItem(REFRESH_TOKEN_KEY);
           }
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email: string, password: string): Promise<User> => {
    setIsLoading(true);
    try {
      // Use gateway axios client so requests go via http://localhost:8080/api
      const response = await axiosClient.post('/auth/login', {
        email: email,
        password: password
      });

      // Backend may wrap the actual payload in { success,message,data }.
      // Normalize to a payload object that contains `user` and `accessToken`.
      const payload = response?.data && response.data.data ? response.data.data : response?.data;

      const user = payload?.user ?? null;
      const accessToken = payload?.accessToken ?? payload?.access_token ?? null;

      if (!user) {
        console.error('Login response missing user payload', { response });
        message.error('Đăng nhập thất bại: server trả về dữ liệu không hợp lệ.');
        throw new Error('Invalid login response: missing user');
      }

      // Provide both `primaryRole` and `role` properties for compatibility
      const userFixed = {
        ...user,
        role: user.primaryRole ?? user.role,
      };
      // ----------------------------------------

      if (accessToken) {
        localStorage.setItem(TOKEN_KEY, accessToken);
      } else {
        console.warn('Login succeeded but no accessToken found in response', { response });
      }
      // Lưu userFixed (đã có role) vào bộ nhớ thay vì user gốc
      localStorage.setItem(USER_KEY, JSON.stringify(userFixed)); 

      setUser(userFixed);
      setToken(accessToken);

      return userFixed;
    } catch (error) {
      console.error("Login error:", error);
      message.error("Đăng nhập thất bại. Kiểm tra email/mật khẩu.");
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      // await authService.logout(); // Có thể comment tạm nếu API logout chưa xong
    } finally {
      setUser(null);
      setToken(null);
      // Xóa sạch sẽ
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(REFRESH_TOKEN_KEY);
      localStorage.removeItem(USER_KEY); // --- Xóa luôn user key ---
      message.info('Đã đăng xuất');
    }
  };

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};