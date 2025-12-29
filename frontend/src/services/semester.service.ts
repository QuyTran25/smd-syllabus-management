import axiosClient from '@/api/axiosClient';
import { Semester, SemesterFilters } from '@/types';

export const semesterService = {
  getSemesters: async (filters?: SemesterFilters): Promise<Semester[]> => {
    const resp = await axiosClient.get('/semesters', { params: filters });
    const payload = resp.data?.data ?? resp.data;
    return payload.rows ?? payload;
  },

  getSemesterById: async (id: string): Promise<Semester> => {
    const resp = await axiosClient.get(`/semesters/${id}`);
    return resp.data?.data ?? resp.data;
  },

  getActiveSemester: async (): Promise<Semester | null> => {
    const resp = await axiosClient.get('/semesters/active');
    return resp.data?.data ?? resp.data;
  },

  createSemester: async (data: Omit<Semester, 'id' | 'createdAt' | 'updatedAt'>): Promise<Semester> => {
    const resp = await axiosClient.post('/semesters', data);
    return resp.data?.data ?? resp.data;
  },

  updateSemester: async (id: string, data: Partial<Semester>): Promise<Semester> => {
    const resp = await axiosClient.put(`/semesters/${id}`, data);
    return resp.data?.data ?? resp.data;
  },

  deleteSemester: async (id: string): Promise<void> => {
    await axiosClient.delete(`/semesters/${id}`);
  },

  setActiveSemester: async (id: string): Promise<Semester> => {
    const resp = await axiosClient.post(`/semesters/${id}/set-active`);
    return resp.data?.data ?? resp.data;
  },
};
