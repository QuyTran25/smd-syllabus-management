import axiosClient from '@/api/axiosClient';
import { Course, CourseFilters, CoursePrerequisite } from '@/types';

export const courseService = {
  getCourses: async (filters?: CourseFilters): Promise<Course[]> => {
    const resp = await axiosClient.get('/courses', { params: filters });
    const payload = resp.data?.data ?? resp.data;
    return payload.rows ?? payload;
  },

  getCourseById: async (id: string): Promise<Course> => {
    const resp = await axiosClient.get(`/courses/${id}`);
    return resp.data?.data ?? resp.data;
  },

  getCourseByCode: async (code: string): Promise<Course | null> => {
    const resp = await axiosClient.get('/courses', { params: { code } });
    const payload = resp.data?.data ?? resp.data;
    const rows = payload.rows ?? payload;
    return rows.length > 0 ? rows[0] : null;
  },

  createCourse: async (data: Omit<Course, 'id' | 'createdAt' | 'updatedAt'>): Promise<Course> => {
    const resp = await axiosClient.post('/courses', data);
    return resp.data?.data ?? resp.data;
  },

  updateCourse: async (id: string, data: Partial<Course>): Promise<Course> => {
    const resp = await axiosClient.put(`/courses/${id}`, data);
    return resp.data?.data ?? resp.data;
  },

  deleteCourse: async (id: string): Promise<void> => {
    await axiosClient.delete(`/courses/${id}`);
  },

  updatePrerequisites: async (courseId: string, prerequisites: CoursePrerequisite[]): Promise<Course> => {
    const resp = await axiosClient.put(`/courses/${courseId}/prerequisites`, { prerequisites });
    return resp.data?.data ?? resp.data;
  },

  toggleCourseStatus: async (id: string): Promise<Course> => {
    const resp = await axiosClient.post(`/courses/${id}/toggle-status`);
    return resp.data?.data ?? resp.data;
  },
};
