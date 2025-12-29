import React, { useState, useEffect } from 'react';
import { Badge, Dropdown, List, Button, Space, Typography, Tag, Empty } from 'antd';
import { BellOutlined, CheckOutlined, DeleteOutlined } from '@ant-design/icons';
import { notificationService } from '@/services/notification.service';

const { Text } = Typography;

export interface Notification {
  id: string;
  type: 'SUBMISSION' | 'APPROVAL' | 'REJECTION' | 'COMMENT' | 'DEADLINE' | 'ASSIGNMENT';
  title: string;
  content: string;
  createdAt: string;
  isRead: boolean;
  relatedEntityId?: string;
  relatedEntityType?: 'SYLLABUS' | 'REVIEW';
}

const defaultNotifications: Notification[] = [];

const typeColors: Record<Notification['type'], string> = {
  SUBMISSION: 'blue',
  APPROVAL: 'success',
  REJECTION: 'error',
  COMMENT: 'processing',
  DEADLINE: 'warning',
  ASSIGNMENT: 'cyan',
};

const NotificationBell: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>(defaultNotifications);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const handleMarkAsRead = (id: string) => {
    notificationService.markAsRead(id).catch(() => {});
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
  };

  const handleMarkAllAsRead = () => {
    notificationService.markAllAsRead().catch(() => {});
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
  };

  const handleDelete = (id: string) => {
    notificationService.deleteNotification(id).catch(() => {});
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const handleClearAll = () => {
    notificationService.clearAll().catch(() => {});
    setNotifications([]);
  };

  useEffect(() => {
    let mounted = true;
    notificationService
      .getNotifications()
      .then((data) => {
        if (mounted) setNotifications(data || []);
      })
      .catch(() => {
        if (mounted) setNotifications([]);
      });
    return () => {
      mounted = false;
    };
  }, []);

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
        <Space size="small">
          <Button type="link" size="small" onClick={handleMarkAllAsRead}>
            Đánh dấu tất cả đã đọc
          </Button>
          <Button type="link" size="small" danger onClick={handleClearAll}>
            Xóa tất cả
          </Button>
        </Space>
      </div>

      {/* Notifications List */}
      <div style={{ maxHeight: 400, overflowY: 'auto' }}>
        {notifications.length === 0 ? (
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

  return (
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
  );
};

export default NotificationBell;
