import React, { useState } from 'react';
import { Card, Descriptions, Tag, Button, Space, Input, List, Avatar, Typography, Divider, Spin, message } from 'antd';
import { ArrowLeftOutlined, CommentOutlined, SendOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { syllabusService } from '@/services';
import { SyllabusStatus } from '@/types';

const { TextArea } = Input;
const { Text } = Typography;

const SyllabusDetailPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [newComment, setNewComment] = useState('');
  const queryClient = useQueryClient();

  // Fetch syllabus detail from backend
  const { data: syllabus, isLoading: isSyllabusLoading } = useQuery({
    queryKey: ['syllabus', id],
    queryFn: () => syllabusService.getSyllabusById(id!),
    enabled: !!id,
  });

  // Fetch comments from backend
  const { data: comments = [], isLoading: isCommentsLoading } = useQuery({
    queryKey: ['syllabus-comments', id],
    queryFn: () => syllabusService.getComments(id!),
    enabled: !!id,
  });

  // Add comment mutation
  const addCommentMutation = useMutation({
    mutationFn: (content: string) => syllabusService.addComment(id!, content),
    onSuccess: () => {
      message.success('Góp ý đã được gửi');
      queryClient.invalidateQueries({ queryKey: ['syllabus-comments', id] });
      setNewComment('');
    },
    onError: () => {
      message.error('Gửi góp ý thất bại');
    },
  });

  const handleAddComment = () => {
    if (newComment.trim()) {
      addCommentMutation.mutate(newComment);
      setNewComment('');
    }
  };

  const statusColors: Record<SyllabusStatus, string> = {
    [SyllabusStatus.DRAFT]: 'default',
    [SyllabusStatus.PENDING_HOD]: 'processing',
    [SyllabusStatus.PENDING_AA]: 'processing',
    [SyllabusStatus.PENDING_PRINCIPAL]: 'processing',
    [SyllabusStatus.APPROVED]: 'cyan',
    [SyllabusStatus.PUBLISHED]: 'success',
    [SyllabusStatus.REJECTED]: 'error',
    [SyllabusStatus.REVISION_IN_PROGRESS]: 'warning',
  };

  const statusLabels: Record<SyllabusStatus, string> = {
    [SyllabusStatus.DRAFT]: 'Bản nháp',
    [SyllabusStatus.PENDING_HOD]: 'Chờ TBM duyệt',
    [SyllabusStatus.PENDING_AA]: 'Chờ PĐT duyệt',
    [SyllabusStatus.PENDING_PRINCIPAL]: 'Chờ Hiệu trưởng',
    [SyllabusStatus.APPROVED]: 'Đã duyệt',
    [SyllabusStatus.PUBLISHED]: 'Đã xuất bản',
    [SyllabusStatus.REJECTED]: 'Bị từ chối',
    [SyllabusStatus.REVISION_IN_PROGRESS]: 'Đang chỉnh sửa',
  };

  if (isSyllabusLoading) {
    return (
      <div style={{ padding: '24px', textAlign: 'center' }}>
        <Spin size="large" tip="Đang tải đề cương..." />
      </div>
    );
  }

  if (!syllabus) {
    return (
      <div style={{ padding: '24px', textAlign: 'center' }}>
        <Text type="secondary">Không tìm thấy đề cương</Text>
      </div>
    );
  }

  return (
    <div style={{ padding: '24px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>
          Quay lại
        </Button>

        <Card
          title={
            <Space>
              <span>
                {syllabus.subjectCode} - {syllabus.subjectNameVi}
              </span>
              <Tag color={statusColors[syllabus.status]}>
                {statusLabels[syllabus.status]}
              </Tag>
              <Tag color="blue">v{syllabus.version}</Tag>
            </Space>
          }
        >
          <Descriptions bordered column={2}>
            <Descriptions.Item label="Số tín chỉ">{syllabus.creditCount}</Descriptions.Item>
            <Descriptions.Item label="Học kỳ">{syllabus.semester}</Descriptions.Item>
            <Descriptions.Item label="Khoa/Bộ môn" span={2}>
              {syllabus.department}
            </Descriptions.Item>
            <Descriptions.Item label="Giảng viên chính" span={2}>
              {syllabus.createdBy || 'N/A'}
            </Descriptions.Item>
          </Descriptions>
        </Card>

        <Card title="Mô tả môn học">
          <Text>{syllabus.description}</Text>
        </Card>

        {syllabus.objectives && syllabus.objectives.length > 0 && (
          <Card title="Mục tiêu môn học">
            <List
              dataSource={syllabus.objectives}
              renderItem={(item, index) => (
                <List.Item>
                  <Text>
                    {index + 1}. {item}
                  </Text>
                </List.Item>
              )}
            />
          </Card>
        )}

        {syllabus.clos && syllabus.clos.length > 0 && (
          <Card title="Chuẩn đầu ra môn học (CLO)">
            <List
              dataSource={syllabus.clos}
              renderItem={(clo) => (
                <List.Item>
                  <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                    <Text strong>{clo.code}:</Text>
                    <Text style={{ flex: 1 }}>{clo.description}</Text>
                  </Space>
                </List.Item>
              )}
            />
          </Card>
        )}

        {syllabus.ploMappings && syllabus.ploMappings.length > 0 && (
          <Card title="Mapping với PLO">
            <Space wrap>
              {syllabus.ploMappings.map((mapping, idx) => (
                <Tag key={idx} color="green">
                  {mapping.ploCode}
                </Tag>
              ))}
            </Space>
          </Card>
        )}

        {syllabus.prerequisites && syllabus.prerequisites.length > 0 && (
          <Card title="Môn học tiên quyết">
            <List
              dataSource={syllabus.prerequisites}
              renderItem={(item) => (
                <List.Item>
                  {item.courseCode} - {item.courseName}
                </List.Item>
              )}
            />
          </Card>
        )}

        {/* Comment Section for Collaborative Review */}
        <Card
          title={
            <Space>
              <CommentOutlined />
              <span>Góp ý & Thảo luận ({comments.length})</span>
            </Space>
          }
          loading={isCommentsLoading}
        >
          {comments.length > 0 ? (
            <List
              dataSource={comments}
              renderItem={(comment) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={<Avatar icon={<UserOutlined />} />}
                    title={
                      <Space>
                        <Text strong>{comment.createdByName}</Text>
                        <Text type="secondary" style={{ fontSize: '12px' }}>
                          {new Date(comment.createdAt).toLocaleString('vi-VN')}
                        </Text>
                      </Space>
                    }
                    description={
                      <div>
                        {comment.section && (
                          <Tag color="blue" style={{ marginBottom: 8 }}>
                            {comment.section}
                          </Tag>
                        )}
                        <div>{comment.content}</div>
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          ) : (
            <Text type="secondary">Chưa có góp ý nào</Text>
          )}

          <Divider />

          <Space.Compact style={{ width: '100%' }}>
            <TextArea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Nhập góp ý của bạn..."
              rows={3}
              style={{ flex: 1 }}
              onPressEnter={(e) => {
                if (e.ctrlKey) {
                  handleAddComment();
                }
              }}
            />
          </Space.Compact>
          <div style={{ marginTop: 8, textAlign: 'right' }}>
            <Button
              type="primary"
              icon={<SendOutlined />}
              onClick={handleAddComment}
              disabled={!newComment.trim()}
              loading={addCommentMutation.isPending}
            >
              Gửi góp ý
            </Button>
          </div>
        </Card>
      </Space>
    </div>
  );
};

export default SyllabusDetailPage;
