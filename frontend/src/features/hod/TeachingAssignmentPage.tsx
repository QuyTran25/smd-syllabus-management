import React, { useState } from 'react';
import { Card, Table, Button, Space, Modal, Form, Select, Input, Tag, message, DatePicker, Tooltip, Badge } from 'antd';
import { PlusOutlined, UserOutlined, ClockCircleOutlined, CheckCircleOutlined, MessageOutlined, EyeOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

interface TeachingAssignment {
  id: string;
  courseCode: string;
  courseName: string;
  semester: string;
  mainLecturer: {
    id: string;
    name: string;
    email: string;
  };
  coLecturers: Array<{
    id: string;
    name: string;
    email: string;
  }>;
  deadline: string;
  status: 'pending' | 'in-progress' | 'submitted' | 'completed';
  syllabusId?: string; // ID của đề cương sau khi giáo viên tạo
  createdAt: string;
  commentCount: number; // Số lượng comment giữa các giáo viên
}

// Mock data lecturers
const mockLecturers = [
  { id: 'l1', name: 'TS. Nguyễn Văn A', email: 'nva@university.edu.vn', department: 'Khoa CNTT' },
  { id: 'l2', name: 'ThS. Trần Thị B', email: 'ttb@university.edu.vn', department: 'Khoa CNTT' },
  { id: 'l3', name: 'TS. Lê Văn C', email: 'lvc@university.edu.vn', department: 'Khoa CNTT' },
  { id: 'l4', name: 'PGS. Phạm Thị D', email: 'ptd@university.edu.vn', department: 'Khoa CNTT' },
  { id: 'l5', name: 'ThS. Hoàng Văn E', email: 'hve@university.edu.vn', department: 'Khoa CNTT' },
];

// Mock courses from AA's course management
const mockCourses = [
  { id: 'c1', code: 'CS101', name: 'Nhập môn Lập trình', semester: 'HK1 2024-2025' },
  { id: 'c2', code: 'CS201', name: 'Cấu trúc Dữ liệu và Giải thuật', semester: 'HK1 2024-2025' },
  { id: 'c3', code: 'CS301', name: 'Cơ sở Dữ liệu', semester: 'HK2 2024-2025' },
  { id: 'c4', code: 'CS401', name: 'Hệ điều hành', semester: 'HK2 2024-2025' },
];

// Mock assignments
const mockAssignments: TeachingAssignment[] = [
  {
    id: 'ta1',
    courseCode: 'CS201',
    courseName: 'Cấu trúc Dữ liệu và Giải thuật',
    semester: 'HK1 2024-2025',
    mainLecturer: { id: 'l1', name: 'TS. Nguyễn Văn A', email: 'nva@university.edu.vn' },
    coLecturers: [
      { id: 'l2', name: 'ThS. Trần Thị B', email: 'ttb@university.edu.vn' },
    ],
    deadline: '2025-01-15',
    status: 'in-progress',
    createdAt: '2024-12-01',
    commentCount: 5,
  },
  {
    id: 'ta2',
    courseCode: 'CS301',
    courseName: 'Cơ sở Dữ liệu',
    semester: 'HK2 2024-2025',
    mainLecturer: { id: 'l3', name: 'TS. Lê Văn C', email: 'lvc@university.edu.vn' },
    coLecturers: [
      { id: 'l4', name: 'PGS. Phạm Thị D', email: 'ptd@university.edu.vn' },
      { id: 'l5', name: 'ThS. Hoàng Văn E', email: 'hve@university.edu.vn' },
    ],
    deadline: '2025-02-28',
    status: 'pending',
    syllabusId: 's10',
    createdAt: '2024-12-05',
    commentCount: 0,
  },
  {
    id: 'ta3',
    courseCode: 'CS101',
    courseName: 'Nhập môn Lập trình',
    semester: 'HK1 2024-2025',
    mainLecturer: { id: 'l2', name: 'ThS. Trần Thị B', email: 'ttb@university.edu.vn' },
    coLecturers: [],
    deadline: '2024-12-20',
    status: 'submitted',
    syllabusId: 's11',
    createdAt: '2024-11-15',
    commentCount: 12,
  },
];

// Mock comments
const mockComments = [
  {
    id: 'c1',
    assignmentId: 'ta1',
    userId: 'l2',
    userName: 'ThS. Trần Thị B',
    content: 'Phần CLO3 cần bổ sung thêm về phân tích độ phức tạp thuật toán',
    createdAt: '2024-12-10 14:30',
  },
  {
    id: 'c2',
    assignmentId: 'ta1',
    userId: 'l1',
    userName: 'TS. Nguyễn Văn A',
    content: 'Đã cập nhật CLO3, cảm ơn góp ý',
    createdAt: '2024-12-10 16:20',
  },
];

export const TeachingAssignmentPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isCommentModalVisible, setIsCommentModalVisible] = useState(false);
  const [selectedAssignment, setSelectedAssignment] = useState<TeachingAssignment | null>(null);
  const [form] = Form.useForm();

  // Fetch assignments
  const { data: assignments, isLoading } = useQuery({
    queryKey: ['teaching-assignments'],
    queryFn: async () => {
      await new Promise((resolve) => setTimeout(resolve, 500));
      return mockAssignments;
    },
  });

  // Fetch comments for selected assignment
  const { data: comments } = useQuery({
    queryKey: ['assignment-comments', selectedAssignment?.id],
    queryFn: async () => {
      await new Promise((resolve) => setTimeout(resolve, 300));
      return mockComments.filter(c => c.assignmentId === selectedAssignment?.id);
    },
    enabled: !!selectedAssignment && isCommentModalVisible,
  });

  // Create assignment mutation
  const createAssignmentMutation = useMutation({
    mutationFn: async (values: any) => {
      await new Promise((resolve) => setTimeout(resolve, 800));
      return values;
    },
    onSuccess: () => {
      message.success('Gán nhiệm vụ thành công');
      setIsModalVisible(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['teaching-assignments'] });
    },
  });

  const handleViewComments = (assignment: TeachingAssignment) => {
    setSelectedAssignment(assignment);
    setIsCommentModalVisible(true);
  };

  const handleViewSyllabus = (syllabusId: string) => {
    navigate(`/syllabi/${syllabusId}`);
  };

  const columns: ColumnsType<TeachingAssignment> = [
    {
      title: 'Mã môn',
      dataIndex: 'courseCode',
      key: 'courseCode',
      width: 100,
    },
    {
      title: 'Tên môn học',
      dataIndex: 'courseName',
      key: 'courseName',
      width: 250,
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <span>{text}</span>
          <span style={{ fontSize: '12px', color: '#999' }}>{record.semester}</span>
        </Space>
      ),
    },
    {
      title: 'Giáo viên chính',
      key: 'mainLecturer',
      width: 200,
      render: (_, record) => (
        <Space>
          <UserOutlined style={{ color: '#1890ff' }} />
          <div>
            <div style={{ fontWeight: 500 }}>{record.mainLecturer.name}</div>
            <div style={{ fontSize: '12px', color: '#999' }}>{record.mainLecturer.email}</div>
          </div>
        </Space>
      ),
    },
    {
      title: 'GV cộng tác',
      key: 'coLecturers',
      width: 120,
      align: 'center',
      render: (_, record) => (
        <Tooltip title={record.coLecturers.map(c => c.name).join(', ') || 'Không có'}>
          <Tag color={record.coLecturers.length > 0 ? 'blue' : 'default'}>
            {record.coLecturers.length} người
          </Tag>
        </Tooltip>
      ),
    },
    {
      title: 'Deadline',
      dataIndex: 'deadline',
      key: 'deadline',
      width: 120,
      render: (date) => {
        const isOverdue = dayjs(date).isBefore(dayjs());
        return (
          <span style={{ color: isOverdue ? '#ff4d4f' : undefined }}>
            {dayjs(date).format('DD/MM/YYYY')}
          </span>
        );
      },
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 140,
      render: (status) => {
        const config = {
          pending: { color: 'default', text: 'Chưa bắt đầu', icon: <ClockCircleOutlined /> },
          'in-progress': { color: 'blue', text: 'Đang làm', icon: <ClockCircleOutlined /> },
          submitted: { color: 'orange', text: 'Đã gửi duyệt', icon: <CheckCircleOutlined /> },
          completed: { color: 'green', text: 'Hoàn thành', icon: <CheckCircleOutlined /> },
        };
        const { color, text, icon } = config[status as keyof typeof config];
        return <Tag color={color} icon={icon}>{text}</Tag>;
      },
    },
    {
      title: 'Comments',
      key: 'comments',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <Badge count={record.commentCount} showZero>
          <Button
            type="text"
            icon={<MessageOutlined />}
            onClick={() => handleViewComments(record)}
          />
        </Badge>
      ),
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 150,
      render: (_, record) => (
        <Space>
          {record.syllabusId && (
            <Button
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleViewSyllabus(record.syllabusId!)}
            >
              Xem đề cương
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2>Quản lý Công tác Giảng dạy</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalVisible(true)}>
          Gán nhiệm vụ
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
            showTotal: (total) => `Tổng ${total} nhiệm vụ`,
          }}
        />
      </Card>

      {/* Create Assignment Modal */}
      <Modal
        title={<Space><PlusOutlined /> Gán nhiệm vụ làm đề cương</Space>}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
        }}
        onOk={() => form.submit()}
        confirmLoading={createAssignmentMutation.isPending}
        width={700}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => createAssignmentMutation.mutate(values)}
        >
          <Form.Item
            label="Chọn môn học"
            name="courseId"
            rules={[{ required: true, message: 'Vui lòng chọn môn học' }]}
          >
            <Select placeholder="Chọn môn học cần làm đề cương">
              {mockCourses.map((course) => (
                <Option key={course.id} value={course.id}>
                  {course.code} - {course.name} ({course.semester})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Giáo viên chính"
            name="mainLecturerId"
            rules={[{ required: true, message: 'Vui lòng chọn giáo viên chính' }]}
            tooltip="Giáo viên chính có trách nhiệm tạo và hoàn thiện đề cương"
          >
            <Select
              placeholder="Chọn giáo viên chính"
              showSearch
              optionFilterProp="children"
            >
              {mockLecturers.map((lecturer) => (
                <Option key={lecturer.id} value={lecturer.id}>
                  {lecturer.name} ({lecturer.email})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Giáo viên cộng tác"
            name="coLecturerIds"
            tooltip="Giáo viên cộng tác chỉ được comment góp ý, không được sửa trực tiếp đề cương"
          >
            <Select
              mode="multiple"
              placeholder="Chọn giáo viên cộng tác (không bắt buộc)"
              showSearch
              optionFilterProp="children"
            >
              {mockLecturers.map((lecturer) => (
                <Option key={lecturer.id} value={lecturer.id}>
                  {lecturer.name} ({lecturer.email})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Thời hạn hoàn thành"
            name="deadline"
            rules={[{ required: true, message: 'Vui lòng chọn thời hạn' }]}
          >
            <DatePicker
              style={{ width: '100%' }}
              format="DD/MM/YYYY"
              placeholder="Chọn ngày deadline"
              disabledDate={(current) => current && current < dayjs().startOf('day')}
            />
          </Form.Item>

          <Form.Item label="Ghi chú" name="note">
            <TextArea rows={3} placeholder="Ghi chú cho giáo viên (không bắt buộc)..." />
          </Form.Item>
        </Form>
      </Modal>

      {/* View Comments Modal */}
      <Modal
        title={
          <Space>
            <MessageOutlined />
            <span>Phản hồi giữa giáo viên</span>
          </Space>
        }
        open={isCommentModalVisible}
        onCancel={() => {
          setIsCommentModalVisible(false);
          setSelectedAssignment(null);
        }}
        footer={null}
        width={800}
      >
        {selectedAssignment && (
          <div>
            <Card size="small" style={{ marginBottom: 16, backgroundColor: '#f5f5f5' }}>
              <Space direction="vertical" size={4}>
                <div>
                  <strong>Môn học:</strong> {selectedAssignment.courseCode} - {selectedAssignment.courseName}
                </div>
                <div>
                  <strong>GV chính:</strong> {selectedAssignment.mainLecturer.name}
                </div>
                <div>
                  <strong>GV cộng tác:</strong>{' '}
                  {selectedAssignment.coLecturers.length > 0
                    ? selectedAssignment.coLecturers.map(c => c.name).join(', ')
                    : 'Không có'}
                </div>
              </Space>
            </Card>

            <div style={{ maxHeight: 400, overflowY: 'auto' }}>
              {comments && comments.length > 0 ? (
                <Space direction="vertical" size={12} style={{ width: '100%' }}>
                  {comments.map((comment) => (
                    <Card key={comment.id} size="small">
                      <Space direction="vertical" size={8} style={{ width: '100%' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <strong>{comment.userName}</strong>
                          <span style={{ fontSize: '12px', color: '#999' }}>{comment.createdAt}</span>
                        </div>
                        <div style={{ padding: '8px 12px', backgroundColor: '#f0f0f0', borderRadius: 4 }}>
                          {comment.content}
                        </div>
                      </Space>
                    </Card>
                  ))}
                </Space>
              ) : (
                <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
                  <MessageOutlined style={{ fontSize: 48, marginBottom: 16 }} />
                  <p>Chưa có phản hồi nào</p>
                </div>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};
