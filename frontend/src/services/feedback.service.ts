import axiosClient from '@/api/axiosClient';
import {
  StudentFeedback,
  FeedbackStatus,
  FeedbackType,
  FeedbackFilters,
} from '@/types';

export interface StudentFeedbackResponse {
  id: string;
  syllabusId: string;
  syllabusCode: string;
  syllabusName: string;
  studentId: string;
  studentName: string;
  studentEmail: string;
  type: FeedbackType;
  section: string;
  title: string;
  description: string;
  status: string;
  adminResponse?: string;
  respondedById?: string;
  respondedByName?: string;
  respondedAt?: string;
  editEnabled: boolean;
  editEnabledAt?: string;
  editEnabledBy?: string;
  resolvedById?: string;
  resolvedByName?: string;
  resolvedAt?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Map backend response to frontend StudentFeedback type
 * ANTI-CONFLICT: Đảm bảo fallback giá trị rỗng để tránh lỗi giao diện
 */
const mapToStudentFeedback = (response: StudentFeedbackResponse): StudentFeedback => ({
  id: response.id,
  syllabusId: response.syllabusId,
  syllabusCode: response.syllabusCode || '',
  syllabusName: response.syllabusName || '',
  studentId: response.studentId,
  studentName: response.studentName || '',
  studentEmail: response.studentEmail || '',
  type: response.type,
  section: response.section,
  title: response.title || '',
  description: response.description,
  status: response.status as FeedbackStatus,
  adminResponse: response.adminResponse,
  respondedBy: response.respondedByName,
  respondedAt: response.respondedAt,
  editEnabled: response.editEnabled,
  editEnabledAt: response.editEnabledAt,
  editEnabledBy: response.editEnabledBy,
  createdAt: response.createdAt,
  updatedAt: response.updatedAt,
});

export const feedbackService = {
  getFeedbacks: async (filters: FeedbackFilters = {}): Promise<StudentFeedback[]> => {
    try {
      const params = new URLSearchParams();
      params.append('page', '0');
      params.append('size', '100');
      params.append('sort', 'createdAt,desc');
      
      // FIX: Gọi qua Gateway 8888 sử dụng axiosClient
      const response = await axiosClient.get<{ data: { content: StudentFeedbackResponse[] } }>(
        `/api/student-feedbacks?${params.toString()}`
      );
      
      // ANTI-CONFLICT: Xử lý payload linh hoạt cho cả trường hợp data bọc hoặc không bọc
      const rawContent = response.data?.data?.content || 
                         (response.data as any)?.content || 
                         [];
                         
      let feedbacks = rawContent.map(mapToStudentFeedback);
      
      // Client-side filtering logic
      if (filters.status && filters.status.length > 0) {
        feedbacks = feedbacks.filter((f) => filters.status?.includes(f.status));
      }

      if (filters.type && filters.type.length > 0) {
        feedbacks = feedbacks.filter((f) => filters.type?.includes(f.type));
      }

      if (filters.syllabusId) {
        feedbacks = feedbacks.filter((f) => f.syllabusId === filters.syllabusId);
      }

      if (filters.studentId) {
        feedbacks = feedbacks.filter((f) => f.studentId === filters.studentId);
      }

      if (filters.search) {
        const searchLower = filters.search.toLowerCase();
        feedbacks = feedbacks.filter(
          (f) =>
            f.title.toLowerCase().includes(searchLower) ||
            f.description.toLowerCase().includes(searchLower) ||
            f.syllabusCode.toLowerCase().includes(searchLower) ||
            f.syllabusName.toLowerCase().includes(searchLower) ||
            f.studentName.toLowerCase().includes(searchLower)
        );
      }

      return feedbacks;
    } catch (error) {
      console.error('Error fetching feedbacks:', error);
      return [];
    }
  },

  getFeedbackById: async (id: string): Promise<StudentFeedback> => {
    const response = await axiosClient.get<{ data: StudentFeedbackResponse }>(
      `/api/student-feedbacks/${id}`
    );
    const data = response.data?.data || response.data;
    return mapToStudentFeedback(data as unknown as StudentFeedbackResponse);
  },

  respondToFeedback: async (
    id: string,
    responseContent: string,
    _respondedBy: string
  ): Promise<StudentFeedback> => {
    const apiResponse = await axiosClient.post<{ data: StudentFeedbackResponse }>(
      `/api/student-feedbacks/${id}/respond`,
      { response: responseContent, enableEdit: false }
    );
    const data = apiResponse.data?.data || apiResponse.data;
    return mapToStudentFeedback(data as unknown as StudentFeedbackResponse);
  },

  enableEditForLecturer: async (id: string, _enabledBy: string): Promise<StudentFeedback> => {
    const response = await axiosClient.post<{ data: StudentFeedbackResponse }>(
      `/api/student-feedbacks/${id}/enable-edit`
    );
    const data = response.data?.data || response.data;
    return mapToStudentFeedback(data as unknown as StudentFeedbackResponse);
  },

  updateFeedbackStatus: async (
    id: string,
    status: FeedbackStatus
  ): Promise<StudentFeedback> => {
    const response = await axiosClient.patch<{ data: StudentFeedbackResponse }>(
      `/api/student-feedbacks/${id}/status?status=${status}`
    );
    const data = response.data?.data || response.data;
    return mapToStudentFeedback(data as unknown as StudentFeedbackResponse);
  },

  getStatistics: async (): Promise<Record<string, number>> => {
    try {
      const feedbacks = await feedbackService.getFeedbacks();
      const stats: Record<string, number> = {};

      Object.values(FeedbackStatus).forEach((status) => {
        stats[status] = feedbacks.filter((f) => f.status === status).length;
      });

      return stats;
    } catch (error) {
      console.error('Error getting statistics:', error);
      return {};
    }
  },
};