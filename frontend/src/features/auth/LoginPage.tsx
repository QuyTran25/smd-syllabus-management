import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, Space, Row, Col } from 'antd';
import { UserOutlined, LockOutlined, LoginOutlined } from '@ant-design/icons';
import { useAuth } from './AuthContext';
import { useNavigate } from 'react-router-dom';
import { UserRole } from '@/types';

const { Title, Text, Paragraph } = Typography;

interface LoginFormValues {
  email: string;
  password: string;
}

export const LoginPage: React.FC = () => {
  const { login, isLoading } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const onFinish = async (values: LoginFormValues) => {
    try {
      const user = await login(values.email, values.password);
      
      // Redirect based on user role
      if (user.role === UserRole.LECTURER) {
        navigate('/lecturer');
      } else if (user.role === UserRole.STUDENT) {
        navigate('/student');
      } else {
        // Admin, HoD, AA, Principal
        navigate('/');
      }
    } catch (error) {
      // Error handled in AuthContext
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #018486 0%, #1EA69A 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '20px',
      }}
    >
      <Row gutter={[32, 32]} style={{ maxWidth: 1200, width: '100%' }}>
        <Col xs={24} lg={12}>
          <div style={{ color: 'white', padding: '20px' }}>
            <Title level={1} style={{ color: 'white', fontSize: '3rem', marginBottom: '1rem' }}>
              SMD
            </Title>
            <Title level={3} style={{ color: 'white', fontWeight: 400 }}>
              Hệ thống Quản lý và Số hóa Giáo trình
            </Title>
            <Paragraph style={{ color: 'rgba(255,255,255,0.9)', fontSize: '1.1rem', marginTop: '2rem' }}>
              Nền tảng tập trung số hóa quy trình duyệt giáo trình, tích hợp AI hỗ trợ so sánh, tóm tắt và ánh xạ CLO-PLO.
            </Paragraph>
            <Space direction="vertical" size="middle" style={{ marginTop: '2rem', width: '100%' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{ width: 40, height: 40, background: 'rgba(255,255,255,0.2)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  ✓
                </div>
                <Text style={{ color: 'white' }}>Quy trình phê duyệt minh bạch 5 cấp</Text>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{ width: 40, height: 40, background: 'rgba(255,255,255,0.2)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  ✓
                </div>
                <Text style={{ color: 'white' }}>Tích hợp AI so sánh và tóm tắt nội dung</Text>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{ width: 40, height: 40, background: 'rgba(255,255,255,0.2)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  ✓
                </div>
                <Text style={{ color: 'white' }}>Quản lý phiên bản và kiểm soát chất lượng</Text>
              </div>
            </Space>
          </div>
        </Col>

        <Col xs={24} lg={12}>
          <Card
            style={{
              boxShadow: '0 8px 24px rgba(0,0,0,0.15)',
              borderRadius: '12px',
            }}
          >
            <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
              <Title level={2} style={{ color: '#018486', marginBottom: '0.5rem' }}>
                Đăng nhập
              </Title>
              <Text type="secondary">Chào mừng bạn quay trở lại</Text>
            </div>

            <Form
              form={form}
              name="login"
              onFinish={onFinish}
              layout="vertical"
              size="large"
              autoComplete="off"
            >
              <Form.Item
                name="email"
                label="Email"
                rules={[
                  { required: true, message: 'Vui lòng nhập email!' },
                  { 
                    pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, 
                    message: 'Email không hợp lệ!' 
                  },
                ]}
              >
                <Input 
                  prefix={<UserOutlined />} 
                  placeholder="lecturer@smd.edu.vn" 
                  autoComplete="off"
                  onBlur={(e) => {
                    // Trim spaces
                    const value = e.target.value.trim();
                    form.setFieldValue('email', value);
                  }}
                />
              </Form.Item>

              <Form.Item
                name="password"
                label="Mật khẩu"
                rules={[{ required: true, message: 'Vui lòng nhập mật khẩu!' }]}
              >
                <Input.Password prefix={<LockOutlined />} placeholder="••••••" />
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  icon={<LoginOutlined />}
                  block
                  loading={isLoading}
                  style={{ height: 45, fontSize: '1rem' }}
                >
                  Đăng nhập
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>
      </Row>
    </div>
  );
};
