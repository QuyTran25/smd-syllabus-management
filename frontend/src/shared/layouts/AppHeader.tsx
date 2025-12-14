import React, { useState } from 'react';
import { Layout, Button, Dropdown, Avatar, Space, Typography, Input } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SearchOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { useAuth } from '@/features/auth';
import NotificationBell from '../components/NotificationBell';
import type { MenuProps } from 'antd';

const { Header } = Layout;
const { Text } = Typography;

interface AppHeaderProps {
  collapsed: boolean;
  onToggle: () => void;
}

export const AppHeader: React.FC<AppHeaderProps> = ({ collapsed, onToggle }) => {
  const { user, logout } = useAuth();
  const [searchVisible, setSearchVisible] = useState(false);

  const handleLogout = async () => {
    await logout();
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Thông tin cá nhân',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: 'Cài đặt',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Đăng xuất',
      onClick: handleLogout,
      danger: true,
    },
  ];

  return (
    <Header
      style={{
        padding: '0 24px',
        background: '#fff',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
        position: 'sticky',
        top: 0,
        zIndex: 1,
      }}
    >
      <Space size="large">
        <Button
          type="text"
          icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          onClick={onToggle}
          style={{ fontSize: '18px', width: 40, height: 40 }}
        />

        {searchVisible ? (
          <Input
            placeholder="Tìm kiếm đề cương, môn học..."
            prefix={<SearchOutlined />}
            style={{ width: 300 }}
            autoFocus
            onBlur={() => setSearchVisible(false)}
          />
        ) : (
          <Button
            type="text"
            icon={<SearchOutlined />}
            onClick={() => setSearchVisible(true)}
            style={{ fontSize: '16px' }}
          >
            Tìm kiếm
          </Button>
        )}
      </Space>

      <Space size="large">
        {/* Notifications */}
        <NotificationBell />

        {/* User menu */}
        <Dropdown menu={{ items: userMenuItems }} trigger={['click']} placement="bottomRight">
          <Space style={{ cursor: 'pointer' }}>
            <Avatar
              src={user?.avatar}
              icon={!user?.avatar && <UserOutlined />}
              style={{ backgroundColor: '#018486' }}
            />
            <div style={{ lineHeight: 1.2 }}>
              <Text strong style={{ display: 'block' }}>
                {user?.fullName}
              </Text>
              <Text type="secondary" style={{ fontSize: '0.85rem' }}>
                {user?.role === 'ADMIN' && 'Quản trị viên'}
                {user?.role === 'HOD' && 'Trưởng Bộ môn'}
                {user?.role === 'AA' && 'Phòng Đào tạo'}
                {user?.role === 'PRINCIPAL' && 'Hiệu trưởng'}
                {user?.role === 'LECTURER' && 'Giảng viên'}
                {user?.role === 'STUDENT' && 'Sinh viên'}
              </Text>
            </div>
          </Space>
        </Dropdown>
      </Space>
    </Header>
  );
};
