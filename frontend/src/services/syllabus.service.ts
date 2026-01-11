import {
  Syllabus,
  SyllabusStatus,
  SyllabusFilters,
  PaginationParams,
  PaginatedResponse,
  ApprovalAction,
  SyllabusComment,
} from '@/types';
import { apiClient } from '@/config/api-config';

export const syllabusService = {
  // Get paginated syllabi with filters
  getSyllabi: async (
    filters: SyllabusFilters = {},
    pagination: PaginationParams = { page: 1, pageSize: 10 }
  ): Promise<PaginatedResponse<Syllabus>> => {
    const params: Record<string, any> = {
      page: pagination.page - 1, // Backend uses 0-based index
      size: pagination.pageSize,
    };

    // Add filters to params
    if (filters.status && filters.status.length > 0) {
      params.status = filters.status.join(',');
    }
    if (filters.department && filters.department.length > 0) {
      params.department = filters.department.join(',');
    }
    if (filters.faculty && filters.faculty.length > 0) {
      params.faculty = filters.faculty.join(',');
    }
    if (filters.semester && filters.semester.length > 0) {
      params.semester = filters.semester.join(',');
    }
    if (filters.ownerId) {
      params.ownerId = filters.ownerId;
    }
    if (filters.search) {
      params.search = filters.search;
    }
    if (pagination.sortBy) {
      params.sort = `${pagination.sortBy},${pagination.sortOrder || 'ASC'}`;
    }

    const response = await apiClient.get('/api/syllabi', { params });

    return {
      data: response.data.data.content,
      total: response.data.data.totalElements,
      page: response.data.data.number + 1, // Convert back to 1-based
      pageSize: response.data.data.size,
      totalPages: response.data.data.totalPages,
    };
  },

  // Get single syllabus by ID
  getSyllabusById: async (id: string): Promise<Syllabus> => {
    const response = await apiClient.get(`/api/syllabi/${id}`);
    return response.data.data;
  },

  // Create new syllabus
  createSyllabus: async (data: Partial<Syllabus>): Promise<Syllabus> => {
    const response = await apiClient.post('/api/syllabi', data);
    return response.data.data;
  },

  // Create syllabus from teaching assignment
  createSyllabusFromAssignment: async (teachingAssignmentId: string): Promise<Syllabus> => {
    const response = await apiClient.post('/api/syllabi/from-assignment', {
      teachingAssignmentId,
    });
    return response.data.data;
  },

  // Update syllabus
  updateSyllabus: async (id: string, data: Partial<Syllabus>): Promise<Syllabus> => {
    const response = await apiClient.put(`/api/syllabi/${id}`, data);
    return response.data.data;
  },

  // Delete syllabus
  deleteSyllabus: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/syllabi/${id}`);
  },

  // Submit syllabus for approval
  submitForApproval: async (id: string, comment?: string): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabi/${id}/submit`, {
      comment: comment || '',
    });
    return response.data.data;
  },

  // Add review comment
  addComment: async (syllabusId: string, content: string): Promise<SyllabusComment> => {
    const response = await apiClient.post('/api/review-comments', {
      syllabusVersionId: syllabusId,
      content: content,
    });
    return response.data.data;
  },

  // Approval actions (Dành cho Hiệu trưởng/TBM - Giữ nguyên URL /approve)
  approveSyllabus: async (action: ApprovalAction): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabi/${action.syllabusId}/approve`, {
      comment: action.reason,
    });
    return response.data.data;
  },

  // Reject syllabus
  rejectSyllabus: async (action: ApprovalAction): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabi/${action.syllabusId}/reject`, {
      reason: action.reason,
    });
    return response.data.data;
  },

  // Get comments for syllabus
  getComments: async (syllabusId: string): Promise<SyllabusComment[]> => {
    const response = await apiClient.get(`/api/review-comments/syllabus/${syllabusId}`);
    return response.data.data;
  },

  // Get statistics
  getStatistics: async (): Promise<Record<string, number>> => {
    const response = await apiClient.get('/api/syllabi', {
      params: { page: 0, size: 1000 },
    });

    const syllabi = response.data.data.content;
    const stats: Record<string, number> = {};

    Object.values(SyllabusStatus).forEach((status) => {
      stats[status] = syllabi.filter((s: Syllabus) => s.status === status).length;
    });

    return stats;
  },

  // Export to CSV
  exportToCSV: async (filters: SyllabusFilters = {}): Promise<Blob> => {
    const { data } = await syllabusService.getSyllabi(filters, { page: 1, pageSize: 1000 });

    const headers = [
      'Mã môn',
      'Tên môn',
      'Tín chỉ',
      'Khoa',
      'Bộ môn',
      'Học kỳ',
      'Trạng thái',
      'Giảng viên',
      'Ngày tạo',
    ];

    const rows = data.map((s) => [
      s.subjectCode,
      s.subjectNameVi,
      s.creditCount,
      s.faculty,
      s.department,
      s.semester,
      s.status,
      s.ownerName,
      new Date(s.createdAt).toLocaleDateString('vi-VN'),
    ]);

    const csvContent = [headers, ...rows].map((row) => row.join(',')).join('\n');

    return new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
  },

  // ==========================================
  // ADMIN FUNCTIONS (Các hàm dành riêng cho Admin)
  // ==========================================

  // 1. Publish syllabus (Xuất hành - Gọi URL /publish mới)
  publishSyllabus: async (
    id: string,
    effectiveDate: string,
    comment?: string
  ): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabi/${id}/publish`, {
      effectiveDate,
      comment,
    });
    return response.data.data;
  },

  // 2. Unpublish syllabus (Gỡ bỏ)
  unpublishSyllabus: async (id: string, reason: string): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabi/${id}/unpublish`, { reason });
    return response.data.data;
  },

  // 3. Archive syllabus (Lưu trữ)
  archiveSyllabus: async (id: string): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabi/${id}/archive`);
    return response.data.data;
  },

  // 4. Update effective date
  updateEffectiveDate: async (id: string, effectiveDate: string): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabi/${id}/effective-date`, { effectiveDate });
    return response.data.data;
  },
};
