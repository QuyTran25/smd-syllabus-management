import {
  Syllabus,
  SyllabusStatus,
  SyllabusFilters,
  PaginationParams,
  PaginatedResponse,
  ApprovalAction,
  SyllabusComment,
} from '@/types';
import { mockSyllabi } from '@/mock';

// Simulate API delay
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const syllabusService = {
  // Get paginated syllabi with filters
  getSyllabi: async (
    filters: SyllabusFilters = {},
    pagination: PaginationParams = { page: 1, pageSize: 10 }
  ): Promise<PaginatedResponse<Syllabus>> => {
    await delay(500);

    let filtered = [...mockSyllabi];

    // Apply filters
    if (filters.status && filters.status.length > 0) {
      filtered = filtered.filter((s) => filters.status?.includes(s.status));
    }

    if (filters.department && filters.department.length > 0) {
      filtered = filtered.filter((s) => filters.department?.includes(s.department));
    }

    if (filters.faculty && filters.faculty.length > 0) {
      filtered = filtered.filter((s) => filters.faculty?.includes(s.faculty));
    }

    if (filters.semester && filters.semester.length > 0) {
      filtered = filtered.filter((s) => filters.semester?.includes(s.semester));
    }

    if (filters.ownerId) {
      filtered = filtered.filter((s) => s.ownerId === filters.ownerId);
    }

    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(
        (s) =>
          s.courseName.toLowerCase().includes(searchLower) ||
          s.courseCode.toLowerCase().includes(searchLower) ||
          s.ownerName.toLowerCase().includes(searchLower)
      );
    }

    // Apply sorting
    if (pagination.sortBy) {
      filtered.sort((a, b) => {
        const aValue = a[pagination.sortBy as keyof Syllabus];
        const bValue = b[pagination.sortBy as keyof Syllabus];

        if (!aValue || !bValue) return 0;

        if (pagination.sortOrder === 'DESC') {
          return aValue > bValue ? -1 : 1;
        }
        return aValue > bValue ? 1 : -1;
      });
    }

    // Calculate pagination
    const total = filtered.length;
    const totalPages = Math.ceil(total / pagination.pageSize);
    const start = (pagination.page - 1) * pagination.pageSize;
    const end = start + pagination.pageSize;
    const data = filtered.slice(start, end);

    return {
      data,
      total,
      page: pagination.page,
      pageSize: pagination.pageSize,
      totalPages,
    };
  },

  // Get single syllabus by ID
  getSyllabusById: async (id: string): Promise<Syllabus> => {
    await delay(300);

    const syllabus = mockSyllabi.find((s) => s.id === id);
    if (!syllabus) {
      throw new Error('Syllabus not found');
    }

    return syllabus;
  },

  // Create new syllabus
  createSyllabus: async (data: Partial<Syllabus>): Promise<Syllabus> => {
    await delay(800);

    const newSyllabus: Syllabus = {
      id: `syllabus-${Date.now()}`,
      ...data,
      status: SyllabusStatus.DRAFT,
      version: 1,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    } as Syllabus;

    mockSyllabi.push(newSyllabus);
    return newSyllabus;
  },

  // Update syllabus
  updateSyllabus: async (id: string, data: Partial<Syllabus>): Promise<Syllabus> => {
    await delay(800);

    const index = mockSyllabi.findIndex((s) => s.id === id);
    if (index === -1) {
      throw new Error('Syllabus not found');
    }

    const updated = {
      ...mockSyllabi[index],
      ...data,
      updatedAt: new Date().toISOString(),
    };

    mockSyllabi[index] = updated;
    return updated;
  },

  // Delete syllabus
  deleteSyllabus: async (id: string): Promise<void> => {
    await delay(500);

    const index = mockSyllabi.findIndex((s) => s.id === id);
    if (index === -1) {
      throw new Error('Syllabus not found');
    }

    mockSyllabi.splice(index, 1);
  },

  // Approval actions
  approveSyllabus: async (action: ApprovalAction): Promise<Syllabus> => {
    await delay(1000);

    const index = mockSyllabi.findIndex((s) => s.id === action.syllabusId);
    if (index === -1) {
      throw new Error('Syllabus not found');
    }

    const syllabus = mockSyllabi[index];
    const now = new Date().toISOString();

    // State machine logic
    let newStatus: SyllabusStatus;
    
    if (action.action === 'REJECT') {
      newStatus = SyllabusStatus.DRAFT;
    } else {
      // APPROVE logic
      switch (syllabus.status) {
        case SyllabusStatus.PENDING_HOD:
          newStatus = SyllabusStatus.PENDING_AA;
          syllabus.hodApprovedAt = now;
          break;
        case SyllabusStatus.PENDING_AA:
          newStatus = SyllabusStatus.PENDING_PRINCIPAL;
          syllabus.aaApprovedAt = now;
          break;
        case SyllabusStatus.PENDING_PRINCIPAL:
          newStatus = SyllabusStatus.APPROVED;
          syllabus.principalApprovedAt = now;
          break;
        case SyllabusStatus.APPROVED:
          newStatus = SyllabusStatus.PUBLISHED;
          syllabus.publishedAt = now;
          break;
        default:
          throw new Error('Invalid status for approval');
      }
    }

    const updated = {
      ...syllabus,
      status: newStatus,
      updatedAt: now,
    };

    mockSyllabi[index] = updated;
    return updated;
  },

  // Get comments for syllabus
  getComments: async (syllabusId: string): Promise<SyllabusComment[]> => {
    await delay(300);

    // Mock comments
    return [
      {
        id: 'comment-1',
        syllabusId,
        userId: 'hod-001',
        userName: 'TS. Nguyễn Văn Trưởng Bộ Môn',
        userRole: 'HOD',
        content: 'Nội dung CLO cần bổ sung thêm chi tiết về kỹ năng thực hành.',
        type: 'INLINE',
        section: 'CLO',
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        updatedAt: new Date(Date.now() - 86400000).toISOString(),
      },
      {
        id: 'comment-2',
        syllabusId,
        userId: 'aa-001',
        userName: 'TS. Phạm Thị Phòng Đào Tạo',
        userRole: 'AA',
        content: 'Ánh xạ PLO cần được kiểm tra lại theo chuẩn đầu ra của chương trình.',
        type: 'INLINE',
        section: 'PLO Mapping',
        createdAt: new Date(Date.now() - 43200000).toISOString(),
        updatedAt: new Date(Date.now() - 43200000).toISOString(),
      },
    ];
  },

  // Add comment
  addComment: async (comment: Omit<SyllabusComment, 'id' | 'createdAt' | 'updatedAt'>): Promise<SyllabusComment> => {
    await delay(500);

    const newComment: SyllabusComment = {
      ...comment,
      id: `comment-${Date.now()}`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    return newComment;
  },

  // Get statistics
  getStatistics: async (): Promise<Record<string, number>> => {
    await delay(300);

    const stats: Record<string, number> = {};
    
    Object.values(SyllabusStatus).forEach((status) => {
      stats[status] = mockSyllabi.filter((s) => s.status === status).length;
    });

    return stats;
  },

  // Export to CSV
  exportToCSV: async (filters: SyllabusFilters = {}): Promise<Blob> => {
    await delay(1000);

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
      s.courseCode,
      s.courseName,
      s.credits,
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

  // Unpublish syllabus (Admin only)
  unpublishSyllabus: async (id: string, reason: string): Promise<Syllabus> => {
    await delay(800);

    const index = mockSyllabi.findIndex((s) => s.id === id);
    if (index === -1) {
      throw new Error('Syllabus not found');
    }

    const syllabus = mockSyllabi[index];
    if (syllabus.status !== SyllabusStatus.PUBLISHED) {
      throw new Error('Only published syllabi can be unpublished');
    }

    const now = new Date().toISOString();

    const updated = {
      ...syllabus,
      status: SyllabusStatus.ARCHIVED,
      archivedAt: now,
      archivedBy: 'Admin User',
      unpublishReason: reason,
      updatedAt: now,
    };

    mockSyllabi[index] = updated;
    return updated;
  },

  // Archive syllabus (Admin only)
  archiveSyllabus: async (id: string): Promise<Syllabus> => {
    await delay(800);

    const index = mockSyllabi.findIndex((s) => s.id === id);
    if (index === -1) {
      throw new Error('Syllabus not found');
    }

    const updated = {
      ...mockSyllabi[index],
      status: SyllabusStatus.ARCHIVED,
      archivedAt: new Date().toISOString(),
      archivedBy: 'Admin User',
      updatedAt: new Date().toISOString(),
    };

    mockSyllabi[index] = updated;
    return updated;
  },

  // Update effective date (Admin only)
  updateEffectiveDate: async (id: string, effectiveDate: string): Promise<Syllabus> => {
    await delay(500);

    const index = mockSyllabi.findIndex((s) => s.id === id);
    if (index === -1) {
      throw new Error('Syllabus not found');
    }

    const updated = {
      ...mockSyllabi[index],
      effectiveDate,
      updatedAt: new Date().toISOString(),
    };

    mockSyllabi[index] = updated;
    return updated;
  },
};

