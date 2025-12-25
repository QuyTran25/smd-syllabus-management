import React from 'react';
import { Card, Col, Row, Space, Typography, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import {
  UserOutlined,
  SettingOutlined,
  HistoryOutlined,
  MessageOutlined,
  BookOutlined,
} from '@ant-design/icons';

const { Title, Text } = Typography;

export default function AdminDashboard() {
  const navigate = useNavigate();

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <div>
        <Title level={2} style={{ margin: 0 }}>Admin Dashboard</Title>
        <Text type="secondary">Trang tổng quan cho Quản trị viên</Text>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12} xl={8}>
          <Card title="Người dùng" extra={<UserOutlined />}>
            <Space direction="vertical">
              <Text>Quản lý tài khoản, vai trò, trạng thái.</Text>
              <Button type="primary" onClick={() => navigate('/admin/users')}>
                Đi tới Quản lý Người dùng
              </Button>
            </Space>
          </Card>
        </Col>

        {/* Nút này sẽ dẫn đến trang danh sách môn học lấy từ DB mà bạn vừa làm */}
        <Col xs={24} md={12} xl={8}>
          <Card title="Môn học" extra={<BookOutlined />}>
            <Space direction="vertical">
              <Text>CRUD danh mục môn học.</Text>
              <Button type="primary" ghost onClick={() => navigate('/admin/subjects')}>
                Đi tới Quản lý Môn học
              </Button>
            </Space>
          </Card>
        </Col>

        <Col xs={24} md={12} xl={8}>
          <Card title="Cấu hình" extra={<SettingOutlined />}>
            <Space direction="vertical">
              <Text>Cấu hình hệ thống, tham số vận hành.</Text>
              <Button onClick={() => navigate('/admin/settings')}>
                Đi tới Cấu hình
              </Button>
            </Space>
          </Card>
        </Col>

        <Col xs={24} md={12} xl={8}>
          <Card title="Phản hồi Sinh viên" extra={<MessageOutlined />}>
            <Space direction="vertical">
              <Text>Xử lý phản hồi, bật quyền chỉnh sửa.</Text>
              <Button onClick={() => navigate('/admin/student-feedback')}>
                Đi tới Phản hồi
              </Button>
            </Space>
          </Card>
        </Col>

        <Col xs={24} md={12} xl={8}>
          <Card title="Nhật ký hoạt động" extra={<HistoryOutlined />}>
            <Space direction="vertical">
              <Text>Tra cứu audit log.</Text>
              <Button onClick={() => navigate('/admin/audit-logs')}>
                Đi tới Audit Logs
              </Button>
            </Space>
          </Card>
        </Col>
      </Row>
    </Space>
  );
}