import React, { useState } from 'react';
import { Layout, Button, Dropdown, Avatar, Space, Typography, Input, Grid } from 'antd';
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
const { useBreakpoint } = Grid;

interface AppHeaderProps {
  collapsed: boolean;
  onToggle: () => void;
}

export const AppHeader: React.FC<AppHeaderProps> = ({ collapsed, onToggle }) => {
  const { user, logout } = useAuth();
  const [searchVisible, setSearchVisible] = useState(false);
  const screens = useBreakpoint();

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

  // Responsive search width
  const getSearchWidth = () => {
    if (screens.xl) return 300;
    if (screens.lg) return 250;
    if (screens.md) return 200;
    return 150;
  };

  return (
    <Header
      style={{
        padding: screens.xs ? '0 8px' : screens.md ? '0 16px' : '0 24px',
        background: '#fff',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
        position: 'sticky',
        top: 0,
        zIndex: 1,
        height: screens.xs ? 48 : 64,
      }}
    >
      <Space size={screens.xs ? 'small' : 'large'}>
        <Button
          type="text"
          icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          onClick={onToggle}
          style={{ 
            fontSize: screens.xs ? '14px' : '18px', 
            width: screens.xs ? 32 : 40, 
            height: screens.xs ? 32 : 40 
          }}
        />

        {searchVisible ? (
          <Input
            placeholder={screens.md ? "Tìm kiếm đề cương, môn học..." : "Tìm kiếm..."}
            prefix={<SearchOutlined />}
            style={{ width: getSearchWidth() }}
            autoFocus
            onBlur={() => setSearchVisible(false)}
          />
        ) : (
          <Button
            type="text"
            icon={<SearchOutlined />}
            onClick={() => setSearchVisible(true)}
            style={{ fontSize: screens.xs ? '14px' : '16px' }}
          >
            {screens.md && 'Tìm kiếm'}
          </Button>
        )}
      </Space>

      <Space size={screens.xs ? 'small' : 'large'}>
        {/* Notifications */}
        <NotificationBell />

        {/* User menu */}
        <Dropdown menu={{ items: userMenuItems }} trigger={['click']} placement="bottomRight">
          <Space style={{ cursor: 'pointer' }}>
            <Avatar
              src={user?.avatar}
              icon={!user?.avatar && <UserOutlined />}
              style={{ backgroundColor: '#018486' }}
              size={screens.xs ? 'small' : 'default'}
            />
            {screens.md && (
              <div style={{ lineHeight: 1.2 }}>
                <Text strong style={{ display: 'block', fontSize: screens.lg ? '14px' : '13px' }}>
                  {user?.fullName}
                </Text>
                <Text type="secondary" style={{ fontSize: screens.lg ? '0.85rem' : '0.75rem' }}>
                  {user?.role === 'ADMIN' && 'Quản trị viên'}
                  {user?.role === 'HOD' && 'Trưởng Bộ môn'}
                  {user?.role === 'AA' && 'Phòng Đào tạo'}
                  {user?.role === 'PRINCIPAL' && 'Hiệu trưởng'}
                  {user?.role === 'LECTURER' && 'Giảng viên'}
                  {user?.role === 'STUDENT' && 'Sinh viên'}
                </Text>
              </div>
            )}
          </Space>
        </Dropdown>
      </Space>
    </Header>
  );
};
