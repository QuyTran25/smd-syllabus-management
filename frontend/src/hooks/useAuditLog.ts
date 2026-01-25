import { useCallback } from 'react';
import { auditLogService } from '@/services/auditlog.service';

export interface AuditLogOptions {
  entityName: string;
  action: string;
  description?: string;
  entityId?: string | null;
  oldValue?: Record<string, unknown>;
  newValue?: Record<string, unknown>;
}

/**
 * Hook để dễ dàng ghi log các hoạt động
 * Sử dụng: const { logAction } = useAuditLog();
 * logAction({ entityName: 'Syllabus', action: 'CREATE', description: '...' });
 */
export const useAuditLog = () => {
  const logAction = useCallback(async (options: AuditLogOptions) => {
    const { entityName, action, description = '', entityId = null, oldValue, newValue } = options;

    try {
      await auditLogService.logAction(
        entityName,
        entityId,
        action,
        description,
        'SUCCESS',
        oldValue,
        newValue
      );
    } catch (error) {
      console.error('❌ Failed to log action:', error);
      // Không throw error, để không ảnh hưởng hành động chính
    }
  }, []);

  const logError = useCallback(async (options: AuditLogOptions) => {
    const { entityName, action, description = '', entityId = null } = options;

    try {
      await auditLogService.logAction(entityName, entityId, action, description, 'FAILED');
    } catch (error) {
      console.error('❌ Failed to log error:', error);
    }
  }, []);

  return { logAction, logError };
};

/**
 * Utility function để ghi log (không cần hook)
 * log('Syllabus', 'CREATE', 'Tạo mới đề cương')
 */
export const logAuditAction = async (
  entityName: string,
  action: string,
  description: string,
  entityId?: string | null,
  status: 'SUCCESS' | 'FAILED' = 'SUCCESS',
  oldValue?: Record<string, unknown>,
  newValue?: Record<string, unknown>
) => {
  try {
    await auditLogService.logAction(
      entityName,
      entityId || null,
      action,
      description,
      status,
      oldValue,
      newValue
    );
  } catch (error) {
    console.error('❌ Failed to log action:', error);
  }
};
