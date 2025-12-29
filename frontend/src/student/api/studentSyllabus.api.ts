import axiosClient from '@/api/axiosClient';
import {
  SyllabusListItem,
  SyllabusDetail,
  StudentSyllabiFilters,
} from '../types';

// MERGE: Giữ định nghĩa Interface của Team ở đây để làm chuẩn
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
  // MERGE: Dùng axiosClient + Endpoint có /api/ prefix
  const res = await axiosClient.get('/student/syllabi', { params: filters });
  
  // Xử lý dữ liệu an toàn (Logic của Team)
  const payload = res.data?.data || res.data;
  const rawData = (Array.isArray(payload) ? payload : (payload.rows || [])) as any[];

  // Map dữ liệu để đảm bảo không bị lỗi null/undefined trên UI
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
  const res = await axiosClient.get(`/student/syllabi/${id}`);
  const d = res.data?.data || res.data;

  // MERGE: Giữ logic map chi tiết của Team để đảm bảo có đủ các trường con (assessmentMatrix, clos...)
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

export async function toggleTrackSyllabus(id: string): Promise<{ tracked: boolean }> {
  const res = await axiosClient.post(`/student/syllabi/${id}/track`);
  return res.data?.data || res.data;
}

/** Báo cáo lỗi */
export async function reportIssue(payload: ReportIssuePayload) {
  const res = await axiosClient.post('/student/issues/report', payload);
  return res.data?.data || res.data;
}

export async function downloadPdfMock(id: string): Promise<void> {
  try {
    // MERGE: Kết hợp endpoint của Team và logic xử lý Blob download của Bạn
    const resp = await axiosClient.get(`/student/syllabi/${id}/export-pdf`, { responseType: 'blob' });
    
    // Logic kích hoạt tải xuống trình duyệt (Code của bạn)
    const blob = new Blob([resp.data], { type: resp.headers['content-type'] });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${id}.pdf`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  } catch (e) {
    console.error("Download failed", e);
  }
}