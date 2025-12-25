import React, { useState } from 'react';
import { Layout, Menu, theme, Avatar, Dropdown, Space } from 'antd';
import {
  DashboardOutlined,
  UserOutlined,
  BookOutlined,
  SettingOutlined,
  LogoutOutlined,
  MessageOutlined,
  AuditOutlined
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

const { Header, Content, Footer, Sider } = Layout;

const AdminLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();
  
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const userMenu = (
    <Menu items={[
      { key: '1', label: 'Hồ sơ', icon: <UserOutlined /> },
      { type: 'divider' },
      { key: 'logout', label: 'Đăng xuất', icon: <LogoutOutlined />, onClick: handleLogout, danger: true },
    ]} />
  );

  const menuItems = [
    {
      key: '/admin/dashboard',
      icon: <DashboardOutlined />,
      label: 'Tổng quan',
      onClick: () => navigate('/admin/dashboard'),
    },
    {
      key: '/admin/subjects',
      icon: <BookOutlined />,
      label: 'Quản lý Môn học',
      onClick: () => navigate('/admin/subjects'),
    },
    {
      key: '/admin/users',
      icon: <UserOutlined />,
      label: 'Quản lý Người dùng',
      onClick: () => navigate('/admin/users'),
    },
    {
      key: '/admin/audit-logs',
      icon: <AuditOutlined />,
      label: 'Nhật ký hệ thống',
      onClick: () => navigate('/admin/audit-logs'),
    },
    {
      key: '/admin/student-feedback',
      icon: <MessageOutlined />,
      label: 'Phản hồi sinh viên',
      onClick: () => navigate('/admin/student-feedback'),
    },
    {
      key: '/admin/settings',
      icon: <SettingOutlined />,
      label: 'Cấu hình hệ thống',
      onClick: () => navigate('/admin/settings'),
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={(value) => setCollapsed(value)}>
        <div style={{ 
          height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', 
          textAlign: 'center', color: '#fff', lineHeight: '32px', fontWeight: 'bold'
        }}>
          {collapsed ? 'SMD' : 'SMD ADMIN'}
        </div>
        <Menu 
            theme="dark" 
            defaultSelectedKeys={[location.pathname]} 
            mode="inline" 
            items={menuItems} 
        />
      </Sider>
      <Layout>
        <Header style={{ padding: '0 24px', background: colorBgContainer, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ fontSize: '16px', fontWeight: 600 }}>Hệ thống Quản lý Đề cương</div>
          <Space>
             <span>Xin chào, <strong>Admin</strong></span>
             <Dropdown overlay={userMenu} placement="bottomRight" arrow>
                <Avatar style={{ backgroundColor: '#f56a00', cursor: 'pointer' }} icon={<UserOutlined />} />
             </Dropdown>
          </Space>
        </Header>
        <Content style={{ margin: '16px 16px' }}>
          <div
            style={{
              padding: 24,
              minHeight: 360,
              background: colorBgContainer,
              borderRadius: borderRadiusLG,
            }}
          >
            <Outlet />
          </div>
        </Content>
        <Footer style={{ textAlign: 'center', color: '#888' }}>SMD System ©{new Date().getFullYear()}</Footer>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;
