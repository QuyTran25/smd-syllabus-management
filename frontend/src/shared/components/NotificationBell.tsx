import React, { useState } from 'react';
import { Badge, Dropdown, List, Button, Space, Typography, Tag, Empty, message, Modal, Descriptions, Divider } from 'antd';
import { BellOutlined, CheckOutlined, DeleteOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { notificationService } from '@/services/notification.service';

const { Text, Title, Paragraph } = Typography;

export interface Notification {
  id: string;
  type: 'SUBMISSION' | 'APPROVAL' | 'REJECTION' | 'COMMENT' | 'DEADLINE' | 'ASSIGNMENT' | 'SYSTEM';
  title: string;
  content: string;
  createdAt: string;
  isRead: boolean;
  readAt?: string;
  relatedEntityId?: string;
  relatedEntityType?: 'SYLLABUS' | 'REVIEW' | 'SUBJECT' | 'TEACHING_ASSIGNMENT';
}

const typeColors: Record<Notification['type'], string> = {
  SUBMISSION: 'blue',
  APPROVAL: 'success',
  REJECTION: 'error',
  COMMENT: 'processing',
  DEADLINE: 'warning',
  ASSIGNMENT: 'cyan',
  SYSTEM: 'default',
};

const NotificationBell: React.FC = () => {
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [selectedNotification, setSelectedNotification] = useState<Notification | null>(null);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  // Fetch notifications
  const { data: notifications = [], isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: notificationService.getNotifications,
    refetchInterval: 30000, // Auto refresh every 30 seconds
  });

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  // Mark as read mutation
  const markAsReadMutation = useMutation({
    mutationFn: notificationService.markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  // Mark all as read mutation
  const markAllAsReadMutation = useMutation({
    mutationFn: notificationService.markAllAsRead,
    onSuccess: () => {
      message.success('Đã đánh dấu tất cả đã đọc');
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  // Delete notification mutation
  const deleteNotificationMutation = useMutation({
    mutationFn: notificationService.deleteNotification,
    onSuccess: () => {
      message.success('Đã xóa thông báo');
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  const handleMarkAsRead = (id: string) => {
    markAsReadMutation.mutate(id);
  };

  const handleMarkAllAsRead = () => {
    markAllAsReadMutation.mutate();
  };

  const handleDelete = (id: string) => {
    deleteNotificationMutation.mutate(id);
  };

  const handleNotificationClick = (notification: Notification) => {
    setSelectedNotification(notification);
    setDetailModalOpen(true);
    setDropdownOpen(false);
    
    // Auto mark as read when opening detail
    if (!notification.isRead) {
      markAsReadMutation.mutate(notification.id);
    }
  };

  const handleNavigateToAction = (notification: Notification) => {
    // Close modals
    setDetailModalOpen(false);
    setDropdownOpen(false);
    
    // Navigate based on type and actionUrl in payload
    if (notification.type === 'ASSIGNMENT' && notification.relatedEntityType === 'SUBJECT') {
      // Navigate to teaching assignment page
      navigate('/admin/teaching-assignment');
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

  const dropdownContent = (
    <div
      style={{
        width: 380,
        maxHeight: 500,
        backgroundColor: 'white',
        borderRadius: '8px',
        boxShadow: '0 3px 6px -4px rgba(0,0,0,.12), 0 6px 16px 0 rgba(0,0,0,.08)',
      }}
    >
      {/* Header */}
      <div
        style={{
          padding: '12px 16px',
          borderBottom: '1px solid #f0f0f0',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Text strong>Thông báo ({unreadCount} chưa đọc)</Text>
        <Button 
          type="link" 
          size="small" 
          onClick={handleMarkAllAsRead}
          loading={markAllAsReadMutation.isPending}
        >
          Đánh dấu tất cả đã đọc
        </Button>
      </div>

      {/* Notifications List */}
      <div style={{ maxHeight: 400, overflowY: 'auto' }}>
        {isLoading ? (
          <div style={{ padding: '40px', textAlign: 'center' }}>
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
                  backgroundColor: item.isRead ? 'white' : '#f6f9ff',
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
                    loading={deleteNotificationMutation.isPending}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete(item.id);
                    }}
                  />,
                ].filter(Boolean)}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Tag color={typeColors[item.type]} style={{ fontSize: '11px' }}>
                        {item.type}
                      </Tag>
                      <Text strong={!item.isRead}>{item.title}</Text>
                    </Space>
                  }
                  description={
                    <>
                      <div style={{ marginTop: '4px', fontSize: '13px' }}>{item.content}</div>
                      <div style={{ marginTop: '4px', fontSize: '12px', color: '#999' }}>
                        {item.createdAt}
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

  // Detail Modal Content
  const detailModalContent = selectedNotification && (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {/* Header with Type Tag */}
      <div>
        <Space>
          <Tag color={typeColors[selectedNotification.type]} style={{ fontSize: '13px', padding: '4px 12px' }}>
            {getNotificationTypeLabel(selectedNotification.type)}
          </Tag>
          {selectedNotification.isRead ? (
            <Tag color="default" icon={<CheckOutlined />}>Đã đọc</Tag>
          ) : (
            <Tag color="blue">Chưa đọc</Tag>
          )}
        </Space>
      </div>

      {/* Title */}
      <div>
        <Title level={4} style={{ marginBottom: 0 }}>
          {selectedNotification.title}
        </Title>
      </div>

      <Divider style={{ margin: '12px 0' }} />

      {/* Content */}
      <div>
        <Paragraph style={{ fontSize: '15px', lineHeight: '1.8', whiteSpace: 'pre-line' }}>
          {selectedNotification.content}
        </Paragraph>
      </div>

      {/* Metadata */}
      <Descriptions column={1} size="small" bordered>
        <Descriptions.Item label={<Space><ClockCircleOutlined /> Thời gian</Space>}>
          {selectedNotification.createdAt}
        </Descriptions.Item>
        {selectedNotification.readAt && (
          <Descriptions.Item label="Đã đọc lúc">
            {selectedNotification.readAt}
          </Descriptions.Item>
        )}
        {selectedNotification.relatedEntityType && (
          <Descriptions.Item label="Liên quan đến">
            {selectedNotification.relatedEntityType}
          </Descriptions.Item>
        )}
      </Descriptions>

      {/* Actions */}
      <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
        {selectedNotification.type === 'ASSIGNMENT' && (
          <Button
            type="primary"
            onClick={() => handleNavigateToAction(selectedNotification)}
          >
            Đi đến Phân công
          </Button>
        )}
        {!selectedNotification.isRead && (
          <Button
            icon={<CheckOutlined />}
            onClick={() => {
              handleMarkAsRead(selectedNotification.id);
              setDetailModalOpen(false);
            }}
          >
            Đánh dấu đã đọc
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
          Xóa thông báo
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
          <Button
            type="text"
            icon={<BellOutlined style={{ fontSize: '18px' }} />}
            style={{ marginRight: '8px' }}
          />
        </Badge>
      </Dropdown>

      {/* Detail Modal */}
      <Modal
        title="Chi tiết thông báo"
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={null}
        width={700}
      >
        {detailModalContent}
      </Modal>
    </>
  );
};

export default NotificationBell;
