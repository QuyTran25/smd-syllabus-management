import axios from 'axios';
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, AuthState } from '@/types';
import { authService } from '@/services/auth.service';
import { message } from 'antd';

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<User>;
  logout: () => Promise<void>;
  setUser: (user: User | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_KEY = 'smd_auth_token';
const REFRESH_TOKEN_KEY = 'smd_refresh_token';
const USER_KEY = 'user'; // --- THÊM KEY NÀY ĐỂ LƯU USER ---

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
      const response = await axios.post('http://localhost:8081/api/auth/login', {
        email: email,
        password: password
      });

      const { user, accessToken } = response.data; 

      // --- SỬA LỖI TẠI ĐÂY: MAPPING DỮ LIỆU ---
      // Frontend cũ đang mong đợi trường 'role', nhưng Backend trả về 'primaryRole'
      // Ta sẽ tạo ra một object user mới có cả 2 trường để "chiều lòng" cả 2 bên.
      const userFixed = {
        ...user,
        role: user.primaryRole // Gán giá trị primaryRole sang role
      };
      // ----------------------------------------

      localStorage.setItem(TOKEN_KEY, accessToken);
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