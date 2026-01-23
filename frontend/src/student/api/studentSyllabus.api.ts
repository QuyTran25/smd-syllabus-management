// 1. QUAN TRỌNG: Dùng apiClient (đã có Token & BaseURL http://localhost:8888/api)
import { apiClient } from '../../config/api-config';
import { SyllabusListItem, SyllabusDetail, StudentSyllabiFilters } from '../types';

export interface ReportIssuePayload {
  syllabusId: string;
  section: string;
  description: string;
}

// ==========================================
// LƯU Ý KỸ: Các đường dẫn bên dưới KHÔNG ĐƯỢC có chữ '/api' ở đầu
// Vì apiClient đã có sẵn '/api' rồi.
// ==========================================

// 1. Lấy danh sách
export async function listStudentSyllabi(
  filters?: StudentSyllabiFilters
): Promise<SyllabusListItem[]> {
  // ✅ ĐÚNG: '/student/syllabi' (Không có /api)
  const res = await apiClient.get('/student/syllabi', { params: filters });

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

// 2. Lấy chi tiết (Nguyên nhân bạn bị văng ra Login nằm ở đây nếu sai URL)
export async function getStudentSyllabusDetail(id: string): Promise<SyllabusDetail> {
  try {
    // ✅ ĐÚNG: `/student/syllabi/${id}` (Không có /api)
    const res = await apiClient.get(`/student/syllabi/${id}`);
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
      tracked: d.tracked ?? d.isTracked ?? false,
      assessmentMatrix: d.assessmentMatrix || [],
      clos: d.clos || [],
      ploList: d.ploList || [],
      cloPloMap: d.cloPloMap || {},
      timeAllocation: {
        theory: d.timeAllocation?.theory || d.theoryHours || 0,
        practice: d.timeAllocation?.practice || d.practiceHours || 0,
        selfStudy: d.timeAllocation?.selfStudy || d.selfStudyHours || 0,
      },
      objectives: d.objectives || [],
      studentTasks: d.studentTasks || [],
      teachingMethods: d.teachingMethods || 'Giảng dạy lý thuyết và thực hành nhóm',
      textbooks: d.textbooks || [],
      references: d.references || [],
    };
  } catch (error: any) {
    console.error('Lỗi khi lấy detail đề cương:', error);
    // apiClient tự handle logout nếu 401.
    // Nếu ID sai format (UUID), server trả 400 -> Không logout, chỉ hiện lỗi.
    throw error;
  }
}

// 3. Toggle theo dõi
export async function toggleTrackSyllabus(id: string) {
  // ✅ ĐÚNG: Không có /api
  const res = await apiClient.post(`/student/syllabi/${id}/track`);
  return res.data;
}

// 4. Báo lỗi
export async function reportIssue(payload: ReportIssuePayload) {
  // ✅ ĐÚNG: Không có /api
  const res = await apiClient.post('/student/syllabi/issues/report', payload);
  return res.data;
}

// 5. Tải PDF
export async function downloadSyllabusPdf(id: string) {
  // ✅ ĐÚNG: Không có /api
  return apiClient.get(`/student/syllabi/${id}/pdf`, {
    responseType: 'blob',
    headers: {
      Accept: 'application/pdf',
    },
  });
}
