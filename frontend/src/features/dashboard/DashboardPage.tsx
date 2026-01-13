import React from 'react';
import { Row, Col, Card, Statistic, Typography, Table, Tag, Space, Progress } from 'antd';
import {
  FileTextOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  EditOutlined,
  RiseOutlined,
} from '@ant-design/icons';
import { useAuth } from '../auth';
import { UserRole, SyllabusStatus } from '@/types';
import { useQuery } from '@tanstack/react-query';
import { syllabusService } from '@/services';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;

interface SyllabusItem {
  key: string;
  subjectNameVi: string;
  subjectCode: string;
  status: SyllabusStatus;
  owner: string;
  updatedAt: string;
}

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  // Fetch statistics
  const { data: stats } = useQuery({
    queryKey: ['syllabus-stats'],
    queryFn: () => syllabusService.getStatistics(),
  });

  // Calculate statistics based on user role
  const isPrincipal = user?.role === UserRole.PRINCIPAL;
  
  // For Principal: Special calculation
  const principalStats = {
    totalCount: (stats?.PENDING_PRINCIPAL || 0) + (stats?.APPROVED || 0) + (stats?.PUBLISHED || 0),
    pendingCount: stats?.PENDING_PRINCIPAL || 0,
    approvedCount: stats?.APPROVED || 0,
    publishedCount: stats?.PUBLISHED || 0,
  };
  
  // For other roles: Original calculation
  const needsEditCount = (stats?.REJECTED || 0) + (stats?.REVISION_IN_PROGRESS || 0);
  const generalStats = {
    totalCount: stats ? Object.values(stats).reduce((sum, count) => sum + count, 0) : 0,
    pendingCount: (stats?.PENDING_HOD || 0) + (stats?.PENDING_AA || 0) + (stats?.PENDING_PRINCIPAL || 0) + (stats?.PENDING_HOD_REVISION || 0) + (stats?.PENDING_ADMIN_REPUBLISH || 0),
    publishedCount: stats?.PUBLISHED || 0,
    needsEditCount,
  };
  
  // Use appropriate stats based on role
  const displayStats = isPrincipal ? principalStats : generalStats;

  // Fetch pending syllabi for current user role
  const { data: pendingSyllabi, isLoading } = useQuery({
    queryKey: ['pending-syllabi', user?.role],
    queryFn: async () => {
      let statusFilter: SyllabusStatus[] = [];

      switch (user?.role) {
        case UserRole.HOD:
          statusFilter = [SyllabusStatus.PENDING_HOD, SyllabusStatus.PENDING_HOD_REVISION];
          break;
        case UserRole.AA:
          statusFilter = [SyllabusStatus.PENDING_AA];
          break;
        case UserRole.PRINCIPAL:
          statusFilter = [SyllabusStatus.PENDING_PRINCIPAL];
          break;
        case UserRole.ADMIN:
          statusFilter = [SyllabusStatus.APPROVED];
          break;
      }

      const response = await syllabusService.getSyllabi(
        { status: statusFilter },
        { page: 1, pageSize: 5 }
      );
      return response.data;
    },
  });

  // Table columns
  const columns: ColumnsType<SyllabusItem> = [
    {
      title: 'Mã môn',
      dataIndex: 'subjectCode',
      key: 'subjectCode',
      width: 100,
    },
    {
      title: 'Tên môn học',
      dataIndex: 'subjectNameVi',
      key: 'subjectNameVi',
    },
    {
      title: 'Giảng viên',
      dataIndex: 'owner',
      key: 'owner',
      width: 200,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 150,
      render: (status: SyllabusStatus) => {
        const statusConfig = {
          [SyllabusStatus.PENDING_HOD]: { color: 'orange', text: 'Chờ Trưởng BM' },
          [SyllabusStatus.PENDING_HOD_REVISION]: { color: 'gold', text: 'Chờ TBM (Sửa)' },
          [SyllabusStatus.PENDING_AA]: { color: 'blue', text: 'Chờ Phòng ĐT' },
          [SyllabusStatus.PENDING_PRINCIPAL]: { color: 'purple', text: 'Chờ Hiệu trưởng' },
          [SyllabusStatus.APPROVED]: { color: 'green', text: 'Đã duyệt' },
          [SyllabusStatus.PENDING_ADMIN_REPUBLISH]: { color: 'lime', text: 'Chờ xuất lại' },
          [SyllabusStatus.PUBLISHED]: { color: 'cyan', text: 'Đã xuất bản' },
          [SyllabusStatus.DRAFT]: { color: 'default', text: 'Nháp' },
          [SyllabusStatus.REJECTED]: { color: 'red', text: 'Từ chối' },
          [SyllabusStatus.REVISION_IN_PROGRESS]: { color: 'volcano', text: 'Đang sửa' },
          [SyllabusStatus.INACTIVE]: { color: 'default', text: 'Không hoạt động' },
          [SyllabusStatus.ARCHIVED]: { color: 'default', text: 'Lưu trữ' },
        };

        const config = statusConfig[status];
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: 'Cập nhật',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 120,
      render: (date: string) => new Date(date).toLocaleDateString('vi-VN'),
    },
  ];

  const tableData: SyllabusItem[] = (pendingSyllabi || []).map((s) => ({
    key: s.id,
    subjectNameVi: s.subjectNameVi,
    subjectCode: s.subjectCode,
    status: s.status,
    owner: s.ownerName,
    updatedAt: s.updatedAt,
  }));

  return (
    <div>
      <Title level={2} style={{ marginBottom: 24 }}>
        Dashboard
        <Text type="secondary" style={{ fontSize: '1rem', fontWeight: 400, marginLeft: 16 }}>
          Chào mừng trở lại, {user?.fullName}
        </Text>
      </Title>

      {/* Statistics Cards */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        {isPrincipal ? (
          <>
            {/* Principal Dashboard Cards */}
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Tổng Đề cương"
                  value={displayStats.totalCount}
                  prefix={<FileTextOutlined />}
                  valueStyle={{ color: '#018486' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Chờ Phê duyệt"
                  value={displayStats.pendingCount}
                  prefix={<ClockCircleOutlined />}
                  valueStyle={{ color: '#faad14' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Đã Duyệt"
                  value={principalStats.approvedCount}
                  prefix={<CheckCircleOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Đã Xuất bản"
                  value={displayStats.publishedCount}
                  prefix={<CheckCircleOutlined />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Card>
            </Col>
          </>
        ) : (
          <>
            {/* General Dashboard Cards for HOD, AA, ADMIN */}
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Tổng Đề cương"
                  value={displayStats.totalCount}
                  prefix={<FileTextOutlined />}
                  valueStyle={{ color: '#018486' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Chờ Phê duyệt"
                  value={displayStats.pendingCount}
                  prefix={<ClockCircleOutlined />}
                  valueStyle={{ color: '#faad14' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Đã Xuất bản"
                  value={displayStats.publishedCount}
                  prefix={<CheckCircleOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Đề cương cần chỉnh"
                  value={generalStats.needsEditCount}
                  prefix={<EditOutlined />}
                  valueStyle={{ color: '#ff4d4f' }}
                />
              </Card>
            </Col>
          </>
        )}
      </Row>

      {/* Main Content */}
      <Row gutter={[16, 16]}>
        {/* Pending Approvals Table */}
        <Col xs={24} xl={16}>
          <Card
            title={
              <Space>
                <ClockCircleOutlined />
                <span>Đề cương Chờ Xét duyệt</span>
              </Space>
            }
            extra={<a href="/admin/syllabi">Xem tất cả</a>}
          >
            <Table
              columns={columns}
              dataSource={tableData}
              loading={isLoading}
              pagination={false}
              size="middle"
            />
          </Card>
        </Col>

        {/* Right Sidebar */}
        <Col xs={24} xl={8}>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            {/* Workflow Progress */}
            <Card
              title="Tiến độ Quy trình"
              extra={<RiseOutlined style={{ color: '#52c41a' }} />}
            >
              {isPrincipal ? (
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text>Đã Xuất bản</Text>
                      <Text strong>{principalStats.publishedCount}</Text>
                    </div>
                    <Progress
                      percent={
                        principalStats.totalCount > 0
                          ? Math.round((principalStats.publishedCount / principalStats.totalCount) * 100)
                          : 0
                      }
                      strokeColor="#1890ff"
                    />
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text>Đã Duyệt</Text>
                      <Text strong>{principalStats.approvedCount}</Text>
                    </div>
                    <Progress
                      percent={
                        principalStats.totalCount > 0
                          ? Math.round((principalStats.approvedCount / principalStats.totalCount) * 100)
                          : 0
                      }
                      strokeColor="#52c41a"
                    />
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text>Chờ Phê duyệt</Text>
                      <Text strong>{principalStats.pendingCount}</Text>
                    </div>
                    <Progress
                      percent={
                        principalStats.totalCount > 0
                          ? Math.round((principalStats.pendingCount / principalStats.totalCount) * 100)
                          : 0
                      }
                      strokeColor="#faad14"
                    />
                  </div>
                </Space>
              ) : (
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text>Đã hoàn thành</Text>
                      <Text strong>{generalStats.publishedCount}</Text>
                    </div>
                    <Progress
                      percent={
                        generalStats.totalCount > 0
                          ? Math.round((generalStats.publishedCount / generalStats.totalCount) * 100)
                          : 0
                      }
                      strokeColor="#52c41a"
                    />
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text>Đang xử lý</Text>
                      <Text strong>{generalStats.pendingCount}</Text>
                    </div>
                    <Progress
                      percent={
                        generalStats.totalCount > 0
                          ? Math.round((generalStats.pendingCount / generalStats.totalCount) * 100)
                          : 0
                      }
                      strokeColor="#faad14"
                    />
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text>Cần chỉnh sửa</Text>
                      <Text strong>{generalStats.needsEditCount}</Text>
                    </div>
                    <Progress
                      percent={
                        generalStats.totalCount > 0
                          ? Math.round((generalStats.needsEditCount / generalStats.totalCount) * 100)
                          : 0
                      }
                      strokeColor="#ff4d4f"
                    />
                  </div>
                </Space>
              )}
            </Card>

            {/* Quick Actions */}
            <Card title="Thông báo Gần đây">
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                <div>
                  <Text strong>Đề cương mới được gửi</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: '0.9rem' }}>
                    SE301 - Công nghệ Phần mềm
                  </Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: '0.85rem' }}>
                    5 phút trước
                  </Text>
                </div>
                <div>
                  <Text strong>Đề cương đã được phê duyệt</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: '0.9rem' }}>
                    CS201 - Trí tuệ Nhân tạo
                  </Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: '0.85rem' }}>
                    1 giờ trước
                  </Text>
                </div>
                <div>
                  <Text strong>Đề cương bị từ chối</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: '0.9rem' }}>
                    EE301 - Vi xử lý và Vi điều khiển
                  </Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: '0.85rem' }}>
                    3 giờ trước
                  </Text>
                </div>
              </Space>
            </Card>
          </Space>
        </Col>
      </Row>
    </div>
  );
};
