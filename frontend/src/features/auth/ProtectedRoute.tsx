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

  console.log('🔐 [ProtectedRoute] CHECKING ACCESS:', {
    isAuthenticated,
    isLoading,
    hasUser: !!user,
    userEmail: user?.email,
    userRole: user?.role,
    userRoleType: typeof user?.role,
    allowedRoles,
    allowedRolesTypes: allowedRoles?.map(r => typeof r),
    hasAccess: allowedRoles ? user && allowedRoles.includes(user.role) : true,
    willRedirect: !isLoading && !isAuthenticated,
    roleCheckResult: allowedRoles && user ? {
      userRole: user.role,
      isIncluded: allowedRoles.includes(user.role),
      roleComparison: allowedRoles.map(ar => ({ 
        allowedRole: ar, 
        matches: ar === user.role,
        strictEqual: ar === user.role,
        looseEqual: ar == user.role,
      })),
    } : 'No role check needed',
  });

  if (isLoading) {
    console.log('⏳ [ProtectedRoute] Still loading, showing spinner...');
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <Spin size="large" fullscreen tip="Đang tải..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    console.log('🚫 [ProtectedRoute] NOT authenticated, redirecting to /login');
    return <Navigate to="/login" replace />;
  }

  // Check role-based access
  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    console.log('🚫 [ProtectedRoute] User role NOT allowed, showing access denied');
    console.error('❌ ROLE MISMATCH:', {
      userRole: user.role,
      allowedRoles,
      message: 'User does not have required role',
    });
    return (
      <div style={{ padding: '50px', textAlign: 'center' }}>
        <h2>Bạn không có quyền truy cập trang này</h2>
        <p>Vui lòng liên hệ quản trị viên để được hỗ trợ.</p>
        <p style={{ color: '#999', fontSize: '12px' }}>
          Your role: {user.role} | Required: {allowedRoles.join(', ')}
        </p>
      </div>
    );
  }

  console.log('✅ [ProtectedRoute] Access granted, rendering children');
  return <>{children}</>;
};
