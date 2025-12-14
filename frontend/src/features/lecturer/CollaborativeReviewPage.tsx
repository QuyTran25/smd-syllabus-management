import React, { useState } from 'react';
import { Card, List, Tag, Space, Button, Input, Avatar, Typography, Divider, Badge } from 'antd';
import {
  CommentOutlined,
  SendOutlined,
  UserOutlined,
  EyeOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { TextArea } = Input;
const { Text, Title } = Typography;

interface Comment {
  id: string;
  author: string;
  content: string;
  timestamp: string;
  isMe?: boolean;
}

interface ReviewAssignment {
  id: string;
  syllabusId: number;
  subjectCode: string;
  subjectName: string;
  mainLecturer: string;
  assignedBy: string;
  deadline: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';
  unreadComments: number;
}

const CollaborativeReviewPage: React.FC = () => {
  const navigate = useNavigate();
  const [selectedAssignment, setSelectedAssignment] = useState<ReviewAssignment | null>(null);
  const [newComment, setNewComment] = useState('');
  const [comments, setComments] = useState<Comment[]>([
    {
      id: '1',
      author: 'PGS.TS Lê Văn C (Giảng viên chính)',
      content: 'Các bạn xem giúp phần CLO và đánh giá có hợp lý chưa nhé.',
      timestamp: '2024-12-10 09:00',
    },
    {
      id: '2',
      author: 'Tôi',
      content: 'Em thấy CLO 2 cần bổ sung thêm phương pháp đánh giá cụ thể hơn ạ.',
      timestamp: '2024-12-10 14:30',
      isMe: true,
    },
    {
      id: '3',
      author: 'ThS. Nguyễn Thị D',
      content: 'Đồng ý với bạn, và tài liệu tham khảo số 3 đã cũ rồi.',
      timestamp: '2024-12-10 15:45',
    },
  ]);

  // Mock data - danh sách nhiệm vụ review được phân công
  const reviewAssignments: ReviewAssignment[] = [
    {
      id: '1',
      syllabusId: 3,
      subjectCode: 'CS401',
      subjectName: 'Trí tuệ nhân tạo',
      mainLecturer: 'PGS.TS Lê Văn C',
      assignedBy: 'TS. Nguyễn Văn A (TBM)',
      deadline: '2024-12-22',
      status: 'IN_PROGRESS',
      unreadComments: 2,
    },
    {
      id: '2',
      syllabusId: 6,
      subjectCode: 'CS302',
      subjectName: 'Mạng máy tính',
      mainLecturer: 'PGS. Trần Thị B',
      assignedBy: 'TS. Nguyễn Văn A (TBM)',
      deadline: '2024-12-21',
      status: 'IN_PROGRESS',
      unreadComments: 0,
    },
    {
      id: '3',
      syllabusId: 7,
      subjectCode: 'CS501',
      subjectName: 'Học máy nâng cao',
      mainLecturer: 'TS. Hoàng Văn E',
      assignedBy: 'TS. Nguyễn Văn A (TBM)',
      deadline: '2024-12-25',
      status: 'PENDING',
      unreadComments: 0,
    },
  ];

  const statusLabels: Record<string, string> = {
    PENDING: 'Chưa bắt đầu',
    IN_PROGRESS: 'Đang review',
    COMPLETED: 'Hoàn thành',
  };

  const statusColors: Record<string, string> = {
    PENDING: 'default',
    IN_PROGRESS: 'processing',
    COMPLETED: 'success',
  };

  const handleSelectAssignment = (assignment: ReviewAssignment) => {
    setSelectedAssignment(assignment);
    // In real app: fetch comments for this assignment
  };

  const handleAddComment = () => {
    if (newComment.trim() && selectedAssignment) {
      const comment: Comment = {
        id: String(comments.length + 1),
        author: 'Tôi',
        content: newComment,
        timestamp: new Date().toLocaleString('vi-VN'),
        isMe: true,
      };
      setComments([...comments, comment]);
      setNewComment('');
    }
  };

  const handleViewSyllabus = (syllabusId: number) => {
    navigate(`/lecturer/syllabi/${syllabusId}`);
  };

  return (
    <div style={{ padding: '24px' }}>
      <Title level={2}>Đánh giá Cộng tác</Title>
      <Text type="secondary">
        Danh sách đề cương được phân công review với vai trò giảng viên cộng tác
      </Text>

      <div style={{ display: 'flex', gap: 24, marginTop: 24 }}>
        {/* Left Panel - Review Assignments */}
        <Card
          title={
            <Space>
              <CommentOutlined />
              <span>Nhiệm vụ Review ({reviewAssignments.length})</span>
            </Space>
          }
          style={{ flex: '0 0 400px' }}
        >
          <List
            dataSource={reviewAssignments}
            renderItem={(assignment) => (
              <List.Item
                style={{
                  cursor: 'pointer',
                  background:
                    selectedAssignment?.id === assignment.id ? '#e6f7ff' : 'transparent',
                  padding: '12px',
                  borderRadius: '4px',
                  marginBottom: '8px',
                }}
                onClick={() => handleSelectAssignment(assignment)}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Text strong>
                        {assignment.subjectCode} - {assignment.subjectName}
                      </Text>
                      {assignment.unreadComments > 0 && (
                        <Badge count={assignment.unreadComments} />
                      )}
                    </Space>
                  }
                  description={
                    <Space direction="vertical" size={4} style={{ width: '100%' }}>
                      <Text type="secondary" style={{ fontSize: '12px' }}>
                        GV chính: {assignment.mainLecturer}
                      </Text>
                      <Text type="secondary" style={{ fontSize: '12px' }}>
                        Phân công bởi: {assignment.assignedBy}
                      </Text>
                      <Space>
                        <ClockCircleOutlined style={{ fontSize: '12px' }} />
                        <Text type="secondary" style={{ fontSize: '12px' }}>
                          Deadline: {assignment.deadline}
                        </Text>
                      </Space>
                      <Tag color={statusColors[assignment.status]} style={{ marginTop: 4 }}>
                        {statusLabels[assignment.status]}
                      </Tag>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        </Card>

        {/* Right Panel - Comments Section */}
        <Card
          title={
            selectedAssignment ? (
              <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                <Space>
                  <CommentOutlined />
                  <span>
                    {selectedAssignment.subjectCode} - {selectedAssignment.subjectName}
                  </span>
                </Space>
                <Button
                  type="primary"
                  icon={<EyeOutlined />}
                  onClick={() => handleViewSyllabus(selectedAssignment.syllabusId)}
                >
                  Xem đề cương
                </Button>
              </Space>
            ) : (
              <span>Chọn nhiệm vụ để xem chi tiết</span>
            )
          }
          style={{ flex: 1 }}
        >
          {selectedAssignment ? (
            <>
              <div
                style={{
                  maxHeight: '500px',
                  overflowY: 'auto',
                  marginBottom: 16,
                  padding: '0 8px',
                }}
              >
                <List
                  dataSource={comments}
                  renderItem={(comment) => (
                    <List.Item
                      style={{
                        border: 'none',
                        padding: '12px',
                        background: comment.isMe ? '#f0f5ff' : '#fafafa',
                        borderRadius: '8px',
                        marginBottom: '12px',
                      }}
                    >
                      <List.Item.Meta
                        avatar={<Avatar icon={<UserOutlined />} />}
                        title={
                          <Space>
                            <Text strong>{comment.author}</Text>
                            {comment.isMe && <Tag color="blue">Bạn</Tag>}
                            <Text type="secondary" style={{ fontSize: '12px' }}>
                              {comment.timestamp}
                            </Text>
                          </Space>
                        }
                        description={<Text style={{ whiteSpace: 'pre-wrap' }}>{comment.content}</Text>}
                      />
                    </List.Item>
                  )}
                />
              </div>

              <Divider />

              <Space direction="vertical" style={{ width: '100%' }} size="middle">
                <TextArea
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  placeholder="Nhập góp ý của bạn với vai trò giảng viên cộng tác..."
                  rows={4}
                  onPressEnter={(e) => {
                    if (e.ctrlKey) {
                      handleAddComment();
                    }
                  }}
                />
                <div style={{ textAlign: 'right' }}>
                  <Text type="secondary" style={{ marginRight: 16, fontSize: '12px' }}>
                    Nhấn Ctrl+Enter để gửi nhanh
                  </Text>
                  <Button
                    type="primary"
                    icon={<SendOutlined />}
                    onClick={handleAddComment}
                    disabled={!newComment.trim()}
                  >
                    Gửi góp ý
                  </Button>
                </div>
              </Space>
            </>
          ) : (
            <div
              style={{
                textAlign: 'center',
                padding: '60px 20px',
                color: '#999',
              }}
            >
              <CommentOutlined style={{ fontSize: 48, marginBottom: 16 }} />
              <div>Chọn một nhiệm vụ review bên trái để bắt đầu thảo luận</div>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};

export default CollaborativeReviewPage;
