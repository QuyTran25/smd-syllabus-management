import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage, ProtectedRoute } from './features/auth';
import { MainLayout } from './shared/layouts/MainLayout';
import { DashboardPage } from './features/dashboard/DashboardPage';
import { SyllabusListPage } from './features/syllabus/SyllabusListPage';
import { SyllabusDetailPage } from './features/syllabus/SyllabusDetailPage';
import { UserManagementPage } from './features/admin/UserManagementPage';
import { SystemSettingsPage } from './features/admin/SystemSettingsPage';
import { AuditLogPage } from './features/admin/AuditLogPage';
import { StudentFeedbackPage } from './features/admin/StudentFeedbackPage';
import { BatchApprovalPage } from './features/principal/BatchApprovalPage';
import { TeachingAssignmentPage } from './features/hod/TeachingAssignmentPage';
import { PLOManagementPage } from './features/aa/PLOManagementPage';
import { CourseManagementPage } from './features/aa/CourseManagementPage';
import { UserRole } from '@/types';

const App: React.FC = () => {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />

      {/* Protected routes - Only 4 roles: Admin, Principal, HoD, AA */}
      <Route
        path="/"
        element={
          <ProtectedRoute
            allowedRoles={[UserRole.ADMIN, UserRole.HOD, UserRole.AA, UserRole.PRINCIPAL]}
          >
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="syllabi" element={<SyllabusListPage />} />
        <Route path="syllabi/:id" element={<SyllabusDetailPage />} />
        
        {/* Admin only routes */}
        <Route path="users" element={
          <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
            <UserManagementPage />
          </ProtectedRoute>
        } />
        <Route path="settings" element={
          <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
            <SystemSettingsPage />
          </ProtectedRoute>
        } />
        <Route path="audit-logs" element={
          <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
            <AuditLogPage />
          </ProtectedRoute>
        } />
        <Route path="student-feedback" element={
          <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
            <StudentFeedbackPage />
          </ProtectedRoute>
        } />
        
        {/* Principal only routes */}
        <Route path="batch-approval" element={
          <ProtectedRoute allowedRoles={[UserRole.PRINCIPAL]}>
            <BatchApprovalPage />
          </ProtectedRoute>
        } />
        
        {/* HoD only routes */}
        <Route path="teaching-assignment" element={
          <ProtectedRoute allowedRoles={[UserRole.HOD]}>
            <TeachingAssignmentPage />
          </ProtectedRoute>
        } />
        
        {/* AA only routes */}
        <Route path="plo-management" element={
          <ProtectedRoute allowedRoles={[UserRole.AA]}>
            <PLOManagementPage />
          </ProtectedRoute>
        } />
        <Route path="course-management" element={
          <ProtectedRoute allowedRoles={[UserRole.AA]}>
            <CourseManagementPage />
          </ProtectedRoute>
        } />
        
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default App;
