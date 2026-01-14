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

// Helper function to count unique syllabi
const countUniqueSyllabi = (r: any) => {
  const ids = new Set((r.data || []).map((s: any) => `${s.subjectId}:${s.academicTermId}`));
  return ids.size;
};

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  // --- Data Fetching for Different Roles ---

  // Fetch counts for AA (Phòng Đào tạo)
  const { data: aaPendingCount = 0 } = useQuery({
    queryKey: ['syllabi-aa-pending'],
    queryFn: () => syllabusService.getSyllabi({ status: [SyllabusStatus.PENDING_AA] }, { page: 1, pageSize: 1000 }),
    select: countUniqueSyllabi,
    enabled: user?.role === UserRole.AA,
  });

  const { data: principalPendingCount = 0 } = useQuery({
    queryKey: ['syllabi-principal-pending'],
    queryFn: () =>
      syllabusService.getSyllabi({ status: [SyllabusStatus.PENDING_PRINCIPAL] }, { page: 1, pageSize: 1000 }),
    select: countUniqueSyllabi,
    enabled: user?.role === UserRole.AA || user?.role === UserRole.PRINCIPAL,
  });

  const { data: aaRejectedCount = 0 } = useQuery({
    queryKey: ['syllabi-aa-rejected'],
    queryFn: () => syllabusService.getSyllabi({ status: [SyllabusStatus.REJECTED] }, { page: 1, pageSize: 1000 }),
    select: countUniqueSyllabi,
    enabled: user?.role === UserRole.AA,
  });

  // Fetch counts for Principal (Hiệu trưởng)
  const { data: principalApprovedCount = 0 } = useQuery({
    queryKey: ['syllabi-principal-approved'],
    queryFn: () => syllabusService.getSyllabi({ status: [SyllabusStatus.APPROVED] }, { page: 1, pageSize: 1000 }),
    select: countUniqueSyllabi,
    enabled: user?.role === UserRole.PRINCIPAL,
  });

  const { data: principalRejectedCount = 0 } = useQuery({
    queryKey: ['syllabi-principal-rejected'],
    queryFn: () => syllabusService.getSyllabi({ status: [SyllabusStatus.REJECTED] }, { page: 1, pageSize: 1000 }),
    select: countUniqueSyllabi,
    enabled: user?.role === UserRole.PRINCIPAL,
  });

  // Fetch counts for HOD (Trưởng bộ môn)
  const { data: hodPendingCount = 0 } = useQuery({
    queryKey: ['syllabi-hod-pending'],
    queryFn: () =>
        syllabusService.getSyllabi(
            { status: [SyllabusStatus.PENDING_HOD, SyllabusStatus.PENDING_HOD_REVISION] },
            { page: 1, pageSize: 1000 },
        ),
    select: countUniqueSyllabi,
    enabled: user?.role === UserRole.HOD,
  });

  const { data: hodApprovedCount = 0 } = useQuery({
    queryKey: ['syllabi-hod-approved'],
    queryFn: () => syllabusService.getSyllabi({ status: [SyllabusStatus.PENDING_AA] }, { page: 1, pageSize: 1000 }),
    select: countUniqueSyllabi,
    enabled: user?.role === UserRole.HOD,
  });

  const { data: hodRejectedCount = 0 } = useQuery({
    queryKey: ['syllabi-hod-rejected'],
    queryFn: () => syllabusService.getSyllabi({ status: [SyllabusStatus.REJECTED] }, { page: 1, pageSize: 1000 }),
    select: countUniqueSyllabi,
    enabled: user?.role === UserRole.HOD,
  });

  // --- Role-based Logic for Display ---

  let totalCount = 0;
  let pendingCount = 0;
  let approvedCount = 0;
  let needsEditCount = 0;
  let approvedTitle = 'Đã duyệt';

  if (user?.role === UserRole.AA) {
    pendingCount = aaPendingCount;
    approvedCount = principalPendingCount;
    needsEditCount = aaRejectedCount;
    totalCount = pendingCount + approvedCount + needsEditCount;
    approvedTitle = 'Đã duyệt';
  } else if (user?.role === UserRole.HOD) {
    pendingCount = hodPendingCount;
    approvedCount = hodApprovedCount;
    needsEditCount = hodRejectedCount;
    totalCount = pendingCount + approvedCount + needsEditCount;
    approvedTitle = 'Đã duyệt';
  } else if (user?.role === UserRole.PRINCIPAL) {
    pendingCount = principalPendingCount;
    approvedCount = principalApprovedCount;
    needsEditCount = principalRejectedCount;
    totalCount = pendingCount + approvedCount + needsEditCount;
    approvedTitle = 'Đã phê duyệt';
  }

  // Fetch pending syllabi for the table view (this remains the same, specific to the user's role)
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
      const response = await syllabusService.getSyllabi({ status: statusFilter }, { page: 1, pageSize: 5 });
      return response.data;
    },
  });

  // Table columns
  const columns: ColumnsType<SyllabusItem> = [
    { title: 'Mã môn', dataIndex: 'subjectCode', key: 'subjectCode', width: 100 },
    { title: 'Tên môn học', dataIndex: 'subjectNameVi', key: 'subjectNameVi' },
    { title: 'Giảng viên', dataIndex: 'owner', key: 'owner', width: 200 },
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
        return <Tag color={config?.color}>{config?.text}</Tag>;
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
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Tổng Đề cương"
                value={totalCount}
                prefix={<FileTextOutlined />}
                valueStyle={{ color: '#018486' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Chờ Phê duyệt"
                value={pendingCount}
                prefix={<ClockCircleOutlined />}
                valueStyle={{ color: '#faad14' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title={approvedTitle}
                value={approvedCount}
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Đề cương cần chỉnh"
                value={needsEditCount}
                prefix={<EditOutlined />}
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Card>
          </Col>
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
              <Table columns={columns} dataSource={tableData} loading={isLoading} pagination={false} size="middle" />
            </Card>
          </Col>

          {/* Right Sidebar */}
          <Col xs={24} xl={8}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              {/* Workflow Progress */}
              <Card title="Tiến độ Quy trình" extra={<RiseOutlined style={{ color: '#52c41a' }} />}>
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text>Đã hoàn thành</Text>
                      <Text strong>{approvedCount}</Text>
                    </div>
                    <Progress
                      percent={totalCount ? Math.round((approvedCount / totalCount) * 100) : 0}
                      strokeColor="#52c41a"
                    />
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text>Đang xử lý</Text>
                      <Text strong>{pendingCount}</Text>
                    </div>
                    <Progress
                      percent={totalCount ? Math.round((pendingCount / totalCount) * 100) : 0}
                      strokeColor="#faad14"
                    />
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <Text>Cần chỉnh sửa</Text>
                      <Text strong>{needsEditCount}</Text>
                    </div>
                    <Progress
                      percent={totalCount ? Math.round((needsEditCount / totalCount) * 100) : 0}
                      strokeColor="#ff4d4f"
                    />
                  </div>
                </Space>
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