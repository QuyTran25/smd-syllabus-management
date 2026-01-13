import { http } from './http';
import { SyllabusListItem, SyllabusDetail, StudentSyllabiFilters } from '../types';

export interface ReportIssuePayload {
  syllabusId: string;
  section: string;
  description: string;
}

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
  try {
    // ✅ FIX: Thêm try-catch để handle error graceful (không reload trang)
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

      // ⭐ Map isTracked từ backend sang tracked
      tracked: d.tracked ?? d.isTracked ?? false,

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
  } catch (error: any) {
    console.error('Lỗi khi lấy detail đề cương:', error);
    // ✅ Handle error: Có thể throw hoặc return default object với message error
    throw new Error(
      error.response?.data?.message || 'Không thể tải đề cương. Vui lòng thử lại sau.'
    );
  }
}

export async function toggleTrackSyllabus(id: string) {
  try {
    // ✅ FIX: Thêm try-catch để handle error (không reload trang)
    const res = await http.post(`/student/syllabi/${id}/track`);
    return res.data;
  } catch (error: any) {
    console.error('Lỗi khi toggle theo dõi:', error);
    throw new Error(
      error.response?.data?.message || 'Không thể cập nhật theo dõi. Vui lòng thử lại.'
    );
  }
}

export async function reportIssue(payload: ReportIssuePayload) {
  console.log('LOG: Đang gửi báo cáo lỗi với data:', payload);
  const res = await http.post('/student/syllabi/issues/report', payload);
  return res.data;
}

export async function downloadSyllabusPdf(id: string) {
  return http.get(`/student/syllabi/${id}/pdf`, {
    responseType: 'blob',
  });
}
