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
import LecturerDashboard from './features/lecturer/DashboardPage';
import ManageSyllabiPage from './features/lecturer/ManageSyllabiPage';
import SyllabusFormPage from './features/lecturer/SyllabusFormPage';
import LecturerSyllabusDetail from './features/lecturer/SyllabusDetailPage';
import CollaborativeReviewPage from './features/lecturer/CollaborativeReviewPage';
import { LecturerLayout } from './features/lecturer/layouts/LecturerLayout';
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

      {/* Lecturer routes - GIAO DIỆN RIÊNG với LecturerLayout */}
      <Route
        path="/lecturer"
        element={
          <ProtectedRoute allowedRoles={[UserRole.LECTURER]}>
            <LecturerLayout>
              <LecturerDashboard />
            </LecturerLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/lecturer/syllabi"
        element={
          <ProtectedRoute allowedRoles={[UserRole.LECTURER]}>
            <LecturerLayout>
              <ManageSyllabiPage />
            </LecturerLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/lecturer/syllabi/create"
        element={
          <ProtectedRoute allowedRoles={[UserRole.LECTURER]}>
            <LecturerLayout>
              <SyllabusFormPage />
            </LecturerLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/lecturer/syllabi/edit/:id"
        element={
          <ProtectedRoute allowedRoles={[UserRole.LECTURER]}>
            <LecturerLayout>
              <SyllabusFormPage />
            </LecturerLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/lecturer/syllabi/:id"
        element={
          <ProtectedRoute allowedRoles={[UserRole.LECTURER]}>
            <LecturerLayout>
              <LecturerSyllabusDetail />
            </LecturerLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/lecturer/reviews"
        element={
          <ProtectedRoute allowedRoles={[UserRole.LECTURER]}>
            <LecturerLayout>
              <CollaborativeReviewPage />
            </LecturerLayout>
          </ProtectedRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default App;
