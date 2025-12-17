import React, { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { Layout, Grid } from 'antd';
import { AppSidebar } from './AppSidebar';
import { AppHeader } from './AppHeader';

const { Content } = Layout;
const { useBreakpoint } = Grid;

export const MainLayout: React.FC = () => {
  const screens = useBreakpoint();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  // Auto collapse sidebar on smaller screens
  useEffect(() => {
    if (screens.lg === false) {
      setSidebarCollapsed(true);
    } else if (screens.xl === true) {
      setSidebarCollapsed(false);
    }
  }, [screens]);

  const handleToggleSidebar = () => {
    setSidebarCollapsed(!sidebarCollapsed);
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

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <AppSidebar collapsed={sidebarCollapsed} />
      <Layout>
        <AppHeader collapsed={sidebarCollapsed} onToggle={handleToggleSidebar} />
        <Content
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
