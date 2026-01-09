import { apiClient } from '@/config/api-config';
import type { Notification } from '@/shared/components/NotificationBell';

interface NotificationResponseDTO {
  id: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  relatedEntityType?: string;
  relatedEntityId?: string;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
}

export const notificationService = {
  async getNotifications(): Promise<Notification[]> {
    const response = await apiClient.get<{ success: boolean; data: NotificationResponseDTO[] }>(
      '/api/notifications'
    );
    
    // Map backend response to frontend format
    return response.data.data.map((n) => ({
      id: n.id,
      type: n.type as Notification['type'],
      title: n.title,
      content: n.message, // Backend uses 'message', frontend uses 'content'
      createdAt: n.createdAt,
      isRead: n.isRead,
      readAt: n.readAt,
      relatedEntityId: n.relatedEntityId,
      relatedEntityType: n.relatedEntityType as Notification['relatedEntityType'],
    }));
  },

  async getUnreadCount(): Promise<number> {
    const response = await apiClient.get<{ success: boolean; data: number }>(
      '/api/notifications/unread-count'
    );
    return response.data.data;
  },

  async markAsRead(notificationId: string): Promise<void> {
    await apiClient.patch(`/api/notifications/${notificationId}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await apiClient.patch('/api/notifications/read-all');
  },

  async deleteNotification(notificationId: string): Promise<void> {
    await apiClient.delete(`/api/notifications/${notificationId}`);
  },
};
