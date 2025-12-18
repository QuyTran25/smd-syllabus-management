import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

// Mock: bạn đổi sang token thật sau
const isStudentLoggedIn = () => {
  return Boolean(localStorage.getItem('student_token'));
};

export default function StudentProtectedRoute({ children }: { children: React.ReactNode }) {
  const location = useLocation();

  if (!isStudentLoggedIn()) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <>{children}</>;
}
