/**
 * Role-based permissions configuration
 * Defines which syllabus statuses each role can view
 */

import { SyllabusStatus } from '../types/syllabus.types';

export const ROLE_STATUS_MAP: Record<string, SyllabusStatus[]> = {
  // Admin: 7 trạng thái
  ADMIN: [
    'APPROVED',
    'PUBLISHED',
    'REJECTED',
    'REVISION_IN_PROGRESS',
    'PENDING_ADMIN_REPUBLISH',
    'INACTIVE',
    'ARCHIVED'
  ],
  
  // Hiệu trưởng: 2 trạng thái
  PRINCIPAL: [
    'PENDING_PRINCIPAL',
    'APPROVED'
  ],
  
  // Phòng đào tạo: 3 trạng thái
  AA: [
    'PENDING_AA',
    'PENDING_PRINCIPAL',
    'REJECTED'
  ],
  
  // Trưởng bộ môn: 5 trạng thái
  HOD: [
    'PENDING_HOD',
    'PENDING_AA',
    'REJECTED',
    'PENDING_HOD_REVISION',
    'PENDING_ADMIN_REPUBLISH'
  ],
  
  // Giảng viên: Có thể xem draft của mình
  LECTURER: [
    'DRAFT',
    'PENDING_HOD',
    'REJECTED',
    'REVISION_IN_PROGRESS'
  ]
};

export const STATUS_DISPLAY_NAMES: Record<SyllabusStatus, string> = {
  DRAFT: 'Bản nháp',
  PENDING_HOD: 'Chờ Trưởng BM',
  PENDING_AA: 'Chờ Phòng ĐT',
  PENDING_PRINCIPAL: 'Chờ Hiệu trưởng duyệt',
  APPROVED: 'Đã phê duyệt',
  PUBLISHED: 'Đã xuất bản',
  REJECTED: 'Bị từ chối',
  REVISION_IN_PROGRESS: 'Đang chỉnh sửa',
  PENDING_HOD_REVISION: 'Chờ TBM duyệt lại',
  PENDING_ADMIN_REPUBLISH: 'Chờ xuất bản lại',
  INACTIVE: 'Không hoạt động',
  ARCHIVED: 'Đã lưu trữ'
};

/**
 * Get allowed statuses for a specific role
 */
export function getAllowedStatuses(role: string): SyllabusStatus[] {
  const normalizedRole = role.toUpperCase();
  return ROLE_STATUS_MAP[normalizedRole] || [];
}

/**
 * Check if a role can view a specific status
 */
export function canViewStatus(role: string, status: SyllabusStatus): boolean {
  const allowedStatuses = getAllowedStatuses(role);
  return allowedStatuses.includes(status);
}

/**
 * Get status tabs configuration for a role
 */
export function getStatusTabs(role: string) {
  const allowedStatuses = getAllowedStatuses(role);
  
  return allowedStatuses.map(status => ({
    key: status,
    label: STATUS_DISPLAY_NAMES[status],
    value: status
  }));
}
