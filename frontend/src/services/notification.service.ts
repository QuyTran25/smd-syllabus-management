import type { Notification } from '@/shared/components/NotificationBell';

// Mock notification service
export const notificationService = {
  getNotifications: async (): Promise<Notification[]> => {
    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 300));
    
    return [
      {
        id: '1',
        type: 'REJECTION',
        title: 'Đề cương bị từ chối',
        content: 'Đề cương "CS501 - Học máy" đã bị Trưởng Bộ môn từ chối. Vui lòng xem góp ý và chỉnh sửa.',
        createdAt: '2024-12-09 09:30',
        isRead: false,
        relatedEntityId: 's1',
        relatedEntityType: 'SYLLABUS',
      },
      {
        id: '2',
        type: 'COMMENT',
        title: 'Bình luận mới',
        content: 'TS. Trần Thị B đã thêm bình luận vào đề cương "CS401 - Trí tuệ nhân tạo".',
        createdAt: '2024-12-09 08:15',
        isRead: false,
        relatedEntityId: 's2',
        relatedEntityType: 'SYLLABUS',
      },
      {
        id: '3',
        type: 'DEADLINE',
        title: 'Sắp hết hạn đánh giá',
        content: 'Đánh giá đề cương "CS601 - Xử lý ngôn ngữ tự nhiên" sẽ hết hạn vào 2024-12-20.',
        createdAt: '2024-12-08 16:00',
        isRead: false,
        relatedEntityId: 'r1',
        relatedEntityType: 'REVIEW',
      },
      {
        id: '4',
        type: 'APPROVAL',
        title: 'Đề cương được phê duyệt',
        content: 'Đề cương "CS201 - Cấu trúc dữ liệu" đã được Trưởng Bộ môn phê duyệt!',
        createdAt: '2024-12-08 14:20',
        isRead: true,
        relatedEntityId: 's3',
        relatedEntityType: 'SYLLABUS',
      },
      {
        id: '5',
        type: 'ASSIGNMENT',
        title: 'Phân công đánh giá mới',
        content: 'Bạn được phân công đánh giá đề cương "CS501 - Học máy".',
        createdAt: '2024-12-07 11:00',
        isRead: true,
        relatedEntityId: 'r2',
        relatedEntityType: 'REVIEW',
      },
    ];
  },

  markAsRead: async (notificationId: string): Promise<void> => {
    await new Promise((resolve) => setTimeout(resolve, 200));
    console.log(`Notification ${notificationId} marked as read`);
  },

  markAllAsRead: async (): Promise<void> => {
    await new Promise((resolve) => setTimeout(resolve, 300));
    console.log('All notifications marked as read');
  },

  deleteNotification: async (notificationId: string): Promise<void> => {
    await new Promise((resolve) => setTimeout(resolve, 200));
    console.log(`Notification ${notificationId} deleted`);
  },

  clearAll: async (): Promise<void> => {
    await new Promise((resolve) => setTimeout(resolve, 300));
    console.log('All notifications cleared');
  },
};
