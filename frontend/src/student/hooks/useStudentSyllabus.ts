import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getStudentSyllabusDetail,
  listStudentSyllabi,
  reportIssue,
  toggleTrackSyllabus,
  downloadSyllabusPdf,
  ReportIssuePayload,
} from '../api/studentSyllabus.api';
import { StudentSyllabiFilters } from '../types';

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

// ⭐ Hook Toggle Update
export function useToggleTrack() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: toggleTrackSyllabus,

    // Optimistic Update: Cập nhật UI ngay khi bấm
    onMutate: async (syllabusId: string) => {
      // 1. Hủy các query đang chạy
      await qc.cancelQueries({ queryKey: ['student-syllabi'] });
      await qc.cancelQueries({ queryKey: ['student-syllabus-detail', syllabusId] });

      // 2. Lưu lại dữ liệu cũ để rollback nếu lỗi
      const previousList = qc.getQueryData(['student-syllabi']);
      const previousDetail = qc.getQueryData(['student-syllabus-detail', syllabusId]);

      // 3. Cập nhật Cache trang Danh sách (List)
      qc.setQueriesData({ queryKey: ['student-syllabi'] }, (oldData: any) => {
        if (!Array.isArray(oldData)) return oldData;
        return oldData.map((item) =>
          item.id === syllabusId ? { ...item, tracked: !item.tracked } : item
        );
      });

      // 4. Cập nhật Cache trang Chi tiết (Detail)
      qc.setQueryData(['student-syllabus-detail', syllabusId], (oldData: any) => {
        if (!oldData) return oldData;
        return { ...oldData, tracked: !oldData.tracked };
      });

      return { previousList, previousDetail };
    },

    // Nếu lỗi thì hoàn tác
    onError: (err, newTodo, context) => {
      if (context?.previousList) {
        qc.setQueryData(['student-syllabi'], context.previousList);
      }
      if (context?.previousDetail) {
        qc.setQueryData(['student-syllabus-detail', newTodo], context.previousDetail);
      }
    },

    // Xong xuôi thì tải lại dữ liệu thật để đảm bảo đồng bộ
    onSettled: (data, error, syllabusId) => {
      qc.invalidateQueries({ queryKey: ['student-syllabi'] });
      qc.invalidateQueries({ queryKey: ['student-syllabus-detail', syllabusId] });
    },
  });
}

export const useDownloadPdf = () => {
  return useMutation({
    mutationFn: async (syllabusId: string) => {
      const response = await downloadSyllabusPdf(syllabusId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;

      const disposition = response.headers['content-disposition'];
      let fileName = `Syllabus_${syllabusId}.pdf`;

      if (disposition && disposition.includes('attachment')) {
        const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        const matches = filenameRegex.exec(disposition);
        if (matches != null && matches[1]) {
          fileName = matches[1].replace(/['"]/g, '');
        }
      }

      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.parentNode?.removeChild(link);
      window.URL.revokeObjectURL(url);
      return response.data;
    },
  });
};

export const useReportIssue = () => {
  return useMutation({
    mutationFn: (payload: ReportIssuePayload) => reportIssue(payload),
  });
};
