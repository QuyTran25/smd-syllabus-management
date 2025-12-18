import React, { useMemo, useState } from 'react';
import { Button, Card, Form, Input, Space, Typography, message } from 'antd';
import { LockOutlined, MailOutlined, EyeInvisibleOutlined, EyeTwoTone } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { Title, Text } = Typography;

type LoginForm = { email: string; password: string };

export const StudentLoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const initialValues = useMemo<LoginForm>(
    () => ({ email: 'student@smd.edu.vn', password: '123456' }),
    []
  );

  const onFinish = async (values: LoginForm) => {
    setLoading(true);
    try {
      await new Promise((r) => setTimeout(r, 350));
      localStorage.setItem('student_token', 'mock-token');
      message.success('Đăng nhập thành công (mock)');
      navigate('/syllabi');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        background: 'linear-gradient(135deg, #018486 0%, #1EA69A 100%)',
        padding: 16,
      }}
    >
      <Card
        style={{
          width: 480,
          maxWidth: '100%',
          borderRadius: 10,
          boxShadow: '0 14px 38px rgba(0,0,0,0.18)',
        }}
        bodyStyle={{ padding: 28 }}
      >
        <Space direction="vertical" style={{ width: '100%' }} size={6} align="center">
          <Title level={2} style={{ margin: 0, color: '#018486' }}>
            Student Portal
          </Title>
          <Text type="secondary">Hệ thống Tra cứu Đề cương - Sinh viên</Text>
        </Space>

        <div style={{ height: 18 }} />

        <Form<LoginForm> layout="vertical" initialValues={initialValues} onFinish={onFinish}>
          <Form.Item
            name="email"
            rules={[
              { required: true, message: 'Vui lòng nhập email' },
              { type: 'email', message: 'Email không hợp lệ' },
            ]}
          >
            <Input size="large" prefix={<MailOutlined />} placeholder="Email" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Vui lòng nhập mật khẩu' }]}
          >
            <Input.Password
              size="large"
              prefix={<LockOutlined />}
              placeholder="Mật khẩu"
              iconRender={(v) => (v ? <EyeTwoTone /> : <EyeInvisibleOutlined />)}
            />
          </Form.Item>

          <Form.Item style={{ marginTop: 10 }}>
            <Button type="primary" htmlType="submit" size="large" block loading={loading}>
              Đăng nhập
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: 'center', marginTop: 6 }}>
          <Text type="secondary" style={{ fontSize: 12 }}>
            Demo: student@smd.edu.vn / 123456
          </Text>
        </div>
      </Card>
    </div>
  );
};
