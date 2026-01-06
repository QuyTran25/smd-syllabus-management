import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  downloadPdfMock,
  getStudentSyllabusDetail,
  listStudentSyllabi,
  reportIssue,
  toggleTrackSyllabus,
  ReportIssuePayload, // ⭐ THÊM: Import Interface từ file API
} from '../api/studentSyllabus.api';
import { StudentSyllabiFilters } from '../types';

// ❌ XÓA ĐOẠN ĐỊNH NGHĨA LOCAL NÀY ĐI ĐỂ TRÁNH XUNG ĐỘT
// export interface ReportIssuePayload {
//   syllabusId: string;
//   section: string;
//   description: string;
// }

export function useStudentSyllabi(filters: StudentSyllabiFilters) {
  return useQuery({
    queryKey: ['student-syllabi', filters],
    queryFn: async () => {
      const data = await listStudentSyllabi(filters);
      return Array.isArray(data) ? data : [];
    },
  });
}

export function useStudentSyllabusDetail(id: string) {
  return useQuery({
    queryKey: ['student-syllabus-detail', id],
    queryFn: async () => {
      const data = await getStudentSyllabusDetail(id);
      return data;
    },
    enabled: !!id,
  });
}

export function useToggleTrack() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: toggleTrackSyllabus,
    // Khi bắt đầu click (chưa cần biết API xong chưa)
    onMutate: async (syllabusId: string) => {
      // 1. Hủy các request đang chạy để tránh xung đột
      await qc.cancelQueries({ queryKey: ['student-syllabi'] });

      // 2. Lấy dữ liệu hiện tại trong Cache
      const previousData = qc.getQueriesData({ queryKey: ['student-syllabi'] });

      // 3. Tự động sửa Cache: Tìm môn học đó và đảo ngược trạng thái tracked
      qc.setQueriesData({ queryKey: ['student-syllabi'] }, (oldData: any) => {
        if (!Array.isArray(oldData)) return oldData;
        return oldData.map((item) =>
          item.id === syllabusId ? { ...item, tracked: !item.tracked } : item
        );
      });

      // Trả về dữ liệu cũ để nếu lỗi thì hoàn tác
      return { previousData };
    },
    // Nếu API bị lỗi (500, 404...)
    onError: (err, newTodo, context) => {
      // Hoàn tác lại giao diện cũ
      if (context?.previousData) {
        context.previousData.forEach(([queryKey, data]) => {
          qc.setQueryData(queryKey, data);
        });
      }
    },
    // Sau khi xong xuôi (thành công hoặc thất bại)
    onSettled: () => {
      // (Tùy chọn) Tải lại dữ liệu thật từ Server để đồng bộ
      // qc.invalidateQueries({ queryKey: ['student-syllabi'] });
      // TẠM THỜI COMMENT DÒNG TRÊN: Vì backend bạn đang hardcode false,
      // nếu bật dòng này lên nó sẽ load lại màu trắng.
    },
  });
}

export const useDownloadPdf = () => useMutation({ mutationFn: downloadPdfMock });

// Sử dụng ReportIssuePayload đã import từ file API
export const useReportIssue = () =>
  useMutation<void, Error, ReportIssuePayload>({
    mutationFn: reportIssue,
  });
