import { http } from './http';
import {
  SyllabusListItem,
  SyllabusDetail,
  // ReportIssuePayload,  <-- XÓA DÒNG NÀY (Không import từ types nữa)
  StudentSyllabiFilters,
} from '../types';

// ⭐ THÊM MỚI: Định nghĩa ngay tại đây để làm chuẩn cho toàn bộ app
export interface ReportIssuePayload {
  syllabusId: string;
  section: string;
  description: string;
}

/**
 * Lấy danh sách Syllabus từ Backend
 */
export async function listStudentSyllabi(
  filters?: StudentSyllabiFilters
): Promise<SyllabusListItem[]> {
  const res = await http.get('/student/syllabi', { params: filters });
  const rawData = (Array.isArray(res.data) ? res.data : []) as any[];

  return rawData.map((item) => ({
    id: item.id,
    code: item.code || 'N/A',
    nameVi: item.nameVi || 'Chưa có tên',
    term: item.term || 'HK2 2024-2025',
    credits: item.credits || 0,
    status: item.status || 'PUBLISHED',
    tracked: item.tracked || false,
    progress: item.progress || 100,
    majorShort: item.majorShort || (item.code ? item.code.substring(0, 2) : 'IT'),
    lecturerName: item.lecturerName || 'Chưa cập nhật',
    faculty: item.faculty || 'Khoa Công nghệ Thông tin',
    program: item.program || 'Chương trình đào tạo',
    publishedAt: item.publishedAt || new Date().toISOString().split('T')[0],
  }));
}

export async function getStudentSyllabusDetail(id: string): Promise<SyllabusDetail> {
  const res = await http.get(`/student/syllabi/${id}`);
  const d = res.data;

  return {
    ...d,
    id: d.id,
    code: d.code,
    nameVi: d.nameVi,
    faculty: d.faculty || 'Khoa Công nghệ Thông tin',
    program: d.program || 'Chương trình đào tạo',
    lecturerName: d.lecturerName || 'Giảng viên hướng dẫn',
    status: d.status || 'PUBLISHED',
    summaryInline: d.description || 'Chưa có tóm tắt nội dung môn học.',
    assessmentMatrix: d.assessmentMatrix || [],
    clos: d.clos || [],
    timeAllocation: {
      theory: d.theoryHours || 30,
      practice: d.practiceHours || 30,
      selfStudy: d.selfStudyHours || 90,
    },
    objectives: d.objectives || [],
    studentTasks: d.studentTasks || [],
    teachingMethods: d.teachingMethods || 'Giảng dạy lý thuyết và thực hành nhóm',
  };
}

export async function toggleTrackSyllabus(id: string) {
  const res = await http.post(`/student/syllabi/${id}/track`);
  return res.data;
}

/** Báo cáo lỗi: Sử dụng Interface vừa định nghĩa bên trên */
export async function reportIssue(payload: ReportIssuePayload) {
  const res = await http.post('/student/issues/report', payload);
  return res.data;
}

export async function downloadPdfMock(id: string) {
  const res = await http.get(`/student/syllabi/${id}/export-pdf`, { responseType: 'blob' });
  return res.data;
}
