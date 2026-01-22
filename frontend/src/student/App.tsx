import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
// Bỏ import ConfigProvider, AntdApp ở đây vì main.tsx đã lo rồi
import { StudentLayout } from '@/student/layouts/StudentLayout';
import { StudentLoginPage } from '@/student/pages/StudentLoginPage';
import { StudentSyllabusListPage } from '@/student/pages/StudentSyllabusListPage';
import { StudentSyllabusDetailPage } from '@/student/pages/StudentSyllabusDetailPage';
import StudentProtectedRoute from '@/student/components/StudentProtectedRoute';

export default function StudentApp() {
  // Xóa ConfigProvider và AntdApp bao quanh, chỉ trả về Routes
  return (
    <Routes>
      <Route path="/login" element={<StudentLoginPage />} />
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
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
