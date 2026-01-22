import { apiClient as api } from '@/config/api-config';
import {
  RevisionSession,
  StartRevisionRequest,
  SubmitRevisionRequest,
  ReviewRevisionRequest,
} from '@/types';

export const revisionService = {
  /**
   * Admin starts a revision session
   */
  startRevision: async (request: StartRevisionRequest): Promise<RevisionSession> => {
    const response = await api.post<{ data: RevisionSession }>(
      '/revisions/start',
      request
    );
    return response.data.data;
  },

  /**
   * Lecturer submits revision to HOD
   */
  submitRevision: async (request: SubmitRevisionRequest): Promise<RevisionSession> => {
    const response = await api.post<{ data: RevisionSession }>(
      '/revisions/submit',
      request
    );
    return response.data.data;
  },

  /**
   * HOD reviews revision
   */
  reviewRevision: async (request: ReviewRevisionRequest): Promise<RevisionSession> => {
    const response = await api.post<{ data: RevisionSession }>(
      '/revisions/review',
      request
    );
    return response.data.data;
  },

  /**
   * Admin republishes syllabus
   */
  republishSyllabus: async (sessionId: string): Promise<RevisionSession> => {
    const response = await api.post<{ data: RevisionSession }>(
      `/revisions/${sessionId}/republish`
    );
    return response.data.data;
  },

  /**
   * Get pending HOD reviews
   */
  getPendingHodReview: async (): Promise<RevisionSession[]> => {
    const response = await api.get<{ data: RevisionSession[] }>(
      '/revisions/pending-hod'
    );
    return response.data.data;
  },

  /**
   * Get pending republish sessions
   */
  getPendingRepublish: async (): Promise<RevisionSession[]> => {
    const response = await api.get<{ data: RevisionSession[] }>(
      '/revisions/pending-republish'
    );
    return response.data.data;
  },

  /**
   * Get active revision session for a syllabus
   */
  getActiveRevisionSession: async (syllabusId: string): Promise<RevisionSession | null> => {
    try {
      const response = await api.get<{ data: RevisionSession }>(
        `/revisions/syllabus/${syllabusId}/active`
      );
      return response.data.data;
    } catch (error: any) {
      // If no active session found (404), return null instead of throwing
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  /**
   * Get completed revision session for a syllabus (for republishing)
   */
  getCompletedRevisionSession: async (syllabusId: string): Promise<RevisionSession | null> => {
    try {
      const response = await api.get<{ data: RevisionSession }>(
        `/revisions/syllabus/${syllabusId}/completed`
      );
      return response.data.data;
    } catch (error: any) {
      // If no completed session found (404), return null instead of throwing
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },
};
