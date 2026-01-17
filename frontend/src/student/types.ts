export type SyllabusStatus = 'PUBLISHED';

export type SyllabusListItem = {
id: string;
code: string; // CS301
nameVi: string;
nameEn?: string;
term: string; // HK2 2024-2025
credits: number;
faculty: string;
program: string;
lecturerName: string;
lecturerEmail?: string;
status: SyllabusStatus;
tracked: boolean;
progress: number; // 0..100
majorShort: string; // CS / EE
publishedAt: string; // YYYY-MM-DD
};

export type AssessmentRow = {
method: string;
form: string;
clo: string[]; // ["CLO1","CLO2"]
criteria: string; // A1.2
weight: number; // %
};

export type CloRow = {
code: string; // CLO1
description: string;
bloomLevel: string;
weight: number; // %
plo: string[]; // ["PLO1","PLO2"]
};

export type SyllabusDetail = SyllabusListItem & {
summaryInline: string;
description: string;
objectives: string[];
teachingMethods: string;
studentTasks: string[];
timeAllocation: {
  theory: number; // tiết
  practice: number; // tiết
  selfStudy: number; // tiết
};
prerequisite?: {
  text: string;
};
assessmentMatrix: AssessmentRow[];
clos: CloRow[];
ploList: string[]; // ["PLO1","PLO2",...]
cloPloMap: Record<string, string[]>; // {"CLO1":["PLO1","PLO2"], ...}
textbooks: string[];
references: string[];
};

export type StudentSyllabiFilters = {
scope: 'ALL' | 'TRACKED';
q?: string;
faculty?: string;
program?: string;
term?: string;
sort: 'newest' | 'oldest';
};

export type ReportIssuePayload = {
syllabusId: string;
section: string;
description: string;
};
