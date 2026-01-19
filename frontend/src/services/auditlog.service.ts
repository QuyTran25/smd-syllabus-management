import { apiClient as api } from '@/config/api-config';

export interface AuditLog {
  id: string;
  entityName: string;
  entityId: string;
  action: string;
  actorId: string;
  actorName: string;
  actorEmail: string;
  actorRole: string;
  description: string;
  status: 'SUCCESS' | 'FAILED';
  oldValue?: Record<string, unknown>;
  newValue?: Record<string, unknown>;
  ipAddress: string;
  userAgent?: string;
  createdAt: string;
}

export interface AuditLogFilters {
  entityName?: string;
  action?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

class AuditLogService {
  private baseUrl = '/api/audit-logs';

  async getAuditLogs(filters: AuditLogFilters = {}): Promise<PageResponse<AuditLog>> {
    const params = new URLSearchParams();

    if (filters.page !== undefined) params.append('page', filters.page.toString());
    if (filters.size !== undefined) params.append('size', filters.size.toString());

    const response = await api.get<{
      data: {
        content: AuditLog[];
        totalElements: number;
        totalPages: number;
        size: number;
        number: number;
      };
    }>(`${this.baseUrl}?${params.toString()}`);
    return response.data.data;
  }

  async searchAuditLogs(filters: AuditLogFilters = {}): Promise<PageResponse<AuditLog>> {
    const params = new URLSearchParams();

    if (filters.entityName) params.append('entityName', filters.entityName);
    if (filters.action) params.append('action', filters.action);
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.page !== undefined) params.append('page', filters.page.toString());
    if (filters.size !== undefined) params.append('size', filters.size.toString());

    const response = await api.get<{
      data: {
        content: AuditLog[];
        totalElements: number;
        totalPages: number;
        size: number;
        number: number;
      };
    }>(`${this.baseUrl}/search?${params.toString()}`);
    return response.data.data;
  }

  async getAuditLogById(id: string): Promise<AuditLog> {
    const response = await api.get<{ data: AuditLog }>(`${this.baseUrl}/${id}`);
    return response.data.data;
  }

  async getAuditLogsByEntity(entityName: string, entityId: string): Promise<AuditLog[]> {
    const response = await api.get<{ data: AuditLog[] }>(
      `${this.baseUrl}/entity/${entityName}/${entityId}`
    );
    return response.data.data;
  }

  async getAuditLogsByUser(userId: string, page = 0, size = 20): Promise<PageResponse<AuditLog>> {
    const response = await api.get<{
      data: {
        content: AuditLog[];
        totalElements: number;
        totalPages: number;
        size: number;
        number: number;
      };
    }>(`${this.baseUrl}/user/${userId}?page=${page}&size=${size}`);
    return response.data.data;
  }

  /**
   * Ghi log hành động (từ frontend)
   * Được gọi khi có hành động CRUD hoặc các hoạt động khác
   */
  async logAction(
    entityName: string,
    entityId: string | null,
    action: string,
    description: string,
    status: 'SUCCESS' | 'FAILED' = 'SUCCESS',
    oldValue?: Record<string, unknown>,
    newValue?: Record<string, unknown>
  ): Promise<void> {
    try {
      const payload = {
        entityName,
        entityId,
        action,
        description,
        status,
        oldValue,
        newValue,
      };

      // Gửi log đến backend (endpoint này cần được tạo hoặc sử dụng endpoint khác)
      await api.post(`${this.baseUrl}/log`, payload);
    } catch (error) {
      // Không làm fail hành động chính nếu log thất bại
      console.error('❌ Failed to log action:', error);
    }
  }
}

export const auditLogService = new AuditLogService();
