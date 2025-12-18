import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import { StudentLayout } from '@/student/layouts/StudentLayout'; // chỉnh đúng path của bạn
import { StudentLoginPage } from '@/student/pages/StudentLoginPage';
import { StudentSyllabusListPage } from '@/student/pages/StudentSyllabusListPage';
import { StudentSyllabusDetailPage } from '@/student/pages/StudentSyllabusDetailPage';
import StudentProtectedRoute from '@/student/components/StudentProtectedRoute'; // bạn tạo ở bước 2

export default function StudentApp() {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#018486',
          colorLink: '#018486',
          colorSuccess: '#1EA69A',
          fontFamily: '"Nunito", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
        },
      }}
    >
      <Routes>
      {/* ✅ Public - không chặn */}
      <Route path="/login" element={<StudentLoginPage />} />

      {/* ✅ Private - có chặn */}
      <Route
        element={
          <StudentProtectedRoute>
            <StudentLayout />
          </StudentProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/syllabi" replace />} />
        <Route path="/syllabi" element={<StudentSyllabusListPage />} />
        <Route path="/syllabi/:id" element={<StudentSyllabusDetailPage />} />
      </Route>

      {/* ✅ vào host => auto login */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
    </ConfigProvider>
  );
}
