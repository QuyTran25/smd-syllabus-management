import React, { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  DatePicker,
  Input,
  message,
  Typography,
  Descriptions,
  Badge,
} from 'antd';
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  FileTextOutlined,
  RocketOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { syllabusService } from '@/services';
import { Syllabus, SyllabusStatus } from '@/types';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { TextArea } = Input;

export const PublishQueuePage: React.FC = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [publishModalVisible, setPublishModalVisible] = useState(false);
  const [selectedSyllabus, setSelectedSyllabus] = useState<Syllabus | null>(null);
  const [form] = Form.useForm();

  // Fetch syllabi with APPROVED status (waiting to be published)
  const { data: approvedSyllabi, isLoading } = useQuery({
    queryKey: ['syllabi', SyllabusStatus.APPROVED],
    queryFn: () => syllabusService.getSyllabi({ status: [SyllabusStatus.APPROVED] }),
    select: (response) => response.data,
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
      form.resetFields();
    },
    onError: () => {
      message.error('Xuất hành đề cương thất bại');
    },
  });

  const handlePublishClick = (syllabus: Syllabus) => {
    setSelectedSyllabus(syllabus);
    form.setFieldsValue({
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
      title: 'TC',
      dataIndex: 'credits',
      key: 'credits',
      width: 50,
      align: 'center',
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
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.faculty}</Text>
          <Text type="secondary" style={{ fontSize: '11px' }}>
            {text}
          </Text>
        </Space>
      ),
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semester',
      key: 'semester',
      width: 100,
    },
    {
      title: 'Ngày duyệt',
      key: 'approvalDates',
      width: 160,
      ellipsis: { showTitle: false },
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          {record.hodApprovedAt && (
            <Text style={{ fontSize: '11px' }}>
              <CheckCircleOutlined style={{ color: '#52c41a' }} /> TBM:{' '}
              {dayjs(record.hodApprovedAt).format('DD/MM')}
            </Text>
          )}
          {record.aaApprovedAt && (
            <Text style={{ fontSize: '11px' }}>
              <CheckCircleOutlined style={{ color: '#1890ff' }} /> PĐT:{' '}
              {dayjs(record.aaApprovedAt).format('DD/MM')}
            </Text>
          )}
          {record.principalApprovedAt && (
            <Text style={{ fontSize: '11px' }}>
              <CheckCircleOutlined style={{ color: '#722ed1' }} /> HT:{' '}
              {dayjs(record.principalApprovedAt).format('DD/MM')}
            </Text>
          )}
        </Space>
      ),
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 160,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            size="small"
            icon={<FileTextOutlined />}
            onClick={() => navigate(`/syllabi/${record.id}`)}
          >
            Xem
          </Button>
          <Button
            type="primary"
            size="small"
            icon={<RocketOutlined />}
            onClick={() => handlePublishClick(record)}
          >
            Xuất hành
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card>
          <Space direction="vertical" size="small" style={{ width: '100%' }}>
            <Title level={4} style={{ margin: 0 }}>
              <ClockCircleOutlined /> Hàng đợi Xuất hành
            </Title>
            <Text type="secondary">
              Danh sách đề cương đã được Hiệu trưởng phê duyệt, chờ Admin xuất hành
            </Text>
            <Space>
              <Badge count={approvedSyllabi?.length || 0} showZero color="#faad14">
                <Tag color="warning">Chờ xuất hành</Tag>
              </Badge>
            </Space>
          </Space>
        </Card>

        <Card>
          <Table
            columns={columns}
            dataSource={approvedSyllabi}
            rowKey="id"
            loading={isLoading}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} đề cương`,
            }}
            scroll={{ x: 1400 }}
          />
        </Card>
      </Space>

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
          form.resetFields();
        }}
        footer={null}
        width={700}
      >
        {selectedSyllabus && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="Mã HP" span={1}>
                {selectedSyllabus.courseCode}
              </Descriptions.Item>
              <Descriptions.Item label="Tín chỉ" span={1}>
                {selectedSyllabus.credits}
              </Descriptions.Item>
              <Descriptions.Item label="Tên học phần" span={2}>
                <Space direction="vertical" size={0}>
                  <Text strong>{selectedSyllabus.courseName}</Text>
                  <Text type="secondary">{selectedSyllabus.courseNameEn}</Text>
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

            <Form form={form} layout="vertical" onFinish={handlePublish}>
              <Form.Item
                label="Ngày hiệu lực"
                name="effectiveDate"
                rules={[{ required: true, message: 'Chọn ngày hiệu lực' }]}
                extra="Đề cương sẽ có hiệu lực từ ngày này"
              >
                <DatePicker format="DD/MM/YYYY" style={{ width: '100%' }} placeholder="Chọn ngày" />
              </Form.Item>

              <Form.Item label="Ghi chú (tùy chọn)" name="note">
                <TextArea rows={3} placeholder="Ghi chú về lần xuất hành này..." />
              </Form.Item>

              <Form.Item style={{ marginBottom: 0 }}>
                <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                  <Button onClick={() => setPublishModalVisible(false)}>Hủy</Button>
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
    </div>
  );
};
