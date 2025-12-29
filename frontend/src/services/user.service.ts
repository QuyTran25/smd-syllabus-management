import axiosClient from '@/api/axiosClient';
import { User, UserRole } from '@/types';

// MERGE: Giữ các interface Response chi tiết của Team
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
  roles: string[];
  facultyId?: string;
  facultyName?: string;
  departmentId?: string;
  departmentName?: string;
  createdAt: string;
  updatedAt: string;
}

// Map backend role to frontend UserRole
const mapRole = (roles: string[]): UserRole => {
  // Use role code (first 3-8 chars) to map
  for (const role of roles) {
    if (role === 'Administrator' || role === 'ADMIN') return UserRole.ADMIN;
    if (role === 'Principal' || role === 'PRINCIPAL') return UserRole.PRINCIPAL;
    if (role === 'Academic Affairs' || role === 'AA') return UserRole.AA;
    if (role === 'Head of Department' || role === 'HOD') return UserRole.HOD;
    if (role === 'Lecturer' || role === 'LECTURER') return UserRole.LECTURER;
    if (role === 'Student' || role === 'STUDENT') return UserRole.STUDENT;
  }
  return UserRole.LECTURER; // default
};

// Map API response to frontend User type
const mapToUser = (data: UserApiResponse): User => ({
  id: data.id,
  email: data.email,
  fullName: data.fullName,
  role: mapRole(data.roles),
  phone: data.phoneNumber,
  faculty: data.facultyName,
  department: data.departmentName,
  isActive: data.status === 'ACTIVE',
  createdAt: data.createdAt,
  lastLogin: data.updatedAt, // Using updatedAt as proxy for lastLogin
});

// Request type for create/update
interface UserRequest {
  email: string;
  fullName: string;
  phoneNumber?: string;
  password?: string;
  roleCode?: string;
}

export const userService = {
  // Get all users with filters
  getUsers: async (filters?: {
    role?: UserRole;
    isActive?: boolean;
    search?: string;
  }): Promise<User[]> => {
    try {
      // MERGE: Dùng axiosClient và endpoint /api/ của Team
      const response = await axiosClient.get<ApiResponse<PageResponse<UserApiResponse>>>('/users', {
        params: {
          page: 0,
          size: 100,
        },
      });

      // Xử lý payload an toàn
      const rawContent = response.data?.data?.content || [];
      let users = rawContent.map(mapToUser);

      // Apply client-side filtering (Logic của Team)
      if (filters?.role) {
        users = users.filter((u) => u.role === filters.role);
      }

      if (filters?.isActive !== undefined) {
        users = users.filter((u) => u.isActive === filters.isActive);
      }

      if (filters?.search) {
        const searchLower = filters.search.toLowerCase();
        users = users.filter(
          (u) =>
            u.fullName.toLowerCase().includes(searchLower) ||
            u.email.toLowerCase().includes(searchLower)
        );
      }

      return users;
    } catch (error) {
      console.error('Failed to fetch users from API:', error);
      throw error;
    }
  },

  // Get user by ID
  getUserById: async (id: string): Promise<User> => {
    const response = await axiosClient.get<ApiResponse<UserApiResponse>>(`/users/${id}`);
    const data = response.data?.data || response.data;
    return mapToUser(data as unknown as UserApiResponse);
  },

  // Create user
  createUser: async (data: Omit<User, 'id' | 'createdAt' | 'lastLogin'>): Promise<User> => {
    const request: UserRequest = {
      email: data.email,
      fullName: data.fullName,
      phoneNumber: data.phone,
      roleCode: data.role,
      password: 'DefaultPass@123', // Default password, should be changed
    };

    const response = await axiosClient.post<ApiResponse<UserApiResponse>>('/users', request);
    const responseData = response.data?.data || response.data;
    return mapToUser(responseData as unknown as UserApiResponse);
  },

  // Update user
  updateUser: async (id: string, data: Partial<User>): Promise<User> => {
    // Get existing user first to merge data
    const existing = await userService.getUserById(id);

    const request: UserRequest = {
      email: data.email ?? existing.email,
      fullName: data.fullName ?? existing.fullName,
      phoneNumber: data.phone ?? existing.phone,
      roleCode: data.role ?? existing.role,
    };

    const response = await axiosClient.put<ApiResponse<UserApiResponse>>(`/users/${id}`, request);
    const responseData = response.data?.data || response.data;
    return mapToUser(responseData as unknown as UserApiResponse);
  },

  // Delete user
  deleteUser: async (id: string): Promise<void> => {
    await axiosClient.delete(`/users/${id}`);
  },
};