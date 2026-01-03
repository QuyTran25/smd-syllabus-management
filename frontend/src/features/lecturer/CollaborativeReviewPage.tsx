import React, { useState, useEffect } from 'react';
import { Card, List, Tag, Space, Button, Input, Avatar, Typography, Divider, Badge, message } from 'antd';
import {
  CommentOutlined,
  SendOutlined,
  UserOutlined,
  EyeOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/AuthContext';
import { syllabusService } from '@/services/syllabus.service';
import { collaborationService } from '@/services/collaboration.service';
import { SyllabusStatus } from '@/types';

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
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [reviewAssignments, setReviewAssignments] = useState<ReviewAssignment[]>([]);
  const [selectedAssignment, setSelectedAssignment] = useState<ReviewAssignment | null>(null);
  const [newComment, setNewComment] = useState('');
  const [comments, setComments] = useState<Comment[]>([]);

  // Fetch review assignments on mount
  useEffect(() => {
    if (user) {
      fetchReviewAssignments();
    }
  }, [user]);

  // Fetch comments when assignment changes
  useEffect(() => {
    if (selectedAssignment) {
      fetchComments(selectedAssignment.id);
    }
  }, [selectedAssignment]);

  const fetchReviewAssignments = async () => {
    if (!user) return;

    try {
      setLoading(true);
      
      // Use new efficient API to get collaborations by user
      const collaborations = await collaborationService.getMyCollaborations(user.id);

      // For each collaboration, fetch syllabus details
      const assignments: ReviewAssignment[] = [];
      
      for (const collab of collaborations) {
        try {
          const syllabus = await syllabusService.getSyllabusById(collab.syllabusVersionId);
          
          // Only show DRAFT syllabi (collaborators can only review DRAFT)
          if (syllabus.status === 'DRAFT') {
            assignments.push({
              id: collab.syllabusVersionId, // Use UUID from collaboration
              subjectCode: syllabus.subjectCode,
              subjectName: syllabus.subjectNameVi,
              mainLecturer: syllabus.ownerName,
              assignedBy: syllabus.ownerName || 'N/A', // Giảng viên chính
              deadline: new Date(syllabus.updatedAt).toLocaleString('vi-VN'),
              status: 'IN_PROGRESS',
              unreadComments: 0, // TODO: Implement unread count
            });
          }
        } catch (error) {
          console.error(`Error fetching syllabus ${collab.syllabusVersionId}:`, error);
        }
      }

      setReviewAssignments(assignments);
      
      // Auto-select first assignment if available
      if (assignments.length > 0 && !selectedAssignment) {
        setSelectedAssignment(assignments[0]);
      }
    } catch (error: any) {
      message.error('Không thể tải danh sách review: ' + (error.message || 'Lỗi không xác định'));
      console.error('Error fetching review assignments:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchComments = async (syllabusVersionId: string) => {
    try {
      const commentsData = await syllabusService.getComments(syllabusVersionId);
      
      // Map to local Comment interface
      const mappedComments: Comment[] = commentsData.map(c => ({
        id: c.id || String(Math.random()),
        author: c.reviewerName || 'Unknown',
        content: c.content,
        timestamp: new Date(c.createdAt).toLocaleString('vi-VN'),
        isMe: c.reviewerId === user?.id,
      }));
      
      setComments(mappedComments);
    } catch (error: any) {
      console.error('Error fetching comments:', error);
      // Don't show error to user, just log it
    }
  };

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
  };

  const handleAddComment = async () => {
    if (newComment.trim() && selectedAssignment) {
      try {
        await syllabusService.addComment(selectedAssignment.id, newComment);
        
        const comment: Comment = {
          id: String(comments.length + 1),
          author: 'Tôi',
          content: newComment,
          timestamp: new Date().toLocaleString('vi-VN'),
          isMe: true,
        };
        setComments([...comments, comment]);
        setNewComment('');
        message.success('Đã gửi góp ý thành công');
      } catch (error: any) {
        message.error('Không thể gửi góp ý: ' + (error.message || 'Lỗi không xác định'));
      }
    }
  };

  const handleViewSyllabus = (syllabusId: string) => {
    navigate(`/lecturer/syllabi/${syllabusId}`);
  };

  return (
    <div style={{ padding: '24px' }}>
      <Title level={2} style={{ margin: 0, marginBottom: 8 }}>Đánh giá Cộng tác</Title>
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
                  onClick={() => handleViewSyllabus(selectedAssignment.id)}
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
