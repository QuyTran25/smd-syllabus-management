import { Semester, SemesterFilters } from '@/types';
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

// Backend semester response type
interface SemesterApiResponse {
  id: string;
  code: string;
  name: string;
  semesterNumber: number;
  academicYear: string;
  startDate: string;
  endDate: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Request type for create/update
interface SemesterRequest {
  code: string;
  name: string;
  semesterNumber: number;
  academicYear: string;
  startDate: string;
  endDate: string;
  isActive?: boolean;
}

// Map API response to frontend Semester type
const mapToSemester = (data: SemesterApiResponse): Semester => ({
  id: data.id,
  code: data.code,
  name: data.name,
  semesterNumber: data.semesterNumber,
  academicYear: data.academicYear,
  startDate: data.startDate,
  endDate: data.endDate,
  isActive: data.isActive,
  createdAt: data.createdAt,
  updatedAt: data.updatedAt,
});

export const semesterService = {
  // Get all semesters
  getSemesters: async (filters?: SemesterFilters): Promise<Semester[]> => {
    const response = await api.get<ApiResponse<PageResponse<SemesterApiResponse>>>('/api/semesters', {
      params: {
        page: 0,
        size: 100,
        sort: 'startDate,desc',
      },
    });

    let semesters = response.data.data.content.map(mapToSemester);

    // Apply client-side filtering
    if (filters?.isActive !== undefined) {
      semesters = semesters.filter((s) => s.isActive === filters.isActive);
    }

    if (filters?.academicYear) {
      semesters = semesters.filter((s) => s.academicYear === filters.academicYear);
    }

    if (filters?.search) {
      const searchLower = filters.search.toLowerCase();
      semesters = semesters.filter(
        (s) =>
          s.code.toLowerCase().includes(searchLower) ||
          s.name.toLowerCase().includes(searchLower)
      );
    }

    return semesters.sort((a, b) =>
      new Date(b.startDate).getTime() - new Date(a.startDate).getTime()
    );
  },

  // Get semester by ID
  getSemesterById: async (id: string): Promise<Semester> => {
    const response = await api.get<ApiResponse<SemesterApiResponse>>(`/api/semesters/${id}`);
    return mapToSemester(response.data.data);
  },

  // Get active semester
  getActiveSemester: async (): Promise<Semester | null> => {
    try {
      const response = await api.get<ApiResponse<SemesterApiResponse>>('/api/semesters/current');
      return mapToSemester(response.data.data);
    } catch {
      // If no current semester is set, return null
      return null;
    }
  },

  // Create semester
  createSemester: async (data: Omit<Semester, 'id' | 'createdAt' | 'updatedAt'>): Promise<Semester> => {
    const request: SemesterRequest = {
      code: data.code,
      name: data.name,
      semesterNumber: data.semesterNumber || 1,
      academicYear: data.academicYear,
      startDate: data.startDate,
      endDate: data.endDate,
      isActive: data.isActive,
    };

    const response = await api.post<ApiResponse<SemesterApiResponse>>('/api/semesters', request);
    return mapToSemester(response.data.data);
  },

  // Update semester
  updateSemester: async (id: string, data: Partial<Semester>): Promise<Semester> => {
    // First get the existing semester to merge with updates
    const existing = await semesterService.getSemesterById(id);

    const request: SemesterRequest = {
      code: data.code ?? existing.code,
      name: data.name ?? existing.name,
      semesterNumber: data.semesterNumber ?? existing.semesterNumber ?? 1,
      academicYear: data.academicYear ?? existing.academicYear,
      startDate: data.startDate ?? existing.startDate,
      endDate: data.endDate ?? existing.endDate,
      isActive: data.isActive ?? existing.isActive,
    };

    const response = await api.put<ApiResponse<SemesterApiResponse>>(`/api/semesters/${id}`, request);
    return mapToSemester(response.data.data);
  },

  // Delete semester
  deleteSemester: async (id: string): Promise<void> => {
    await api.delete(`/api/semesters/${id}`);
  },

  // Set active semester (only one can be active at a time)
  setActiveSemester: async (id: string): Promise<Semester> => {
    // First get the existing semester
    const existing = await semesterService.getSemesterById(id);

    const request: SemesterRequest = {
      code: existing.code,
      name: existing.name,
      semesterNumber: existing.semesterNumber ?? 1,
      academicYear: existing.academicYear,
      startDate: existing.startDate,
      endDate: existing.endDate,
      isActive: true,
    };

    const response = await api.put<ApiResponse<SemesterApiResponse>>(`/api/semesters/${id}`, request);
    return mapToSemester(response.data.data);
  },
};
