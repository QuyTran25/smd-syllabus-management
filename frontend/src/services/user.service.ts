import { User, UserRole } from '@/types';
import { apiClient as api } from '@/config/api-config';

// --- Interfaces & Types ---

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

// Backend user response type
interface UserApiResponse {
  id: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  status: string;
  roles: string[]; // Backend trả về Set<String> (Role Code)
  facultyId?: string;
  facultyName?: string;
  departmentId?: string;
  departmentName?: string;
  createdAt: string;
  updatedAt: string;
}

// Request type for create/update (Khớp với UserRequest.java của Backend)
interface UserRequest {
  email: string;
  fullName: string;
  phoneNumber?: string;
  password?: string;
  role?: string; // 🔥 QUAN TRỌNG: Backend chờ 'role', không phải 'roleCode'
  status?: string; // 'ACTIVE' | 'INACTIVE'
  facultyId?: string;
  departmentId?: string;
  managerId?: string;
}

// --- Helper Functions ---

// Map backend role string[] to frontend UserRole Enum
const mapRole = (roles: string[]): UserRole => {
  if (!roles || roles.length === 0) return UserRole.LECTURER;

  // Ưu tiên các role quyền cao hoặc đặc biệt
  for (const role of roles) {
    if (role === 'ADMIN' || role === 'Administrator') return UserRole.ADMIN;
    if (role === 'PRINCIPAL' || role === 'Principal') return UserRole.PRINCIPAL;
    if (role === 'AA' || role === 'Academic Affairs') return UserRole.AA;
    if (role === 'HOD' || role === 'Head of Department') return UserRole.HOD;
    if (role === 'LECTURER' || role === 'Lecturer') return UserRole.LECTURER;
    if (role === 'STUDENT' || role === 'Student') return UserRole.STUDENT;
  }
  return UserRole.LECTURER; // Fallback default
};

// Map API response to frontend User object
const mapToUser = (data: UserApiResponse): User => ({
  id: data.id,
  email: data.email,
  fullName: data.fullName,
  role: mapRole(data.roles),
  phone: data.phoneNumber,

  // Map ID để binding vào Dropdown khi mở Form Edit
  facultyId: data.facultyId,
  faculty: data.facultyName,
  departmentId: data.departmentId,
  department: data.departmentName,

  isActive: data.status === 'ACTIVE',
  createdAt: data.createdAt,
  lastLogin: data.updatedAt,
});

// --- Service Implementation ---

export const userService = {
  // Get all users with filters
  getUsers: async (filters?: {
    role?: UserRole;
    isActive?: boolean;
    search?: string;
    page?: number;
    size?: number;
  }): Promise<{ data: User[]; total: number; page: number; pageSize: number }> => {
    // Trả về object chuẩn cho Antd Table
    try {
      const params: any = {
        page: filters?.page || 0,
        size: filters?.size || 10,
      };

      if (filters?.role) params.role = filters.role;
      if (filters?.isActive !== undefined) params.isActive = filters.isActive;
      if (filters?.search) params.search = filters.search;

      const response = await api.get<ApiResponse<PageResponse<UserApiResponse>>>('/api/users', {
        params,
      });

      return {
        data: response.data.data.content.map(mapToUser),
        total: response.data.data.totalElements,
        page: response.data.data.number + 1, // Convert 0-based to 1-based
        pageSize: response.data.data.size,
      };
    } catch (error) {
      console.error('Failed to fetch users from API:', error);
      throw error;
    }
  },

  // Get user by ID
  getUserById: async (id: string): Promise<User> => {
    const response = await api.get<ApiResponse<UserApiResponse>>(`/api/users/${id}`);
    return mapToUser(response.data.data);
  },

  // Create user
  createUser: async (data: any): Promise<User> => {
    // 🔥 FIX: Mapping đúng field names theo Backend DTO
    const request: UserRequest = {
      email: data.email,
      fullName: data.fullName,
      phoneNumber: data.phone,

      role: data.role, // 🔥 Gửi key là 'role'

      password: data.password || 'Smd@123456', // Default password
      facultyId: data.facultyId,
      departmentId: data.departmentId,
      managerId: data.managerId,
      status: data.isActive === false ? 'INACTIVE' : 'ACTIVE',
    };

    const response = await api.post<ApiResponse<UserApiResponse>>('/api/users', request);
    return mapToUser(response.data.data);
  },

  // Update user
  updateUser: async (id: string, data: any): Promise<User> => {
    // Không cần gọi getUserById trước, gửi thẳng data cập nhật lên
    const request: UserRequest = {
      email: data.email,
      fullName: data.fullName,
      phoneNumber: data.phone,

      role: data.role, // 🔥 Gửi key là 'role'

      facultyId: data.facultyId,
      departmentId: data.departmentId,
      managerId: data.managerId,
      status: data.isActive === false ? 'INACTIVE' : 'ACTIVE',
    };

    const response = await api.put<ApiResponse<UserApiResponse>>(`/api/users/${id}`, request);
    return mapToUser(response.data.data);
  },

  // Delete user
  deleteUser: async (id: string): Promise<void> => {
    await api.delete(`/api/users/${id}`);
  },

  // Toggle user status (lock/unlock)
  toggleUserStatus: async (id: string): Promise<User> => {
    // Gọi endpoint mới trong Controller (không cần body JSON)
    const response = await api.patch<ApiResponse<UserApiResponse>>(
      `/api/users/${id}/toggle-status`
    );
    return mapToUser(response.data.data);
  },

  // 🔥 FIX: Bulk import users (Real API Call)
  importUsers: async (
    file: File
  ): Promise<{ success: number; failed: number; errors: string[] }> => {
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post<ApiResponse<any>>('/api/users/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      // Backend trả về: { success: 10, failed: 2, errors: [...] }
      return response.data.data;
    } catch (error: any) {
      console.error('Import API error:', error);
      throw new Error(error?.response?.data?.message || 'Lỗi kết nối khi import file');
    }
  },
};
