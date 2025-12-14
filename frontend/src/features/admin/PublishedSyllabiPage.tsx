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
  Timeline,
  Badge,
  Tabs,
} from 'antd';
import {
  FileTextOutlined,
  StopOutlined,
  HistoryOutlined,
  ExclamationCircleOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { syllabusService } from '@/services';
import { Syllabus, SyllabusStatus } from '@/types';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { TextArea } = Input;

export const PublishedSyllabiPage: React.FC = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [unpublishModalVisible, setUnpublishModalVisible] = useState(false);
  const [historyModalVisible, setHistoryModalVisible] = useState(false);
  const [selectedSyllabus, setSelectedSyllabus] = useState<Syllabus | null>(null);
  const [form] = Form.useForm();

  // Fetch published and archived syllabi
  const { data: publishedSyllabi, isLoading: loadingPublished } = useQuery({
    queryKey: ['syllabi', SyllabusStatus.PUBLISHED],
    queryFn: () =>
      syllabusService.getSyllabi({ status: [SyllabusStatus.PUBLISHED] }),
    select: (response) => response.data,
  });

  const { data: archivedSyllabi, isLoading: loadingArchived } = useQuery({
    queryKey: ['syllabi', SyllabusStatus.ARCHIVED],
    queryFn: () =>
      syllabusService.getSyllabi({ status: [SyllabusStatus.ARCHIVED] }),
    select: (response) => response.data,
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
      form.resetFields();
    },
    onError: () => {
      message.error('Gỡ bỏ đề cương thất bại');
    },
  });

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
          <Text strong>{selectedSyllabus.courseCode} - {selectedSyllabus.courseName}</Text>
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

  const handleViewHistory = (syllabus: Syllabus) => {
    setSelectedSyllabus(syllabus);
    setHistoryModalVisible(true);
  };

  const columns: ColumnsType<Syllabus> = [
    {
      title: 'Mã HP',
      dataIndex: 'courseCode',
      key: 'courseCode',
      width: 80,
      fixed: 'left',
    },
    {
      title: 'Tên học phần',
      dataIndex: 'courseName',
      key: 'courseName',
      width: 200,
      ellipsis: { showTitle: false },
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{text}</Text>
          <Text type="secondary" style={{ fontSize: '11px' }}>
            {record.courseNameEn}
          </Text>
        </Space>
      ),
    },
    {
      title: 'Giảng viên',
      dataIndex: 'ownerName',
      key: 'ownerName',
      width: 120,
      ellipsis: { showTitle: false },
    },
    {
      title: 'Khoa/Bộ môn',
      dataIndex: 'department',
      key: 'department',
      width: 140,
      ellipsis: { showTitle: false },
    },
    {
      title: 'Ver',
      dataIndex: 'version',
      key: 'version',
      width: 60,
      align: 'center',
      render: (version) => <Tag color="blue">v{version}</Tag>,
    },
    {
      title: 'Xuất bản',
      dataIndex: 'publishedAt',
      key: 'publishedAt',
      width: 90,
      render: (date) => date ? dayjs(date).format('DD/MM/YY') : '-',
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semester',
      key: 'semester',
      width: 100,
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/syllabi/${record.id}`)}
          >
            Xem
          </Button>
          <Button
            size="small"
            icon={<HistoryOutlined />}
            onClick={() => handleViewHistory(record)}
          >
            Lịch sử
          </Button>
          {record.status === SyllabusStatus.PUBLISHED && (
            <Button
              size="small"
              danger
              icon={<StopOutlined />}
              onClick={() => handleUnpublishClick(record)}
            >
              Gỡ bỏ
            </Button>
          )}
        </Space>
      ),
    },
  ];

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

  return (
    <div style={{ padding: '24px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card>
          <Title level={4} style={{ margin: 0 }}>
            <FileTextOutlined /> Quản lý Đề cương Đã xuất hành
          </Title>
        </Card>

        <Tabs
          items={[
            {
              key: 'published',
              label: (
                <Badge count={publishedSyllabi?.length || 0} showZero>
                  <span style={{ marginRight: 8 }}>Đang xuất hành</span>
                </Badge>
              ),
              children: (
                <Card>
                  <Table
                    columns={columns}
                    dataSource={publishedSyllabi}
                    rowKey="id"
                    loading={loadingPublished}
                    pagination={{
                      pageSize: 10,
                      showSizeChanger: true,
                      showTotal: (total) => `Tổng ${total} đề cương`,
                    }}
                    scroll={{ x: 1400 }}
                  />
                </Card>
              ),
            },
            {
              key: 'archived',
              label: (
                <Badge count={archivedSyllabi?.length || 0} showZero color="#d9d9d9">
                  <span style={{ marginRight: 8 }}>Đã gỡ bỏ</span>
                </Badge>
              ),
              children: (
                <Card>
                  <Table
                    columns={columns}
                    dataSource={archivedSyllabi}
                    rowKey="id"
                    loading={loadingArchived}
                    pagination={{
                      pageSize: 10,
                      showSizeChanger: true,
                      showTotal: (total) => `Tổng ${total} đề cương`,
                    }}
                    scroll={{ x: 1400 }}
                  />
                </Card>
              ),
            },
          ]}
        />
      </Space>

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
          form.resetFields();
        }}
        footer={null}
        width={600}
      >
        {selectedSyllabus && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="Mã HP">
                {selectedSyllabus.courseCode}
              </Descriptions.Item>
              <Descriptions.Item label="Tên học phần">
                {selectedSyllabus.courseName}
              </Descriptions.Item>
              <Descriptions.Item label="Giảng viên">
                {selectedSyllabus.ownerName}
              </Descriptions.Item>
            </Descriptions>

            <Form form={form} layout="vertical" onFinish={handleUnpublish}>
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
                {selectedSyllabus.courseCode}
              </Descriptions.Item>
              <Descriptions.Item label="Phiên bản" span={1}>
                <Tag color="blue">v{selectedSyllabus.version}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Tên học phần" span={2}>
                {selectedSyllabus.courseName}
              </Descriptions.Item>
            </Descriptions>

            <Card title="Vòng đời phê duyệt" size="small">
              {renderLifecycleTimeline(selectedSyllabus)}
            </Card>
          </Space>
        )}
      </Modal>
    </div>
  );
};
