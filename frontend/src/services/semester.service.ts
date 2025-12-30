import axiosClient from '@/api/axiosClient'; // Ưu tiên axiosClient đã cấu hình chuẩn
import { Semester, SemesterFilters } from '@/types';

// API Response types (Giữ lại của Main)
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
  getSemesters: async (filters?: SemesterFilters): Promise<Semester[]> => {
    // Dùng axiosClient và bỏ prefix /api để khớp với Gateway StripPrefix
    const response = await axiosClient.get<ApiResponse<PageResponse<SemesterApiResponse>>>('/semesters', {
      params: {
        page: 0,
        size: 100,
        sort: 'startDate,desc',
      },
    });

    // Xử lý payload an toàn (Logic của Bạn nhưng cần thiết)
    const rawContent = response.data?.data?.content || 
                       (response.data as any)?.content || 
                       [];
                       
    let semesters = rawContent.map(mapToSemester);

    // Apply client-side filtering (Logic của Team)
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

  getSemesterById: async (id: string): Promise<Semester> => {
    const response = await axiosClient.get<ApiResponse<SemesterApiResponse>>(`/semesters/${id}`);
    const data = response.data?.data || response.data;
    return mapToSemester(data as unknown as SemesterApiResponse);
  },

  getActiveSemester: async (): Promise<Semester | null> => {
    try {
      const response = await axiosClient.get<ApiResponse<SemesterApiResponse>>('/semesters/current');
      const data = response.data?.data || response.data;
      return mapToSemester(data as unknown as SemesterApiResponse);
    } catch {
      return null;
    }
  },

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

    const response = await axiosClient.post<ApiResponse<SemesterApiResponse>>('/semesters', request);
    const responseData = response.data?.data || response.data;
    return mapToSemester(responseData as unknown as SemesterApiResponse);
  },

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

    const response = await axiosClient.put<ApiResponse<SemesterApiResponse>>(`/semesters/${id}`, request);
    const responseData = response.data?.data || response.data;
    return mapToSemester(responseData as unknown as SemesterApiResponse);
  },

  deleteSemester: async (id: string): Promise<void> => {
    await axiosClient.delete(`/semesters/${id}`);
  },

  setActiveSemester: async (id: string): Promise<Semester> => {
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

    const response = await axiosClient.put<ApiResponse<SemesterApiResponse>>(`/semesters/${id}`, request);
    const responseData = response.data?.data || response.data;
    return mapToSemester(responseData as unknown as SemesterApiResponse);
  },
};