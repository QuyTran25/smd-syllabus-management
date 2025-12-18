import { mockDetailsById, mockSyllabi } from './studentSyllabus.mock';
import {
  ReportIssuePayload,
  StudentSyllabiFilters,
  SyllabusDetail,
  SyllabusListItem,
} from '../types';

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms));

export async function listStudentSyllabi(filters: StudentSyllabiFilters): Promise<{
  rows: SyllabusListItem[];
  trackedCount: number;
}> {
  await delay(250);

  const q = (filters.q ?? '').toLowerCase().trim();

  let rows = mockSyllabi.filter((x) => {
    if (filters.scope === 'TRACKED' && !x.tracked) return false;
    if (filters.faculty && x.faculty !== filters.faculty) return false;
    if (filters.program && x.program !== filters.program) return false;
    if (filters.term && x.term !== filters.term) return false;

    if (q) {
      const blob = `${x.code} ${x.nameVi} ${x.nameEn ?? ''} ${x.lecturerName}`.toLowerCase();
      if (!blob.includes(q)) return false;
    }

    return true;
  });

  rows = rows.sort((a, b) => {
    const da = new Date(a.publishedAt).getTime();
    const db = new Date(b.publishedAt).getTime();
    return filters.sort === 'newest' ? db - da : da - db;
  });

  const trackedCount = mockSyllabi.filter((x) => x.tracked).length;

  return { rows, trackedCount };
}

export async function getStudentSyllabusDetail(id: string): Promise<SyllabusDetail> {
  await delay(200);
  const detail = mockDetailsById[id];
  if (!detail) throw new Error('Syllabus not found');
  return detail;
}

export async function toggleTrackSyllabus(id: string): Promise<{ tracked: boolean }> {
  await delay(150);
  const item = mockSyllabi.find((x) => x.id === id);
  if (!item) throw new Error('Syllabus not found');
  item.tracked = !item.tracked;
  return { tracked: item.tracked };
}

export async function downloadPdfMock(_id: string): Promise<void> {
  await delay(300);
  // Mock: không làm gì. UI sẽ show message thành công.
}

export async function reportIssue(payload: ReportIssuePayload): Promise<{ ok: true }> {
  await delay(400);
  // Mock: có thể console.log để debug
  // eslint-disable-next-line no-console
  console.log('[MOCK] reportIssue', payload);
  return { ok: true };
}
