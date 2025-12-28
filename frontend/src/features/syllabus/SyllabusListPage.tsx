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
  Modal,
  Form,
  DatePicker,
  Descriptions,
  Timeline,
} from 'antd';
import {
  SearchOutlined,
  DownloadOutlined,
  EyeOutlined,
  DeleteOutlined,
  RocketOutlined,
  StopOutlined,
  HistoryOutlined,
  ExclamationCircleOutlined,
  EditOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { syllabusService, feedbackService } from '@/services';
import { Syllabus, SyllabusStatus, SyllabusFilters, FeedbackStatus, UserRole } from '@/types';
import type { ColumnsType } from 'antd/es/table';
import type { TablePaginationConfig } from 'antd/es/table/interface';
import dayjs from 'dayjs';
import { useAuth } from '@/features/auth';

const { Title, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;

export const SyllabusListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const [filters, setFilters] = useState<SyllabusFilters>({});
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string[]>([]);
  const [pagination, setPagination] = useState({ page: 1, pageSize: 10 });
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);
  
  // Modals state
  const [publishModalVisible, setPublishModalVisible] = useState(false);
  const [unpublishModalVisible, setUnpublishModalVisible] = useState(false);
  const [historyModalVisible, setHistoryModalVisible] = useState(false);
  const [compareModalVisible, setCompareModalVisible] = useState(false);
  const [selectedSyllabus, setSelectedSyllabus] = useState<Syllabus | null>(null);
  const [publishForm] = Form.useForm();
  const [unpublishForm] = Form.useForm();

  // Fetch syllabi with filters and pagination
  const { data, isLoading } = useQuery({
    queryKey: ['syllabi', filters, pagination],
    queryFn: () => syllabusService.getSyllabi(filters, pagination),
  });

  // Fetch feedbacks for needs-edit indicator
  const { data: feedbacksData } = useQuery({
    queryKey: ['feedbacks', FeedbackStatus.PENDING],
    queryFn: () => feedbackService.getFeedbacks({ status: [FeedbackStatus.PENDING] }),
  });

  // Publish mutation
  const publishMutation = useMutation({
    mutationFn: ({ id, effectiveDate }: { id: string; effectiveDate: string }) =>
      syllabusService.approveSyllabus({
        syllabusId: id,
        action: 'APPROVE',
        reason: `Xuất hành với ngày hiệu lực: ${effectiveDate}`,
      }),
    onSuccess: () => {
      message.success('Xuất hành đề cương thành công');
      queryClient.invalidateQueries({ queryKey: ['syllabi'] });
      setPublishModalVisible(false);
      setSelectedSyllabus(null);
      publishForm.resetFields();
    },
    onError: () => {
      message.error('Xuất hành đề cương thất bại');
    },
  });

  // Unpublish mutation
  const unpublishMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      syllabusService.unpublishSyllabus(id, reason),
    onSuccess: () => {
      message.success('Gỡ bỏ đề cương thành công');
      queryClient.invalidateQueries({ queryKey: ['syllabi'] });
      setUnpublishModalVisible(false);
      setSelectedSyllabus(null);
      unpublishForm.resetFields();
    },
    onError: () => {
      message.error('Gỡ bỏ đề cương thất bại');
    },
  });

  // Calculate needs-edit syllabus IDs
  const needsEditSyllabiIds = new Set(
    feedbacksData
      ?.filter((f: any) => f.status === FeedbackStatus.PENDING)
      .map((f: any) => f.syllabusId) || []
  );

  // Handle publish
  const handlePublishClick = (syllabus: Syllabus) => {
    setSelectedSyllabus(syllabus);
    publishForm.setFieldsValue({
      effectiveDate: dayjs(),
    });
    setPublishModalVisible(true);
  };

  const handlePublish = (values: any) => {
    if (!selectedSyllabus) return;

    publishMutation.mutate({
      id: selectedSyllabus.id,
      effectiveDate: values.effectiveDate.format('YYYY-MM-DD'),
    });
  };

  // Handle unpublish
  const handleUnpublishClick = (syllabus: Syllabus) => {
    setSelectedSyllabus(syllabus);
    setUnpublishModalVisible(true);
  };

  const handleUnpublish = (values: any) => {
    if (!selectedSyllabus) return;

    Modal.confirm({
      title: 'Xác nhận gỡ bỏ đề cương',
      icon: <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />,
      content: (
        <Space direction="vertical">
          <Text>Bạn có chắc muốn gỡ bỏ đề cương này?</Text>
          <Text strong>{selectedSyllabus.subjectCode} - {selectedSyllabus.subjectNameVi}</Text>
          <Text type="danger">Đề cương sẽ không còn hiển thị cho sinh viên.</Text>
        </Space>
      ),
      okText: 'Gỡ bỏ',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: () => {
        unpublishMutation.mutate({
          id: selectedSyllabus.id,
          reason: values.reason,
        });
      },
    });
  };

  // Handle view history
  const handleViewHistory = (syllabus: Syllabus) => {
    setSelectedSyllabus(syllabus);
    setHistoryModalVisible(true);
  };

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

  // Handle status filter change
  const handleStatusFilterChange = (values: string[]) => {
    setStatusFilter(values);
    
    // Map special filters
    if (values.includes('needs-edit')) {
      // For "needs-edit", we need PUBLISHED syllabi only
      setFilters({ 
        ...filters, 
        status: [SyllabusStatus.PUBLISHED]
      });
    } else if (values.includes('not-published')) {
      // Replace 'not-published' with APPROVED
      const mappedStatuses = values
        .filter(v => v !== 'not-published')
        .map(v => v as SyllabusStatus)
        .concat([SyllabusStatus.APPROVED]);
      setFilters({ ...filters, status: mappedStatuses });
    } else if (values.includes('approved-statuses')) {
      // AA "Đã duyệt" includes PENDING_PRINCIPAL, APPROVED, PUBLISHED
      const mappedStatuses = values
        .filter(v => v !== 'approved-statuses')
        .map(v => v as SyllabusStatus)
        .concat([SyllabusStatus.PENDING_PRINCIPAL, SyllabusStatus.APPROVED, SyllabusStatus.PUBLISHED]);
      setFilters({ ...filters, status: mappedStatuses });
    } else if (values.includes('pending-hod-all')) {
      // HoD "Chưa duyệt" includes PENDING_HOD and PENDING_HOD_REVISION
      const mappedStatuses = values
        .filter(v => v !== 'pending-hod-all')
        .map(v => v as SyllabusStatus)
        .concat([SyllabusStatus.PENDING_HOD, SyllabusStatus.PENDING_HOD_REVISION]);
      setFilters({ ...filters, status: mappedStatuses });
    } else if (values.includes('approved-hod')) {
      // HoD "Đã duyệt" includes PENDING_AA, PENDING_PRINCIPAL, APPROVED, PUBLISHED
      const mappedStatuses = values
        .filter(v => v !== 'approved-hod')
        .map(v => v as SyllabusStatus)
        .concat([SyllabusStatus.PENDING_AA, SyllabusStatus.PENDING_PRINCIPAL, SyllabusStatus.APPROVED, SyllabusStatus.PUBLISHED]);
      setFilters({ ...filters, status: mappedStatuses });
    } else if (values.length > 0) {
      // Normal status filter
      setFilters({ 
        ...filters, 
        status: values.map(v => v as SyllabusStatus)
      });
    } else {
      // No filter
      setFilters({ ...filters, status: undefined });
    }
    
    setPagination({ ...pagination, page: 1 });
  };

  // Filter display data for "needs-edit" special case
  const displayData = React.useMemo(() => {
    if (statusFilter.includes('needs-edit')) {
      // Only show PUBLISHED syllabi that have PENDING feedback
      return data?.data?.filter((s: Syllabus) => 
        s.status === SyllabusStatus.PUBLISHED && 
        needsEditSyllabiIds.has(s.id)
      ) || [];
    }
    return data?.data || [];
  }, [data?.data, statusFilter, needsEditSyllabiIds]);

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

  // Render lifecycle timeline
  const renderLifecycleTimeline = (syllabus: Syllabus) => {
    const events = [];

    if (syllabus.createdAt) {
      events.push({
        color: 'blue',
        label: 'Tạo đề cương',
        time: syllabus.createdAt,
        user: syllabus.ownerName,
      });
    }

    if (syllabus.submittedAt) {
      events.push({
        color: 'cyan',
        label: 'Gửi duyệt',
        time: syllabus.submittedAt,
        user: syllabus.ownerName,
      });
    }

    if (syllabus.hodApprovedAt) {
      events.push({
        color: 'green',
        label: 'Trưởng Bộ môn duyệt',
        time: syllabus.hodApprovedAt,
        user: syllabus.hodApprovedBy,
      });
    }

    if (syllabus.aaApprovedAt) {
      events.push({
        color: 'green',
        label: 'Phòng Đào tạo duyệt',
        time: syllabus.aaApprovedAt,
        user: syllabus.aaApprovedBy,
      });
    }

    if (syllabus.principalApprovedAt) {
      events.push({
        color: 'green',
        label: 'Hiệu trưởng duyệt',
        time: syllabus.principalApprovedAt,
        user: syllabus.principalApprovedBy,
      });
    }

    if (syllabus.publishedAt) {
      events.push({
        color: 'purple',
        label: 'Xuất hành',
        time: syllabus.publishedAt,
        user: 'Admin',
      });
    }

    return (
      <Timeline
        items={events.map((event) => ({
          color: event.color,
          children: (
            <Space direction="vertical" size={0}>
              <Text strong>{event.label}</Text>
              <Text type="secondary" style={{ fontSize: '12px' }}>
                {dayjs(event.time).format('DD/MM/YYYY HH:mm')} - {event.user}
              </Text>
            </Space>
          ),
        }))}
      />
    );
  };

  // Table columns
  const columns: ColumnsType<Syllabus> = [
    {
      title: 'Mã môn',
      dataIndex: 'subjectCode',
      key: 'subjectCode',
      width: 100,
      fixed: 'left',
      sorter: true,
      align: 'center',
    },
    {
      title: 'Tên môn học',
      dataIndex: 'subjectNameVi',
      key: 'subjectNameVi',
      width: 180,
      sorter: true,
      align: 'center',
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <a onClick={() => navigate(`/syllabi/${record.id}`)} style={{ color: '#018486' }}>
            {text}
          </a>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {record.subjectNameEn}
          </Text>
        </Space>
      ),
    },
    {
      title: 'Tín chỉ',
      dataIndex: 'creditCount',
      key: 'creditCount',
      width: 80,
      align: 'center',
    },
    {
      title: 'Giảng viên',
      dataIndex: 'ownerName',
      key: 'ownerName',
      width: 150,
      align: 'center',
    },
    {
      title: 'Khoa/Bộ môn',
      dataIndex: 'department',
      key: 'department',
      width: 180,
      align: 'center',
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.faculty}</Text>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {text}
          </Text>
        </Space>
      ),
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semester',
      key: 'semester',
      width: 120,
      align: 'center',
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 150,
      align: 'center',
      render: (status: SyllabusStatus, record) => {
        const statusConfig = {
          [SyllabusStatus.DRAFT]: { color: 'default', text: 'Nháp' },
          [SyllabusStatus.PENDING_HOD]: { color: 'orange', text: 'Chờ Trưởng BM' },
          [SyllabusStatus.PENDING_HOD_REVISION]: { color: 'gold', text: 'Chờ TBM (Sửa lỗi)' },
          [SyllabusStatus.PENDING_AA]: { color: 'blue', text: 'Chờ Phòng ĐT' },
          [SyllabusStatus.PENDING_PRINCIPAL]: { color: 'purple', text: 'Chờ Hiệu trưởng' },
          [SyllabusStatus.APPROVED]: { color: 'green', text: 'Đã duyệt' },
          [SyllabusStatus.PENDING_ADMIN_REPUBLISH]: { color: 'lime', text: 'Chờ xuất hành lại' },
          [SyllabusStatus.PUBLISHED]: { color: 'cyan', text: 'Đã xuất hành' },
          [SyllabusStatus.REJECTED]: { color: 'red', text: 'Từ chối' },
          [SyllabusStatus.ARCHIVED]: { color: 'default', text: 'Đã gỡ bỏ' },
        };
        const config = statusConfig[status];
        
        const hasPendingFeedback = needsEditSyllabiIds.has(record.id);
        
        return (
          <Space direction="vertical" size={2}>
            <Tag color={config.color}>{config.text}</Tag>
            {hasPendingFeedback && (
              <Tag icon={<EditOutlined />} color="warning" style={{ fontSize: '11px' }}>
                Cần chỉnh sửa
              </Tag>
            )}
          </Space>
        );
      },
    },
    {
      title: 'Cập nhật',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 120,
      sorter: true,
      align: 'center',
      render: (date: string) => dayjs(date).format('DD/MM/YYYY'),
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 150,
      fixed: 'right',
      align: 'center',
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="Xem chi tiết">
            <Button
              size="small"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/syllabi/${record.id}`)}
            />
          </Tooltip>
          
          {record.status === SyllabusStatus.APPROVED && user?.role === UserRole.ADMIN && (
            <Tooltip title="Xuất hành">
              <Button
                type="primary"
                size="small"
                icon={<RocketOutlined />}
                onClick={() => handlePublishClick(record)}
              >
                Xuất hành
              </Button>
            </Tooltip>
          )}
          
          {record.status === SyllabusStatus.PUBLISHED && user?.role === UserRole.ADMIN && (
            <>
              <Tooltip title="Lịch sử">
                <Button
                  size="small"
                  icon={<HistoryOutlined />}
                  onClick={() => handleViewHistory(record)}
                />
              </Tooltip>
              <Tooltip title="Gỡ bỏ">
                <Button
                  danger
                  size="small"
                  icon={<StopOutlined />}
                  onClick={() => handleUnpublishClick(record)}
                />
              </Tooltip>
            </>
          )}
          
          {user?.role === UserRole.AA && (
            <Tooltip title="So sánh phiên bản">
              <Button
                size="small"
                icon={<HistoryOutlined />}
                onClick={() => {
                  setSelectedSyllabus(record);
                  setCompareModalVisible(true);
                }}
              >
                So sánh
              </Button>
            </Tooltip>
          )}
          
          {record.status === SyllabusStatus.DRAFT && (
            <Tooltip title="Xóa">
              <Button
                type="text"
                danger
                size="small"
                icon={<DeleteOutlined />}
                onClick={() => handleDelete(record.id)}
              />
            </Tooltip>
          )}
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
      <div style={{ marginBottom: 24 }}>
        <Title level={2} style={{ margin: 0 }}>
          Quản lý Đề cương
        </Title>
      </div>

      <Card>
        {/* Search and Filters */}
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
          <Col xs={24} md={12} lg={8}>
            <Select
              mode="multiple"
              placeholder="Lọc theo trạng thái"
              style={{ width: '100%' }}
              value={statusFilter}
              onChange={handleStatusFilterChange}
              allowClear
            >
              {user?.role === UserRole.PRINCIPAL ? (
                <>
                  <Option value={SyllabusStatus.PENDING_PRINCIPAL}>Chưa xử lý</Option>
                  <Option value={SyllabusStatus.APPROVED}>Đã duyệt</Option>
                  <Option value={SyllabusStatus.REJECTED}>Từ chối</Option>
                </>
              ) : user?.role === UserRole.AA ? (
                <>
                  <Option value={SyllabusStatus.PENDING_AA}>Chưa duyệt</Option>
                  <Option value="approved-statuses">Đã duyệt</Option>
                  <Option value={SyllabusStatus.REJECTED}>Từ chối</Option>
                </>
              ) : user?.role === UserRole.HOD ? (
                <>
                  <Option value="pending-hod-all">Chưa duyệt</Option>
                  <Option value="approved-hod">Đã duyệt</Option>
                  <Option value={SyllabusStatus.REJECTED}>Từ chối</Option>
                </>
              ) : (
                <>
                  <Option value="not-published">Chưa xuất hành</Option>
                  <Option value={SyllabusStatus.PUBLISHED}>Đã xuất hành</Option>
                  <Option value="needs-edit">Cần chỉnh sửa</Option>
                </>
              )}
            </Select>
          </Col>
          <Col xs={24} md={12} lg={4}>
            <Button icon={<SearchOutlined />} onClick={handleSearch} block>
              Tìm kiếm
            </Button>
          </Col>
          <Col xs={24} md={12} lg={4}>
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
          dataSource={displayData}
          rowKey="id"
          loading={isLoading}
          pagination={{
            current: pagination.page,
            pageSize: pagination.pageSize,
            total: statusFilter.includes('needs-edit') ? displayData.length : data?.total,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} đề cương`,
            pageSizeOptions: ['10', '20', '50', '100'],
          }}
          onChange={handleTableChange}
          scroll={{ x: 1250 }}
        />
      </Card>

      {/* Publish Modal */}
      <Modal
        title={
          <Space>
            <RocketOutlined style={{ color: '#1890ff' }} />
            <span>Xuất hành Đề cương</span>
          </Space>
        }
        open={publishModalVisible}
        onCancel={() => {
          setPublishModalVisible(false);
          setSelectedSyllabus(null);
          publishForm.resetFields();
        }}
        footer={null}
        width={700}
      >
        {selectedSyllabus && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="Mã HP" span={1}>
                {selectedSyllabus.subjectCode}
              </Descriptions.Item>
              <Descriptions.Item label="Tín chỉ" span={1}>
                {selectedSyllabus.creditCount}
              </Descriptions.Item>
              <Descriptions.Item label="Tên học phần" span={2}>
                <Space direction="vertical" size={0}>
                  <Text strong>{selectedSyllabus.subjectNameVi}</Text>
                  <Text type="secondary">{selectedSyllabus.subjectNameEn}</Text>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="Giảng viên" span={1}>
                {selectedSyllabus.ownerName}
              </Descriptions.Item>
              <Descriptions.Item label="Khoa/Bộ môn" span={1}>
                {selectedSyllabus.department}
              </Descriptions.Item>
              <Descriptions.Item label="Học kỳ" span={1}>
                {selectedSyllabus.semester}
              </Descriptions.Item>
              <Descriptions.Item label="Năm học" span={1}>
                {selectedSyllabus.academicYear}
              </Descriptions.Item>
            </Descriptions>

            <Form form={publishForm} layout="vertical" onFinish={handlePublish}>
              <Form.Item
                label="Ngày hiệu lực"
                name="effectiveDate"
                rules={[{ required: true, message: 'Chọn ngày hiệu lực' }]}
                extra="Đề cương sẽ có hiệu lực từ ngày này"
              >
                <DatePicker
                  format="DD/MM/YYYY"
                  style={{ width: '100%' }}
                  placeholder="Chọn ngày"
                />
              </Form.Item>

              <Form.Item
                label="Ghi chú (tùy chọn)"
                name="note"
              >
                <TextArea
                  rows={3}
                  placeholder="Ghi chú về lần xuất hành này..."
                />
              </Form.Item>

              <Form.Item style={{ marginBottom: 0 }}>
                <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                  <Button onClick={() => setPublishModalVisible(false)}>
                    Hủy
                  </Button>
                  <Button
                    type="primary"
                    htmlType="submit"
                    icon={<RocketOutlined />}
                    loading={publishMutation.isPending}
                  >
                    Xuất hành
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Space>
        )}
      </Modal>

      {/* Unpublish Modal */}
      <Modal
        title={
          <Space>
            <StopOutlined style={{ color: '#ff4d4f' }} />
            <span>Gỡ bỏ Đề cương</span>
          </Space>
        }
        open={unpublishModalVisible}
        onCancel={() => {
          setUnpublishModalVisible(false);
          setSelectedSyllabus(null);
          unpublishForm.resetFields();
        }}
        footer={null}
        width={600}
      >
        {selectedSyllabus && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="Mã HP">
                {selectedSyllabus.subjectCode}
              </Descriptions.Item>
              <Descriptions.Item label="Tên học phần">
                {selectedSyllabus.subjectNameVi}
              </Descriptions.Item>
              <Descriptions.Item label="Giảng viên">
                {selectedSyllabus.ownerName}
              </Descriptions.Item>
            </Descriptions>

            <Form form={unpublishForm} layout="vertical" onFinish={handleUnpublish}>
              <Form.Item
                label="Lý do gỡ bỏ"
                name="reason"
                rules={[{ required: true, message: 'Nhập lý do gỡ bỏ' }]}
              >
                <TextArea
                  rows={4}
                  placeholder="Nhập lý do gỡ bỏ đề cương (bắt buộc)..."
                />
              </Form.Item>

              <Form.Item style={{ marginBottom: 0 }}>
                <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                  <Button onClick={() => setUnpublishModalVisible(false)}>
                    Hủy
                  </Button>
                  <Button
                    type="primary"
                    danger
                    htmlType="submit"
                    icon={<StopOutlined />}
                    loading={unpublishMutation.isPending}
                  >
                    Gỡ bỏ
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Space>
        )}
      </Modal>

      {/* History Modal */}
      <Modal
        title={
          <Space>
            <HistoryOutlined />
            <span>Lịch sử Vòng đời Đề cương</span>
          </Space>
        }
        open={historyModalVisible}
        onCancel={() => {
          setHistoryModalVisible(false);
          setSelectedSyllabus(null);
        }}
        footer={[
          <Button key="close" onClick={() => setHistoryModalVisible(false)}>
            Đóng
          </Button>,
        ]}
        width={700}
      >
        {selectedSyllabus && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="Mã HP" span={1}>
                {selectedSyllabus.subjectCode}
              </Descriptions.Item>
              <Descriptions.Item label="Phiên bản" span={1}>
                <Tag color="blue">v{selectedSyllabus.version}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Tên học phần" span={2}>
                {selectedSyllabus.subjectNameVi}
              </Descriptions.Item>
            </Descriptions>

            <Card title="Vòng đời phê duyệt" size="small">
              {renderLifecycleTimeline(selectedSyllabus)}
            </Card>
          </Space>
        )}
      </Modal>

      {/* Version Compare Modal (AA only) */}
      <Modal
        title={
          <Space>
            <HistoryOutlined />
            <span>So sánh Phiên bản Đề cương</span>
          </Space>
        }
        open={compareModalVisible}
        onCancel={() => {
          setCompareModalVisible(false);
          setSelectedSyllabus(null);
        }}
        footer={[
          <Button key="close" onClick={() => setCompareModalVisible(false)}>
            Đóng
          </Button>,
        ]}
        width={900}
      >
        {selectedSyllabus && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="Mã HP" span={1}>
                {selectedSyllabus.subjectCode}
              </Descriptions.Item>
              <Descriptions.Item label="Phiên bản hiện tại" span={1}>
                <Tag color="blue">v{selectedSyllabus.version}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Tên học phần" span={2}>
                {selectedSyllabus.subjectNameVi}
              </Descriptions.Item>
            </Descriptions>

            <Card title="Lịch sử phiên bản" size="small">
              <Timeline
                items={[
                  {
                    color: 'green',
                    children: (
                      <Space direction="vertical">
                        <Text strong>Phiên bản {selectedSyllabus.version} (Hiện tại)</Text>
                        <Text type="secondary">
                          Cập nhật: {dayjs(selectedSyllabus.updatedAt).format('DD/MM/YYYY HH:mm')}
                        </Text>
                        <Text type="secondary">Người tạo: {selectedSyllabus.ownerName}</Text>
                      </Space>
                    ),
                  },
                  {
                    color: 'blue',
                    children: (
                      <Space direction="vertical">
                        <Text>Phiên bản {selectedSyllabus.version - 1}</Text>
                        <Text type="secondary">
                          Cập nhật: {dayjs(selectedSyllabus.createdAt).format('DD/MM/YYYY HH:mm')}
                        </Text>
                        <Button size="small" type="link">
                          Xem chi tiết khác biệt
                        </Button>
                      </Space>
                    ),
                  },
                ]}
              />
            </Card>

            <Card title="So sánh nội dung" size="small">
              <Space direction="vertical" style={{ width: '100%' }}>
                <Text strong>Thay đổi chính:</Text>
                <ul style={{ marginLeft: 16 }}>
                  <li>
                    <Text>Cập nhật mục tiêu học tập (CLO 1, CLO 2)</Text>
                  </li>
                  <li>
                    <Text>Điều chỉnh phương pháp đánh giá</Text>
                  </li>
                  <li>
                    <Text>Bổ sung tài liệu tham khảo</Text>
                  </li>
                </ul>
                <Button type="primary" icon={<EyeOutlined />}>
                  Xem so sánh chi tiết
                </Button>
              </Space>
            </Card>
          </Space>
        )}
      </Modal>
    </div>
  );
};
