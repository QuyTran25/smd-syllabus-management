import React, { useEffect, useState } from 'react';
import { Space, Row, Col, Card, Statistic, Typography } from 'antd';
import {
  FileTextOutlined,
  ClockCircleOutlined,
  TeamOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
// Đảm bảo đường dẫn import đúng tới file LecturerDashboard.tsx bạn đã sửa trước đó
import LecturerDashboard from './dashboard/LecturerDashboard';
import { syllabusService } from '@/services/syllabus.service';

const { Title } = Typography;

const DashboardPage: React.FC = () => {
  // State lưu số liệu thống kê
  const [stats, setStats] = useState({
    total: 0,
    draft: 0,
    review: 0,
    rejected: 0,
  });

  useEffect(() => {
    const fetchStats = async () => {
      try {
        // Gọi API lấy dữ liệu thật để tính toán số liệu
        const data = await syllabusService.getMySyllabuses();
        
        if (data) {
          const total = data.length;
          
          // 1. Đang soạn (Draft)
          const draft = data.filter(s => String(s.status) === 'DRAFT').length;
          
          // 2. Đang review (Các trạng thái Pending/Waiting nhưng không phải Revision)
          const review = data.filter(s => {
            const st = String(s.status);
            return (st.includes('PENDING') || st.includes('WAITING')) && !st.includes('REVISION');
          }).length;

          // 3. Cần sửa (Từ chối hoặc Yêu cầu chỉnh sửa)
          const rejected = data.filter(s => {
            const st = String(s.status);
            return st.includes('REJECT') || st.includes('REVISION');
          }).length;

          setStats({ total, draft, review, rejected });
        }
      } catch (error) {
        console.error("Lỗi tải thống kê Dashboard:", error);
      }
    };

    fetchStats();
  }, []);

  return (
    <div style={{ padding: 24 }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Title level={2} style={{ margin: 0 }}>Dashboard Giảng viên</Title>

        {/* --- Phần thống kê (Stats Cards) --- */}
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Tổng nhiệm vụ"
                value={stats.total}
                prefix={<FileTextOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Đang soạn"
                value={stats.draft}
                prefix={<ClockCircleOutlined />}
                valueStyle={{ color: '#faad14' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Đang review"
                value={stats.review}
                prefix={<TeamOutlined />}
                valueStyle={{ color: '#13c2c2' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Cần sửa"
                value={stats.rejected}
                prefix={<ExclamationCircleOutlined />}
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Card>
          </Col>
        </Row>

        {/* --- Phần bảng nhiệm vụ (Gọi component con) --- */}
        {/* Lưu ý: Tiêu đề "Nhiệm vụ của tôi" và logic bảng đã nằm trong LecturerDashboard */}
        <LecturerDashboard />
      </Space>
    </div>
  );
};

export default DashboardPage;