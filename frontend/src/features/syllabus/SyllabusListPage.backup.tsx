import React, { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Input,
  Select,
  Row,
  Col,
  Typography,
  Tooltip,
  message,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  DownloadOutlined,
  EyeOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { syllabusService } from '@/services';
import { Syllabus, SyllabusStatus, SyllabusFilters } from '@/types';
import type { ColumnsType } from 'antd/es/table';
import type { TablePaginationConfig } from 'antd/es/table/interface';

const { Title } = Typography;
const { Option } = Select;

export const SyllabusListPage: React.FC = () => {
  const navigate = useNavigate();
  const [filters, setFilters] = useState<SyllabusFilters>({});
  const [searchText, setSearchText] = useState('');
  const [pagination, setPagination] = useState({ page: 1, pageSize: 10 });
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  // Fetch syllabi with filters and pagination
  const { data, isLoading } = useQuery({
    queryKey: ['syllabi', filters, pagination],
    queryFn: () => syllabusService.getSyllabi(filters, pagination),
  });

  // Handle table change (pagination, filters, sorter)
  const handleTableChange = (paginationConfig: TablePaginationConfig) => {
    setPagination({
      page: paginationConfig.current || 1,
      pageSize: paginationConfig.pageSize || 10,
    });
  };

  // Handle search
  const handleSearch = () => {
    setFilters({ ...filters, search: searchText });
    setPagination({ ...pagination, page: 1 });
  };

  // Handle status filter
  const handleStatusFilter = (status: SyllabusStatus[]) => {
    setFilters({ ...filters, status });
    setPagination({ ...pagination, page: 1 });
  };

  // Handle export CSV
  const handleExport = async () => {
    try {
      const blob = await syllabusService.exportToCSV(filters);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `de-cuong-${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      message.success('Xuất file CSV thành công');
    } catch (error) {
      message.error('Xuất file thất bại');
    }
  };

  // Handle delete
  const handleDelete = (id: string) => {
    // In real app, show confirmation modal
    message.info(`Xóa đề cương ${id}`);
  };

  // Handle bulk delete
  const handleBulkDelete = () => {
    message.info(`Xóa ${selectedRowKeys.length} đề cương`);
    setSelectedRowKeys([]);
  };

  // Table columns
  const columns: ColumnsType<Syllabus> = [
    {
      title: 'Mã môn',
      dataIndex: 'courseCode',
      key: 'courseCode',
      width: 100,
      fixed: 'left',
      sorter: true,
    },
    {
      title: 'Tên môn học',
      dataIndex: 'courseName',
      key: 'courseName',
      width: 300,
      sorter: true,
      render: (text, record) => (
        <a onClick={() => navigate(`/syllabi/${record.id}`)} style={{ color: '#018486' }}>
          {text}
        </a>
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
      title: 'Loại HP',
      dataIndex: 'courseType',
      key: 'courseType',
      width: 120,
      filters: [
        { text: 'Bắt buộc', value: 'required' },
        { text: 'Tự chọn', value: 'elective' },
        { text: 'Tự chọn tự do', value: 'free' },
      ],
      render: (type: string) => {
        if (type === 'required') return <Tag color="red">Bắt buộc</Tag>;
        if (type === 'elective') return <Tag color="blue">Tự chọn</Tag>;
        if (type === 'free') return <Tag color="green">TC tự do</Tag>;
        return <Tag>N/A</Tag>;
      },
    },
    {
      title: 'Thành phần',
      dataIndex: 'componentType',
      key: 'componentType',
      width: 130,
      filters: [
        { text: 'Chuyên ngành', value: 'major' },
        { text: 'Cơ sở ngành', value: 'foundation' },
        { text: 'Đại cương', value: 'general' },
        { text: 'Khóa luận', value: 'thesis' },
      ],
      render: (type: string) => {
        const typeMap = {
          major: 'Chuyên ngành',
          foundation: 'Cơ sở ngành',
          general: 'Đại cương',
          thesis: 'Khóa luận/TT',
        };
        return typeMap[type as keyof typeof typeMap] || 'N/A';
      },
    },
    {
      title: 'Khoa',
      dataIndex: 'faculty',
      key: 'faculty',
      width: 180,
    },
    {
      title: 'Bộ môn',
      dataIndex: 'department',
      key: 'department',
      width: 180,
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semester',
      key: 'semester',
      width: 120,
    },
    {
      title: 'Giảng viên',
      dataIndex: 'ownerName',
      key: 'ownerName',
      width: 180,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 150,
      filters: Object.values(SyllabusStatus).map((s) => ({ text: s, value: s })),
      render: (status: SyllabusStatus) => {
        const statusConfig = {
          [SyllabusStatus.DRAFT]: { color: 'default', text: 'Nháp' },
          [SyllabusStatus.PENDING_HOD]: { color: 'orange', text: 'Chờ Trưởng BM' },
          [SyllabusStatus.PENDING_AA]: { color: 'blue', text: 'Chờ Phòng ĐT' },
          [SyllabusStatus.PENDING_PRINCIPAL]: { color: 'purple', text: 'Chờ Hiệu trưởng' },
          [SyllabusStatus.APPROVED]: { color: 'green', text: 'Đã duyệt' },
          [SyllabusStatus.PUBLISHED]: { color: 'cyan', text: 'Đã xuất bản' },
          [SyllabusStatus.REJECTED]: { color: 'red', text: 'Từ chối' },
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
      sorter: true,
      render: (date: string) => new Date(date).toLocaleDateString('vi-VN'),
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 120,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Tooltip title="Xem chi tiết">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/syllabi/${record.id}`)}
            />
          </Tooltip>
          <Tooltip title="Xóa">
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDelete(record.id)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => setSelectedRowKeys(keys as string[]),
  };

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>
          Quản lý Đề cương
        </Title>
        <Button type="primary" icon={<PlusOutlined />} size="large">
          Tạo Đề cương mới
        </Button>
      </div>

      <Card>
        {/* Filters */}
        <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
          <Col xs={24} md={12} lg={8}>
            <Input
              placeholder="Tìm theo mã môn, tên môn, giảng viên..."
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              onPressEnter={handleSearch}
              allowClear
            />
          </Col>
          <Col xs={24} md={12} lg={6}>
            <Select
              mode="multiple"
              placeholder="Lọc theo trạng thái"
              style={{ width: '100%' }}
              onChange={handleStatusFilter}
              allowClear
            >
              <Option value={SyllabusStatus.DRAFT}>Nháp</Option>
              <Option value={SyllabusStatus.PENDING_HOD}>Chờ Trưởng BM</Option>
              <Option value={SyllabusStatus.PENDING_AA}>Chờ Phòng ĐT</Option>
              <Option value={SyllabusStatus.PENDING_PRINCIPAL}>Chờ Hiệu trưởng</Option>
              <Option value={SyllabusStatus.APPROVED}>Đã duyệt</Option>
              <Option value={SyllabusStatus.PUBLISHED}>Đã xuất bản</Option>
            </Select>
          </Col>
          <Col xs={24} md={12} lg={5}>
            <Button icon={<SearchOutlined />} onClick={handleSearch} block>
              Tìm kiếm
            </Button>
          </Col>
          <Col xs={24} md={12} lg={5}>
            <Button icon={<DownloadOutlined />} onClick={handleExport} block>
              Xuất CSV
            </Button>
          </Col>
        </Row>

        {/* Bulk actions */}
        {selectedRowKeys.length > 0 && (
          <div style={{ marginBottom: 16 }}>
            <Space>
              <span>Đã chọn {selectedRowKeys.length} mục</span>
              <Button danger onClick={handleBulkDelete}>
                Xóa đã chọn
              </Button>
            </Space>
          </div>
        )}

        {/* Table */}
        <Table
          rowSelection={rowSelection}
          columns={columns}
          dataSource={data?.data}
          rowKey="id"
          loading={isLoading}
          pagination={{
            current: pagination.page,
            pageSize: pagination.pageSize,
            total: data?.total,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} đề cương`,
            pageSizeOptions: ['10', '20', '50', '100'],
          }}
          onChange={handleTableChange}
          scroll={{ x: 1500 }}
        />
      </Card>
    </div>
  );
};
