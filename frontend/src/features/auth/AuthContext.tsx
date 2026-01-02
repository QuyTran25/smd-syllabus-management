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

let authProviderRenderCount = 0;

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  authProviderRenderCount++;
  console.log(`[AuthProvider] 🔌 RENDER #${authProviderRenderCount}`);
  
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize auth state from localStorage
  useEffect(() => {
    console.log('[AuthProvider] 🔗 useEffect ran (dependency: [])');
    const initAuth = async () => {
      console.log("[AuthContext] 🚀 initAuth started");
      const storedToken = localStorage.getItem(TOKEN_KEY);
      const storedUser = localStorage.getItem(USER_KEY); // --- Đọc user từ bộ nhớ ---

      console.log("[AuthContext] 📦 storedToken:", storedToken ? "EXISTS" : "NOT FOUND");
      console.log("[AuthContext] 👤 storedUser:", storedUser ? "EXISTS" : "NOT FOUND");

      if (storedToken) {
        console.log("[AuthContext] ✅ Token found, setting it");
        setToken(storedToken);
        // Nếu có sẵn thông tin user trong LocalStorage thì lấy ra dùng luôn cho nhanh
        if (storedUser) {
           try {
             const parsed = JSON.parse(storedUser);
             console.log("[AuthContext] ✅ User parsed from localStorage:", parsed.email);
             setUser(parsed);
           } catch (e) {
             console.error("[AuthContext] ❌ Lỗi parse user json", e);
           }
        } else {
           // Nếu không có user (trường hợp hiếm), gọi API lấy lại
           try {
             console.log("[AuthContext] 🔄 Fetching user from API");
             const currentUser = await authService.getCurrentUser(storedToken);
             console.log("[AuthContext] ✅ User fetched from API:", currentUser.email);
             setUser(currentUser);
           } catch (error) {
             console.error("[AuthContext] ❌ Error fetching user:", error);
             localStorage.removeItem(TOKEN_KEY);
             localStorage.removeItem(REFRESH_TOKEN_KEY);
           }
        }
      } else {
        console.log("[AuthContext] ⚠️ No token found, user stays null");
      }
      console.log("[AuthContext] ✅ initAuth completed, setting isLoading=false");
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email: string, password: string): Promise<User> => {
    console.log("[AuthContext] 🔑 login() called with email:", email);
    setIsLoading(true);
    try {
      // Use gateway axios client so requests go via http://localhost:8080/api
      console.log("[AuthContext] 📡 Sending POST /auth/login to gateway");
      const response = await axiosClient.post('/auth/login', {
        email: email,
        password: password
      });

      console.log("[AuthContext] 📨 Response received:", response.status);
      console.log("[AuthContext] 📨 Full response.data:", JSON.stringify(response.data, null, 2));

      // Backend may wrap the actual payload in { success,message,data }.
      // Normalize to a payload object that contains `user` and `accessToken`.
      const payload = response?.data && response.data.data ? response.data.data : response?.data;

      console.log("[AuthContext] 📦 Payload:", JSON.stringify(payload, null, 2));

      const user = payload?.user ?? payload;
      const accessToken = payload?.accessToken ?? payload?.access_token ?? null;

      if (!user) {
        console.error('[AuthContext] ❌ Login response missing user payload', { response });
        message.error('Đăng nhập thất bại: server trả về dữ liệu không hợp lệ.');
        throw new Error('Invalid login response: missing user');
      }

      console.log("[AuthContext] ✅ User received:", user.email);
      console.log("[AuthContext] ✅ AccessToken received:", accessToken ? "YES" : "NO");

      // Extract role: try primaryRole first, then role field, then first item from roles array
      // Also normalize to uppercase to match enum values (e.g., "Lecturer" -> "LECTURER")
      let roleValue = user.primaryRole ?? user.role ?? (user.roles && user.roles[0]) ?? null;
      if (roleValue && typeof roleValue === 'string') {
        roleValue = roleValue.toUpperCase();
      }

      console.log("[AuthContext] 🎭 Role extracted and normalized:", roleValue);

      // Provide both `primaryRole` and `role` properties for compatibility
      const userFixed = {
        ...user,
        role: roleValue,
      };
      // ----------------------------------------

      if (accessToken) {
        localStorage.setItem(TOKEN_KEY, accessToken);
        console.log("[AuthContext] 💾 Token saved to localStorage");
      } else {
        console.warn('[AuthContext] ⚠️ Login succeeded but no accessToken found in response', { response });
      }
      // Lưu userFixed (đã có role) vào bộ nhớ thay vì user gốc
      localStorage.setItem(USER_KEY, JSON.stringify(userFixed));
      console.log("[AuthContext] 💾 User saved to localStorage");

      setUser(userFixed);
      setToken(accessToken);

      console.log("[AuthContext] ✅ Login completed successfully");
      return userFixed;
    } catch (error) {
      console.error("[AuthContext] ❌ Login error:", error);
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