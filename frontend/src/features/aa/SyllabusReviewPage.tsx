import React, { useState } from 'react';
import {
  Card,
  Table,
  Tag,
  Space,
  Typography,
  Input,
  Button,
  Tooltip,
  Empty,
} from 'antd';
import {
  SearchOutlined,
  EyeOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { syllabusService } from '@/services';
import { SyllabusStatus } from '@/types';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

interface SyllabusItem {
  id: string;
  subjectCode: string;
  subjectNameVi: string;
  subjectNameEn: string;
  credits: number;
  status: SyllabusStatus;
  createdBy: string;
  createdAt: string;
  department: string;
  faculty: string;
}

const SyllabusReviewPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchText, setSearchText] = useState('');
  const [pagination, setPagination] = useState({ page: 1, pageSize: 10 });

  // Fetch syllabi with PENDING_AA status
  const { data, isLoading } = useQuery({
    queryKey: ['syllabi-pending-aa', pagination],
    queryFn: () =>
      syllabusService.getSyllabi(
        { status: [SyllabusStatus.PENDING_AA] },
        pagination
      ),
  });

  const getStatusTag = (status: SyllabusStatus) => {
    const statusConfig: Record<string, { color: string; text: string }> = {
      [SyllabusStatus.PENDING_AA]: { color: 'orange', text: 'Chờ Phòng ĐT' },
      [SyllabusStatus.APPROVED]: { color: 'green', text: 'Đã duyệt' },
      [SyllabusStatus.REJECTED]: { color: 'red', text: 'Từ chối' },
    };
    const config = statusConfig[status] || { color: 'default', text: status };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  const columns: ColumnsType<SyllabusItem> = [
    {
      title: 'Mã môn',
      dataIndex: 'subjectCode',
      key: 'subjectCode',
      width: 120,
      render: (text) => <Text strong>{text}</Text>,
    },
    {
      title: 'Tên môn học',
      dataIndex: 'subjectNameVi',
      key: 'subjectNameVi',
      ellipsis: true,
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <Text>{text}</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {record.subjectNameEn}
          </Text>
        </Space>
      ),
    },
    {
      title: 'Tín chỉ',
      dataIndex: 'credits',
      key: 'credits',
      width: 80,
      align: 'center',
    },
    {
      title: 'Bộ môn',
      dataIndex: 'department',
      key: 'department',
      width: 180,
    },
    {
      title: 'Người tạo',
      dataIndex: 'createdBy',
      key: 'createdBy',
      width: 150,
    },
    {
      title: 'Ngày gửi',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 120,
      render: (date) => dayjs(date).format('DD/MM/YYYY'),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 160,
      render: (status) => getStatusTag(status),
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <Tooltip title="Xem chi tiết">
          <Button
            type="primary"
            ghost
            icon={<EyeOutlined />}
            onClick={() => navigate(`/admin/syllabi/${record.id}`)}
          />
        </Tooltip>
      ),
    },
  ];

  // Transform API data to table format
  const tableData: SyllabusItem[] =
    data?.data?.map((item: any) => ({
      id: item.id,
      subjectCode: item.subjectCode || 'N/A',
      subjectNameVi: item.subjectNameVi || 'Không có tên',
      subjectNameEn: item.subjectNameEn || '',
      credits: item.creditCount || 0,
      status: item.status,
      createdBy: item.ownerName || 'N/A',
      createdAt: item.createdAt,
      department: item.department || 'N/A',
      faculty: item.faculty || 'N/A',
    })) || [];

  // Filter by search text
  const filteredData = tableData.filter(
    (item) =>
      item.subjectCode.toLowerCase().includes(searchText.toLowerCase()) ||
      item.subjectNameVi.toLowerCase().includes(searchText.toLowerCase()) ||
      item.createdBy.toLowerCase().includes(searchText.toLowerCase())
  );

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {/* Header */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Space>
              <FileTextOutlined style={{ fontSize: 24, color: '#1890ff' }} />
              <Title level={4} style={{ margin: 0 }}>
                Duyệt Đề cương môn học
              </Title>
            </Space>
            <Text type="secondary">
              Danh sách đề cương chờ Phòng Đào tạo duyệt
            </Text>
          </div>

          {/* Search */}
          <Input
            placeholder="Tìm kiếm theo mã môn, tên môn học hoặc người tạo..."
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            style={{ maxWidth: 400 }}
            allowClear
          />

          {/* Table */}
          <Table
            columns={columns}
            dataSource={filteredData}
            rowKey="id"
            loading={isLoading}
            pagination={{
              current: pagination.page,
              pageSize: pagination.pageSize,
              total: data?.total || 0,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} đề cương`,
              onChange: (page, pageSize) => setPagination({ page, pageSize }),
            }}
            locale={{
              emptyText: (
                <Empty
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                  description="Không có đề cương nào chờ duyệt"
                />
              ),
            }}
          />
        </Space>
      </Card>
    </div>
  );
};

export default SyllabusReviewPage;
