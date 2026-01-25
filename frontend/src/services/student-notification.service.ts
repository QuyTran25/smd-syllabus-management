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
      const response = await http.get('/notifications');
      const rawData = response.data?.data || [];
      
      if (!rawData || rawData.length === 0) {
        return [];
      }

      // Map dữ liệu an toàn và sort theo createdAt giảm dần (mới nhất lên đầu)
      const notifications = rawData
        .map((n: any) => {
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
            type: n.type || 'SYSTEM',
            title: n.title || 'Thông báo mới',
            content: n.message || '',
            createdAt: safeDate(n.createdAt),
            isRead: !!n.isRead,
            readAt: n.readAt,
            relatedEntityId: n.relatedEntityId,
            relatedEntityType: n.relatedEntityType,
            payload: safePayload,
          };
        })
        .sort((a: NotificationDTO, b: NotificationDTO) => {
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          return dateB - dateA;
        });
      
      return notifications;
    } catch (error) {
      console.error('Lỗi parse notification:', error);
      return [];
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
