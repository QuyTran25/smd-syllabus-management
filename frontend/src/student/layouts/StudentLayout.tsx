import React from 'react';
import { Layout, Space, Typography, Dropdown, Avatar, MenuProps } from 'antd';
import { UserOutlined, SettingOutlined, LogoutOutlined } from '@ant-design/icons';
import { Outlet, useNavigate } from 'react-router-dom';

// 1. IMPORT CÁC COMPONENT & CONTEXT QUAN TRỌNG
import { useAuth } from '@/features/auth/AuthContext'; // (Đảm bảo đường dẫn đúng tới file AuthContext)
import { StudentNotificationBell } from '@/student/components/StudentNotificationBell';

const { Header, Content, Footer } = Layout;
const { Text } = Typography;

export const StudentLayout: React.FC = () => {
  const navigate = useNavigate();

  // 2. LẤY THÔNG TIN USER TỪ AUTH CONTEXT
  const { user, logout } = useAuth();

  // Hàm đăng xuất chuẩn
  const handleLogout = async () => {
    await logout(); // Xóa token trong localStorage/Session
    navigate('/login');
  };

  const items: MenuProps['items'] = [
    {
      key: 'profileCard',
      label: (
        <div style={{ padding: '10px 12px' }}>
          {/* Hiển thị Tên thật */}
          <div style={{ fontWeight: 800, fontSize: 16, marginBottom: 8, color: '#1f1f1f' }}>
            {user?.fullName || 'Sinh viên'}
          </div>

          <div
            style={{
              padding: 10,
              borderRadius: 10,
              border: '1px solid rgba(0,0,0,0.06)',
              background: 'rgba(0,0,0,0.02)',
            }}
          >
            <div style={{ display: 'flex', gap: 10, marginBottom: 6, color: '#595959' }}>
              <span style={{ width: 18, textAlign: 'center' }}>✉️</span>
              {/* Hiển thị Email thật */}
              <span>{user?.email || 'Chưa cập nhật'}</span>
            </div>

            {/* Các thông tin khác nếu User có trường dữ liệu thì bind vào đây */}
            <div style={{ display: 'flex', gap: 10, marginBottom: 6, color: '#595959' }}>
              <span style={{ width: 18, textAlign: 'center' }}>🏛️</span>
              <span>Ngành: Công nghệ Thông tin</span>
            </div>
            <div style={{ display: 'flex', gap: 10, marginBottom: 6, color: '#595959' }}>
              <span style={{ width: 18, textAlign: 'center' }}>📚</span>
              <span>Khóa: K16 (2021-2025)</span>
            </div>

            <div style={{ display: 'flex', gap: 10, marginBottom: 6, color: '#595959' }}>
              <span style={{ width: 18, textAlign: 'center' }}>✉️</span>
              <span>student@smd.edu.vn</span>
            </div>

            <div style={{ display: 'flex', gap: 10, color: '#595959' }}>
              <span style={{ width: 18, textAlign: 'center' }}>📞</span>
              <span>0901234588</span>
            </div>
          </div>
        </div>
      ),
      disabled: true, // để không click/hover như item menu
    },

    { type: 'divider' },

    {
      key: 'tracked',
      icon: <UserOutlined />,
      label: 'Đề cương đang theo dõi',
      onClick: () => navigate('/student/syllabi?scope=TRACKED'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: 'Cài đặt',
      onClick: () => navigate('/student/settings'),
    },

    { type: 'divider' },

    {
      key: 'logout',
      icon: <LogoutOutlined />,
      danger: true,
      label: 'Đăng xuất',
      onClick: handleLogout, // Gọi hàm logout chuẩn
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh', background: '#f5f7fb' }}>
      <Header
        style={{
          background: 'rgba(255,255,255,0.92)',
          backdropFilter: 'blur(10px)',
          position: 'sticky',
          top: 0,
          zIndex: 50,
          borderBottom: '1px solid rgba(0,0,0,0.06)',
          padding: '0 20px',
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        {/* Left: Brand */}
        <Space
          align="center"
          size={12}
          style={{ cursor: 'pointer', userSelect: 'none' }}
          onClick={() => navigate('/syllabi?scope=ALL')}
        >
          <div
            style={{
              width: 50,
              height: 50,
              borderRadius: 12,
              display: 'grid',
              placeItems: 'center',
              background:
                'radial-gradient(circle at 30% 20%, rgba(255,255,255,0.35), transparent 40%),' +
                'linear-gradient(135deg, #018486, #1EA69A)',
              color: 'white',
              fontWeight: 800,
              fontSize: 16,
              lineHeight: '1',
              letterSpacing: 0.5,
              boxShadow: '0 10px 24px rgba(1,132,134,0.25)',
            }}
          >
            SMD
          </div>

          <div style={{ lineHeight: 1.1 }}>
            <div style={{ fontWeight: 800, fontSize: 15, color: '#1f1f1f' }}>Student Portal</div>
            <Text type="secondary" style={{ fontSize: 12 }}>
              Tra cứu đề cương
            </Text>
          </div>
        </Space>

        {/* Right: Actions */}
        <Space align="center" size={14}>
          {/* 3. THAY THẾ CHUÔNG CỨNG BẰNG COMPONENT CHUÔNG THÔNG MINH */}
          <StudentNotificationBell />

          <Dropdown menu={{ items }} trigger={['click']}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 10,
                padding: '0px 10px',
                borderRadius: 14,
                border: '1px solid rgba(0,0,0,0.06)',
                background: 'white',
                cursor: 'pointer',
              }}
            >
              <Avatar
                size={30}
                src={user?.avatar} // Hiển thị Avatar thật nếu có
                style={{
                  background: 'linear-gradient(135deg, #018486, #1EA69A)',
                  fontWeight: 700,
                }}
              >
                {/* Fallback nếu không có avatar thì lấy chữ cái đầu */}
                {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
              </Avatar>

              <div style={{ lineHeight: 1.05 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#1f1f1f' }}>
                  {user?.fullName || 'Sinh viên'}
                </div>
                <Text type="secondary" style={{ fontSize: 11 }}>
                  {user?.email || 'student@smd.edu.vn'}
                </Text>
              </div>

              <div style={{ marginLeft: 2, color: '#8c8c8c', fontSize: 12 }}>▾</div>
            </div>
          </Dropdown>
        </Space>
      </Header>

      <Content style={{ padding: 0, overflowX: 'hidden' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <Outlet />
        </div>
      </Content>

      <Footer
        style={{
          marginTop: 20,
          background: '#018486',
          color: 'rgba(255,255,255,0.9)',
          textAlign: 'center',
          padding: '14px 18px',
        }}
      >
        <Text style={{ color: 'rgba(255,255,255,0.9)' }}>
          Bản quyền thuộc về © Trung tâm Thông tin - Thư viện
        </Text>
      </Footer>
    </Layout>
  );
};
