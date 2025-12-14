import {
  StudentFeedback,
  FeedbackStatus,
  FeedbackType,
  FeedbackFilters,
} from '@/types';

// Mock feedback data
export const mockFeedbacks: StudentFeedback[] = [
  {
    id: 'fb-001',
    syllabusId: 'syl-001',
    syllabusCode: 'CS101',
    syllabusName: 'Nhập môn Lập trình',
    studentId: 'std-001',
    studentName: 'Nguyễn Văn Sinh Viên A',
    studentEmail: 'student.a@university.edu.vn',
    type: FeedbackType.ERROR,
    section: 'CLO',
    title: 'CLO 1.2 có lỗi chính tả',
    description: 'CLO 1.2 ghi "áp dungj" thay vì "áp dụng". Đề nghị sửa lại.',
    status: FeedbackStatus.PENDING,
    editEnabled: false,
    createdAt: new Date(Date.now() - 86400000 * 2).toISOString(),
    updatedAt: new Date(Date.now() - 86400000 * 2).toISOString(),
  },
  {
    id: 'fb-002',
    syllabusId: 'syl-001',
    syllabusCode: 'CS101',
    syllabusName: 'Nhập môn Lập trình',
    studentId: 'std-002',
    studentName: 'Trần Thị Sinh Viên B',
    studentEmail: 'student.b@university.edu.vn',
    type: FeedbackType.ERROR,
    section: 'Assessment',
    title: 'Tổng % đánh giá không đúng 100%',
    description: 'Tổng tỷ lệ các phương pháp đánh giá chỉ có 95%, thiếu 5%. Đề nghị kiểm tra lại.',
    status: FeedbackStatus.IN_REVIEW,
    adminResponse: 'Đã xác nhận có lỗi. Đang gửi yêu cầu giảng viên chỉnh sửa.',
    respondedBy: 'Admin User',
    respondedAt: new Date(Date.now() - 43200000).toISOString(),
    editEnabled: true,
    editEnabledAt: new Date(Date.now() - 43200000).toISOString(),
    editEnabledBy: 'Admin User',
    createdAt: new Date(Date.now() - 86400000 * 3).toISOString(),
    updatedAt: new Date(Date.now() - 43200000).toISOString(),
  },
  {
    id: 'fb-003',
    syllabusId: 'syl-002',
    syllabusCode: 'CS201',
    syllabusName: 'Cấu trúc Dữ liệu & Giải thuật',
    studentId: 'std-003',
    studentName: 'Lê Văn Sinh Viên C',
    studentEmail: 'student.c@university.edu.vn',
    type: FeedbackType.SUGGESTION,
    section: 'Materials',
    title: 'Đề xuất thêm tài liệu tham khảo',
    description: 'Đề xuất thêm sách "Introduction to Algorithms" (CLRS) vào danh mục tài liệu tham khảo.',
    status: FeedbackStatus.RESOLVED,
    adminResponse: 'Đã chuyển đề xuất cho giảng viên xem xét. Giảng viên đã thêm sách vào đề cương.',
    respondedBy: 'Admin User',
    respondedAt: new Date(Date.now() - 86400000).toISOString(),
    editEnabled: false,
    createdAt: new Date(Date.now() - 86400000 * 5).toISOString(),
    updatedAt: new Date(Date.now() - 86400000).toISOString(),
  },
  {
    id: 'fb-004',
    syllabusId: 'syl-003',
    syllabusCode: 'CS202',
    syllabusName: 'Hệ Quản trị CSDL',
    studentId: 'std-004',
    studentName: 'Phạm Thị Sinh Viên D',
    studentEmail: 'student.d@university.edu.vn',
    type: FeedbackType.ERROR,
    section: 'Prerequisites',
    title: 'Thiếu môn học tiên quyết CS101',
    description: 'Đề cương không liệt kê CS101 là môn tiên quyết, nhưng trong lớp học thầy có yêu cầu.',
    status: FeedbackStatus.PENDING,
    editEnabled: false,
    createdAt: new Date(Date.now() - 21600000).toISOString(),
    updatedAt: new Date(Date.now() - 21600000).toISOString(),
  },
  {
    id: 'fb-005',
    syllabusId: 'syl-002',
    syllabusCode: 'CS201',
    syllabusName: 'Cấu trúc Dữ liệu & Giải thuật',
    studentId: 'std-005',
    studentName: 'Hoàng Văn Sinh Viên E',
    studentEmail: 'student.e@university.edu.vn',
    type: FeedbackType.QUESTION,
    section: 'Outline',
    title: 'Làm rõ nội dung buổi 7',
    description: 'Buổi 7 ghi "Dynamic Programming" nhưng không có ví dụ cụ thể. Có thể bổ sung các bài toán mẫu?',
    status: FeedbackStatus.RESOLVED,
    adminResponse: 'Đã gửi yêu cầu cho giảng viên. Giảng viên đã bổ sung 3 ví dụ trong outline.',
    respondedBy: 'Admin User',
    respondedAt: new Date(Date.now() - 172800000).toISOString(),
    editEnabled: false,
    createdAt: new Date(Date.now() - 86400000 * 4).toISOString(),
    updatedAt: new Date(Date.now() - 172800000).toISOString(),
  },
];

// Simulate API delay
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const feedbackService = {
  // Get feedbacks with filters
  getFeedbacks: async (filters: FeedbackFilters = {}): Promise<StudentFeedback[]> => {
    await delay(500);

    let filtered = [...mockFeedbacks];

    if (filters.status && filters.status.length > 0) {
      filtered = filtered.filter((f) => filters.status?.includes(f.status));
    }

    if (filters.type && filters.type.length > 0) {
      filtered = filtered.filter((f) => filters.type?.includes(f.type));
    }

    if (filters.syllabusId) {
      filtered = filtered.filter((f) => f.syllabusId === filters.syllabusId);
    }

    if (filters.studentId) {
      filtered = filtered.filter((f) => f.studentId === filters.studentId);
    }

    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(
        (f) =>
          f.title.toLowerCase().includes(searchLower) ||
          f.description.toLowerCase().includes(searchLower) ||
          f.syllabusCode.toLowerCase().includes(searchLower) ||
          f.syllabusName.toLowerCase().includes(searchLower) ||
          f.studentName.toLowerCase().includes(searchLower)
      );
    }

    // Sort by createdAt descending
    filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    return filtered;
  },

  // Get feedback by ID
  getFeedbackById: async (id: string): Promise<StudentFeedback> => {
    await delay(300);

    const feedback = mockFeedbacks.find((f) => f.id === id);
    if (!feedback) {
      throw new Error('Feedback not found');
    }

    return feedback;
  },

  // Respond to feedback
  respondToFeedback: async (
    id: string,
    response: string,
    respondedBy: string
  ): Promise<StudentFeedback> => {
    await delay(800);

    const index = mockFeedbacks.findIndex((f) => f.id === id);
    if (index === -1) {
      throw new Error('Feedback not found');
    }

    const now = new Date().toISOString();
    const updated: StudentFeedback = {
      ...mockFeedbacks[index],
      status: FeedbackStatus.IN_REVIEW,
      adminResponse: response,
      respondedBy,
      respondedAt: now,
      updatedAt: now,
    };

    mockFeedbacks[index] = updated;
    return updated;
  },

  // Enable edit for lecturer
  enableEditForLecturer: async (id: string, enabledBy: string): Promise<StudentFeedback> => {
    await delay(800);

    const index = mockFeedbacks.findIndex((f) => f.id === id);
    if (index === -1) {
      throw new Error('Feedback not found');
    }

    const now = new Date().toISOString();
    const updated: StudentFeedback = {
      ...mockFeedbacks[index],
      editEnabled: true,
      editEnabledAt: now,
      editEnabledBy: enabledBy,
      updatedAt: now,
    };

    mockFeedbacks[index] = updated;
    return updated;
  },

  // Update feedback status
  updateFeedbackStatus: async (
    id: string,
    status: FeedbackStatus
  ): Promise<StudentFeedback> => {
    await delay(500);

    const index = mockFeedbacks.findIndex((f) => f.id === id);
    if (index === -1) {
      throw new Error('Feedback not found');
    }

    const updated: StudentFeedback = {
      ...mockFeedbacks[index],
      status,
      updatedAt: new Date().toISOString(),
    };

    mockFeedbacks[index] = updated;
    return updated;
  },

  // Get statistics
  getStatistics: async (): Promise<Record<string, number>> => {
    await delay(300);

    const stats: Record<string, number> = {};

    Object.values(FeedbackStatus).forEach((status) => {
      stats[status] = mockFeedbacks.filter((f) => f.status === status).length;
    });

    return stats;
  },
};
