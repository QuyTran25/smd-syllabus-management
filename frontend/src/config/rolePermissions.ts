/**
 * Role-based permissions configuration
 * Defines which syllabus statuses each role can view
 */

import { SyllabusStatus } from '../types/syllabus.types';

/**
 * Supported user roles
 */
export type UserRole = 'ADMIN' | 'PRINCIPAL' | 'AA' | 'HOD' | 'LECTURER';

/**
 * Mapping between user roles and allowed syllabus statuses
 */
export const ROLE_STATUS_MAP: Record<UserRole, SyllabusStatus[]> = {
  // =========================
  // Admin: 7 statuses
  // =========================
  ADMIN: [
    SyllabusStatus.APPROVED,
    SyllabusStatus.PUBLISHED,
    SyllabusStatus.REJECTED,
    SyllabusStatus.REVISION_IN_PROGRESS,
    SyllabusStatus.PENDING_ADMIN_REPUBLISH,
    SyllabusStatus.INACTIVE,
    SyllabusStatus.ARCHIVED,
  ],

  // =========================
  // Principal: 2 statuses
  // =========================
  PRINCIPAL: [SyllabusStatus.PENDING_PRINCIPAL, SyllabusStatus.APPROVED],

  // =========================
  // Academic Affairs: 3 statuses
  // =========================
  AA: [SyllabusStatus.PENDING_AA, SyllabusStatus.PENDING_PRINCIPAL, SyllabusStatus.REJECTED],

  // =========================
  // Head of Department: 5 statuses
  // =========================
  HOD: [
    SyllabusStatus.PENDING_HOD,
    SyllabusStatus.PENDING_AA,
    SyllabusStatus.REJECTED,
    SyllabusStatus.PENDING_HOD_REVISION,
    SyllabusStatus.PENDING_ADMIN_REPUBLISH,
  ],

  // =========================
  // Lecturer: own drafts & revisions
  // =========================
  LECTURER: [
    SyllabusStatus.DRAFT,
    SyllabusStatus.PENDING_HOD,
    SyllabusStatus.REJECTED,
    SyllabusStatus.REVISION_IN_PROGRESS,
  ],
};

/**
 * Display names for syllabus statuses (Vietnamese)
 */
export const STATUS_DISPLAY_NAMES: Record<SyllabusStatus, string> = {
  [SyllabusStatus.DRAFT]: 'Bản nháp',
  [SyllabusStatus.PENDING_HOD]: 'Chờ Trưởng BM',
  [SyllabusStatus.PENDING_AA]: 'Chờ Phòng ĐT',
  [SyllabusStatus.PENDING_PRINCIPAL]: 'Chờ Hiệu trưởng duyệt',
  [SyllabusStatus.APPROVED]: 'Đã phê duyệt',
  [SyllabusStatus.PUBLISHED]: 'Đã xuất bản',
  [SyllabusStatus.REJECTED]: 'Bị từ chối',
  [SyllabusStatus.REVISION_IN_PROGRESS]: 'Đang chỉnh sửa',
  [SyllabusStatus.PENDING_HOD_REVISION]: 'Chờ TBM duyệt lại',
  [SyllabusStatus.PENDING_ADMIN_REPUBLISH]: 'Chờ xuất bản lại',
  [SyllabusStatus.INACTIVE]: 'Không hoạt động',
  [SyllabusStatus.ARCHIVED]: 'Đã lưu trữ',
};

/**
 * Get allowed statuses for a specific role
 */
export function getAllowedStatuses(role: string): SyllabusStatus[] {
  const normalizedRole = role.toUpperCase() as UserRole;
  return ROLE_STATUS_MAP[normalizedRole] || [];
}

/**
 * Check if a role can view a specific status
 */
export function canViewStatus(role: string, status: SyllabusStatus): boolean {
  return getAllowedStatuses(role).includes(status);
}

/**
 * Get status tabs configuration for UI
 */
export function getStatusTabs(role: string) {
  const allowedStatuses = getAllowedStatuses(role);

  return allowedStatuses.map((status) => ({
    key: status,
    label: STATUS_DISPLAY_NAMES[status],
    value: status,
  }));
}
