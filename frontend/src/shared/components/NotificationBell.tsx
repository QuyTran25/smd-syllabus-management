import React, { useState } from 'react';
import { Badge, Dropdown, List, Button, Space, Typography, Tag, Empty } from 'antd';
import { BellOutlined, CheckOutlined, DeleteOutlined } from '@ant-design/icons';

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

// Mock notifications
const mockNotifications: Notification[] = [
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

const typeColors: Record<Notification['type'], string> = {
  SUBMISSION: 'blue',
  APPROVAL: 'success',
  REJECTION: 'error',
  COMMENT: 'processing',
  DEADLINE: 'warning',
  ASSIGNMENT: 'cyan',
};

const NotificationBell: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>(mockNotifications);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const handleMarkAsRead = (id: string) => {
    setNotifications(notifications.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
  };

  const handleMarkAllAsRead = () => {
    setNotifications(notifications.map((n) => ({ ...n, isRead: true })));
  };

  const handleDelete = (id: string) => {
    setNotifications(notifications.filter((n) => n.id !== id));
  };

  const handleClearAll = () => {
    setNotifications([]);
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
      dropdownRender={() => dropdownContent}
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
