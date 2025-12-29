import axiosClient from '@/api/axiosClient';
import {
  ReportIssuePayload,
  StudentSyllabiFilters,
  SyllabusDetail,
  SyllabusListItem,
} from '../types';

export async function listStudentSyllabi(filters: StudentSyllabiFilters) {
  const response = await axiosClient.get('/student/syllabuses', { params: filters });
  const payload = response.data?.data ?? response.data;
  return {
    rows: payload.rows ?? payload,
    trackedCount: payload.trackedCount ?? 0,
  };
}

export async function getStudentSyllabusDetail(id: string): Promise<SyllabusDetail> {
  const response = await axiosClient.get(`/student/syllabuses/${id}`);
  return response.data?.data ?? response.data;
}

export async function toggleTrackSyllabus(id: string): Promise<{ tracked: boolean }> {
  const response = await axiosClient.post(`/student/syllabuses/${id}/toggle-track`);
  return response.data?.data ?? response.data;
}

export async function downloadPdfMock(id: string): Promise<void> {
  // Attempt to download real PDF if endpoint exists, otherwise no-op
  try {
    const resp = await axiosClient.get(`/student/syllabuses/${id}/pdf`, { responseType: 'blob' });
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
    // fallback: no-op
  }
}

export async function reportIssue(payload: ReportIssuePayload): Promise<{ ok: true }> {
  const response = await axiosClient.post('/student/syllabuses/report-issue', payload);
  return response.data?.data ?? response.data;
}
