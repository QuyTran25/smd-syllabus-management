import { http } from '@/student/api/http';

export interface NotificationDTO {
  id: string;
  type: 'SUBMISSION' | 'APPROVAL' | 'REJECTION' | 'COMMENT' | 'DEADLINE' | 'ASSIGNMENT' | 'SYSTEM';
  title: string;
  content: string;
  createdAt: string;
  isRead: boolean;
  readAt?: string;
  relatedEntityId?: string;
  relatedEntityType?: string;
  payload?: Record<string, any>;
}

// Hàm hỗ trợ format ngày tháng an toàn
const safeDate = (dateInput: any): string => {
  try {
    if (!dateInput) return new Date().toISOString();
    // Trường hợp Java trả về mảng [2024, 1, 11, 13, 00]
    if (Array.isArray(dateInput)) {
      return new Date(
        dateInput[0],
        dateInput[1] - 1, // Tháng trong JS bắt đầu từ 0
        dateInput[2],
        dateInput[3] || 0,
        dateInput[4] || 0,
        dateInput[5] || 0
      ).toISOString();
    }
    return dateInput;
  } catch (e) {
    return new Date().toISOString();
  }
};

export const studentNotificationService = {
  async getNotifications(): Promise<NotificationDTO[]> {
    try {
      console.log('🔔 [Student] Fetching notifications...');
      const response = await http.get('/notifications');
      console.log('📨 [Student] Raw response:', response.data);
      
      const rawData = response.data?.data || [];
      
      if (!rawData || rawData.length === 0) {
        console.log('ℹ️ [Student] No notifications found');
        return [];
      }

      // Map dữ liệu an toàn (Safe Mapping)
      const mapped = rawData.map((n: any) => {
        // Xử lý Payload an toàn (tránh lỗi JSON parse)
        let safePayload = {};
        if (n.payload) {
          if (typeof n.payload === 'string') {
            try {
              safePayload = JSON.parse(n.payload);
            } catch {}
          } else {
            safePayload = n.payload;
          }
        }

        return {
          id: n.id,
          type: n.type || 'SYSTEM', // Fallback type nếu null
          title: n.title || 'Thông báo mới',
          content: n.message || '',
          createdAt: safeDate(n.createdAt), // Xử lý ngày tháng
          isRead: !!n.isRead,
          readAt: n.readAt,
          relatedEntityId: n.relatedEntityId,
          relatedEntityType: n.relatedEntityType,
          payload: safePayload,
        };
      });
      
      console.log('📋 [Student] Before sort:', mapped.map(n => `${n.title.substring(0, 30)} - ${n.createdAt}`));
      
      // Sort theo createdAt giảm dần (mới nhất lên đầu)
      const notifications = mapped.sort((a, b) => {
        const dateA = new Date(a.createdAt).getTime();
        const dateB = new Date(b.createdAt).getTime();
        return dateB - dateA; // Descending: newest first
      });
      
      console.log('✅ [Student] After sort:', notifications.map(n => `${n.title.substring(0, 30)} - ${n.createdAt}`));
      console.log('✅ [Student] Notifications processed:', notifications.length);
      return notifications;
    } catch (error) {
      console.error('❌ [Student] Lỗi parse notification:', error);
      return []; // Trả về mảng rỗng để không crash web
    }
  },

  async getUnreadCount(): Promise<number> {
    try {
      const response = await http.get('/notifications/unread-count');
      return response.data?.data || 0;
    } catch {
      return 0;
    }
  },

  async markAsRead(notificationId: string): Promise<void> {
    await http.patch(`/notifications/${notificationId}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await http.patch('/notifications/read-all');
  },

  async deleteNotification(notificationId: string): Promise<void> {
    await http.delete(`/notifications/${notificationId}`);
  },
};
