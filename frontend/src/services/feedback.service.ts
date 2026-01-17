import { apiClient as api } from '@/config/api-config';
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
  lecturerId?: string;
  lecturerName?: string;
  lecturerEmail?: string;
  studentId: string;
  studentName: string;
  studentEmail: string;
  type: FeedbackType;
  section: string;
  sectionDisplay?: string;
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

// Map backend response to frontend type
const mapToStudentFeedback = (response: StudentFeedbackResponse): StudentFeedback => ({
  id: response.id,
  syllabusId: response.syllabusId,
  syllabusCode: response.syllabusCode || '',
  syllabusName: response.syllabusName || '',
  lecturerId: response.lecturerId,
  lecturerName: response.lecturerName,
  lecturerEmail: response.lecturerEmail,
  studentId: response.studentId,
  studentName: response.studentName || '',
  studentEmail: response.studentEmail || '',
  type: response.type,
  section: response.section,
  sectionDisplay: response.sectionDisplay,
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
  // Get feedbacks with filters
  getFeedbacks: async (filters: FeedbackFilters = {}): Promise<StudentFeedback[]> => {
    try {
      const params = new URLSearchParams();
      params.append('page', '0');
      params.append('size', '100');
      params.append('sort', 'createdAt,desc');
      
      const response = await api.get<{ data: { content: StudentFeedbackResponse[] } }>(
        `/api/student-feedbacks?${params.toString()}`
      );
      
      let feedbacks = response.data.data.content.map(mapToStudentFeedback);
      
      // Apply client-side filters
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

  // Get feedback by ID
  getFeedbackById: async (id: string): Promise<StudentFeedback> => {
    const response = await api.get<{ data: StudentFeedbackResponse }>(
      `/api/student-feedbacks/${id}`
    );
    return mapToStudentFeedback(response.data.data);
  },

  // Respond to feedback
  respondToFeedback: async (
    id: string,
    response: string,
    enableEdit: boolean = false,
    _respondedBy?: string
  ): Promise<StudentFeedback> => {
    const apiResponse = await api.post<{ data: StudentFeedbackResponse }>(
      `/api/student-feedbacks/${id}/respond`,
      { response, enableEdit }
    );
    return mapToStudentFeedback(apiResponse.data.data);
  },

  // Enable edit for lecturer
  enableEditForLecturer: async (id: string, _enabledBy: string): Promise<StudentFeedback> => {
    const response = await api.post<{ data: StudentFeedbackResponse }>(
      `/api/student-feedbacks/${id}/enable-edit`
    );
    return mapToStudentFeedback(response.data.data);
  },

  // Update feedback status
  updateFeedbackStatus: async (
    id: string,
    status: FeedbackStatus
  ): Promise<StudentFeedback> => {
    const response = await api.patch<{ data: StudentFeedbackResponse }>(
      `/api/student-feedbacks/${id}/status?status=${status}`
    );
    return mapToStudentFeedback(response.data.data);
  },

  // Get statistics
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
