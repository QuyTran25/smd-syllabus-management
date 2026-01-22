import React, { useState } from 'react';

import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  message,
  Typography,
  Descriptions,
  Select,
  Row,
  Col,
  Popconfirm,
  Tooltip,
} from 'antd';

import {
  FileTextOutlined,
  StopOutlined,
  HistoryOutlined,
  EyeOutlined,
  SearchOutlined,
  DownloadOutlined,
  ReloadOutlined,
  DeleteOutlined,
} from '@ant-design/icons';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

import { useNavigate } from 'react-router-dom';

import { syllabusService, revisionService, syllabusAuditService } from '@/services';

import facultyService from '@/services/faculty.service';

import { SyllabusStatus } from '@/types';

import type { ColumnsType } from 'antd/es/table';

import dayjs from 'dayjs';

const { Title, Text } = Typography;

const { TextArea } = Input;

const { Option } = Select;

// Định nghĩa lại Status List để hiển thị trên Admin Dashboard

const ADMIN_SYLLABUS_STATUSES = [
  SyllabusStatus.APPROVED,

  SyllabusStatus.PUBLISHED,

  SyllabusStatus.REJECTED,

  SyllabusStatus.REVISION_IN_PROGRESS,

  SyllabusStatus.PENDING_ADMIN_REPUBLISH,

  SyllabusStatus.INACTIVE,

  SyllabusStatus.ARCHIVED,

  SyllabusStatus.DRAFT,
];

const STATUS_LABELS: Record<string, string> = {
  PUBLISHED: 'Đã xuất bản',

  APPROVED: 'Đã phê duyệt',

  DRAFT: 'Bản nháp',

  PENDING: 'Chờ duyệt',

  REJECTED: 'Bị từ chối',

  INACTIVE: 'Ngưng hoạt động',

  ARCHIVED: 'Lưu trữ',
};

const STATUS_COLORS: Record<string, string> = {
  PUBLISHED: 'green',

  APPROVED: 'cyan',

  DRAFT: 'default',

  PENDING: 'gold',

  REJECTED: 'error',

  INACTIVE: 'default',

  ARCHIVED: 'purple',
};

export const PublishedSyllabiPage: React.FC = () => {
  const queryClient = useQueryClient();

  const navigate = useNavigate();

  const [form] = Form.useForm();

  // --- States ---

  const [unpublishModalVisible, setUnpublishModalVisible] = useState(false);

  const [historyModalVisible, setHistoryModalVisible] = useState(false);

  const [selectedSyllabus, setSelectedSyllabus] = useState<any | null>(null);

  // Filter States

  const [searchText, setSearchText] = useState('');

  const [statusFilter, setStatusFilter] = useState<string[]>([]);

  const [selectedFaculty, setSelectedFaculty] = useState<string | undefined>(undefined);

  const [selectedDepartment, setSelectedDepartment] = useState<string | undefined>(undefined);

  // --- Queries ---

  // 1. Fetch Khoa (Faculties)

  const { data: faculties } = useQuery({
    queryKey: ['faculties'],

    queryFn: () => facultyService.getAllFaculties(),
  });

  // 2. Fetch Bộ môn (Departments)

  const { data: departments } = useQuery({
    queryKey: ['departments', selectedFaculty],

    queryFn: () => facultyService.getDepartmentsByFaculty(selectedFaculty!),

    enabled: !!selectedFaculty,
  });

  // 3. Fetch Syllabi

  const {
    data: syllabiResponse,

    isLoading,

    isFetching,
  } = useQuery({
    queryKey: ['syllabi', 'admin', statusFilter, searchText, selectedFaculty, selectedDepartment],

    queryFn: () => {
      const filters: any = {
        search: searchText,
      };

      if (statusFilter.length > 0) filters.status = statusFilter;

      if (selectedFaculty) filters.faculty = [selectedFaculty];

      if (selectedDepartment) filters.department = [selectedDepartment];

      return syllabusService.getSyllabi(filters, { page: 1, pageSize: 50 });
    },
  });

  const syllabiList = syllabiResponse?.data || [];

  const totalRecords = syllabiResponse?.total || 0;

  // --- Mutations ---

  // 1. Unpublish

  const unpublishMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      syllabusAuditService.unpublishSyllabusWithLog(id, reason),

    onSuccess: () => {
      message.success('Gỡ bỏ đề cương thành công');

      queryClient.invalidateQueries({ queryKey: ['syllabi'] });

      setUnpublishModalVisible(false);

      setSelectedSyllabus(null);

      form.resetFields();
    },

    onError: (error: any) => {
      message.error(error?.response?.data?.message || 'Gỡ bỏ thất bại');
    },
  });

  // 2. Delete

  const deleteMutation = useMutation({
    mutationFn: (id: string) => syllabusAuditService.deleteSyllabusWithLog(id, selectedSyllabus),

    onSuccess: () => {
      message.success('Xóa đề cương thành công');

      queryClient.invalidateQueries({ queryKey: ['syllabi'] });
    },

    onError: (error: any) => {
      message.error(error?.response?.data?.message || 'Xóa thất bại');
    },
  });

  // 3. Export CSV

  const exportMutation = useMutation({
    mutationFn: async () => {
      const filters: any = { search: searchText };

      if (statusFilter.length > 0) filters.status = statusFilter;

      if (selectedFaculty) filters.faculty = [selectedFaculty];

      if (selectedDepartment) filters.department = [selectedDepartment];

      return await syllabusService.exportToCSV(filters);
    },

    onSuccess: (data: Blob) => {
      // Ghi log thành công

      syllabusAuditService.logSystemAction(
        'EXPORT',

        `Xuất file CSV danh sách đề cương (${syllabiList.length} records)`,

        'SUCCESS'
      );

      const url = window.URL.createObjectURL(new Blob([data]));

      const link = document.createElement('a');

      link.href = url;

      link.setAttribute('download', `Syllabus_Export_${dayjs().format('DDMMYYYY')}.csv`);

      document.body.appendChild(link);

      link.click();

      link.parentNode?.removeChild(link);

      message.success('Xuất file thành công');
    },

    onError: (error: any) => {
      syllabusAuditService.logSystemAction(
        'EXPORT',

        `Lỗi xuất file CSV: ${error?.message}`,

        'FAILED'
      );

      message.error('Xuất file thất bại');
    },
  });

  // 4. Republish

  const republishMutation = useMutation({
    mutationFn: async (syllabusId: string) => {
      const completedSession = await revisionService.getCompletedRevisionSession(syllabusId);

      if (!completedSession) {
        throw new Error('Không tìm thấy revision session đã hoàn thành');
      }

      return revisionService.republishSyllabus(completedSession.id);
    },

    onSuccess: () => {
      message.success('Đã xuất bản lại đề cương thành công!');

      queryClient.invalidateQueries({ queryKey: ['syllabi'] });
    },

    onError: (error: any) => {
      message.error(error.message || 'Xuất bản lại thất bại');
    },
  });

  // --- Handlers ---

  const handleUnpublishClick = (syllabus: any) => {
    setSelectedSyllabus(syllabus);

    setUnpublishModalVisible(true);
  };

  const handleUnpublish = (values: any) => {
    if (!selectedSyllabus) return;

    unpublishMutation.mutate({
      id: selectedSyllabus.id,

      reason: values.reason,
    });
  };

  const handleViewHistory = (syllabus: any) => {
    setSelectedSyllabus(syllabus);

    setHistoryModalVisible(true);
  };

  const handleDeleteClick = (syllabus: any) => {
    setSelectedSyllabus(syllabus);
  };

  // --- Columns ---

  const columns: ColumnsType<any> = [
    {
      title: 'Mã HP',

      key: 'code',

      width: 100,

      fixed: 'left',

      render: (_, record) => <Text strong>{record.subjectCode || record.code}</Text>,
    },

    {
      title: 'Tên học phần',

      key: 'name',

      width: 250,

      ellipsis: true,

      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.subjectNameVi || record.nameVi}</Text>

          <Text type="secondary" style={{ fontSize: '11px' }}>
            {record.subjectNameEn || record.nameEn}
          </Text>
        </Space>
      ),
    },

    {
      title: 'TC',

      dataIndex: 'creditCount',

      key: 'credits',

      width: 60,

      align: 'center',

      render: (val, r) => val || r.credits,
    },

    {
      title: 'Giảng viên',

      dataIndex: 'ownerName',

      key: 'ownerName',

      width: 150,

      ellipsis: true,

      render: (val, r) => val || r.lecturerName,
    },

    {
      title: 'Khoa/Bộ môn',

      key: 'org',

      width: 180,

      ellipsis: true,

      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.faculty}</Text>

          <Text type="secondary" style={{ fontSize: '11px' }}>
            {record.department}
          </Text>
        </Space>
      ),
    },

    {
      title: 'HK',

      dataIndex: 'semester',

      key: 'semester',

      width: 100,

      render: (val, r) => val || r.term,
    },

    {
      title: 'Trạng thái',

      dataIndex: 'status',

      key: 'status',

      width: 140,

      align: 'center',

      render: (status: string) => (
        <Tag color={STATUS_COLORS[status] || 'default'}>{STATUS_LABELS[status] || status}</Tag>
      ),
    },

    {
      title: 'Hành động',

      key: 'actions',

      width: 180,

      fixed: 'right',

      render: (_, record) => (
        <Space size="small">
          <Tooltip title="Xem chi tiết">
            <Button
              size="small"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/syllabi/${record.id}`)}
            />
          </Tooltip>

          <Tooltip title="Lịch sử">
            <Button
              size="small"
              icon={<HistoryOutlined />}
              onClick={() => handleViewHistory(record)}
            />
          </Tooltip>

          {record.status === 'PENDING_ADMIN_REPUBLISH' && (
            <Tooltip title="Xuất bản lại">
              <Popconfirm
                title="Xuất bản lại đề cương"
                description="Đề cương đã được chỉnh sửa và duyệt. Xác nhận xuất bản lại?"
                onConfirm={() => republishMutation.mutate(record.id)}
                okText="Xuất bản"
                cancelText="Hủy"
              >
                <Button
                  size="small"
                  type="primary"
                  icon={<ReloadOutlined />}
                  loading={republishMutation.isPending}
                >
                  Xuất bản lại
                </Button>
              </Popconfirm>
            </Tooltip>
          )}

          {record.status === 'PUBLISHED' && (
            <Tooltip title="Gỡ bỏ (Unpublish)">
              <Button
                size="small"
                danger
                icon={<StopOutlined />}
                onClick={() => handleUnpublishClick(record)}
              />
            </Tooltip>
          )}

          {record.status !== 'PUBLISHED' && (
            <Tooltip title="Xóa vĩnh viễn">
              <Popconfirm
                title="Xóa đề cương này?"
                description="Hành động này không thể hoàn tác!"
                onConfirm={() => {
                  handleDeleteClick(record);

                  deleteMutation.mutate(record.id);
                }}
                okText="Xóa"
                cancelText="Hủy"
                okButtonProps={{ danger: true }}
              >
                <Button size="small" danger icon={<DeleteOutlined />} />
              </Popconfirm>
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        {/* Header */}

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={4} style={{ margin: 0 }}>
            <FileTextOutlined /> Quản lý Đề cương
          </Title>

          <Space>
            <Button
              icon={<ReloadOutlined />}
              onClick={() => queryClient.invalidateQueries({ queryKey: ['syllabi'] })}
              loading={isFetching}
            >
              Làm mới
            </Button>

            <Button
              type="primary"
              icon={<DownloadOutlined />}
              onClick={() => exportMutation.mutate()}
              loading={exportMutation.isPending}
            >
              Export Excel
            </Button>
          </Space>
        </div>

        {/* Filters */}

        <Card bodyStyle={{ padding: '16px' }}>
          <Row gutter={[16, 16]}>
            <Col xs={24} md={6}>
              <Input
                placeholder="Tìm Mã HP, Tên HP..."
                prefix={<SearchOutlined />}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                allowClear
              />
            </Col>

            <Col xs={24} md={5}>
              <Select
                style={{ width: '100%' }}
                placeholder="Lọc theo Khoa"
                allowClear
                showSearch
                optionFilterProp="children"
                onChange={(val) => {
                  setSelectedFaculty(val);

                  setSelectedDepartment(undefined);
                }}
                filterOption={(input, option) =>
                  (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
                options={faculties?.map((f) => ({ label: f.name, value: f.id }))}
              />
            </Col>

            <Col xs={24} md={5}>
              <Select
                style={{ width: '100%' }}
                placeholder={selectedFaculty ? 'Lọc theo Bộ môn' : 'Chọn Khoa trước'}
                allowClear
                disabled={!selectedFaculty}
                showSearch
                optionFilterProp="children"
                onChange={setSelectedDepartment}
                options={departments?.map((d) => ({ label: d.name, value: d.id }))}
              />
            </Col>

            <Col xs={24} md={8}>
              <Select
                mode="multiple"
                placeholder="Trạng thái"
                style={{ width: '100%' }}
                value={statusFilter}
                onChange={setStatusFilter}
                allowClear
                maxTagCount="responsive"
              >
                {Object.keys(STATUS_LABELS).map((status) => (
                  <Option key={status} value={status}>
                    <Tag color={STATUS_COLORS[status]} style={{ marginRight: 0 }}>
                      {STATUS_LABELS[status]}
                    </Tag>
                  </Option>
                ))}
              </Select>
            </Col>
          </Row>
        </Card>

        {/* Table */}

        <Card bodyStyle={{ padding: 0 }}>
          <Table
            columns={columns}
            dataSource={syllabiList}
            rowKey="id"
            loading={isLoading}
            pagination={{
              current: syllabiResponse?.page || 1,

              pageSize: syllabiResponse?.pageSize || 10,

              total: totalRecords,

              showSizeChanger: true,

              showTotal: (total) => `Tổng ${total} đề cương`,
            }}
            scroll={{ x: 1300 }}
          />
        </Card>
      </Space>

      {/* Unpublish Modal */}

      <Modal
        title={
          <Space>
            <StopOutlined style={{ color: '#ff4d4f' }} />

            <span>Gỡ bỏ Đề cương (Unpublish)</span>
          </Space>
        }
        open={unpublishModalVisible}
        onCancel={() => {
          setUnpublishModalVisible(false);

          setSelectedSyllabus(null);

          form.resetFields();
        }}
        footer={null}
        width={600}
      >
        {selectedSyllabus && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="Mã HP">
                {selectedSyllabus.subjectCode || selectedSyllabus.code}
              </Descriptions.Item>

              <Descriptions.Item label="Tên học phần">
                {selectedSyllabus.subjectNameVi || selectedSyllabus.nameVi}
              </Descriptions.Item>

              <Descriptions.Item label="Giảng viên">
                {selectedSyllabus.ownerName || selectedSyllabus.lecturerName}
              </Descriptions.Item>
            </Descriptions>

            <Form form={form} layout="vertical" onFinish={handleUnpublish}>
              <Form.Item
                label="Lý do gỡ bỏ"
                name="reason"
                rules={[{ required: true, message: 'Nhập lý do gỡ bỏ' }]}
              >
                <TextArea rows={4} placeholder="Nhập lý do gỡ bỏ đề cương (bắt buộc)..." />
              </Form.Item>

              <Form.Item style={{ marginBottom: 0 }}>
                <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                  <Button onClick={() => setUnpublishModalVisible(false)}>Hủy</Button>

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

      {/* --- History Modal (Giữ nguyên hoặc dùng lại logic timeline) --- */}
      <Modal
        title={
          <Space>
            <HistoryOutlined />
            <span>Lịch sử hoạt động</span>
          </Space>
        }
        open={historyModalVisible}
        onCancel={() => setHistoryModalVisible(false)}
        footer={null}
      >
        <p>Tính năng xem lịch sử chi tiết đang được phát triển...</p>
        {/* Bạn có thể copy lại component Timeline từ file cũ vào đây */}
      </Modal>
    </div>
  );
};
