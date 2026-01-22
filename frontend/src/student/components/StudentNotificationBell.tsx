import React, { useState } from 'react';
import {
  Badge,
  Dropdown,
  List,
  Button,
  Space,
  Typography,
  Tag,
  Empty,
  message,
  Modal,
  Descriptions,
  Divider,
} from 'antd';
import {
  BellOutlined,
  CheckOutlined,
  DeleteOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
// IMPORT SERVICE RIÊNG CỦA BẠN
import {
  studentNotificationService,
  NotificationDTO,
} from '@/services/student-notification.service';

const { Text, Title, Paragraph } = Typography;

const typeColors: Record<string, string> = {
  SUBMISSION: 'blue',
  APPROVAL: 'success',
  REJECTION: 'error',
  COMMENT: 'processing',
  DEADLINE: 'warning',
  ASSIGNMENT: 'cyan',
  SYSTEM: 'default',
};

export const StudentNotificationBell: React.FC = () => {
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [selectedNotification, setSelectedNotification] = useState<NotificationDTO | null>(null);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  // 1. Fetch notifications dùng service riêng
  const { data: notifications = [], isLoading } = useQuery({
    queryKey: ['student-notifications'], // Đổi key khác để không đụng cache của admin
    queryFn: studentNotificationService.getNotifications,
    refetchInterval: 10000, // Auto refresh every 10 seconds for realtime feel
    refetchOnWindowFocus: true, // Refresh when user returns to tab
    refetchOnMount: true, // Always fetch fresh data on mount
  });

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  // 2. Các Mutation dùng service riêng
  const markAsReadMutation = useMutation({
    mutationFn: studentNotificationService.markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['student-notifications'] });
    },
  });

  const markAllAsReadMutation = useMutation({
    mutationFn: studentNotificationService.markAllAsRead,
    onSuccess: () => {
      message.success('Đã đánh dấu tất cả đã đọc');
      queryClient.invalidateQueries({ queryKey: ['student-notifications'] });
    },
  });

  const deleteNotificationMutation = useMutation({
    mutationFn: studentNotificationService.deleteNotification,
    onSuccess: () => {
      message.success('Đã xóa thông báo');
      queryClient.invalidateQueries({ queryKey: ['student-notifications'] });
    },
  });

  // --- LOGIC XỬ LÝ SỰ KIỆN (Giữ nguyên logic hiển thị) ---
  const handleMarkAsRead = (id: string) => markAsReadMutation.mutate(id);
  const handleMarkAllAsRead = () => markAllAsReadMutation.mutate();
  const handleDelete = (id: string) => deleteNotificationMutation.mutate(id);

  const handleNotificationClick = (notification: NotificationDTO) => {
    setSelectedNotification(notification);
    setDetailModalOpen(true);
    setDropdownOpen(false);
    if (!notification.isRead) {
      markAsReadMutation.mutate(notification.id);
    }
  };

  const handleNavigateToAction = (notification: NotificationDTO) => {
    setDetailModalOpen(false);
    setDropdownOpen(false);

    // Navigate to student portal syllabi list
    if (notification.payload?.actionUrl) {
      let url = notification.payload.actionUrl;
      
      // Strip /student prefix since student portal runs standalone
      if (url.startsWith('/student/')) {
        url = url.replace('/student', '');
      }
      
      // Navigate to the cleaned URL
      if (url.startsWith('/syllabi')) {
        navigate(url);
      } else {
        // For other URLs, go to syllabi list as default
        navigate('/syllabi');
      }
    } else {
      // Default to syllabi list
      navigate('/syllabi');
    }
  };

  const getNotificationTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      SUBMISSION: 'Nộp đề cương',
      APPROVAL: 'Phê duyệt',
      REJECTION: 'Từ chối',
      COMMENT: 'Bình luận',
      DEADLINE: 'Hạn chót',
      ASSIGNMENT: 'Phân công',
      SYSTEM: 'Hệ thống',
    };
    return labels[type] || type;
  };

  // --- GIAO DIỆN DROPDOWN (Copy y nguyên, chỉ sửa style nếu thích) ---
  const dropdownContent = (
    <div
      style={{
        width: 380,
        maxHeight: 500,
        background: '#fff',
        borderRadius: 8,
        boxShadow: '0 3px 6px -4px rgba(0,0,0,.12), 0 6px 16px 0 rgba(0,0,0,.08)',
      }}
    >
      <div
        style={{
          padding: '12px 16px',
          borderBottom: '1px solid #f0f0f0',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Text strong>Thông báo ({unreadCount})</Text>
        <Button
          type="link"
          size="small"
          onClick={handleMarkAllAsRead}
          loading={markAllAsReadMutation.isPending}
        >
          Đọc tất cả
        </Button>
      </div>

      <div style={{ maxHeight: 400, overflowY: 'auto' }}>
        {isLoading ? (
          <div style={{ padding: 40, textAlign: 'center' }}>
            <Text type="secondary">Đang tải...</Text>
          </div>
        ) : notifications.length === 0 ? (
          <Empty
            description="Không có thông báo"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            style={{ padding: '40px 0' }}
          />
        ) : (
          <List
            dataSource={notifications}
            renderItem={(item) => (
              <List.Item
                style={{
                  padding: '12px 16px',
                  background: item.isRead ? '#fff' : '#e6f7ff',
                  cursor: 'pointer',
                  borderBottom: '1px solid #f0f0f0',
                }}
                onClick={() => handleNotificationClick(item)}
                actions={[
                  !item.isRead && (
                    <Button
                      key="read"
                      type="link"
                      size="small"
                      icon={<CheckOutlined />}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleMarkAsRead(item.id);
                      }}
                    />
                  ),
                  <Button
                    key="delete"
                    type="link"
                    size="small"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete(item.id);
                    }}
                  />,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Tag color={typeColors[item.type]}>{item.type}</Tag>
                      <Text strong={!item.isRead}>{item.title}</Text>
                    </Space>
                  }
                  description={
                    <>
                      <div style={{ marginTop: 4 }}>{item.content}</div>
                      <div style={{ fontSize: 11, color: '#999' }}>
                        {new Date(item.createdAt).toLocaleString()}
                      </div>
                    </>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </div>
    </div>
  );

  // --- GIAO DIỆN MODAL CHI TIẾT ---
  const detailModalContent = selectedNotification && (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <div>
        <Title level={4}>{selectedNotification.title}</Title>
      </div>
      <Divider style={{ margin: '12px 0' }} />
      <Paragraph>{selectedNotification.content}</Paragraph>
      <Descriptions column={1} size="small" bordered>
        <Descriptions.Item label="Thời gian">
          {new Date(selectedNotification.createdAt).toLocaleString()}
        </Descriptions.Item>
      </Descriptions>
      <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
        {selectedNotification.payload?.actionUrl && (
          <Button type="primary" onClick={() => handleNavigateToAction(selectedNotification)}>
            Xem chi tiết
          </Button>
        )}
        <Button
          danger
          icon={<DeleteOutlined />}
          onClick={() => {
            handleDelete(selectedNotification.id);
            setDetailModalOpen(false);
          }}
        >
          Xóa
        </Button>
      </Space>
    </Space>
  );

  return (
    <>
      <Dropdown
        popupRender={() => dropdownContent}
        trigger={['click']}
        open={dropdownOpen}
        onOpenChange={setDropdownOpen}
        placement="bottomRight"
      >
        <Badge count={unreadCount} offset={[-5, 5]}>
          <Button type="text" icon={<BellOutlined style={{ fontSize: '18px' }} />} />
        </Badge>
      </Dropdown>
      <Modal
        title="Chi tiết thông báo"
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={null}
        width={600}
      >
        {detailModalContent}
      </Modal>
    </>
  );
};
