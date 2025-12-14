import React, { useState } from 'react';
import { Card, Table, Button, Space, Modal, Form, Select, Input, Tag, message, Avatar } from 'antd';
import { PlusOutlined, UserAddOutlined, CheckCircleOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';

const { Option } = Select;
const { TextArea } = Input;

interface ReviewAssignment {
  id: string;
  syllabusId: string;
  syllabusName: string;
  courseCode: string;
  reviewers: Array<{
    id: string;
    name: string;
    email: string;
    status: 'pending' | 'in-progress' | 'completed';
    feedback?: string;
    reviewedAt?: string;
  }>;
  deadline: string;
  createdAt: string;
  status: 'pending' | 'in-progress' | 'completed';
}

// Mock data
const mockReviewers = [
  { id: 'r1', name: 'TS. Nguyễn Văn A', email: 'nva@university.edu.vn' },
  { id: 'r2', name: 'TS. Trần Thị B', email: 'ttb@university.edu.vn' },
  { id: 'r3', name: 'ThS. Lê Văn C', email: 'lvc@university.edu.vn' },
  { id: 'r4', name: 'PGS. Phạm Thị D', email: 'ptd@university.edu.vn' },
];

const mockAssignments: ReviewAssignment[] = [
  {
    id: '1',
    syllabusId: 's1',
    syllabusName: 'Lập trình hướng đối tượng',
    courseCode: 'CS201',
    reviewers: [
      { id: 'r1', name: 'TS. Nguyễn Văn A', email: 'nva@university.edu.vn', status: 'completed', feedback: 'Đề cương tốt, cần bổ sung thêm tài liệu', reviewedAt: '2024-12-01' },
      { id: 'r2', name: 'TS. Trần Thị B', email: 'ttb@university.edu.vn', status: 'in-progress' },
    ],
    deadline: '2024-12-15',
    createdAt: '2024-11-25',
    status: 'in-progress',
  },
  {
    id: '2',
    syllabusId: 's2',
    syllabusName: 'Cấu trúc dữ liệu và Giải thuật',
    courseCode: 'CS301',
    reviewers: [
      { id: 'r3', name: 'ThS. Lê Văn C', email: 'lvc@university.edu.vn', status: 'pending' },
      { id: 'r4', name: 'PGS. Phạm Thị D', email: 'ptd@university.edu.vn', status: 'pending' },
    ],
    deadline: '2024-12-20',
    createdAt: '2024-12-01',
    status: 'pending',
  },
];

export const CollaborativeReviewPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isFeedbackModalVisible, setIsFeedbackModalVisible] = useState(false);
  const [selectedAssignment, setSelectedAssignment] = useState<ReviewAssignment | null>(null);
  const [form] = Form.useForm();

  // Mock query for assignments
  const { data: assignments, isLoading } = useQuery({
    queryKey: ['review-assignments'],
    queryFn: async () => {
      await new Promise((resolve) => setTimeout(resolve, 500));
      return mockAssignments;
    },
  });

  // Create assignment mutation
  const createAssignmentMutation = useMutation({
    mutationFn: async (values: any) => {
      await new Promise((resolve) => setTimeout(resolve, 800));
      return values;
    },
    onSuccess: () => {
      message.success('Gán reviewer thành công');
      setIsModalVisible(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['review-assignments'] });
    },
  });

  const columns: ColumnsType<ReviewAssignment> = [
    {
      title: 'Mã môn',
      dataIndex: 'courseCode',
      key: 'courseCode',
      width: 100,
    },
    {
      title: 'Tên đề cương',
      dataIndex: 'syllabusName',
      key: 'syllabusName',
      render: (text, record) => (
        <a onClick={() => navigate(`/syllabi/${record.syllabusId}`)}>{text}</a>
      ),
    },
    {
      title: 'Reviewers',
      key: 'reviewers',
      width: 250,
      render: (_, record) => (
        <Space direction="vertical" size={4}>
          {record.reviewers.map((reviewer) => (
            <Space key={reviewer.id} size={4}>
              <Avatar size="small" style={{ backgroundColor: '#1890ff' }}>
                {reviewer.name.charAt(0)}
              </Avatar>
              <span style={{ fontSize: 12 }}>{reviewer.name}</span>
              {reviewer.status === 'completed' && <CheckCircleOutlined style={{ color: '#52c41a' }} />}
              {reviewer.status === 'in-progress' && <ClockCircleOutlined style={{ color: '#faad14' }} />}
            </Space>
          ))}
        </Space>
      ),
    },
    {
      title: 'Tiến độ',
      key: 'progress',
      width: 120,
      render: (_, record) => {
        const completed = record.reviewers.filter((r) => r.status === 'completed').length;
        const total = record.reviewers.length;
        return (
          <span>
            {completed}/{total} hoàn thành
          </span>
        );
      },
    },
    {
      title: 'Deadline',
      dataIndex: 'deadline',
      key: 'deadline',
      width: 120,
      render: (date) => new Date(date).toLocaleDateString('vi-VN'),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => {
        const config = {
          pending: { color: 'default', text: 'Chưa bắt đầu' },
          'in-progress': { color: 'blue', text: 'Đang review' },
          completed: { color: 'green', text: 'Hoàn thành' },
        };
        const { color, text } = config[status as keyof typeof config];
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 180,
      render: (_, record) => (
        <Space>
          <Button
            size="small"
            onClick={() => {
              setSelectedAssignment(record);
              setIsFeedbackModalVisible(true);
            }}
          >
            Xem Feedback
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2>Quản lý Review Cộng tác</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalVisible(true)}>
          Gán Reviewer
        </Button>
      </div>

      <Card>
        <Table
          columns={columns}
          dataSource={assignments || []}
          rowKey="id"
          loading={isLoading}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `Tổng ${total} phân công review`,
          }}
        />
      </Card>

      {/* Assign Reviewers Modal */}
      <Modal
        title={<Space><UserAddOutlined /> Gán Reviewer cho Đề cương</Space>}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
        }}
        onOk={() => form.submit()}
        confirmLoading={createAssignmentMutation.isPending}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => createAssignmentMutation.mutate(values)}
        >
          <Form.Item
            label="Chọn đề cương"
            name="syllabusId"
            rules={[{ required: true, message: 'Vui lòng chọn đề cương' }]}
          >
            <Select placeholder="Chọn đề cương cần review">
              <Option value="s1">CS201 - Lập trình hướng đối tượng</Option>
              <Option value="s2">CS301 - Cấu trúc dữ liệu và Giải thuật</Option>
              <Option value="s3">CS401 - Hệ điều hành</Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="Chọn Reviewers"
            name="reviewers"
            rules={[{ required: true, message: 'Vui lòng chọn ít nhất 1 reviewer' }]}
          >
            <Select mode="multiple" placeholder="Chọn các reviewer">
              {mockReviewers.map((reviewer) => (
                <Option key={reviewer.id} value={reviewer.id}>
                  {reviewer.name} ({reviewer.email})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Deadline"
            name="deadline"
            rules={[{ required: true, message: 'Vui lòng chọn deadline' }]}
          >
            <Input type="date" />
          </Form.Item>

          <Form.Item label="Ghi chú" name="note">
            <TextArea rows={3} placeholder="Ghi chú cho reviewers..." />
          </Form.Item>
        </Form>
      </Modal>

      {/* View Feedback Modal */}
      <Modal
        title="Tổng hợp Feedback từ Reviewers"
        open={isFeedbackModalVisible}
        onCancel={() => {
          setIsFeedbackModalVisible(false);
          setSelectedAssignment(null);
        }}
        footer={null}
        width={700}
      >
        {selectedAssignment && (
          <div>
            <h3>{selectedAssignment.syllabusName}</h3>
            <p style={{ color: '#999' }}>Mã môn: {selectedAssignment.courseCode}</p>

            <Space direction="vertical" size={16} style={{ width: '100%', marginTop: 16 }}>
              {selectedAssignment.reviewers.map((reviewer) => (
                <Card key={reviewer.id} size="small">
                  <Space direction="vertical" size={8} style={{ width: '100%' }}>
                    <Space>
                      <Avatar style={{ backgroundColor: '#1890ff' }}>
                        {reviewer.name.charAt(0)}
                      </Avatar>
                      <div>
                        <div style={{ fontWeight: 500 }}>{reviewer.name}</div>
                        <div style={{ fontSize: 12, color: '#999' }}>{reviewer.email}</div>
                      </div>
                      {reviewer.status === 'completed' && (
                        <Tag color="green" icon={<CheckCircleOutlined />}>
                          Đã review
                        </Tag>
                      )}
                      {reviewer.status === 'in-progress' && (
                        <Tag color="blue" icon={<ClockCircleOutlined />}>
                          Đang review
                        </Tag>
                      )}
                      {reviewer.status === 'pending' && <Tag>Chưa bắt đầu</Tag>}
                    </Space>

                    {reviewer.feedback && (
                      <div
                        style={{
                          padding: 12,
                          backgroundColor: '#f5f5f5',
                          borderRadius: 4,
                          marginTop: 8,
                        }}
                      >
                        <div style={{ fontSize: 12, color: '#999', marginBottom: 4 }}>
                          Feedback ({reviewer.reviewedAt && new Date(reviewer.reviewedAt).toLocaleDateString('vi-VN')}):
                        </div>
                        <div>{reviewer.feedback}</div>
                      </div>
                    )}

                    {reviewer.status === 'pending' && (
                      <div style={{ color: '#999', fontSize: 12, fontStyle: 'italic' }}>
                        Chờ reviewer bắt đầu đánh giá...
                      </div>
                    )}
                  </Space>
                </Card>
              ))}
            </Space>
          </div>
        )}
      </Modal>
    </div>
  );
};
