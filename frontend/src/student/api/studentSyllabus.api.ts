import axiosClient from '@/api/axiosClient';
import {
  SyllabusListItem,
  SyllabusDetail,
  StudentSyllabiFilters,
} from '../types';

/**
 * ⭐ THÊM MỚI: Định nghĩa Interface báo cáo lỗi ngay tại đây (Theo chuẩn Main)
 * Giúp các component sử dụng service này không cần import từ file types cồng kềnh.
 */
export interface ReportIssuePayload {
  syllabusId: string;
  section: string;
  description: string;
}

/**
 * Lấy danh sách Syllabus dành cho sinh viên
 * Đã fix: Đi qua Gateway 8888 và xử lý dữ liệu an toàn
 */
export async function listStudentSyllabi(
  filters?: StudentSyllabiFilters
): Promise<SyllabusListItem[]> {
  const res = await axiosClient.get('/api/student/syllabi', { params: filters });
  
  // Xử lý payload linh hoạt cho cả trường hợp data bọc hoặc không bọc (Unwrap)
  const payload = res.data?.data || res.data;
  const rawData = (Array.isArray(payload) ? payload : (payload.content || payload.rows || [])) as any[];

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

/**
 * Lấy chi tiết đề cương cho sinh viên
 */
export async function getStudentSyllabusDetail(id: string): Promise<SyllabusDetail> {
  const res = await axiosClient.get(`/api/student/syllabi/${id}`);
  const d = res.data?.data || res.data;

  // Cung cấp giá trị mặc định (Fallback) để tránh crash giao diện React
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

/**
 * Đánh dấu theo dõi đề cương
 */
export async function toggleTrackSyllabus(id: string): Promise<{ tracked: boolean }> {
  const res = await axiosClient.post(`/api/student/syllabi/${id}/track`);
  return res.data?.data || res.data;
}

/** * Gửi báo cáo lỗi nội dung đề cương 
 */
export async function reportIssue(payload: ReportIssuePayload) {
  const res = await axiosClient.post('/api/student/issues/report', payload);
  return res.data?.data || res.data;
}

/** * Xuất và tải xuống file PDF thực tế
 */
export async function downloadPdfMock(id: string): Promise<void> {
  try {
    const resp = await axiosClient.get(`/api/student/syllabi/${id}/export-pdf`, { 
      responseType: 'blob' 
    });
    
    // Tạo link tải xuống an toàn
    const blob = new Blob([resp.data], { type: resp.headers['content-type'] || 'application/pdf' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Syllabus_${id}.pdf`;
    document.body.appendChild(a);
    a.click();
    
    // Dọn dẹp bộ nhớ
    a.remove();
    URL.revokeObjectURL(url);
  } catch (e) {
    console.error("PDF download failed:", e);
  }
}