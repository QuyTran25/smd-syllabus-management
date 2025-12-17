import React, { useState, useEffect } from 'react';
import { Layout, Button, Dropdown, Avatar, Space, Typography, Menu, Grid } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
  EditOutlined,
  CommentOutlined,
  DashboardOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { useAuth } from '@/features/auth/AuthContext';
import NotificationBell from '@/shared/components/NotificationBell';
import type { MenuProps } from 'antd';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;
const { useBreakpoint } = Grid;

export const LecturerLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const screens = useBreakpoint();

  // Auto collapse sidebar on smaller screens
  useEffect(() => {
    if (screens.lg === false) {
      setCollapsed(true);
    } else if (screens.xl === true) {
      setCollapsed(false);
    }
  }, [screens]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Thông tin cá nhân',
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

  const menuItems = [
    {
      key: '/lecturer',
      icon: <DashboardOutlined />,
      label: 'Dashboard',
    },
    {
      key: '/lecturer/syllabi',
      icon: <EditOutlined />,
      label: 'Đề cương của tôi',
    },
    {
      key: '/lecturer/reviews',
      icon: <CommentOutlined />,
      label: 'Đánh giá Cộng tác',
    },
  ];

  // Responsive width
  const getSiderWidth = () => {
    if (screens.xl) return 250;
    if (screens.lg) return 220;
    return 200;
  };

  // Responsive padding/margin
  const getContentStyle = () => {
    if (screens.xs) {
      return { margin: '8px', padding: 8 };
    }
    if (screens.sm && !screens.md) {
      return { margin: '12px 8px', padding: 12 };
    }
    if (screens.md && !screens.lg) {
      return { margin: '16px 8px', padding: 16 };
    }
    if (screens.lg && !screens.xl) {
      return { margin: '20px 12px', padding: 20 };
    }
    return { margin: '24px 16px', padding: 24 };
  };

  const contentStyle = getContentStyle();
  const isMobile = !screens.md;

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        trigger={null}
        width={getSiderWidth()}
        collapsedWidth={isMobile ? 0 : 80}
        breakpoint="lg"
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'sticky',
          top: 0,
          left: 0,
          zIndex: 100,
          display: isMobile && collapsed ? 'none' : 'block',
        }}
      >
        <div
          style={{
            height: screens.xs ? 48 : 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: '#018486',
            color: '#fff',
            fontSize: collapsed ? '1.2rem' : screens.lg ? '1.5rem' : '1.2rem',
            fontWeight: 700,
            letterSpacing: '2px',
          }}
        >
          {collapsed ? 'L' : 'LECTURER'}
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ 
            borderRight: 0,
            fontSize: screens.lg ? '14px' : '13px',
          }}
        />
      </Sider>

      <Layout>
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
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ 
              fontSize: screens.xs ? '14px' : '18px', 
              width: screens.xs ? 32 : 40, 
              height: screens.xs ? 32 : 40 
            }}
          />

          <Space size={screens.xs ? 'small' : 'large'}>
            <NotificationBell />

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
                      Giảng viên{screens.lg && ` - ${user?.department}`}
                    </Text>
                  </div>
                )}
              </Space>
            </Dropdown>
          </Space>
        </Header>

        <Content
          className="lecturer-dashboard"
          style={{
            margin: contentStyle.margin,
            padding: contentStyle.padding,
            minHeight: 280,
            background: '#fff',
            borderRadius: screens.xs ? 4 : 8,
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};
