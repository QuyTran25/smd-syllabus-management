import React, { useState } from 'react';
import { Card, Table, Button, Space, Modal, Form, Select, Input, Tag, message, DatePicker, Tooltip, Badge, Spin, Empty } from 'antd';
import { PlusOutlined, UserOutlined, ClockCircleOutlined, CheckCircleOutlined, MessageOutlined, EyeOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import dayjs, { Dayjs } from 'dayjs';
import { teachingAssignmentService, TeachingAssignment } from '@/services/teaching-assignment.service';
import { academicTermService } from '@/services/academic-term.service';
import { syllabusService, SyllabusComment } from '@/services/syllabus.service';

const { Option } = Select;
const { TextArea } = Input;

export const TeachingAssignmentPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isCommentModalVisible, setIsCommentModalVisible] = useState(false);
  const [selectedAssignment, setSelectedAssignment] = useState<TeachingAssignment | null>(null);
  const [commentText, setCommentText] = useState('');
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
    setCommentText('');
    setIsCommentModalVisible(true);
  };

  // Fetch review comments for selected assignment's syllabus
  const { data: reviewComments = [], isLoading: isLoadingComments, refetch: refetchComments } = useQuery({
    queryKey: ['review-comments', selectedAssignment?.syllabusId],
    queryFn: () => syllabusService.getComments(selectedAssignment!.syllabusId!),
    enabled: !!selectedAssignment?.syllabusId && isCommentModalVisible,
  });

  // Add comment mutation for HOD
  const addCommentMutation = useMutation({
    mutationFn: async (content: string) => {
      if (!selectedAssignment?.syllabusId) {
        throw new Error('Chưa có đề cương');
      }
      return syllabusService.addComment(selectedAssignment.syllabusId, content);
    },
    onSuccess: () => {
      message.success('Đã thêm bình luận');
      setCommentText('');
      refetchComments();
    },
    onError: (error: any) => {
      message.error(error?.response?.data?.message || 'Thêm bình luận thất bại');
    },
  });

  const handleAddComment = () => {
    if (!commentText.trim()) {
      message.warning('Vui lòng nhập nội dung bình luận');
      return;
    }
    addCommentMutation.mutate(commentText.trim());
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
          'pending': { color: 'default', text: 'Chưa bắt đầu', icon: <ClockCircleOutlined /> },
          'in-progress': { color: 'blue', text: 'Đang làm', icon: <ClockCircleOutlined /> },
          'submitted': { color: 'orange', text: 'Đã gửi duyệt', icon: <CheckCircleOutlined /> },
          'completed': { color: 'green', text: 'Hoàn thành', icon: <CheckCircleOutlined /> },
        };
        const { color, text, icon } = config[status as keyof typeof config] || config['pending'];
        return <Tag color={color} icon={icon}>{text}</Tag>;
      },
    },
    {
      title: 'Comments',
      key: 'comments',
      width: 100,
      align: 'center',
      render: (_, record) => {
        // If no syllabus yet, show assignment comments indicator
        if (!record.syllabusId) {
          return (
            <Badge count={record.comments ? 1 : 0} showZero>
              <Button
                type="text"
                icon={<MessageOutlined />}
                onClick={() => handleViewComments(record)}
                disabled={!record.comments}
              />
            </Badge>
          );
        }
        
        // If syllabus exists, show "Xem" button (comments count will be fetched in modal)
        return (
          <Button
            type="link"
            icon={<MessageOutlined />}
            onClick={() => handleViewComments(record)}
          >
            Xem
          </Button>
        );
      },
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            size="small"
            icon={<MessageOutlined />}
            onClick={() => handleViewComments(record)}
          >
            Bình luận
          </Button>
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
            <span>Bình luận đề cương</span>
          </Space>
        }
        open={isCommentModalVisible}
        onCancel={() => {
          setIsCommentModalVisible(false);
          setSelectedAssignment(null);
          setCommentText('');
        }}
        footer={[
          <Button 
            key="close" 
            onClick={() => {
              setIsCommentModalVisible(false);
              setCommentText('');
            }}
          >
            Đóng
          </Button>,
        ]}
        width={900}
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
                <div>
                  <strong>Trạng thái:</strong>{' '}
                  <Tag color={
                    selectedAssignment.status === 'pending' ? 'default' :
                    selectedAssignment.status === 'in-progress' ? 'blue' :
                    selectedAssignment.status === 'submitted' ? 'orange' : 'green'
                  }>
                    {selectedAssignment.status === 'pending' ? 'Chưa bắt đầu' :
                     selectedAssignment.status === 'in-progress' ? 'Đang làm' :
                     selectedAssignment.status === 'submitted' ? 'Đã gửi duyệt' : 'Hoàn thành'}
                  </Tag>
                </div>
              </Space>
            </Card>

            {/* Assignment Comments (HOD's note) */}
            {selectedAssignment.comments && (
              <Card size="small" style={{ marginBottom: 16 }}>
                <Space direction="vertical" size={8} style={{ width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <strong>Ghi chú của trưởng bộ môn</strong>
                  </div>
                  <div style={{ padding: '8px 12px', backgroundColor: '#fff3cd', borderRadius: 4, border: '1px solid #ffc107' }}>
                    {selectedAssignment.comments}
                  </div>
                </Space>
              </Card>
            )}

            {/* Review Comments from Lecturers */}
            <div style={{ marginBottom: 8 }}>
              <strong>Bình luận của giảng viên ({reviewComments.length})</strong>
            </div>
            
            {!selectedAssignment.syllabusId ? (
              <Empty
                description="Chưa có đề cương. Giảng viên chưa tạo đề cương."
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            ) : (
              <>
                {isLoadingComments ? (
                  <div style={{ textAlign: 'center', padding: 40 }}>
                    <Spin />
                  </div>
                ) : reviewComments.length === 0 ? (
                  <div style={{ textAlign: 'center', padding: 20, color: '#999', backgroundColor: '#fafafa', borderRadius: 4, marginBottom: 16 }}>
                    <MessageOutlined style={{ fontSize: 32, marginBottom: 8 }} />
                    <p>Chưa có bình luận nào</p>
                  </div>
                ) : (
                  <div style={{ maxHeight: 300, overflowY: 'auto', marginBottom: 16 }}>
                    <Space direction="vertical" size={12} style={{ width: '100%' }}>
                      {reviewComments.map((comment) => (
                        <Card key={comment.id} size="small">
                          <Space direction="vertical" size={4} style={{ width: '100%' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <Space>
                                <UserOutlined style={{ color: '#1890ff' }} />
                                <strong>{comment.createdByName || 'Không rõ'}</strong>
                                {comment.section && (
                                  <Tag color="blue">{comment.section}</Tag>
                                )}
                              </Space>
                              <span style={{ fontSize: '12px', color: '#999' }}>
                                {dayjs(comment.createdAt).format('DD/MM/YYYY HH:mm')}
                              </span>
                            </div>
                            <div style={{ padding: '8px 0', whiteSpace: 'pre-wrap' }}>
                              {comment.content}
                            </div>
                          </Space>
                        </Card>
                      ))}
                    </Space>
                  </div>
                )}

                {/* Add Comment Section for HOD */}
                <Card size="small" title="Thêm bình luận của bạn" style={{ marginTop: 16 }}>
                  <Space direction="vertical" style={{ width: '100%' }} size={12}>
                    <TextArea
                      rows={4}
                      placeholder="Nhập bình luận của bạn về đề cương..."
                      value={commentText}
                      onChange={(e) => setCommentText(e.target.value)}
                      disabled={addCommentMutation.isPending}
                    />
                    <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                      <Button
                        type="primary"
                        icon={<MessageOutlined />}
                        onClick={handleAddComment}
                        loading={addCommentMutation.isPending}
                        disabled={!commentText.trim()}
                      >
                        Gửi bình luận
                      </Button>
                    </div>
                  </Space>
                </Card>
              </>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};
