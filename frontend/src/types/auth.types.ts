// User roles
export enum UserRole {
  ADMIN = 'ADMIN',
  LECTURER = 'LECTURER',
  HOD = 'HOD', // Head of Department
  AA = 'AA', // Academic Affairs
  PRINCIPAL = 'PRINCIPAL',
  STUDENT = 'STUDENT',
}

// User type
export interface User {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  department?: string;
  faculty?: string;
  avatar?: string;
  phone?: string;
  isActive: boolean;
  createdAt: string;
  lastLogin?: string;
  managerId?: string; // ID của Trưởng bộ môn quản lý (chỉ cho LECTURER)
  managerName?: string; // Tên Trưởng bộ môn (auto-populate)
  facultyId?: string;
  departmentId?: string;
}

// Auth types
export interface LoginCredentials {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}
