/**
 * Semester Management Types
 */

export interface Semester {
  id: string;
  code: string; // HK1-2024, HK2-2024
  name: string; // Học kỳ 1 năm 2024-2025
  semesterNumber?: number; // 1, 2, 3 (for summer)
  startDate: string; // ISO date
  endDate: string; // ISO date
  academicYear: string; // 2024-2025
  isActive: boolean; // Học kỳ hiện tại
  createdAt: string;
  createdBy?: string; // Admin user ID
  updatedAt: string;
}

export interface SemesterFilters {
  isActive?: boolean;
  academicYear?: string;
  search?: string;
}
