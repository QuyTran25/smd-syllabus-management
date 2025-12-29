import axiosClient from '@/api/axiosClient';
import { StudentFeedback, FeedbackStatus, FeedbackFilters } from '@/types';

export const feedbackService = {
  getFeedbacks: async (filters: FeedbackFilters = {}): Promise<StudentFeedback[]> => {
    const resp = await axiosClient.get('/feedbacks', { params: filters });
    const payload = resp.data?.data ?? resp.data;
    return payload.rows ?? payload;
  },

  getFeedbackById: async (id: string): Promise<StudentFeedback> => {
    const resp = await axiosClient.get(`/feedbacks/${id}`);
    return resp.data?.data ?? resp.data;
  },

  respondToFeedback: async (
    id: string,
    response: string,
    respondedBy: string
  ): Promise<StudentFeedback> => {
    const resp = await axiosClient.post(`/feedbacks/${id}/respond`, { response, respondedBy });
    return resp.data?.data ?? resp.data;
  },

  enableEditForLecturer: async (id: string, enabledBy: string): Promise<StudentFeedback> => {
    const resp = await axiosClient.post(`/feedbacks/${id}/enable-edit`, { enabledBy });
    return resp.data?.data ?? resp.data;
  },

  updateFeedbackStatus: async (id: string, status: FeedbackStatus): Promise<StudentFeedback> => {
    const resp = await axiosClient.put(`/feedbacks/${id}/status`, { status });
    return resp.data?.data ?? resp.data;
  },

  getStatistics: async (): Promise<Record<string, number>> => {
    const resp = await axiosClient.get('/feedbacks/statistics');
    return resp.data?.data ?? resp.data;
  },
};
