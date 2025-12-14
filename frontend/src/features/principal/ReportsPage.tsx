import React, { useState } from 'react';
import { Card, Row, Col, Statistic, Table, Select, DatePicker, Button, Space, Progress, Tag } from 'antd';
import {
  FileTextOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  TeamOutlined,
  DownloadOutlined,
  BarChartOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;
const { Option } = Select;

interface DepartmentStats {
  department: string;
  total: number;
  approved: number;
  pending: number;
  rejected: number;
  avgApprovalTime: number;
}

interface WorkflowBottleneck {
  stage: string;
  count: number;
  avgTime: number;
  maxTime: number;
}

// Mock data
const mockDepartmentStats: DepartmentStats[] = [
  { department: 'Công nghệ Phần mềm', total: 45, approved: 38, pending: 5, rejected: 2, avgApprovalTime: 4.2 },
  { department: 'Khoa học Máy tính', total: 38, approved: 32, pending: 4, rejected: 2, avgApprovalTime: 3.8 },
  { department: 'Hệ thống Thông tin', total: 32, approved: 28, pending: 3, rejected: 1, avgApprovalTime: 4.5 },
  { department: 'Trí tuệ Nhân tạo', total: 28, approved: 24, pending: 3, rejected: 1, avgApprovalTime: 3.9 },
];

const mockBottlenecks: WorkflowBottleneck[] = [
  { stage: 'Trưởng Bộ môn', count: 12, avgTime: 5.2, maxTime: 15 },
  { stage: 'Phòng Đào tạo', count: 8, avgTime: 6.8, maxTime: 18 },
  { stage: 'Hiệu trưởng', count: 5, avgTime: 4.1, maxTime: 12 },
];

export const ReportsPage: React.FC = () => {
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
    dayjs().subtract(30, 'day'),
    dayjs(),
  ]);
  const [selectedFaculty, setSelectedFaculty] = useState<string>('all');

  // Mock query
  const { data: stats, isLoading } = useQuery({
    queryKey: ['reports', dateRange, selectedFaculty],
    queryFn: async () => {
      await new Promise((resolve) => setTimeout(resolve, 800));
      return {
        total: 143,
        approved: 122,
        pending: 15,
        rejected: 6,
        avgApprovalTime: 4.2,
      };
    },
  });

  const departmentColumns: ColumnsType<DepartmentStats> = [
    {
      title: 'Bộ môn',
      dataIndex: 'department',
      key: 'department',
    },
    {
      title: 'Tổng số',
      dataIndex: 'total',
      key: 'total',
      align: 'center',
      width: 100,
    },
    {
      title: 'Đã duyệt',
      dataIndex: 'approved',
      key: 'approved',
      align: 'center',
      width: 100,
      render: (val, record) => (
        <Space>
          <span>{val}</span>
          <Tag color="green">{((val / record.total) * 100).toFixed(0)}%</Tag>
        </Space>
      ),
    },
    {
      title: 'Đang chờ',
      dataIndex: 'pending',
      key: 'pending',
      align: 'center',
      width: 100,
      render: (val) => <Tag color="orange">{val}</Tag>,
    },
    {
      title: 'Từ chối',
      dataIndex: 'rejected',
      key: 'rejected',
      align: 'center',
      width: 100,
      render: (val) => <Tag color="red">{val}</Tag>,
    },
    {
      title: 'Thời gian duyệt TB',
      dataIndex: 'avgApprovalTime',
      key: 'avgApprovalTime',
      align: 'center',
      width: 150,
      render: (val) => `${val} ngày`,
    },
    {
      title: 'Hiệu suất',
      key: 'performance',
      width: 150,
      render: (_, record) => {
        const rate = (record.approved / record.total) * 100;
        return (
          <Progress
            percent={rate}
            size="small"
            status={rate > 80 ? 'success' : rate > 60 ? 'normal' : 'exception'}
          />
        );
      },
    },
  ];

  const bottleneckColumns: ColumnsType<WorkflowBottleneck> = [
    {
      title: 'Giai đoạn',
      dataIndex: 'stage',
      key: 'stage',
    },
    {
      title: 'Số lượng đang chờ',
      dataIndex: 'count',
      key: 'count',
      align: 'center',
      width: 150,
      render: (val) => <Tag color="orange">{val}</Tag>,
    },
    {
      title: 'Thời gian TB',
      dataIndex: 'avgTime',
      key: 'avgTime',
      align: 'center',
      width: 150,
      render: (val) => `${val} ngày`,
    },
    {
      title: 'Thời gian tối đa',
      dataIndex: 'maxTime',
      key: 'maxTime',
      align: 'center',
      width: 150,
      render: (val) => <Tag color="red">{val} ngày</Tag>,
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2>Báo cáo & Thống kê</h2>
        <Space>
          <RangePicker
            value={dateRange}
            onChange={(dates) => dates && setDateRange(dates as [dayjs.Dayjs, dayjs.Dayjs])}
            format="DD/MM/YYYY"
          />
          <Select value={selectedFaculty} onChange={setSelectedFaculty} style={{ width: 200 }}>
            <Option value="all">Tất cả khoa</Option>
            <Option value="cntt">Khoa CNTT</Option>
            <Option value="kt">Khoa Kỹ thuật</Option>
          </Select>
          <Button icon={<DownloadOutlined />} type="primary">
            Export Excel
          </Button>
        </Space>
      </div>

      {/* Overview Statistics */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="Tổng đề cương"
              value={stats?.total || 0}
              prefix={<FileTextOutlined />}
              loading={isLoading}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Đã phê duyệt"
              value={stats?.approved || 0}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#3f8600' }}
              loading={isLoading}
              suffix={`/ ${stats?.total || 0}`}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Đang chờ"
              value={stats?.pending || 0}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
              loading={isLoading}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Thời gian duyệt TB"
              value={stats?.avgApprovalTime || 0}
              suffix="ngày"
              prefix={<BarChartOutlined />}
              loading={isLoading}
            />
          </Card>
        </Col>
      </Row>

      {/* Department Performance */}
      <Card title={<Space><TeamOutlined /> Hiệu suất theo Bộ môn</Space>} style={{ marginBottom: 24 }}>
        <Table
          columns={departmentColumns}
          dataSource={mockDepartmentStats}
          rowKey="department"
          pagination={false}
        />
      </Card>

      {/* Workflow Bottlenecks */}
      <Card title="Điểm nghẽn Quy trình (Bottlenecks)">
        <Table
          columns={bottleneckColumns}
          dataSource={mockBottlenecks}
          rowKey="stage"
          pagination={false}
        />

        <div style={{ marginTop: 16, padding: 12, backgroundColor: '#fff7e6', border: '1px solid #ffd591', borderRadius: 4 }}>
          <strong>⚠️ Khuyến nghị:</strong>
          <ul style={{ marginTop: 8, marginBottom: 0 }}>
            <li>Giai đoạn "Phòng Đào tạo" có thời gian chờ trung bình cao nhất (6.8 ngày) - Cần tăng cường nhân sự review</li>
            <li>12 đề cương đang chờ "Trưởng Bộ môn" - Nhắc nhở các trưởng bộ môn xử lý kịp thời</li>
            <li>Một số đề cương có thời gian chờ tối đa lên đến 18 ngày - Cần thiết lập quy trình nhắc nhở tự động</li>
          </ul>
        </div>
      </Card>
    </div>
  );
};
