import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage, ProtectedRoute } from './features/auth';

// --- LAYOUTS ---
import { MainLayout } from './shared/layouts/MainLayout';
import { LecturerLayout } from './features/lecturer/layouts/LecturerLayout';
import AdminLayout from './shared/layouts/AdminLayout';

// --- PAGES: ADMIN ---
import AdminDashboard from './features/admin/AdminDashboard';
import AdminSubjectsPage from './features/admin/AdminSubjectsPage';
import { UserManagementPage } from './features/admin/UserManagementPage';
import { SystemSettingsPage } from './features/admin/SystemSettingsPage';
import { AuditLogPage } from './features/admin/AuditLogPage';
import { StudentFeedbackPage } from './features/admin/StudentFeedbackPage';

// --- PAGES: LECTURER ---
import LecturerDashboard from './features/lecturer/DashboardPage';
import ManageSyllabiPage from './features/lecturer/ManageSyllabiPage';
import SyllabusFormPage from './features/lecturer/SyllabusFormPage';
import LecturerSyllabusDetail from './features/lecturer/SyllabusDetailPage';
import CollaborativeReviewPage from './features/lecturer/CollaborativeReviewPage';

// --- PAGES: PRINCIPAL ---
import { BatchApprovalPage } from './features/principal/BatchApprovalPage';

// --- PAGES: HOD (Head of Department) ---
import { TeachingAssignmentPage } from './features/hod/TeachingAssignmentPage';
import HodSyllabusReviewPage from './features/hod/SyllabusReviewPage';

// --- PAGES: AA (Academic Affairs) ---
import { PLOManagementPage } from './features/aa/PLOManagementPage';
import { CourseManagementPage } from './features/aa/CourseManagementPage';
import AaSyllabusReviewPage from './features/aa/SyllabusReviewPage';

// --- PAGES: COMMON / SHARED ---
import { DashboardPage } from './features/dashboard/DashboardPage';
import { SyllabusListPage } from './features/syllabus/SyllabusListPage';
import { SyllabusDetailPage } from './features/syllabus/SyllabusDetailPage';

import { UserRole } from '@/types';

const App: React.FC = () => {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />

      {/* 1. ADMIN ROUTES - AdminLayout */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboard />} />
        <Route path="subjects" element={<AdminSubjectsPage />} />
        <Route path="users" element={<UserManagementPage />} />
        <Route path="settings" element={<SystemSettingsPage />} />
        <Route path="audit-logs" element={<AuditLogPage />} />
        <Route path="student-feedback" element={<StudentFeedbackPage />} />
        <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
      </Route>

      {/* 2. LECTURER ROUTES - LecturerLayout */}
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
        <Route path="syllabi/:id" element={<LecturerSyllabusDetail />} />
        <Route path="reviews" element={<CollaborativeReviewPage />} />
        <Route path="*" element={<Navigate to="/lecturer" replace />} />
      </Route>

      {/* 3. OTHER ROLES (HoD, AA, Principal) - MainLayout */}
      <Route
        path="/"
        element={
          <ProtectedRoute
            allowedRoles={[UserRole.HOD, UserRole.AA, UserRole.PRINCIPAL]}
          >
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="syllabi" element={<SyllabusListPage />} />
        <Route path="syllabi/:id" element={<SyllabusDetailPage />} />

        {/* Principal only */}
        <Route
          path="batch-approval"
          element={
            <ProtectedRoute allowedRoles={[UserRole.PRINCIPAL]}>
              <BatchApprovalPage />
            </ProtectedRoute>
          }
        />

        {/* HoD only */}
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

        {/* AA only */}
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

        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>

      {/* Fallback chung */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default App;