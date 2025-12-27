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
};

export default subjectService;
