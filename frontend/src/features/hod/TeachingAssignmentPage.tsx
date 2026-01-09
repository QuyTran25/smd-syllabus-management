import React, { useState } from 'react';
import { Card, Table, Button, Space, Modal, Form, Select, Input, Tag, message, DatePicker, Tooltip, Badge } from 'antd';
import { PlusOutlined, UserOutlined, ClockCircleOutlined, CheckCircleOutlined, MessageOutlined, EyeOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import dayjs, { Dayjs } from 'dayjs';
import { teachingAssignmentService, TeachingAssignment } from '@/services/teaching-assignment.service';
import { academicTermService } from '@/services/academic-term.service';

const { Option } = Select;
const { TextArea } = Input;

export const TeachingAssignmentPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isCommentModalVisible, setIsCommentModalVisible] = useState(false);
  const [selectedAssignment, setSelectedAssignment] = useState<TeachingAssignment | null>(null);
  const [form] = Form.useForm();

  // Fetch assignments from real API
  const { data: assignments = [], isLoading, error } = useQuery({
    queryKey: ['teaching-assignments'],
    queryFn: () => teachingAssignmentService.getAll(),
  });

  // Fetch subjects for HOD
  const { data: subjects = [] } = useQuery({
    queryKey: ['hod-subjects'],
    queryFn: () => teachingAssignmentService.getHodSubjects(),
  });

  // Fetch lecturers for HOD
  const { data: lecturers = [] } = useQuery({
    queryKey: ['hod-lecturers'],
    queryFn: () => teachingAssignmentService.getHodLecturers(),
  });

  // Fetch academic terms
  const { data: academicTerms = [] } = useQuery({
    queryKey: ['academic-terms'],
    queryFn: () => academicTermService.getAllTerms(),
  });

  // Show error message if API call fails
  React.useEffect(() => {
    if (error) {
      message.error('Không thể tải dữ liệu. Vui lòng thử lại sau.');
    }
  }, [error]);

  // Create assignment mutation
  const createAssignmentMutation = useMutation({
    mutationFn: async (values: {
      subjectId: string;
      academicTermId: string;
      mainLecturerId: string;
      coLecturers?: string[];
      deadline: Dayjs;
      comments?: string;
    }) => {
      return teachingAssignmentService.create({
        subjectId: values.subjectId,
        academicTermId: values.academicTermId,
        mainLecturerId: values.mainLecturerId,
        collaboratorIds: values.coLecturers || [],
        deadline: values.deadline.format('YYYY-MM-DD'),
        comments: values.comments,
      });
    },
    onSuccess: () => {
      message.success('Gán nhiệm vụ thành công');
      setIsModalVisible(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['teaching-assignments'] });
    },
    onError: (error: any) => {
      message.error(error?.response?.data?.message || 'Gán nhiệm vụ thất bại');
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
          PENDING: { color: 'default', text: 'Chưa bắt đầu', icon: <ClockCircleOutlined /> },
          IN_PROGRESS: { color: 'blue', text: 'Đang làm', icon: <ClockCircleOutlined /> },
          SUBMITTED: { color: 'orange', text: 'Đã gửi duyệt', icon: <CheckCircleOutlined /> },
          COMPLETED: { color: 'green', text: 'Hoàn thành', icon: <CheckCircleOutlined /> },
        };
        const { color, text, icon } = config[status as keyof typeof config] || config.PENDING;
        return <Tag color={color} icon={icon}>{text}</Tag>;
      },
    },
    {
      title: 'Comments',
      key: 'comments',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <Badge count={record.comments ? 1 : 0} showZero>
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
          scroll={{ x: 1100 }}
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
            name="subjectId"
            rules={[{ required: true, message: 'Vui lòng chọn môn học' }]}
          >
            <Select 
              placeholder="Chọn môn học cần làm đề cương"
              showSearch
              onChange={() => {
                // Auto-select first/latest academic term when subject is selected
                if (academicTerms.length > 0) {
                  form.setFieldValue('academicTermId', academicTerms[0].id);
                }
              }}
              filterOption={(input, option) =>
                (option?.children as string)?.toLowerCase().includes(input.toLowerCase())
              }
            >
              {subjects.map((subject) => (
                <Option key={subject.id} value={subject.id}>
                  {subject.code} - {subject.nameVi} ({subject.credits} TC)
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Học kỳ"
            name="academicTermId"
            rules={[{ required: true, message: 'Vui lòng chọn học kỳ' }]}
            tooltip="Học kỳ được tự động chọn, bạn có thể thay đổi nếu cần"
          >
            <Select placeholder="Chọn học kỳ">
              {academicTerms.map((term) => (
                <Option key={term.id} value={term.id}>
                  {term.name}
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
              filterOption={(input, option) =>
                (option?.children as string)?.toLowerCase().includes(input.toLowerCase())
              }
            >
              {lecturers.map((lecturer) => (
                <Option key={lecturer.id} value={lecturer.id}>
                  {lecturer.fullName} ({lecturer.email})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Giáo viên cộng tác"
            name="coLecturers"
            tooltip="Giáo viên cộng tác chỉ được comment góp ý, không được sửa trực tiếp đề cương"
          >
            <Select
              mode="multiple"
              placeholder="Chọn giáo viên cộng tác (không bắt buộc)"
              showSearch
              filterOption={(input, option) =>
                (option?.children as string)?.toLowerCase().includes(input.toLowerCase())
              }
            >
              {lecturers
                .filter(l => l.id !== form.getFieldValue('mainLecturerId'))
                .map((lecturer) => (
                  <Option key={lecturer.id} value={lecturer.id}>
                    {lecturer.fullName} ({lecturer.email})
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

          <Form.Item label="Ghi chú" name="comments">
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
              {selectedAssignment.comments ? (
                <Card size="small">
                  <Space direction="vertical" size={8} style={{ width: '100%' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <strong>Ghi chú</strong>
                    </div>
                    <div style={{ padding: '8px 12px', backgroundColor: '#f0f0f0', borderRadius: 4 }}>
                      {selectedAssignment.comments}
                    </div>
                  </Space>
                </Card>
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
