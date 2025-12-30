import axiosClient from '@/api/axiosClient'; // Ưu tiên dùng axiosClient đã cấu hình chuẩn
import {
  SyllabusListItem,
  SyllabusDetail,
  StudentSyllabiFilters,
} from '../types';

// ⭐ ƯU TIÊN MAIN: Định nghĩa Interface ngay tại đây để làm chuẩn cho module này
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
  // Dùng axiosClient và bỏ prefix /api để khớp với Gateway
  const res = await axiosClient.get('/student/syllabi', { params: filters });
  
  // Xử lý dữ liệu an toàn (Logic của Bạn giúp tránh lỗi null)
  const payload = res.data?.data || res.data;
  const rawData = (Array.isArray(payload) ? payload : (payload.rows || [])) as any[];

  // Map dữ liệu cẩn thận
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

  // MERGE: Giữ logic map chi tiết để đảm bảo các trường con (assessmentMatrix, clos...) không bị thiếu
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

/** Báo cáo lỗi: Sử dụng Interface vừa định nghĩa bên trên (Theo Main) */
export async function reportIssue(payload: ReportIssuePayload) {
  const res = await axiosClient.post('/student/issues/report', payload);
  return res.data?.data || res.data;
}

export async function downloadPdfMock(id: string): Promise<void> {
  try {
    // MERGE: Endpoint của Main, nhưng giữ logic xử lý file của Bạn để trình duyệt tải xuống được
    const resp = await axiosClient.get(`/student/syllabi/${id}/export-pdf`, { responseType: 'blob' });
    
    // Logic kích hoạt tải xuống trình duyệt
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