import axios from 'axios';
import { API_BASE_URL } from '@/constants';

export interface TaskStatusResponse {
  taskId: string;
  action?: string;
  status: 'QUEUED' | 'PROCESSING' | 'SUCCESS' | 'FAILED' | 'NOT_FOUND';
  progress: number;
  message?: string;
  timestamp?: number;
  result?: any;
  error?: string;
}

export interface CloPloResult {
  status: 'COMPLIANT' | 'NEEDS_IMPROVEMENT' | 'NON_COMPLIANT';
  score: number;
  total_mappings: number;
  compliant_mappings: number;
  issues: Array<{
    type: string;
    severity: 'HIGH' | 'MEDIUM' | 'LOW';
    clo_code: string;
    plo_codes: string[];
    message: string;
  }>;
  suggestions: Array<{
    clo_code: string;
    current_plos: string[];
    suggested_plos: string[];
    reason: string;
  }>;
}

export interface CompareVersionsResult {
  summary: {
    total_changes: number;
    additions: number;
    deletions: number;
    modifications: number;
  };
  changes: Array<{
    section: string;
    type: 'ADDED' | 'DELETED' | 'MODIFIED';
    old_value?: string;
    new_value?: string;
    impact: 'HIGH' | 'MEDIUM' | 'LOW';
  }>;
  ai_analysis: {
    overall_assessment: string;
    key_improvements: string[];
    potential_issues: string[];
    recommendation: string;
  };
}

export interface SummarizeResult {
  overview: string;
  key_highlights: string[];
  learning_outcomes_summary: string;
  assessment_methods: string[];
  recommendations: string[];
}

/**
 * AI Service - Gọi các AI analysis endpoints
 */
class AIService {
  
  /**
   * Yêu cầu AI kiểm tra CLO-PLO mapping
   */
  async checkCloPloMapping(syllabusId: string, curriculumId: string): Promise<string> {
    const response = await axios.post<{ task_id: string }>(
      `${API_BASE_URL}/api/ai/syllabus/${syllabusId}/check-clo-plo`,
      null,
      {
        params: { curriculumId },
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      }
    );
    return response.data.task_id;
  }

  /**
   * So sánh 2 phiên bản syllabus
   */
  async compareVersions(
    oldVersionId: string,
    newVersionId: string,
    subjectId: string
  ): Promise<string> {
    const response = await axios.post<{ task_id: string }>(
      `${API_BASE_URL}/api/ai/syllabus/compare`,
      null,
      {
        params: { oldVersionId, newVersionId, subjectId },
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      }
    );
    return response.data.task_id;
  }

  /**
   * Tóm tắt syllabus (cho sinh viên)
   */
  async summarizeSyllabus(syllabusId: string): Promise<string> {
    const response = await axios.post<{ task_id: string }>(
      `${API_BASE_URL}/api/ai/syllabus/${syllabusId}/summarize`,
      null,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      }
    );
    return response.data.task_id;
  }

  /**
   * Poll task status (gọi định kỳ để kiểm tra tiến trình)
   */
  async getTaskStatus(taskId: string): Promise<TaskStatusResponse> {
    const response = await axios.get<TaskStatusResponse>(
      `${API_BASE_URL}/api/ai/tasks/${taskId}/status`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      }
    );
    return response.data;
  }
}

export const aiService = new AIService();
export default aiService;
