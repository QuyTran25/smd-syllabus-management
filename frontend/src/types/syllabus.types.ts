// Syllabus workflow states
export enum SyllabusStatus {
  DRAFT = 'DRAFT',
  PENDING_HOD = 'PENDING_HOD',
  PENDING_HOD_REVISION = 'PENDING_HOD_REVISION', // Đề cương sửa lỗi sau xuất hành, chờ HoD duyệt
  PENDING_AA = 'PENDING_AA',
  PENDING_PRINCIPAL = 'PENDING_PRINCIPAL',
  APPROVED = 'APPROVED',
  PENDING_ADMIN_REPUBLISH = 'PENDING_ADMIN_REPUBLISH', // Đã HoD duyệt revision, chờ Admin xuất hành lại
  PUBLISHED = 'PUBLISHED',
  REJECTED = 'REJECTED',
  REVISION_IN_PROGRESS = 'REVISION_IN_PROGRESS', // Đang chỉnh sửa
  INACTIVE = 'INACTIVE', // Không hoạt động
  ARCHIVED = 'ARCHIVED',
}

// Course Learning Outcome
export interface CLO {
  id: string;
  code: string; // CLO1, CLO2, etc.
  description: string;
  bloomLevel: string; // Remember, Understand, Apply, Analyze, Evaluate, Create
  weight: number; // percentage
  piMappings?: { ploId: string; piCode: string; level: 'H' | 'M' | 'L' }[]; // Ánh xạ chi tiết đến PI
}

// Performance Indicator (sub-indicator of PLO)
export interface PI {
  code: string; // PI2.1, PI2.2, etc.
  description: string;
}

// Program Learning Outcome
export interface PLO {
  id: string;
  code: string; // PLO1, PLO2, etc.
  description: string;
  category: string; // Knowledge, Skills, Competence
  pis: PI[]; // Performance Indicators
}

// CLO-PLO Mapping
export interface CLOPLOMapping {
  cloId: string;
  ploId: string;
  contributionLevel: 'LOW' | 'MEDIUM' | 'HIGH'; // Mức độ đóng góp
}

// Assessment Method (Ma trận đánh giá)
export interface AssessmentMethod {
  id: string;
  method: string; // Tên phương pháp: Thi giữa kỳ, Cuối kỳ, Bài tập...
  form: string; // Hình thức: Cá nhân, Nhóm, Kiểm tra, Bài tập, Thi
  clos: string[]; // Mã CLO áp dụng
  criteria: string; // Tiêu chí đánh giá (A1.2, A2.1...)
  weight: number; // Trọng số %
}

// Assessment criteria (kept for backward compatibility)
export interface AssessmentCriteria {
  id: string;
  name: string;
  description: string;
  weight: number; // percentage
  method: string; // Exam, Assignment, Project, etc.
  relatedCLOs: string[]; // CLO IDs
}

// Prerequisite course
export interface PrerequisiteCourse {
  courseId: string;
  courseCode: string;
  courseName: string;
  type: 'required' | 'recommended' | 'corequisite'; // Bắt buộc, Khuyến nghị, Song hành
}

// Time allocation structure
export interface TimeAllocation {
  theory: number; // Số tiết lý thuyết
  practice: number; // Số tiết thực hành
  selfStudy: number; // Số tiết tự học
}

// Syllabus main entity
export interface Syllabus {
  id: string;
  subjectCode: string;  // Mapped from backend snap_subject_code
  subjectNameVi: string;  // Mapped from backend snap_subject_name_vi
  subjectNameEn: string; // Mapped from backend snap_subject_name_en
  creditCount: number;  // Mapped from backend snap_credit_count
  
  // Deprecated fields for backward compatibility
  courseCode?: string;
  courseName?: string;
  courseNameEn?: string;
  credits?: number;
  
  // Academic info
  semester: string; // HK1-2024, HK2-2024
  academicYear: string; // 2024-2025
  department: string;
  faculty: string;
  
  // New fields theo template chính thức
  courseType: 'required' | 'elective' | 'free'; // Loại học phần
  componentType: 'major' | 'foundation' | 'general' | 'thesis'; // Thành phần
  timeAllocation: TimeAllocation; // Phân bổ thời gian chi tiết
  gradeScale: number; // Thang điểm (10 hoặc 4)
  studentDuties?: string; // Nhiệm vụ của sinh viên
  
  // Content
  description: string;
  objectives: string[];
  outline: string; // Rich text content
  
  // Learning outcomes
  clos: CLO[];
  ploMappings: CLOPLOMapping[];
  
  // Assessment
  assessmentMethods: AssessmentMethod[]; // Ma trận đánh giá mới
  assessmentCriteria: AssessmentCriteria[]; // Giữ lại cho backward compatibility
  
  // Prerequisites
  prerequisites: PrerequisiteCourse[];
  
  // Workflow
  status: SyllabusStatus;
  version: number;
  previousVersionId?: string;
  
  // Ownership
  ownerId: string; // Lecturer ID
  ownerName: string;
  
  // Approval tracking
  createdAt: string;
  updatedAt: string;
  submittedAt?: string;
  hodApprovedAt?: string;
  hodApprovedBy?: string;
  aaApprovedAt?: string;
  aaApprovedBy?: string;
  principalApprovedAt?: string;
  principalApprovedBy?: string;
  publishedAt?: string;
  publishedBy?: string;
  effectiveDate?: string; // Ngày hiệu lực
  archivedAt?: string;
  archivedBy?: string;
  unpublishReason?: string; // Lý do gỡ bỏ
  
  // Additional metadata
  totalStudyHours: number;
  theoryHours: number;
  practiceHours: number;
  selfStudyHours: number;
  
  // References
  references: string[];
  materials: string[];
}

// Comment/Feedback
export interface SyllabusComment {
  id: string;
  syllabusId: string;
  userId: string;
  userName: string;
  userRole: string;
  content: string;
  section?: string; // Which section of syllabus (optional - for inline comments)
  type: 'INLINE' | 'OFFICIAL'; // Inline comment or official approval reason
  createdAt: string;
  updatedAt: string;
}

// Approval action
export interface ApprovalAction {
  syllabusId: string;
  action: 'APPROVE' | 'REJECT';
  reason?: string; // Required for REJECT
  comments?: SyllabusComment[];
}

// Syllabus filters
export interface SyllabusFilters {
  status?: SyllabusStatus[];
  department?: string[];
  faculty?: string[];
  semester?: string[];
  ownerId?: string;
  search?: string; // Search by course name or code
}

// Pagination
export interface PaginationParams {
  page: number;
  pageSize: number;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

// AI features
export interface VersionComparison {
  added: string[];
  removed: string[];
  modified: Array<{
    field: string;
    oldValue: string;
    newValue: string;
  }>;
}

export interface AIFeatures {
  summary?: string;
  comparison?: VersionComparison;
  cloPloDiagram?: any; // Will be defined based on visualization library
}
