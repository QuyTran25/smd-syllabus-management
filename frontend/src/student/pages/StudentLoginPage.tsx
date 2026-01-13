import React, { useState } from 'react';
import { Button, Card, Form, Input, Space, Typography, App } from 'antd';
import { LockOutlined, MailOutlined } from '@ant-design/icons';
// Không cần useNavigate vì dùng window.location.href
import { http } from '../api/http';

const { Title, Text } = Typography;

export const StudentLoginPage: React.FC = () => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      // Gọi API Login
      const res = await http.post('/auth/login', values);

      // Lấy token (tìm kỹ ở mọi chỗ)
      const token = res.data?.data?.accessToken || res.data?.accessToken || res.data?.token;

      if (token) {
        // 1. Lưu vào LOCAL STORAGE (Giống bạn của bạn)
        localStorage.setItem('student_token', token);
        // Lưu thêm key chuẩn của hệ thống để AuthContext đọc được
        localStorage.setItem('access_token', token);

        message.success('Đăng nhập thành công!');

        // 2. Reload trang để AuthContext verify lại token và cấp quyền
        setTimeout(() => {
          window.location.href = '/syllabi';
        }, 500);
      } else {
        message.error('Lỗi: Server không trả về Token');
        console.error('Response:', res.data);
      }
    } catch (err: any) {
      console.error('Login Error:', err);
      message.error('Sai tài khoản hoặc mật khẩu');
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
      <Card style={{ width: 420, borderRadius: 12, boxShadow: '0 10px 30px rgba(0,0,0,0.15)' }}>
        <Space direction="vertical" style={{ width: '100%', textAlign: 'center' }} size={4}>
          <Title level={3} style={{ margin: 0, color: '#018486' }}>
            Student Portal
          </Title>
          <Text type="secondary">Hệ thống Tra cứu Đề cương</Text>
        </Space>
        <div style={{ height: 24 }} />
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item
            name="email"
            rules={[{ required: true, type: 'email', message: 'Email không hợp lệ' }]}
          >
            <Input size="large" prefix={<MailOutlined />} placeholder="Email sinh viên" />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Vui lòng nhập mật khẩu' }]}
          >
            <Input.Password size="large" prefix={<LockOutlined />} placeholder="Mật khẩu" />
          </Form.Item>
          <Button type="primary" htmlType="submit" size="large" block loading={loading}>
            Đăng nhập
          </Button>
        </Form>
      </Card>
    </div>
  );
};
