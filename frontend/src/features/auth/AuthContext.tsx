import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, AuthState } from '@/types';
import { authService } from '@/services/auth.service';
import { App } from 'antd';
import { STORAGE_KEYS } from '@/constants';
import { useFCM } from '@/hooks/useFCM';
import { unregisterFCMToken } from '@/config/firebase';

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<User>;
  logout: () => Promise<void>;
  setUser: (user: User | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { message } = App.useApp();

  // 1. QUAN TRỌNG: Luôn khởi tạo user là NULL (Chưa tin ngay vào LocalStorage)
  const [user, setUser] = useState<User | null>(null);

  // Lấy token ra để chuẩn bị đi kiểm tra
  const [token, setToken] = useState<string | null>(
    () => localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN) || localStorage.getItem('student_token')
  );

  const [isLoading, setIsLoading] = useState(true);

  // 🔔 Initialize FCM when user is authenticated
  useFCM(!!user);

  // 2. LOGIC "VERIFY FIRST": Kiểm tra Token với Server khi App khởi động
  useEffect(() => {
    const verifyToken = async () => {
      const storedToken =
        localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN) || localStorage.getItem('student_token');

      if (storedToken) {
        try {
          console.log('🔄 Đang kiểm tra token với Server...');
          // Gọi API verify token (API /me)
          const currentUser = await authService.getCurrentUser(storedToken);

          // Nếu Server trả về OK -> Set User -> Vào App
          setUser(currentUser);
          setToken(storedToken);
          console.log('✅ Token hợp lệ. Chào mừng:', currentUser.email);
        } catch (error) {
          // Nếu Token hết hạn hoặc server lỗi -> XÓA SẠCH -> Văng ra Login
          console.error('❌ Token không hợp lệ hoặc hết hạn.');
          localStorage.clear();
          setUser(null);
          setToken(null);
        }
      } else {
        // Không có token -> Chắc chắn là chưa đăng nhập
        setUser(null);
      }

      // Tắt màn hình chờ
      setIsLoading(false);
    };

    verifyToken();
  }, []);

  const login = async (email: string, password: string) => {
    try {
      setIsLoading(true);
      const response = await authService.login({ email, password });

      setUser(response.user);
      setToken(response.token);

      // Lưu vào LocalStorage
      localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.token);
      localStorage.setItem('student_token', response.token); // Lưu thêm key này cho chắc
      localStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(response.user));

      message.success(`Chào mừng ${response.user.fullName}!`);
      return response.user;
    } catch (error) {
      const msg = error instanceof Error ? error.message : 'Đăng nhập thất bại';
      message.error(msg);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      // 🔔 Unregister FCM token from backend
      await unregisterFCMToken();
      
      await authService.logout(); // Gọi API logout nếu có
    } catch (e) {
      console.error(e);
    } finally {
      localStorage.clear(); // Xóa sạch LocalStorage
      setUser(null);
      setToken(null);
      message.info('Đã đăng xuất');
      // Reload trang để xóa sạch các state rác còn sót lại
      window.location.href = '/login';
    }
  };

  const value = { user, token, isAuthenticated: !!user, isLoading, login, logout, setUser };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
