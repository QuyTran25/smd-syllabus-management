import { apiClient } from '@/config/api-config';

export interface TeachingAssignmentDTO {
  id: string;
  subjectId: string;
  subjectCode: string;
  subjectNameVi: string;
  subjectNameEn: string | null;
  credits: number;
  academicTermId: string;
  semester: string;
  mainLecturerId: string;
  mainLecturerName: string;
  mainLecturerEmail: string;
  coLecturers: Array<{
    id: string;
    name: string;
    email: string;
  }>;
  coLecturerCount: number;
  deadline: string;
  status: string;
  syllabusId: string | null;
  assignedById: string;
  assignedByName: string;
  comments: string;
  createdAt: string;
  updatedAt: string;
}

export interface TeachingAssignmentPageResponse {
  content: TeachingAssignmentDTO[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// Map backend status to frontend status
const mapStatus = (backendStatus: string): 'pending' | 'in-progress' | 'submitted' | 'completed' => {
  const statusMap: Record<string, 'pending' | 'in-progress' | 'submitted' | 'completed'> = {
    'PENDING': 'pending',
    'IN_PROGRESS': 'in-progress',
    'SUBMITTED': 'submitted',
    'COMPLETED': 'completed',
    'APPROVED': 'completed',
  };
  return statusMap[backendStatus] || 'pending';
};

export interface TeachingAssignment {
  id: string;
  courseCode: string;
  courseName: string;
  semester: string;
  mainLecturer: {
    id: string;
    name: string;
    email: string;
  };
  coLecturers: Array<{
    id: string;
    name: string;
    email: string;
  }>;
  deadline: string;
  status: 'pending' | 'in-progress' | 'submitted' | 'completed';
  syllabusId?: string;
  createdAt: string;
  commentCount: number;
}

// Map backend DTO to frontend model
const mapToTeachingAssignment = (dto: TeachingAssignmentDTO): TeachingAssignment => ({
  id: dto.id,
  courseCode: dto.subjectCode,
  courseName: dto.subjectNameVi || dto.subjectNameEn || 'Unknown',
  semester: dto.semester,
  mainLecturer: {
    id: dto.mainLecturerId,
    name: dto.mainLecturerName,
    email: dto.mainLecturerEmail,
  },
  coLecturers: dto.coLecturers || [],
  deadline: dto.deadline,
  status: mapStatus(dto.status),
  syllabusId: dto.syllabusId || undefined,
  createdAt: dto.createdAt,
  commentCount: 0, // Backend doesn't have comment count yet
});

export const teachingAssignmentService = {
  // Get all teaching assignments
  getAll: async (page = 0, size = 20): Promise<TeachingAssignment[]> => {
    const response = await apiClient.get<{ data: TeachingAssignmentPageResponse }>(
      `/api/teaching-assignments?page=${page}&size=${size}`
    );
    return response.data.data.content.map(mapToTeachingAssignment);
  },

  // Get assignment by ID
  getById: async (id: string): Promise<TeachingAssignment | null> => {
    try {
      const response = await apiClient.get<{ data: TeachingAssignmentDTO }>(
        `/api/teaching-assignments/${id}`
      );
      return mapToTeachingAssignment(response.data.data);
    } catch {
      return null;
    }
  },

  // Get assignments by lecturer ID
  getByLecturerId: async (lecturerId: string): Promise<TeachingAssignment[]> => {
    const response = await apiClient.get<{ data: TeachingAssignmentDTO[] }>(
      `/api/teaching-assignments/lecturer/${lecturerId}`
    );
    return response.data.data.map(mapToTeachingAssignment);
  },

  // Create new teaching assignment
  create: async (data: {
    subjectId: string;
    academicTermId: string;
    mainLecturerId: string;
    collaboratorIds?: string[];
    deadline: string;
    comments?: string;
  }): Promise<TeachingAssignment> => {
    const response = await apiClient.post<{ data: TeachingAssignmentDTO }>(
      '/api/teaching-assignments',
      data
    );
    return mapToTeachingAssignment(response.data.data);
  },

  // Get subjects for HOD
  getHodSubjects: async (): Promise<Array<{
    id: string;
    code: string;
    nameVi: string;
    nameEn: string;
    credits: number;
  }>> => {
    const response = await apiClient.get<{ data: Array<{
      id: string;
      code: string;
      nameVi: string;
      nameEn: string;
      credits: number;
    }> }>('/api/teaching-assignments/hod/subjects');
    return response.data.data;
  },

  // Get lecturers for HOD
  getHodLecturers: async (): Promise<Array<{
    id: string;
    fullName: string;
    email: string;
    phone?: string;
  }>> => {
    const response = await apiClient.get<{ data: Array<{
      id: string;
      fullName: string;
      email: string;
      phone?: string;
    }> }>('/api/teaching-assignments/hod/lecturers');
    return response.data.data;
  },
};
