// Revision session types for post-publication workflow

export enum RevisionSessionStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  PENDING_HOD = 'PENDING_HOD',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export interface RevisionSession {
  id: string;
  syllabusVersionId: string;
  syllabusCode: string;
  syllabusName: string;
  
  sessionNumber: number;
  status: RevisionSessionStatus;
  
  initiatedById: string;
  initiatedByName: string;
  initiatedAt: string;
  
  description?: string;
  
  assignedLecturerId?: string;
  assignedLecturerName?: string;
  
  startedAt?: string;
  completedAt?: string;
  
  hodReviewedById?: string;
  hodReviewedByName?: string;
  hodReviewedAt?: string;
  hodDecision?: string;
  hodComment?: string;
  
  republishedById?: string;
  republishedByName?: string;
  republishedAt?: string;
  
  feedbackCount: number;
  feedbackIds: string[];
  
  createdAt: string;
  updatedAt: string;
}

export interface StartRevisionRequest {
  syllabusVersionId: string;
  feedbackIds: string[];
  description?: string;
}

export interface SubmitRevisionRequest {
  revisionSessionId: string;
  summary?: string;
}

export interface ReviewRevisionRequest {
  revisionSessionId: string;
  decision: 'APPROVED' | 'REJECTED';
  comment?: string;
}
