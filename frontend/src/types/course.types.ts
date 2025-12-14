/**
 * Course Management Types
 * Định nghĩa cấu trúc dữ liệu cho Môn học
 */

export interface Course {
  id: string;
  code: string; // Mã môn học (VD: CS101)
  name: string; // Tên tiếng Việt
  nameEn: string; // Tên tiếng Anh
  credits: number; // Số tín chỉ
  
  // Academic organization
  departmentId: string;
  departmentName: string;
  facultyId: string;
  facultyName: string;
  
  // Semester info
  semesterId: string;
  semesterName: string;
  
  // Prerequisites (Quan hệ môn học - do PĐT thiết lập)
  prerequisites: CoursePrerequisite[];
  
  // Metadata
  createdAt: string;
  createdBy: string; // User ID của AA
  updatedAt: string;
  isActive: boolean; // Môn có đang hoạt động không
}

export interface CoursePrerequisite {
  courseId: string;
  courseCode: string;
  courseName: string;
  type: 'required' | 'recommended' | 'corequisite'; // Bắt buộc, Khuyến nghị, Song hành
}

// Filters for course list
export interface CourseFilters {
  departmentId?: string;
  facultyId?: string;
  semesterId?: string;
  isActive?: boolean;
  search?: string; // Search by code or name
}
