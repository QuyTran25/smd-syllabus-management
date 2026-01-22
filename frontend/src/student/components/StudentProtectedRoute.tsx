import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/features/auth/AuthContext'; // Import hook từ Context vừa sửa
import { Spin } from 'antd';

export default function StudentProtectedRoute({ children }: { children: React.ReactNode }) {
  // Lấy trạng thái từ "Bộ não" AuthContext
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  // 1. CHẶN CỬA (LOADING STATE):
  // Nếu AuthContext đang gọi API verify token -> Hiện màn hình chờ.
  // Không cho render children (trang Dashboard) lúc này.
  if (isLoading) {
    return (
      <div
        style={{
          height: '100vh',
          width: '100vw',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          background: '#f0f2f5',
          flexDirection: 'column',
          gap: 16,
        }}
      >
        <Spin size="large" />
        <span style={{ color: '#666', fontFamily: 'sans-serif', marginTop: 10 }}>
          Đang kiểm tra thông tin sinh viên...
        </span>
      </div>
    );
  }

  // 2. KIỂM TRA XONG (DONE STATE):
  // isLoading = false. Lúc này isAuthenticated là chính xác (do Server trả về).

  if (!isAuthenticated) {
    // Nếu verify thất bại hoặc không có user -> Đá về Login
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  // 3. HỢP LỆ: Mời vào Dashboard
  return <>{children}</>;
}
