import React from 'react';
import { Layout, Menu, Grid } from 'antd';
import {
  DashboardOutlined,
  FileTextOutlined,
  UserOutlined,
  SettingOutlined,
  CheckOutlined,
  HistoryOutlined,
  TeamOutlined,
  ApartmentOutlined,
  EditOutlined,
  SearchOutlined,
  CommentOutlined,
  MessageOutlined,
  BookOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/features/auth';
import { UserRole } from '@/types';

const { Sider } = Layout;
const { useBreakpoint } = Grid;

interface AppSidebarProps {
  collapsed: boolean;
}

export const AppSidebar: React.FC<AppSidebarProps> = ({ collapsed }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const screens = useBreakpoint();

  // Responsive width
  const getSiderWidth = () => {
    if (screens.xl) return 250;
    if (screens.lg) return 220;
    return 200;
  };

  // Get menu items based on user role
  const getMenuItems = () => {
    const baseItems = [
      {
        key: '/',
        icon: <DashboardOutlined />,
        label: 'Dashboard',
      },
      {
        key: '/syllabi',
        icon: <FileTextOutlined />,
        label: 'Quản lý Đề cương',
      },
    ];

    // Admin-only items
    if (user?.role === UserRole.ADMIN) {
      baseItems.push(
        {
          key: '/users',
          icon: <UserOutlined />,
          label: 'Quản lý Người dùng',
        },
        {
          key: '/settings',
          icon: <SettingOutlined />,
          label: 'Cấu hình Hệ thống',
        },
        {
          key: '/student-feedback',
          icon: <MessageOutlined />,
          label: 'Phản hồi Sinh viên',
        },
        {
          key: '/audit-logs',
          icon: <HistoryOutlined />,
          label: 'Nhật ký Hoạt động',
        }
      );
    }

    // HoD-only items
    if (user?.role === UserRole.HOD) {
      baseItems.push({
        key: '/teaching-assignment',
        icon: <TeamOutlined />,
        label: 'Quản lý Công tác',
      });
    }

    // AA-only items
    if (user?.role === UserRole.AA) {
      baseItems.push({
        key: '/plo-management',
        icon: <ApartmentOutlined />,
        label: 'Quản lý PLO',
      });
      baseItems.push({
        key: '/course-management',
        icon: <BookOutlined />,
        label: 'Quản lý Môn học',
      });
    }

    // Principal-only items
    if (user?.role === UserRole.PRINCIPAL) {
      baseItems.push({
        key: '/batch-approval',
        icon: <CheckOutlined />,
        label: 'Phê duyệt Hàng loạt',
      });
    }

    // Lecturer-only items
    if (user?.role === UserRole.LECTURER) {
      baseItems.push(
        {
          key: '/lecturer/syllabi',
          icon: <EditOutlined />,
          label: 'Đề cương của tôi',
        },
        {
          key: '/lecturer/reviews',
          icon: <CommentOutlined />,
          label: 'Đánh giá Cộng tác',
        }
      );
    }

    // Student-only items
    if (user?.role === UserRole.STUDENT) {
      baseItems.push({
        key: '/student/syllabi',
        icon: <SearchOutlined />,
        label: 'Tra cứu Đề cương',
      });
    }

    return baseItems;
  };

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key);
  };

  // Get current selected key
  const selectedKey = location.pathname;

  // Hide completely on mobile (controlled by MainLayout and CSS)
  const isMobile = !screens.md;

  return (
    <Sider
      collapsible
      collapsed={collapsed}
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
          fontSize: collapsed ? '1.2rem' : screens.lg ? '1.8rem' : '1.5rem',
          fontWeight: 700,
          letterSpacing: '2px',
        }}
      >
        {collapsed ? 'S' : 'SMD'}
      </div>
      <Menu
        mode="inline"
        selectedKeys={[selectedKey]}
        items={getMenuItems()}
        onClick={handleMenuClick}
        style={{ 
          borderRight: 0,
          fontSize: screens.lg ? '14px' : '13px',
        }}
      />
    </Sider>
  );
};
