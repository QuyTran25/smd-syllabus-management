import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, AuthState } from '@/types';
import { authService } from '@/services/auth.service';
import { App } from 'antd';
import { STORAGE_KEYS } from '@/constants';

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<User>;
  logout: () => Promise<void>;
  setUser: (user: User | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  // CRITICAL: Dùng App.useApp() để lấy message từ context thay vì static
  const { message } = App.useApp();
  
  // CRITICAL FIX: Initialize user from localStorage IMMEDIATELY to prevent race condition
  const [user, setUser] = useState<User | null>(() => {
    const storedUser = localStorage.getItem(STORAGE_KEYS.USER_DATA);
    if (storedUser) {
      try {
        const parsed = JSON.parse(storedUser);
        console.log('⚡ FAST INIT: User restored from localStorage:', parsed.email, 'role:', parsed.role);
        return parsed;
      } catch (error) {
        console.error('❌ Failed to parse stored user:', error);
        return null;
      }
    }
    return null;
  });
  
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  });
  
  const [isLoading, setIsLoading] = useState(true);

  // Verify token and refresh user data from server
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
      const storedUser = localStorage.getItem(STORAGE_KEYS.USER_DATA);
      
      console.log('🔄 [AuthContext] initAuth START:', {
        hasToken: !!storedToken,
        hasStoredUser: !!storedUser,
        tokenPreview: storedToken ? storedToken.substring(0, 20) + '...' : 'none',
      });

      if (storedToken) {
        try {
          console.log('📡 [AuthContext] Calling /api/auth/me to verify token...');
          const currentUser = await authService.getCurrentUser(storedToken);
          console.log('✅ [AuthContext] Auth verified successfully:', {
            email: currentUser?.email,
            role: currentUser?.role,
            id: currentUser?.id,
          });
          setUser(currentUser);
          setToken(storedToken);
          // CRITICAL: Persist user to localStorage
          localStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(currentUser));
        } catch (error) {
          console.error('❌ [AuthContext] Token verification FAILED:', error);
          console.error('❌ [AuthContext] Clearing all storage and setting user to null');
          // Token invalid, clear ALL storage
          localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.USER_DATA);
          setUser(null);
          setToken(null);
        }
      } else {
        console.log('⚠️ [AuthContext] No stored token found, user will be null');
        setUser(null);
      }

      console.log('🏁 [AuthContext] initAuth COMPLETE, setting isLoading = false');
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email: string, password: string) => {
    try {
      setIsLoading(true);
      const response = await authService.login({ email, password });

      console.log('🎉 Login successful:', {
        email: response.user.email,
        role: response.user.role,
        fullName: response.user.fullName,
        hasToken: !!response.token,
      });

      setUser(response.user);
      setToken(response.token);

      // CRITICAL: Store BOTH token AND user data in localStorage với đúng keys
      localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.token);
      localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
      localStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(response.user));

      console.log('💾 Token + User data stored in localStorage');

      message.success(`Chào mừng ${response.user.fullName}!`);
      
      return response.user;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Đăng nhập thất bại';
      message.error(errorMessage);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } finally {
      setUser(null);
      setToken(null);
      // CRITICAL: Clear ALL auth data với đúng keys
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER_DATA);
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

// Custom hook to use auth context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
