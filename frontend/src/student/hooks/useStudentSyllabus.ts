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
import { aiService } from '../../services/aiService';

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

/**
 * Hook để gọi AI Summarize
 * Returns task_id để có thể poll status
 */
export const useSummarizeSyllabus = () =>
  useMutation<string, Error, string>({
    mutationFn: (syllabusId: string) => aiService.summarizeSyllabus(syllabusId),
  });

/**
 * Hook để poll AI task status
 * Polling interval: 2 seconds
 */
export const useAITaskStatus = (taskId: string | null) =>
  useQuery({
    queryKey: ['ai-task-status', taskId],
    queryFn: () => aiService.getTaskStatus(taskId!),
    enabled: !!taskId,
    refetchInterval: (data) => {
      // Continue polling if status is QUEUED or PROCESSING
      if (data?.status === 'QUEUED' || data?.status === 'PROCESSING') {
        return 2000; // Poll every 2 seconds
      }
      return false; // Stop polling
    },
    retry: false,
  });

