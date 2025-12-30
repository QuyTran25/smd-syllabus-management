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
  Badge,
  Select,
} from 'antd';
import {
  MessageOutlined,
  CheckCircleOutlined,
  EditOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { feedbackService } from '@/services';
import {
  StudentFeedback,
  FeedbackStatus,
  FeedbackType,
} from '@/types';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { TextArea } = Input;
const { Option } = Select;

const FEEDBACK_TYPE_LABELS: Record<FeedbackType, string> = {
  [FeedbackType.ERROR]: 'Lỗi sai',
  [FeedbackType.SUGGESTION]: 'Đề xuất',
  [FeedbackType.QUESTION]: 'Câu hỏi',
  [FeedbackType.OTHER]: 'Khác',
};

const FEEDBACK_TYPE_COLORS: Record<FeedbackType, string> = {
  [FeedbackType.ERROR]: 'red',
  [FeedbackType.SUGGESTION]: 'blue',
  [FeedbackType.QUESTION]: 'orange',
  [FeedbackType.OTHER]: 'default',
};

const FEEDBACK_STATUS_LABELS: Record<FeedbackStatus, string> = {
  [FeedbackStatus.PENDING]: 'Chờ xử lý',
  [FeedbackStatus.IN_REVIEW]: 'Đang xem xét',
  [FeedbackStatus.RESOLVED]: 'Đã giải quyết',
  [FeedbackStatus.REJECTED]: 'Từ chối',
};

const FEEDBACK_STATUS_COLORS: Record<FeedbackStatus, string> = {
  [FeedbackStatus.PENDING]: 'gold',
  [FeedbackStatus.IN_REVIEW]: 'processing',
  [FeedbackStatus.RESOLVED]: 'success',
  [FeedbackStatus.REJECTED]: 'error',
};

export const StudentFeedbackPage: React.FC = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [responseModalVisible, setResponseModalVisible] = useState(false);
  const [selectedFeedback, setSelectedFeedback] = useState<StudentFeedback | null>(null);
  const [form] = Form.useForm();
  const [statusFilter, setStatusFilter] = useState<FeedbackStatus[]>([]);
  const [typeFilter, setTypeFilter] = useState<FeedbackType[]>([]);

  // Fetch feedbacks
  const { data: feedbacks, isLoading } = useQuery({
    queryKey: ['feedbacks', statusFilter, typeFilter],
    queryFn: () =>
      feedbackService.getFeedbacks({
        status: statusFilter.length > 0 ? statusFilter : undefined,
        type: typeFilter.length > 0 ? typeFilter : undefined,
      }),
  });

  // Respond mutation
  const respondMutation = useMutation({
    mutationFn: ({ id, response }: { id: string; response: string }) =>
      feedbackService.respondToFeedback(id, response, 'Admin User'),
    onSuccess: () => {
      message.success('Đã gửi phản hồi');
      queryClient.invalidateQueries({ queryKey: ['feedbacks'] });
      setResponseModalVisible(false);
      setSelectedFeedback(null);
      form.resetFields();
    },
    onError: () => {
      message.error('Gửi phản hồi thất bại');
    },
  });

  // Enable edit mutation
  const enableEditMutation = useMutation({
    mutationFn: (id: string) =>
      feedbackService.enableEditForLecturer(id, 'Admin User'),
    onSuccess: () => {
      message.success('Đã bật quyền chỉnh sửa cho giảng viên');
      queryClient.invalidateQueries({ queryKey: ['feedbacks'] });
    },
    onError: () => {
      message.error('Bật quyền chỉnh sửa thất bại');
    },
  });

  // Update status mutation
  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: FeedbackStatus }) =>
      feedbackService.updateFeedbackStatus(id, status),
    onSuccess: () => {
      message.success('Đã cập nhật trạng thái');
      queryClient.invalidateQueries({ queryKey: ['feedbacks'] });
    },
    onError: () => {
      message.error('Cập nhật trạng thái thất bại');
    },
  });

  const handleViewDetail = (feedback: StudentFeedback) => {
    setSelectedFeedback(feedback);
    setDetailModalVisible(true);
  };

  const handleRespond = (feedback: StudentFeedback) => {
    setSelectedFeedback(feedback);
    setResponseModalVisible(true);
  };

  const handleEnableEdit = (feedback: StudentFeedback) => {
    Modal.confirm({
      title: 'Bật quyền chỉnh sửa',
      content: (
        <Space direction="vertical">
          <Text>Bạn có chắc muốn bật quyền chỉnh sửa cho giảng viên?</Text>
          <Text strong>
            {feedback.syllabusCode} - {feedback.syllabusName}
          </Text>
          <Text type="secondary">
            Giảng viên sẽ nhận thông báo và có thể chỉnh sửa đề cương đã xuất hành.
          </Text>
        </Space>
      ),
      okText: 'Bật quyền',
      cancelText: 'Hủy',
      onOk: () => {
        enableEditMutation.mutate(feedback.id);
      },
    });
  };

  const handleSubmitResponse = (values: any) => {
    if (!selectedFeedback) return;

    respondMutation.mutate({
      id: selectedFeedback.id,
      response: values.response,
    });
  };

  const columns: ColumnsType<StudentFeedback> = [
    {
      title: 'Loại',
      dataIndex: 'type',
      key: 'type',
      width: 80,
      align: 'center',
      filters: Object.values(FeedbackType).map((type) => ({
        text: FEEDBACK_TYPE_LABELS[type],
        value: type,
      })),
      onFilter: (value, record) => record.type === value,
      render: (type: FeedbackType) => (
        <Tag color={FEEDBACK_TYPE_COLORS[type]}>
          {FEEDBACK_TYPE_LABELS[type]}
        </Tag>
      ),
    },
    {
      title: 'Mã HP',
      dataIndex: 'syllabusCode',
      key: 'syllabusCode',
      width: 75,
      align: 'center',
    },
    {
      title: 'Tên học phần',
      dataIndex: 'syllabusName',
      key: 'syllabusName',
      width: 220,
      align: 'center',
      ellipsis: { showTitle: false },
    },
    {
      title: 'Phần',
      dataIndex: 'section',
      key: 'section',
      width: 150,
      render: (section) => <Tag>{section}</Tag>,
    },
    {
      title: 'Tiêu đề',
      dataIndex: 'title',
      key: 'title',
      width: 200,
      ellipsis: { showTitle: false },
    },
    {
      title: 'Sinh viên',
      dataIndex: 'studentName',
      key: 'studentName',
      width: 180,
      align: 'center',
      ellipsis: { showTitle: false },
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 105,
      align: 'center',
      filters: Object.values(FeedbackStatus).map((status) => ({
        text: FEEDBACK_STATUS_LABELS[status],
        value: status,
      })),
      onFilter: (value, record) => record.status === value,
      render: (status: FeedbackStatus) => (
        <Tag color={FEEDBACK_STATUS_COLORS[status]}>
          {FEEDBACK_STATUS_LABELS[status]}
        </Tag>
      ),
    },
    {
      title: 'Quyền sửa',
      dataIndex: 'editEnabled',
      key: 'editEnabled',
      width: 120,
      align: 'center',
      render: (enabled: boolean) =>
        enabled ? (
          <Tag color="green" icon={<CheckCircleOutlined />}>
            Bật
          </Tag>
        ) : (
          <Tag color="default">Tắt</Tag>
        ),
    },
    {
      title: 'Ngày',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 100,
      align: 'center',
      sorter: (a, b) =>
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
      defaultSortOrder: 'descend',
      render: (date) => dayjs(date).format('DD/MM'),
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 250,
      render: (_, record) => (
        <Space>
          <Button
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            Xem
          </Button>
          <Button
            size="small"
            icon={<MessageOutlined />}
            onClick={() => handleRespond(record)}
            disabled={record.status === FeedbackStatus.RESOLVED}
          >
            Phản hồi
          </Button>
          {!record.editEnabled && (
            <Button
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEnableEdit(record)}
              disabled={record.type !== FeedbackType.ERROR}
            >
              Bật sửa
            </Button>
          )}
          {record.status === FeedbackStatus.IN_REVIEW && (
            <Button
              size="small"
              type="primary"
              icon={<CheckCircleOutlined />}
              onClick={() =>
                updateStatusMutation.mutate({
                  id: record.id,
                  status: FeedbackStatus.RESOLVED,
                })
              }
            >
              Giải quyết
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const pendingCount = feedbacks?.filter(
    (f) => f.status === FeedbackStatus.PENDING
  ).length || 0;

  return (
    <div style={{ padding: '24px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card>
          <Space style={{ width: '100%', justifyContent: 'space-between' }}>
            <Title level={4} style={{ margin: 0 }}>
              <MessageOutlined /> Quản lý Phản hồi từ Sinh viên
              {pendingCount > 0 && (
                <Badge
                  count={pendingCount}
                  style={{ marginLeft: 16 }}
                  showZero
                />
              )}
            </Title>
            <Space>
              <Select
                mode="multiple"
                placeholder="Lọc theo loại"
                style={{ minWidth: 200 }}
                value={typeFilter}
                onChange={setTypeFilter}
                allowClear
              >
                {Object.values(FeedbackType).map((type) => (
                  <Option key={type} value={type}>
                    {FEEDBACK_TYPE_LABELS[type]}
                  </Option>
                ))}
              </Select>
              <Select
                mode="multiple"
                placeholder="Lọc theo trạng thái"
                style={{ minWidth: 200 }}
                value={statusFilter}
                onChange={setStatusFilter}
                allowClear
              >
                {Object.values(FeedbackStatus).map((status) => (
                  <Option key={status} value={status}>
                    {FEEDBACK_STATUS_LABELS[status]}
                  </Option>
                ))}
              </Select>
            </Space>
          </Space>
        </Card>

        <Card>
          <Table
            columns={columns}
            dataSource={feedbacks}
            rowKey="id"
            loading={isLoading}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} phản hồi`,
            }}
            scroll={{ x: 1480 }}
          />
        </Card>
      </Space>

      {/* Detail Modal */}
      <Modal
        title={
          <Space>
            <EyeOutlined />
            <span>Chi tiết Phản hồi</span>
          </Space>
        }
        open={detailModalVisible}
        onCancel={() => {
          setDetailModalVisible(false);
          setSelectedFeedback(null);
        }}
        footer={[
          <Button
            key="close"
            onClick={() => setDetailModalVisible(false)}
          >
            Đóng
          </Button>,
        ]}
        width={700}
      >
        {selectedFeedback && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="Loại" span={1}>
                <Tag color={FEEDBACK_TYPE_COLORS[selectedFeedback.type]}>
                  {FEEDBACK_TYPE_LABELS[selectedFeedback.type]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Trạng thái" span={1}>
                <Tag color={FEEDBACK_STATUS_COLORS[selectedFeedback.status]}>
                  {FEEDBACK_STATUS_LABELS[selectedFeedback.status]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Mã HP" span={1}>
                {selectedFeedback.syllabusCode}
              </Descriptions.Item>
              <Descriptions.Item label="Phần" span={1}>
                <Tag>{selectedFeedback.section}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Tên học phần" span={2}>
                {selectedFeedback.syllabusName}
              </Descriptions.Item>
              <Descriptions.Item label="Sinh viên" span={2}>
                {selectedFeedback.studentName} ({selectedFeedback.studentEmail})
              </Descriptions.Item>
              <Descriptions.Item label="Tiêu đề" span={2}>
                {selectedFeedback.title}
              </Descriptions.Item>
              <Descriptions.Item label="Mô tả" span={2}>
                {selectedFeedback.description}
              </Descriptions.Item>
              {selectedFeedback.adminResponse && (
                <>
                  <Descriptions.Item label="Phản hồi" span={2}>
                    {selectedFeedback.adminResponse}
                  </Descriptions.Item>
                  <Descriptions.Item label="Người phản hồi" span={1}>
                    {selectedFeedback.respondedBy}
                  </Descriptions.Item>
                  <Descriptions.Item label="Ngày phản hồi" span={1}>
                    {selectedFeedback.respondedAt
                      ? dayjs(selectedFeedback.respondedAt).format(
                          'DD/MM/YYYY HH:mm'
                        )
                      : '-'}
                  </Descriptions.Item>
                </>
              )}
              {selectedFeedback.editEnabled && (
                <>
                  <Descriptions.Item label="Người bật sửa" span={1}>
                    {selectedFeedback.editEnabledBy}
                  </Descriptions.Item>
                  <Descriptions.Item label="Ngày bật sửa" span={1}>
                    {selectedFeedback.editEnabledAt
                      ? dayjs(selectedFeedback.editEnabledAt).format(
                          'DD/MM/YYYY HH:mm'
                        )
                      : '-'}
                  </Descriptions.Item>
                </>
              )}
              <Descriptions.Item label="Ngày tạo" span={1}>
                {dayjs(selectedFeedback.createdAt).format('DD/MM/YYYY HH:mm')}
              </Descriptions.Item>
              <Descriptions.Item label="Ngày cập nhật" span={1}>
                {dayjs(selectedFeedback.updatedAt).format('DD/MM/YYYY HH:mm')}
              </Descriptions.Item>
            </Descriptions>

            <Space>
              <Button
                icon={<EyeOutlined />}
                onClick={() =>
                  navigate(`/syllabi/${selectedFeedback.syllabusId}`)
                }
              >
                Xem đề cương
              </Button>
            </Space>
          </Space>
        )}
      </Modal>

      {/* Response Modal */}
      <Modal
        title={
          <Space>
            <MessageOutlined />
            <span>Phản hồi Sinh viên</span>
          </Space>
        }
        open={responseModalVisible}
        onCancel={() => {
          setResponseModalVisible(false);
          setSelectedFeedback(null);
          form.resetFields();
        }}
        footer={null}
        width={600}
      >
        {selectedFeedback && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="Tiêu đề">
                {selectedFeedback.title}
              </Descriptions.Item>
              <Descriptions.Item label="Mô tả">
                {selectedFeedback.description}
              </Descriptions.Item>
            </Descriptions>

            <Form form={form} layout="vertical" onFinish={handleSubmitResponse}>
              <Form.Item
                label="Phản hồi"
                name="response"
                rules={[{ required: true, message: 'Nhập nội dung phản hồi' }]}
              >
                <TextArea
                  rows={4}
                  placeholder="Nhập nội dung phản hồi cho sinh viên..."
                />
              </Form.Item>

              <Form.Item style={{ marginBottom: 0 }}>
                <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                  <Button
                    onClick={() => setResponseModalVisible(false)}
                  >
                    Hủy
                  </Button>
                  <Button
                    type="primary"
                    htmlType="submit"
                    icon={<MessageOutlined />}
                    loading={respondMutation.isPending}
                  >
                    Gửi phản hồi
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Space>
        )}
      </Modal>
    </div>
  );
};
