import { apiClient } from '@/config/api-config';

export interface Subject {
  id: string;
  code: string;
  currentNameVi: string;
  currentNameEn?: string;
  defaultCredits: number;
  defaultTheoryHours: number;
  defaultPracticeHours: number;
  defaultSelfStudyHours: number;
  subjectType?: string;
  component?: string;
  description?: string;
  departmentId?: string;
  departmentCode?: string;
  departmentName?: string;
  facultyName?: string;
  semester?: string;
  prerequisites?: string;

  recommendedTerm?: number;
  curriculumId?: string;
  curriculumCode?: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SubjectResponse {
  success: boolean;
  message: string;
  data: {
    content: Subject[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  };
}

export const subjectService = {
  async getAllSubjects(page = 0, size = 100): Promise<Subject[]> {
    try {
      const response = await apiClient.get<SubjectResponse>(
        `/api/subjects?page=${page}&size=${size}`
      );
      return response.data.data.content || [];
    } catch (error) {
      console.error('Error fetching subjects:', error);
      return [];
    }
  },

  async getSubjectById(id: string): Promise<Subject | null> {
    try {
      const response = await apiClient.get<{ success: boolean; data: Subject }>(
        `/api/subjects/${id}`
      );
      return response.data.data;
    } catch (error) {
      console.error('Error fetching subject:', error);
      return null;
    }
  },

  async getSubjectByCode(code: string): Promise<Subject | null> {
    try {
      const response = await apiClient.get<{ success: boolean; data: Subject }>(
        `/api/subjects/code/${code}`
      );
      return response.data.data;
    } catch (error) {
      console.error('Error fetching subject by code:', error);
      return null;
    }
  },

  async createSubject(data: Partial<Subject>): Promise<Subject> {
    const response = await apiClient.post<{ success: boolean; data: Subject }>(
      '/api/subjects',
      data
    );
    return response.data.data;
  },

  async updateSubject(id: string, data: Partial<Subject>): Promise<Subject> {
    const response = await apiClient.put<{ success: boolean; data: Subject }>(
      `/api/subjects/${id}`,
      data
    );
    return response.data.data;
  },

  async deleteSubject(id: string): Promise<void> {
    await apiClient.delete(`/api/subjects/${id}`);
  },

  // Relationship management
  async checkCyclicDependency(
    subjectId: string,
    prerequisiteId: string,
    type: 'PREREQUISITE' | 'CO_REQUISITE' | 'REPLACEMENT' = 'PREREQUISITE'
  ): Promise<boolean> {
    const response = await apiClient.get<{ success: boolean; data: boolean }>(
      `/api/subjects/${subjectId}/check-cycle`,
      { params: { prerequisiteId, type } }
    );
    return response.data.data;
  },

  async getAllRelationships(subjectId: string): Promise<{
    PREREQUISITE: Array<{
      id: string;
      subjectId: string;
      subjectCode: string;
      subjectName: string;
      relatedSubjectId: string;
      relatedSubjectCode: string;
      relatedSubjectName: string;
      type: string;
      createdAt: string;
    }>;
    CO_REQUISITE: Array<{
      id: string;
      subjectId: string;
      subjectCode: string;
      subjectName: string;
      relatedSubjectId: string;
      relatedSubjectCode: string;
      relatedSubjectName: string;
      type: string;
      createdAt: string;
    }>;
    REPLACEMENT: Array<{
      id: string;
      subjectId: string;
      subjectCode: string;
      subjectName: string;
      relatedSubjectId: string;
      relatedSubjectCode: string;
      relatedSubjectName: string;
      type: string;
      createdAt: string;
    }>;
  }> {
    const response = await apiClient.get(
      `/api/subjects/${subjectId}/relationships`
    );
    return response.data.data;
  },

  async deleteRelationship(subjectId: string, relationshipId: string): Promise<void> {
    await apiClient.delete(`/api/subjects/${subjectId}/prerequisites/${relationshipId}`);
  },
};

export default subjectService;
