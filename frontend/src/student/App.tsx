import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, App as AntdApp } from 'antd';
import { StudentLayout } from '@/student/layouts/StudentLayout';
import { StudentLoginPage } from '@/student/pages/StudentLoginPage';
import { StudentSyllabusListPage } from '@/student/pages/StudentSyllabusListPage';
import { StudentSyllabusDetailPage } from '@/student/pages/StudentSyllabusDetailPage';
import StudentProtectedRoute from '@/student/components/StudentProtectedRoute';

export default function StudentApp() {
  return (
    <ConfigProvider theme={{ token: { colorPrimary: '#018486' } }}>
      <AntdApp>
        {' '}
        {/* ⭐ Bắt buộc có thẻ này để sửa lỗi Warning context */}
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
      </AntdApp>
    </ConfigProvider>
  );
}
