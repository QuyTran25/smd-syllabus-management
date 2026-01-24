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
      console.log('🔔 Fetching notifications...');
      const response = await apiClient.get<{ success: boolean; data: NotificationResponseDTO[] }>(
        '/notifications'
      );
      
      console.log('📨 Raw response:', response.data);
      
      if (!response.data || !response.data.data) {
        console.warn('⚠️ No data in response');
        return [];
      }
      
      // Map backend response to frontend format
      const mapped = response.data.data.map((n) => ({
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
      }));
      
      console.log('📋 Before sort:', mapped.map(n => `${n.title.substring(0, 30)} - ${n.createdAt}`));
      
      // Sort by createdAt descending (newest first)
      const notifications = mapped.sort((a, b) => {
        const dateA = new Date(a.createdAt).getTime();
        const dateB = new Date(b.createdAt).getTime();
        return dateB - dateA; // Descending: newest first
      });
      
      console.log('✅ After sort:', notifications.map(n => `${n.title.substring(0, 30)} - ${n.createdAt}`));
      console.log('✅ Notifications processed:', notifications.length);
      return notifications;
    } catch (error) {
      console.error('❌ Error fetching notifications:', error);
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
