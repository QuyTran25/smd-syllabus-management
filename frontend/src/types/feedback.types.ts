// Student feedback types for error reports and syllabus issues

export enum FeedbackStatus {
  PENDING = 'PENDING',
  IN_REVIEW = 'IN_REVIEW',
  RESOLVED = 'RESOLVED',
  REJECTED = 'REJECTED',
  CLOSED = 'CLOSED',
  AWAITING_REVISION = 'AWAITING_REVISION',
  IN_REVISION = 'IN_REVISION',
}

export enum FeedbackType {
  ERROR = 'ERROR',              // Lỗi sai nội dung
  SUGGESTION = 'SUGGESTION',    // Đề xuất cải thiện
  QUESTION = 'QUESTION',        // Câu hỏi làm rõ
  OTHER = 'OTHER',              // Khác
}

export interface StudentFeedback {
  id: string;
  syllabusId: string;
  syllabusCode: string;
  syllabusName: string;
  
  // Lecturer info
  lecturerId?: string;
  lecturerName?: string;
  lecturerEmail?: string;
  
  // Student info
  studentId: string;
  studentName: string;
  studentEmail: string;
  
  // Feedback content
  type: FeedbackType;
  section: string; // CLO, PLO Mapping, Assessment, etc. (enum value)
  sectionDisplay?: string; // Display name in Vietnamese
  title: string;
  description: string;
  
  // Status
  status: FeedbackStatus;
  
  // Response
  adminResponse?: string;
  respondedBy?: string;
  respondedAt?: string;
  
  // Edit permission
  editEnabled: boolean;
  editEnabledAt?: string;
  editEnabledBy?: string;
  
  // Timestamps
  createdAt: string;
  updatedAt: string;
}

export interface FeedbackFilters {
  status?: FeedbackStatus[];
  type?: FeedbackType[];
  syllabusId?: string;
  studentId?: string;
  search?: string;
}
