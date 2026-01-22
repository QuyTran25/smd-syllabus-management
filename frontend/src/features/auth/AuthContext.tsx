import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, AuthState } from '@/types';
import { authService } from '@/services/auth.service';
import { App } from 'antd';
import { STORAGE_KEYS } from '@/constants'; // ✅ Giữ nguyên import này

// ✅ Giữ nguyên Interface của bạn
interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<User>;
  logout: () => Promise<void>;
  setUser: (user: User | null) => void;
  isLoading: boolean; // Đảm bảo biến này được expose ra ngoài
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { message } = App.useApp();

  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(
    () => localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN) || localStorage.getItem('student_token')
  );

  // 🟢 QUAN TRỌNG: Mặc định isLoading = TRUE để chặn UI khi mới vào trang
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // LOGIC VERIFY TOKEN KHI F5
  useEffect(() => {
    const verifyToken = async () => {
      // Lấy token từ các key định sẵn
      const storedToken =
        localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN) || localStorage.getItem('student_token');

      if (storedToken) {
        try {
          console.log('🔄 [AuthContext] Đang kiểm tra token với Server...');

          // Gọi API verify token (Hàm này bạn đã có trong auth.service.ts)
          const currentUser = await authService.getCurrentUser(storedToken);

          // Server OK -> Cập nhật State
          setUser(currentUser);
          setToken(storedToken);
          console.log('✅ [AuthContext] Token hợp lệ:', currentUser.email);
        } catch (error) {
          console.error('❌ [AuthContext] Token hết hạn hoặc không hợp lệ.');

          // Token hỏng -> Xóa sạch
          localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
          localStorage.removeItem('student_token');
          localStorage.removeItem(STORAGE_KEYS.USER_DATA);

          setUser(null);
          setToken(null);
        }
      } else {
        // Không có token
        setUser(null);
        setToken(null);
      }

      // Dù thành công hay thất bại -> Tắt Loading để Router quyết định đi tiếp hay đá ra
      setIsLoading(false);
    };

    verifyToken();
  }, []);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    try {
      const response = await authService.login({ email, password });

      setUser(response.user);
      setToken(response.token);

      // Lưu LocalStorage với STORAGE_KEYS chuẩn của bạn
      localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.token);
      localStorage.setItem('student_token', response.token); // Backup key
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
      await authService.logout();
    } catch (e) {
      console.error(e);
    } finally {
      // Xóa sạch LocalStorage với các Key chuẩn
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem('student_token');
      localStorage.removeItem(STORAGE_KEYS.USER_DATA);

      setUser(null);
      setToken(null);
      message.info('Đã đăng xuất');
      window.location.href = '/login';
    }
  };

  const value = {
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

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
