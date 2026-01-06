import { apiClient as api } from '@/config/api-config';

// API Response types
interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Academic Term type
export interface AcademicTerm {
  id: string;
  code: string;
  name: string;
  academicYear: string;
  startDate: string;
  endDate: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

// Request type for create/update
interface AcademicTermRequest {
  code: string;
  name: string;
  academicYear: string;
  startDate: string;
  endDate: string;
  isActive?: boolean;
}

export const academicTermService = {
  // Get all academic terms
  getAllTerms: async (): Promise<AcademicTerm[]> => {
    const response = await api.get<ApiResponse<PageResponse<AcademicTerm>>>('/api/academic-terms', {
      params: {
        page: 0,
        size: 100,
        sort: 'startDate,desc',
      },
    });

    return response.data.data.content;
  },

  // Get academic term by ID
  getTermById: async (id: string): Promise<AcademicTerm> => {
    const response = await api.get<ApiResponse<AcademicTerm>>(`/api/academic-terms/${id}`);
    return response.data.data;
  },

  // Get active academic term
  getActiveTerm: async (): Promise<AcademicTerm | null> => {
    try {
      const response = await api.get<ApiResponse<AcademicTerm>>('/api/academic-terms/current');
      return response.data.data;
    } catch {
      return null;
    }
  },

  // Get all active academic terms
  getActiveTerms: async (): Promise<AcademicTerm[]> => {
    const response = await api.get<ApiResponse<AcademicTerm[]>>('/api/academic-terms/active');
    return response.data.data;
  },

  // Create academic term
  createTerm: async (data: Omit<AcademicTerm, 'id' | 'createdAt' | 'updatedAt'>): Promise<AcademicTerm> => {
    const request: AcademicTermRequest = {
      code: data.code,
      name: data.name,
      academicYear: data.academicYear,
      startDate: data.startDate,
      endDate: data.endDate,
      isActive: data.isActive || false,
    };

    const response = await api.post<ApiResponse<AcademicTerm>>('/api/academic-terms', request);
    return response.data.data;
  },

  // Update academic term
  updateTerm: async (id: string, data: Partial<AcademicTermRequest>): Promise<AcademicTerm> => {
    const response = await api.put<ApiResponse<AcademicTerm>>(`/api/academic-terms/${id}`, data);
    return response.data.data;
  },

  // Delete academic term
  deleteTerm: async (id: string): Promise<void> => {
    await api.delete(`/api/academic-terms/${id}`);
  },

  // Set active academic term (backend will auto-deactivate others via trigger)
  setActiveTerm: async (id: string): Promise<AcademicTerm> => {
    const response = await api.patch<ApiResponse<AcademicTerm>>(`/api/academic-terms/${id}/activate`);
    return response.data.data;
  },
};
