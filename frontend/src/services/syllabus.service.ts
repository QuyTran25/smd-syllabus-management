import {
  Syllabus,
  SyllabusFilters,
  PaginationParams,
  PaginatedResponse,
  ApprovalAction,
  SyllabusComment,
} from '@/types';
import { apiClient } from '@/config/api-config';

export const syllabusService = {
  // Lấy danh sách đề cương có phân trang & bộ lọc
  getSyllabi: async (
    filters: SyllabusFilters = {},
    pagination: PaginationParams = { page: 1, pageSize: 10 }
  ): Promise<PaginatedResponse<Syllabus>> => {
    const params: Record<string, any> = {
      page: pagination.page - 1, // Backend dùng index bắt đầu từ 0
      size: pagination.pageSize,
    };

    if (filters.status && filters.status.length > 0) params.status = filters.status.join(',');
    if (filters.department && filters.department.length > 0) params.department = filters.department.join(',');
    if (filters.faculty && filters.faculty.length > 0) params.faculty = filters.faculty.join(',');
    if (filters.semester && filters.semester.length > 0) params.semester = filters.semester.join(',');
    if (filters.ownerId) params.ownerId = filters.ownerId;
    if (filters.search) params.search = filters.search;
    if (pagination.sortBy) params.sort = `${pagination.sortBy},${pagination.sortOrder || 'ASC'}`;

    const response = await apiClient.get('/api/syllabuses', { params });
    
    return {
      data: response.data.data.content,
      total: response.data.data.totalElements,
      page: response.data.data.number + 1,
      pageSize: response.data.data.size,
      totalPages: response.data.data.totalPages,
    };
  },

  // --- [BỔ SUNG QUAN TRỌNG] Hàm lấy danh sách của tôi cho trang ManageSyllabusesPage ---
  getMySyllabuses: async (): Promise<Syllabus[]> => {
    const response = await apiClient.get('/api/syllabuses/my-syllabuses');
    return response.data.data;
  },
  // ---------------------------------------------------------------------------------

  // --- [BỔ SUNG QUAN TRỌNG] Hàm submit cho nút "Gửi phê duyệt" ---
  submitSyllabus: async (id: string): Promise<Syllabus> => {
    // Sử dụng PATCH để đồng bộ với phong cách của approve/reject
    const response = await apiClient.patch(`/api/syllabuses/${id}/submit`);
    return response.data.data;
  },
  // ---------------------------------------------------------------------------------

  // Lấy chi tiết đề cương
  getSyllabusById: async (id: string): Promise<Syllabus> => {
    const response = await apiClient.get(`/api/syllabuses/${id}`);
    return response.data.data;
  },

  // Tạo mới
  createSyllabus: async (data: Partial<Syllabus>): Promise<Syllabus> => {
    const response = await apiClient.post('/api/syllabuses', data);
    return response.data.data;
  },

  // Cập nhật
  updateSyllabus: async (id: string, data: Partial<Syllabus>): Promise<Syllabus> => {
    const response = await apiClient.put(`/api/syllabuses/${id}`, data);
    return response.data.data;
  },

  // Xóa
  deleteSyllabus: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/syllabuses/${id}`);
  },

  // Duyệt
  approveSyllabus: async (action: ApprovalAction): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabuses/${action.syllabusId}/approve`, {
      comment: action.reason,
    });
    return response.data.data;
  },

  // Từ chối
  rejectSyllabus: async (action: ApprovalAction): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabuses/${action.syllabusId}/reject`, {
      reason: action.reason,
    });
    return response.data.data;
  },

  // Lấy bình luận
  getComments: async (syllabusId: string): Promise<SyllabusComment[]> => {
    const response = await apiClient.get(`/api/review-comments/syllabus/${syllabusId}`);
    return response.data.data;
  },

  // Thêm bình luận
  addComment: async (comment: Omit<SyllabusComment, 'id' | 'createdAt' | 'updatedAt'>): Promise<SyllabusComment> => {
    const response = await apiClient.post('/api/review-comments', comment);
    return response.data.data;
  },

  // Lấy thống kê
  getStatistics: async (): Promise<Record<string, number>> => {
    const response = await apiClient.get('/api/syllabuses/statistics');
    return response.data.data || {};
  },

  // Xuất CSV
  exportToCSV: async (filters: SyllabusFilters = {}, onlyMine = false): Promise<Blob> => {
    let data: Syllabus[] = [];
    if (onlyMine) {
      const response = await apiClient.get('/api/syllabuses/my-syllabuses');
      data = response.data.data || [];
    } else {
      const pag = await (syllabusService as any).getSyllabi(filters, { page: 1, pageSize: 1000 });
      data = pag.data;
    }

    const headers = [
      'Mã môn', 'Tên môn', 'Tín chỉ', 'Khoa', 'Bộ môn', 'Học kỳ', 'Trạng thái', 'Giảng viên', 'Ngày tạo',
    ];

    const rows = data.map((s: any) => [
      s.subjectCode || s.subject_code || '',
      s.subjectNameVi || s.subject_name_vi || s.snapSubjectNameVi || '',
      (s.creditCount ?? s.snapCreditCount) || '',
      s.faculty || '',
      s.department || '',
      s.semester || '',
      s.status || '',
      s.ownerName || s.owner_full_name || '',
      new Date(s.createdAt || s.created_at || Date.now()).toLocaleDateString('vi-VN'),
    ]);

    const csvContent = [headers, ...rows].map((row) => row.join(',')).join('\n');
    return new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
  },

  // Hủy xuất bản (Admin)
  unpublishSyllabus: async (id: string, reason: string): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabuses/${id}/unpublish`, { reason });
    return response.data.data;
  },

  // Lưu trữ (Admin)
  archiveSyllabus: async (id: string): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabuses/${id}/archive`);
    return response.data.data;
  },

  // Cập nhật ngày hiệu lực (Admin)
  updateEffectiveDate: async (id: string, effectiveDate: string): Promise<Syllabus> => {
    const response = await apiClient.patch(`/api/syllabuses/${id}/effective-date`, { effectiveDate });
    return response.data.data;
  },
};

