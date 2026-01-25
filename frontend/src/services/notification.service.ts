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
  payload?: Record<string, any>;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
}

export const notificationService = {
  async getNotifications(): Promise<Notification[]> {
    try {
      const response = await apiClient.get<{ success: boolean; data: NotificationResponseDTO[] }>(
        '/notifications'
      );
      
      if (!response.data || !response.data.data) {
        return [];
      }
      
      // Map backend response to frontend format and sort by createdAt descending (newest first)
      const notifications = response.data.data
        .map((n) => ({
          id: n.id,
          type: n.type as Notification['type'],
          title: n.title,
          content: n.message, // Backend uses 'message', frontend uses 'content'
          createdAt: n.createdAt,
          isRead: n.isRead,
          readAt: n.readAt,
          relatedEntityId: n.relatedEntityId,
          relatedEntityType: n.relatedEntityType as Notification['relatedEntityType'],
          payload: n.payload,
        }))
        .sort((a, b) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA; // Descending: newest first
        });
      
      return notifications;
    } catch (error) {
      console.error('Error fetching notifications:', error);
      return [];
    }
  },

  async getUnreadCount(): Promise<number> {
    const response = await apiClient.get<{ success: boolean; data: number }>(
      '/notifications/unread-count'
    );
    return response.data.data;
  },

  async markAsRead(notificationId: string): Promise<void> {
    await apiClient.patch(`/notifications/${notificationId}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await apiClient.patch('/notifications/read-all');
  },

  async deleteNotification(notificationId: string): Promise<void> {
    await apiClient.delete(`/notifications/${notificationId}`);
  },
};
