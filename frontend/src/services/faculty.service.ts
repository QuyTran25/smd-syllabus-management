import { apiClient as api } from '@/config/api-config';

// API Response types
interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface Faculty {
  id: string;
  code: string;
  name: string;
  description?: string;
}

export interface Department {
  id: string;
  code: string;
  name: string;
  facultyId: string;
  description?: string;
}

const facultyService = {
  getAllFaculties: async (): Promise<Faculty[]> => {
    const response = await api.get<ApiResponse<Faculty[]>>('/api/faculties/all');
    return response.data.data;
  },

  getDepartmentsByFaculty: async (facultyId: string): Promise<Department[]> => {
    const response = await api.get<ApiResponse<Department[]>>(`/api/departments/faculty/${facultyId}`);
    return response.data.data;
  },
};

export default facultyService;
