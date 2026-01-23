import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage, ProtectedRoute, useAuth } from './features/auth';
import { MainLayout } from './shared/layouts/MainLayout';
import { DashboardPage } from './features/dashboard/DashboardPage';
import { SyllabusListPage } from './features/syllabus/SyllabusListPage';
import { SyllabusDetailPage } from './features/syllabus/SyllabusDetailPage';
import { UserManagementPage } from './features/admin/UserManagementPage';
import { SystemSettingsPage } from './features/admin/SystemSettingsPage';
import AuditLogPage from './features/admin/AuditLogPage';
import { StudentFeedbackPage } from './features/admin/StudentFeedbackPage';
import { BatchApprovalPage } from './features/principal/BatchApprovalPage';
import { TeachingAssignmentPage } from './features/hod/TeachingAssignmentPage';
import HodSyllabusReviewPage from './features/hod/SyllabusReviewPage';
import { PLOManagementPage } from './features/aa/PLOManagementPage';
import { CourseManagementPage } from './features/aa/CourseManagementPage';
import AaSyllabusReviewPage from './features/aa/SyllabusReviewPage';
import LecturerDashboard from './features/lecturer/DashboardPage';
import ManageSyllabiPage from './features/lecturer/ManageSyllabiPage';
import SyllabusFormPage from './features/lecturer/SyllabusFormPage';
import CollaborativeReviewPage from './features/lecturer/CollaborativeReviewPage';
import { LecturerLayout } from './features/lecturer/layouts/LecturerLayout';

import { UserRole } from '@/types';

// Smart redirect component based on user role
const RoleBasedRedirect: React.FC = () => {
  const { user, isLoading } = useAuth();

  console.log('🔀 RoleBasedRedirect:', { hasUser: !!user, userRole: user?.role, isLoading });

  if (isLoading) {
    return <div style={{ padding: '50px', textAlign: 'center' }}>Đang tải...</div>;
  }

  if (!user) {
    console.log('⚠️ No user, redirecting to login');
    return <Navigate to="/login" replace />;
  }

  // Redirect based on role
  console.log('✅ User found, redirecting based on role:', user.role);
  switch (user.role) {
    case UserRole.LECTURER:
      return <Navigate to="/lecturer" replace />;
    case UserRole.STUDENT:
      return <Navigate to="/student" replace />;
    case UserRole.ADMIN:
    case UserRole.HOD:
    case UserRole.AA:
    case UserRole.PRINCIPAL:
      return <Navigate to="/admin/dashboard" replace />;
    default:
      return <Navigate to="/login" replace />;
  }
};

const App: React.FC = () => {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />

      {/* Root redirect - Smart redirect based on role */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <RoleBasedRedirect />
          </ProtectedRoute>
        }
      />

      {/* Protected routes - Only 4 roles: Admin, Principal, HoD, AA */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute
            allowedRoles={[UserRole.ADMIN, UserRole.HOD, UserRole.AA, UserRole.PRINCIPAL]}
          >
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="syllabi" element={<SyllabusListPage />} />
        <Route path="syllabi/:id" element={<SyllabusDetailPage />} />

        {/* Admin only routes */}
        <Route
          path="users"
          element={
            <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
              <UserManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="settings"
          element={
            <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
              <SystemSettingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="audit-logs"
          element={
            <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
              <AuditLogPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="student-feedback"
          element={
            <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
              <StudentFeedbackPage />
            </ProtectedRoute>
          }
        />

        {/* Principal only routes */}
        <Route
          path="batch-approval"
          element={
            <ProtectedRoute allowedRoles={[UserRole.PRINCIPAL]}>
              <BatchApprovalPage />
            </ProtectedRoute>
          }
        />

        {/* HoD only routes */}
        <Route
          path="teaching-assignment"
          element={
            <ProtectedRoute allowedRoles={[UserRole.HOD]}>
              <TeachingAssignmentPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="syllabus-review"
          element={
            <ProtectedRoute allowedRoles={[UserRole.HOD]}>
              <HodSyllabusReviewPage />
            </ProtectedRoute>
          }
        />

        {/* AA only routes */}
        <Route
          path="plo-management"
          element={
            <ProtectedRoute allowedRoles={[UserRole.AA]}>
              <PLOManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="course-management"
          element={
            <ProtectedRoute allowedRoles={[UserRole.AA]}>
              <CourseManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="aa-syllabus-review"
          element={
            <ProtectedRoute allowedRoles={[UserRole.AA]}>
              <AaSyllabusReviewPage />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<Navigate to="/admin" replace />} />
      </Route>

      {/* Lecturer routes - GIAO DIỆN RIÊNG với LecturerLayout */}
      <Route
        path="/lecturer"
        element={
          <ProtectedRoute allowedRoles={[UserRole.LECTURER]}>
            <LecturerLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<LecturerDashboard />} />
        <Route path="syllabi" element={<ManageSyllabiPage />} />
        <Route path="syllabi/create" element={<SyllabusFormPage />} />
        <Route path="syllabi/edit/:id" element={<SyllabusFormPage />} />
        <Route path="syllabi/:id" element={<SyllabusFormPage />} />
        <Route path="reviews" element={<CollaborativeReviewPage />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default App;
