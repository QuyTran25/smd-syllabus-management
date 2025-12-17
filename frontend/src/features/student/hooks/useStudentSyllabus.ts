import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  downloadPdfMock,
  getStudentSyllabusDetail,
  listStudentSyllabi,
  reportIssue,
  toggleTrackSyllabus,
} from '../api/studentSyllabus.api';
import { ReportIssuePayload, StudentSyllabiFilters } from '../types';

export function useStudentSyllabi(filters: StudentSyllabiFilters) {
  return useQuery({
    queryKey: ['student-syllabi', filters],
    queryFn: () => listStudentSyllabi(filters),
  });
}

export function useStudentSyllabusDetail(id: string) {
  return useQuery({
    queryKey: ['student-syllabus-detail', id],
    queryFn: () => getStudentSyllabusDetail(id),
    enabled: !!id,
  });
}

export function useToggleTrack() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => toggleTrackSyllabus(id),
    onSuccess: (_res, id) => {
      qc.invalidateQueries({ queryKey: ['student-syllabi'] });
      qc.invalidateQueries({ queryKey: ['student-syllabus-detail', id] });
    },
  });
}

export function useDownloadPdf() {
  return useMutation({
    mutationFn: (id: string) => downloadPdfMock(id),
  });
}

export function useReportIssue() {
  return useMutation({
    mutationFn: (payload: ReportIssuePayload) => reportIssue(payload),
  });
}
