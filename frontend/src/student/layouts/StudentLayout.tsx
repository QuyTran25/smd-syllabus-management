import React from 'react';
import { Layout, Space, Typography, Badge, Dropdown, Avatar, MenuProps } from 'antd';
import {
  BellOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  ProfileOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate } from 'react-router-dom';

const { Header, Content, Footer } = Layout;
const { Text } = Typography;

export const StudentLayout: React.FC = () => {
  const navigate = useNavigate();

  const items: MenuProps['items'] = [
    {
      key: 'profileCard',
      label: (
        <div style={{ padding: '10px 12px' }}>
          <div style={{ fontWeight: 800, fontSize: 16, marginBottom: 8, color: '#1f1f1f' }}>
            Nguyễn Văn A
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
              <span style={{ width: 18, textAlign: 'center' }}>🆔</span>
              <span>
                MSSV: <b>2021600001</b>
              </span>
            </div>

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

    // {
    //   key: 'profile',
    //   icon: <ProfileOutlined />,
    //   label: 'Thông tin cá nhân',
    //   onClick: () => navigate('/student/profile'),
    // },
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
      onClick: () => navigate('/login'),
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
                'linear-gradient(135deg, rgba(92,110,235,1), rgba(114,73,160,1))',
              color: 'white',
              fontWeight: 800,
              fontSize: 16,
              lineHeight: '1',
              letterSpacing: 0.5,
              boxShadow: '0 10px 24px rgba(92,110,235,0.25)',
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
          <Badge count={2} size="small" offset={[-2, 2]}>
            <div
              style={{
                width: 36,
                height: 36,
                borderRadius: 12,
                display: 'grid',
                placeItems: 'center',
                border: '1px solid rgba(0,0,0,0.06)',
                background: 'white',
                cursor: 'pointer',
              }}
            >
              <BellOutlined style={{ fontSize: 18, color: '#262626' }} />
            </div>
          </Badge>

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
                style={{
                  background: 'linear-gradient(135deg, #1677ff, #69c0ff)',
                  fontWeight: 700,
                }}
              >
                A
              </Avatar>

              <div style={{ lineHeight: 1.05 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#1f1f1f' }}>Nguyễn Văn A</div>
                <Text type="secondary" style={{ fontSize: 11 }}>
                  2021600001
                </Text>
              </div>

              <div style={{ marginLeft: 2, color: '#8c8c8c', fontSize: 12 }}>▾</div>
            </div>
          </Dropdown>
        </Space>
      </Header>

      <Content style={{ padding: 0 }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <Outlet />
        </div>
      </Content>

      <Footer
        style={{
          marginTop: 20,
          background: '#2f7f7d',
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
