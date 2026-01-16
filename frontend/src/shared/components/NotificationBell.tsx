import React, { useState } from 'react';
import { Badge, Dropdown, List, Button, Space, Typography, Tag, Empty, message, Modal, Descriptions, Divider } from 'antd';
import { BellOutlined, CheckOutlined, DeleteOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { notificationService } from '@/services/notification.service';

const { Text, Title, Paragraph } = Typography;

export interface Notification {
  id: string;
  type: 'SUBMISSION' | 'APPROVAL' | 'REJECTION' | 'COMMENT' | 'DEADLINE' | 'ASSIGNMENT' | 'SYSTEM' | 'ERROR_REPORT';
  title: string;
  content: string;
  createdAt: string;
  isRead: boolean;
  readAt?: string;
  relatedEntityId?: string;
  relatedEntityType?: 'SYLLABUS' | 'REVIEW' | 'SUBJECT' | 'TEACHING_ASSIGNMENT' | 'SYLLABUS_VERSION';
  payload?: Record<string, any>;
}

const typeColors: Record<Notification['type'], string> = {
  SUBMISSION: 'blue',
  APPROVAL: 'success',
  REJECTION: 'error',
  COMMENT: 'processing',
  DEADLINE: 'warning',
  ASSIGNMENT: 'cyan',
  SYSTEM: 'default',
  ERROR_REPORT: 'orange',
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
    console.log('Notification clicked:', notification);
    console.log('Notification payload:', notification.payload);
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
    
    // Navigate using actionUrl from payload if available
    if (notification.payload?.actionUrl) {
      navigate(notification.payload.actionUrl);
    } else if (notification.type === 'ASSIGNMENT' && notification.relatedEntityType === 'SUBJECT') {
      // Fallback for HOD assignment notification
      navigate('/admin/teaching-assignment');
    } else if (notification.type === 'ASSIGNMENT' && notification.relatedEntityType === 'TEACHING_ASSIGNMENT') {
      // For lecturer assignment notification
      const actionUrl = notification.payload?.actionUrl || '/lecturer/dashboard';
      navigate(actionUrl);
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
      ERROR_REPORT: 'Yêu cầu chỉnh sửa',
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
                      {/* Action button if payload has actionUrl */}
                      {item.payload?.actionUrl && (
                        <Button
                          type="primary"
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleNavigateToAction(item);
                            if (!item.isRead) {
                              markAsReadMutation.mutate(item.id);
                            }
                          }}
                          style={{ marginTop: '8px' }}
                        >
                          {item.payload.actionLabel || 'Xem chi tiết'}
                        </Button>
                      )}
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

      {/* Feedbacks List (if exists in payload) */}
      {(() => {
        console.log('Checking feedbacks in payload:', selectedNotification.payload);
        console.log('Feedbacks array:', selectedNotification.payload?.feedbacks);
        return selectedNotification.payload?.feedbacks && selectedNotification.payload.feedbacks.length > 0;
      })() && (
        <div>
          <Divider style={{ margin: '12px 0' }}>
            <Text strong style={{ fontSize: '14px' }}>
              Chi tiết các lỗi cần sửa ({selectedNotification.payload.feedbacks.length})
            </Text>
          </Divider>
          <List
            size="small"
            dataSource={selectedNotification.payload.feedbacks}
            renderItem={(fb: any, index: number) => (
              <List.Item style={{ padding: '12px 0', borderBottom: '1px solid #f0f0f0' }}>
                <List.Item.Meta
                  avatar={
                    <Tag color="red" style={{ fontSize: '14px', padding: '4px 8px' }}>
                      #{index + 1}
                    </Tag>
                  }
                  title={
                    <Space>
                      <Tag color="orange">{fb.type}</Tag>
                      <Text strong>{fb.title}</Text>
                    </Space>
                  }
                  description={
                    <div style={{ marginTop: 8 }}>
                      <div style={{ marginBottom: 4 }}>
                        <Text type="secondary" style={{ fontSize: '12px' }}>Phần: </Text>
                        <Tag>{fb.section}</Tag>
                      </div>
                      <div style={{ marginBottom: 4, padding: '8px 12px', backgroundColor: '#fafafa', borderRadius: 4 }}>
                        <Text>{fb.description}</Text>
                      </div>
                      <div style={{ fontSize: '12px', color: '#888' }}>
                        <Text type="secondary">Phản hồi từ sinh viên: </Text>
                        <Text>{fb.studentName || 'Không rõ'}</Text>
                      </div>
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        </div>
      )}

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
        {/* Action button for ERROR_REPORT type */}
        {selectedNotification.type === 'ERROR_REPORT' && selectedNotification.payload?.actionUrl && (
          <Button
            type="primary"
            onClick={() => handleNavigateToAction(selectedNotification)}
          >
            {selectedNotification.payload?.actionLabel || 'Chỉnh sửa ngay'}
          </Button>
        )}
        {selectedNotification.type === 'ASSIGNMENT' && selectedNotification.payload?.actionUrl && (
          <Button
            type="primary"
            onClick={() => handleNavigateToAction(selectedNotification)}
          >
            {selectedNotification.payload?.actionLabel || 'Đi đến Phiên giao'}
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
