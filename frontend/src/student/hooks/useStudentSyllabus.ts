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
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['student-syllabi'] });
    },
  });
}

export const useDownloadPdf = () => useMutation({ mutationFn: downloadPdfMock });

// Sử dụng ReportIssuePayload đã import từ file API
export const useReportIssue = () =>
  useMutation<void, Error, ReportIssuePayload>({
    mutationFn: reportIssue,
  });
