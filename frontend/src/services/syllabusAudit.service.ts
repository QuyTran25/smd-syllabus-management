import { auditLogService } from './auditlog.service';
import { syllabusService } from './index';

/**
 * Wrapper service để tự động ghi log các hoạt động liên quan đến Syllabus
 * Mọi hoạt động CRUD đều được ghi vào audit log
 */
export const syllabusAuditService = {
  /**
   * Ghi log và tạo mới đề cương
   */
  async createSyllabusWithLog(data: any): Promise<any> {
    try {
      const result = await syllabusService.createSyllabus(data);
      await auditLogService.logAction(
        'Syllabus',
        result?.id || null,
        'CREATE',
        `Tạo mới đề cương: ${data?.versionNo || data?.version || data?.subjectCode || 'N/A'}`,
        'SUCCESS',
        undefined,
        result as any
      );
      return result;
    } catch (error: any) {
      await auditLogService.logAction(
        'Syllabus',
        null,
        'CREATE',
        `Lỗi tạo mới đề cương: ${error?.response?.data?.message || error?.message}`,
        'FAILED'
      );
      throw error;
    }
  },

  /**
   * Ghi log và cập nhật đề cương
   */
  async updateSyllabusWithLog(id: string, data: any, oldData?: any): Promise<any> {
    try {
      const result = await syllabusService.updateSyllabus(id, data);
      await auditLogService.logAction(
        'Syllabus',
        id,
        'UPDATE',
        `Cập nhật đề cương: ${result?.version || result?.subjectCode || 'N/A'}`,
        'SUCCESS',
        oldData || { version: 'N/A' },
        result as any
      );
      return result;
    } catch (error: any) {
      await auditLogService.logAction(
        'Syllabus',
        id,
        'UPDATE',
        `Lỗi cập nhật đề cương: ${error?.response?.data?.message || error?.message}`,
        'FAILED'
      );
      throw error;
    }
  },

  /**
   * Ghi log và xóa đề cương
   */
  async deleteSyllabusWithLog(id: string, syllabusInfo?: any): Promise<void> {
    try {
      await syllabusService.deleteSyllabus(id);
      await auditLogService.logAction(
        'Syllabus',
        id,
        'DELETE',
        `Xóa đề cương: ${syllabusInfo?.versionNo || syllabusInfo?.subjectCode || syllabusInfo?.subjectNameVi || 'N/A'}`,
        'SUCCESS',
        syllabusInfo || { id }
      );
    } catch (error: any) {
      await auditLogService.logAction(
        'Syllabus',
        id,
        'DELETE',
        `Lỗi xóa đề cương: ${error?.response?.data?.message || error?.message}`,
        'FAILED'
      );
      throw error;
    }
  },

  /**
   * Ghi log và submit đề cương
   */
  async submitSyllabusWithLog(id: string, comment?: string): Promise<any> {
    try {
      const result = await syllabusService.submitForApproval(id, comment);
      await auditLogService.logAction(
        'Syllabus',
        id,
        'SUBMIT',
        `Gửi duyệt đề cương: ${result?.version || result?.subjectCode || 'N/A'}${comment ? ` (${comment})` : ''}`,
        'SUCCESS'
      );
      return result;
    } catch (error: any) {
      await auditLogService.logAction(
        'Syllabus',
        id,
        'SUBMIT',
        `Lỗi gửi duyệt đề cương: ${error?.response?.data?.message || error?.message}`,
        'FAILED'
      );
      throw error;
    }
  },

  /**
   * Ghi log và phê duyệt đề cương
   */
  async approveSyllabusWithLog(id: string, comment?: string): Promise<any> {
    try {
      const result = await syllabusService.approveSyllabus({
        syllabusId: id,
        action: 'APPROVE',
        reason: comment || '',
      });
      await auditLogService.logAction(
        'Syllabus',
        id,
        'APPROVE',
        `Phê duyệt đề cương: ${result?.version || result?.subjectCode || 'N/A'}${comment ? ` (${comment})` : ''}`,
        'SUCCESS'
      );
      return result;
    } catch (error: any) {
      await auditLogService.logAction(
        'Syllabus',
        id,
        'APPROVE',
        `Lỗi phê duyệt đề cương: ${error?.response?.data?.message || error?.message}`,
        'FAILED'
      );
      throw error;
    }
  },

  /**
   * Ghi log và từ chối đề cương
   */
  async rejectSyllabusWithLog(id: string, reason: string): Promise<any> {
    try {
      const result = await syllabusService.rejectSyllabus({
        syllabusId: id,
        action: 'REJECT',
        reason,
      });
      await auditLogService.logAction(
        'Syllabus',
        id,
        'REJECT',
        `Từ chối đề cương: ${result?.version || result?.subjectCode || 'N/A'} (Lý do: ${reason})`,
        'SUCCESS'
      );
      return result;
    } catch (error: any) {
      await auditLogService.logAction(
        'Syllabus',
        id,
        'REJECT',
        `Lỗi từ chối đề cương: ${error?.response?.data?.message || error?.message}`,
        'FAILED'
      );
      throw error;
    }
  },

  /**
   * Ghi log và xuất bản đề cương
   */
  async publishSyllabusWithLog(id: string, effectiveDate: string, comment?: string): Promise<any> {
    try {
      const result = await syllabusService.publishSyllabus(id, effectiveDate, comment);

      // Safe access cho version với nhiều fallback
      const versionStr = result?.version || result?.subjectCode || result?.subjectNameVi || 'N/A';

      await auditLogService.logAction(
        'Syllabus',
        id,
        'PUBLISH',
        `Xuất hành đề cương: ${versionStr} (Ngày hiệu lực: ${effectiveDate})${
          comment ? ` (${comment})` : ''
        }`,
        'SUCCESS'
      );
      return result;
    } catch (error: any) {
      await auditLogService.logAction(
        'Syllabus',
        id,
        'PUBLISH',
        `Lỗi xuất hành đề cương: ${error?.response?.data?.message || error?.message}`,
        'FAILED'
      );
      throw error;
    }
  },

  /**
   * Ghi log và gỡ bỏ đề cương
   */
  async unpublishSyllabusWithLog(id: string, reason: string): Promise<any> {
    try {
      const result = await syllabusService.unpublishSyllabus(id, reason);

      // Safe access
      const versionStr = result?.version || result?.subjectCode || result?.subjectNameVi || 'N/A';

      await auditLogService.logAction(
        'Syllabus',
        id,
        'UNPUBLISH',
        `Gỡ bỏ đề cương: ${versionStr} (Lý do: ${reason})`,
        'SUCCESS'
      );
      return result;
    } catch (error: any) {
      await auditLogService.logAction(
        'Syllabus',
        id,
        'UNPUBLISH',
        `Lỗi gỡ bỏ đề cương: ${error?.response?.data?.message || error?.message}`,
        'FAILED'
      );
      throw error;
    }
  },

  /**
   * Ghi log và xuất file
   */
  async exportSyllabusWithLog(id: string, format: string = 'csv'): Promise<any> {
    try {
      const result = await syllabusService.exportToCSV({ search: id });
      await auditLogService.logAction(
        'Syllabus',
        id,
        'EXPORT',
        `Xuất file đề cương (${format.toUpperCase()})`,
        'SUCCESS'
      );
      return result;
    } catch (error: any) {
      await auditLogService.logAction(
        'Syllabus',
        id,
        'EXPORT',
        `Lỗi xuất file đề cương: ${error?.response?.data?.message || error?.message}`,
        'FAILED'
      );
      throw error;
    }
  },

  /**
   * Ghi log hành động User
   */
  async logUserAction(
    action: string,
    description: string,
    status: 'SUCCESS' | 'FAILED' = 'SUCCESS'
  ): Promise<void> {
    try {
      await auditLogService.logAction('User', null, action, description, status);
    } catch (error) {
      console.error('❌ Failed to log user action:', error);
    }
  },

  /**
   * Ghi log hành động System (đặc biệt cho LOGIN/LOGOUT)
   */
  async logSystemAction(
    action: string,
    description: string,
    status: 'SUCCESS' | 'FAILED' = 'SUCCESS'
  ): Promise<void> {
    try {
      await auditLogService.logAction('System', null, action, description, status);
    } catch (error) {
      console.error('❌ Failed to log system action:', error);
    }
  },
};
