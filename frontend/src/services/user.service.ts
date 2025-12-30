import axiosClient from '@/api/axiosClient'; // Ưu tiên axiosClient đã cấu hình interceptor
import { User, UserRole } from '@/types';

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
      // Dùng axiosClient và bỏ prefix /api để khớp với Gateway
      const response = await axiosClient.get<ApiResponse<PageResponse<UserApiResponse>>>('/users', {
        params: {
          page: 0,
          size: 100,
        },
      });

      // Xử lý payload an toàn (Logic của Bạn nhưng cần thiết)
      const rawContent = response.data?.data?.content || 
                         (response.data as any)?.content || 
                         [];
                         
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

  // Toggle user status (lock/unlock) - Logic từ Main
  toggleUserStatus: async (id: string): Promise<User> => {
    // Get current user status
    const user = await userService.getUserById(id);
    const newStatus = user.isActive ? 'INACTIVE' : 'ACTIVE';

    // Dùng endpoint update status riêng hoặc update full tùy backend
    // Ở đây ta dùng cách gọi update patch status nếu backend hỗ trợ
    // Hoặc nếu không thì dùng update full
    const response = await axiosClient.patch<ApiResponse<UserApiResponse>>(`/users/${id}/status`, {
      status: newStatus,
    });
    const data = response.data?.data || response.data;
    return mapToUser(data as unknown as UserApiResponse);
  },

  // Bulk import users from CSV - Logic từ Main (Giữ nguyên)
  importUsers: async (file: File): Promise<{ success: number; failed: number; errors: string[] }> => {
    // TODO: Implement real API call for bulk import
    // For now, simulate with delay
    return new Promise((resolve) => {
      const reader = new FileReader();
      const errors: string[] = [];
      let successCount = 0;
      let failedCount = 0;

      reader.onload = (e) => {
        try {
          const text = e.target?.result as string;
          const lines = text.split('\n').filter((line) => line.trim());
          const header = lines[0]?.toLowerCase();

          // Validate CSV header
          if (!header?.includes('email') || !header?.includes('fullname')) {
            errors.push('CSV phải có các cột: email, fullName');
            resolve({ success: 0, failed: lines.length - 1, errors });
            return;
          }

          // Process rows
          const headerCols = header.split(',').map((col) => col.trim());
          const emailIndex = headerCols.indexOf('email');
          const fullNameIndex = headerCols.indexOf('fullname');
          const roleIndex = headerCols.indexOf('role');

          for (let i = 1; i < lines.length; i++) {
            const cols = lines[i].split(',').map((col) => col.trim());
            const email = cols[emailIndex];
            const fullName = cols[fullNameIndex];
            const role = cols[roleIndex];

            if (!email || !fullName) {
              errors.push(`Dòng ${i + 1}: Thiếu email hoặc tên`);
              failedCount++;
              continue;
            }

            successCount++;
          }

          resolve({ success: successCount, failed: failedCount, errors });
        } catch {
          errors.push('Lỗi xử lý file CSV');
          resolve({ success: 0, failed: 0, errors });
        }
      };

      reader.onerror = () => {
        resolve({ success: 0, failed: 0, errors: ['Không đọc được file'] });
      };

      reader.readAsText(file);
    });
  },
};