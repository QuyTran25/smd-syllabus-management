import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { Spin } from 'antd';
import { UserRole } from '@/types';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: UserRole[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <Spin size="large" fullscreen tip="Đang tải..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // --- PHẦN SỬA ĐỔI: LINH HOẠT HƠN TRONG VIỆC LẤY ROLE ---
  
  // 1. Lấy role từ nhiều nguồn có thể có (để tránh bị undefined)
  // Ép kiểu (user as any) để tránh lỗi TypeScript nếu interface User chưa cập nhật
  const currentUserRole = user?.role || (user as any)?.primaryRole || (user as any)?.roles?.[0];

  // 2. DEBUG: Bật F12 lên xem dòng này in ra cái gì nếu vẫn lỗi
  console.log("=== CHECK PERMISSION ===");
  console.log("Quyền yêu cầu:", allowedRoles);
  console.log("Quyền hiện có (Raw):", currentUserRole);

  // 3. Logic kiểm tra quyền
  const hasPermission = allowedRoles 
    ? allowedRoles.some(role => role.toString() === currentUserRole?.toString())
    : true; // Nếu không yêu cầu role cụ thể thì cho qua

  if (allowedRoles && !hasPermission) {
    return (
      <div style={{ 
        height: '100vh', 
        display: 'flex', 
        flexDirection: 'column', 
        justifyContent: 'center', 
        alignItems: 'center',
        background: '#f5f5f5'
      }}>
        <h2 style={{ color: '#ff4d4f' }}>⛔ Bạn không có quyền truy cập</h2>
        <div style={{ background: '#fff', padding: 20, borderRadius: 8, marginTop: 10 }}>
            <p>Tài khoản: <strong>{user?.email}</strong></p>
            <p>Quyền hiện tại: <strong style={{color: 'blue'}}>{currentUserRole || 'Không xác định'}</strong></p>
            <p>Yêu cầu quyền: <strong>{allowedRoles.join(', ')}</strong></p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};