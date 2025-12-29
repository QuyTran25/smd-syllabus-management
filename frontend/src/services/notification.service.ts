import axiosClient from '@/api/axiosClient';
import type { Notification } from '@/shared/components/NotificationBell';

const API_BASE = '/notifications';

const mapBackend = (b: any): Notification => ({
  id: String(b.id),
  type: (b.type as Notification['type']) || 'ASSIGNMENT',
  title: b.title || b.message || '',
  content: b.message || b.title || '',
  createdAt: b.createdAt ? new Date(b.createdAt).toLocaleString('vi-VN') : '',
  isRead: !!b.isRead,
  relatedEntityId: b.relatedEntityId ? String(b.relatedEntityId) : undefined,
  relatedEntityType: b.relatedEntityType || undefined,
});

export const notificationService = {
  getNotifications: async (): Promise<Notification[]> => {
    const resp = await axiosClient.get(API_BASE);
    const payload = resp.data?.data ?? resp.data ?? [];
    // payload is expected to be an array of NotificationResponse
    return (payload as any[]).map(mapBackend);
  },

  markAsRead: async (notificationId: string): Promise<void> => {
    await axiosClient.patch(`${API_BASE}/${notificationId}/read`);
  },

  markAllAsRead: async (): Promise<void> => {
    await axiosClient.patch(`${API_BASE}/read-all`);
  },

  deleteNotification: async (notificationId: string): Promise<void> => {
    await axiosClient.delete(`${API_BASE}/${notificationId}`);
  },

  // Clear all by deleting each notification (backend doesn't expose a single endpoint)
  clearAll: async (): Promise<void> => {
    const list = await (await axiosClient.get(API_BASE)).data?.data ?? [];
    await Promise.all((list as any[]).map((n) => axiosClient.delete(`${API_BASE}/${n.id}`)));
  },
};
