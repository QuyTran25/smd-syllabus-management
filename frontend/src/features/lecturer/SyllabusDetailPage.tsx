import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Tag, Button, Space, Input, List, Avatar, Typography, Divider, Spin, Timeline, Alert, message } from 'antd';
import { ArrowLeftOutlined, CommentOutlined, SendOutlined, UserOutlined, ClockCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { syllabusService } from '@/services/syllabus.service';
import { Syllabus } from '@/types';
import dayjs from 'dayjs';

const { TextArea } = Input;
const { Text } = Typography;

interface Comment {
  id: string;
  author: string;
  content: string;
  timestamp: string;
  avatar?: string;
}

interface SyllabusDetail {
  id: string;
  subjectCode: string;
  subjectName: string;
  credits: number;
  semester: string;
  department: string;
  status: string;
  version: string;
  description: string;
  objectives: string[];
  clos: Array<{ code: string; description: string; weight: number }>;
  plos: string[];
  prerequisites: string[];
  teachingMethod: string;
  assessment: string;
  references: string[];
  mainLecturer: string;
  coLecturers: string[];
}

const SyllabusDetailPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(true);
  const [syllabus, setSyllabus] = useState<Syllabus | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);

  // Fetch syllabus data from API
  useEffect(() => {
    const fetchSyllabus = async () => {
      if (!id) return;
      
      setLoading(true);
      try {
        const data = await syllabusService.getSyllabusById(id);
        setSyllabus(data);
        
        // Fetch comments if available
        try {
          const commentsData = await syllabusService.getComments(id);
          setComments(commentsData.map((c: any) => ({
            id: c.id,
            author: c.authorName || 'Unknown',
            content: c.content,
            timestamp: dayjs(c.createdAt).format('DD/MM/YYYY HH:mm'),
          })));
        } catch (err) {
          console.log('No comments available');
        }
      } catch (error) {
        console.error('Error fetching syllabus:', error);
        message.error('Không thể tải thông tin đề cương');
      } finally {
        setLoading(false);
      }
    };

    fetchSyllabus();
  }, [id]);

  const handleAddComment = async () => {
    if (newComment.trim() && id && syllabus) {
      try {
        // Backend expects syllabusVersionId, not syllabusId
        const response = await syllabusService.addComment({
          syllabusVersionId: id, // Send syllabus ID as syllabusVersionId
          content: newComment,
        } as any);
        
        const comment: Comment = {
          id: response?.id || String(comments.length + 1),
          author: response?.userName || 'Tôi',
          content: newComment,
          timestamp: dayjs().format('DD/MM/YYYY HH:mm'),
        };
        setComments([...comments, comment]);
        setNewComment('');
        message.success('Đã gửi góp ý');
      } catch (error: any) {
        console.error('Error adding comment:', error);
        const errorMsg = error?.response?.data?.message || 'Lỗi khi gửi góp ý';
        message.error(errorMsg);
      }
    }
  };

  const statusColors: Record<string, string> = {
    DRAFT: 'default',
    PENDING_HOD: 'processing',
    PENDING_HOD_REVISION: 'warning',
    PENDING_AA: 'processing',
    PENDING_PRINCIPAL: 'processing',
    APPROVED: 'success',
    PUBLISHED: 'success',
    REJECTED: 'error',
    REVISION_IN_PROGRESS: 'orange',
  };

  const statusLabels: Record<string, string> = {
    DRAFT: 'Bản nháp',
    PENDING_HOD: 'Chờ Trưởng BM duyệt',
    PENDING_HOD_REVISION: 'Chờ TBM (Sửa lỗi)',
    PENDING_AA: 'Chờ Phòng ĐT duyệt',
    PENDING_PRINCIPAL: 'Chờ Hiệu trưởng duyệt',
    APPROVED: 'Đã phê duyệt',
    PUBLISHED: 'Đã xuất bản',
    REJECTED: 'Bị từ chối',
    REVISION_IN_PROGRESS: 'Đang chỉnh sửa',
  };

  const renderApprovalTimeline = () => {
    if (!syllabus) return null;

    const events = [];

    if (syllabus.createdAt) {
      events.push({
        color: 'blue',
        icon: <ClockCircleOutlined />,
        children: (
          <Space direction="vertical" size={0}>
            <Text strong>Tạo đề cương</Text>
            <Text type="secondary">{dayjs(syllabus.createdAt).format('DD/MM/YYYY HH:mm')}</Text>
            <Text type="secondary">Bởi: {syllabus.ownerName}</Text>
          </Space>
        ),
      });
    }

    if (syllabus.submittedAt) {
      events.push({
        color: 'cyan',
        icon: <CheckCircleOutlined />,
        children: (
          <Space direction="vertical" size={0}>
            <Text strong>Gửi phê duyệt</Text>
            <Text type="secondary">{dayjs(syllabus.submittedAt).format('DD/MM/YYYY HH:mm')}</Text>
          </Space>
        ),
      });
    }

    if (syllabus.hodApprovedAt) {
      events.push({
        color: 'green',
        icon: <CheckCircleOutlined />,
        children: (
          <Space direction="vertical" size={0}>
            <Text strong>Trưởng Bộ môn duyệt</Text>
            <Text type="secondary">{dayjs(syllabus.hodApprovedAt).format('DD/MM/YYYY HH:mm')}</Text>
            <Text type="secondary">Bởi: {syllabus.hodApprovedBy}</Text>
          </Space>
        ),
      });
    }

    if (syllabus.aaApprovedAt) {
      events.push({
        color: 'green',
        icon: <CheckCircleOutlined />,
        children: (
          <Space direction="vertical" size={0}>
            <Text strong>Phòng Đào tạo duyệt</Text>
            <Text type="secondary">{dayjs(syllabus.aaApprovedAt).format('DD/MM/YYYY HH:mm')}</Text>
            <Text type="secondary">Bởi: {syllabus.aaApprovedBy}</Text>
          </Space>
        ),
      });
    }

    if (syllabus.principalApprovedAt) {
      events.push({
        color: 'green',
        icon: <CheckCircleOutlined />,
        children: (
          <Space direction="vertical" size={0}>
            <Text strong>Hiệu trưởng duyệt</Text>
            <Text type="secondary">{dayjs(syllabus.principalApprovedAt).format('DD/MM/YYYY HH:mm')}</Text>
            <Text type="secondary">Bởi: {syllabus.principalApprovedBy}</Text>
          </Space>
        ),
      });
    }

    if (syllabus.publishedAt) {
      events.push({
        color: 'purple',
        icon: <CheckCircleOutlined />,
        children: (
          <Space direction="vertical" size={0}>
            <Text strong>Xuất hành</Text>
            <Text type="secondary">{dayjs(syllabus.publishedAt).format('DD/MM/YYYY HH:mm')}</Text>
          </Space>
        ),
      });
    }

    return events.length > 0 ? <Timeline items={events} /> : <Text type="secondary">Chưa có lịch sử phê duyệt</Text>;
  };

  if (loading) {
    return (
      <div style={{ padding: '24px', textAlign: 'center' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!syllabus) {
    return (
      <div style={{ padding: '24px' }}>
        <Alert message="Không tìm thấy đề cương" type="error" />
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
                {statusLabels[syllabus.status] || syllabus.status}
              </Tag>
              <Tag color="blue">v{syllabus.version}</Tag>
            </Space>
          }
        >
          <Descriptions bordered column={2}>
            <Descriptions.Item label="Số tín chỉ">{syllabus.creditCount}</Descriptions.Item>
            <Descriptions.Item label="Học kỳ">{syllabus.semester}</Descriptions.Item>
            <Descriptions.Item label="Khoa" span={2}>
              {syllabus.faculty}
            </Descriptions.Item>
            <Descriptions.Item label="Bộ môn" span={2}>
              {syllabus.department}
            </Descriptions.Item>
            <Descriptions.Item label="Giảng viên" span={2}>
              {syllabus.ownerName}
            </Descriptions.Item>
          </Descriptions>
        </Card>

        {/* Approval Timeline */}
        {syllabus.submittedAt && (
          <Card title={<Space><ClockCircleOutlined /> Lịch sử phê duyệt</Space>}>
            {renderApprovalTimeline()}
          </Card>
        )}

        <Card title="Mô tả môn học">
          <Text>{(syllabus as any).description || 'Chưa có mô tả'}</Text>
        </Card>

        {(syllabus as any).clos && (syllabus as any).clos.length > 0 && (
          <Card title="Chuẩn đầu ra môn học (CLO)">
            <List
              dataSource={(syllabus as any).clos}
              renderItem={(clo: any) => (
                <List.Item>
                  <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                    <Text strong>{clo.code}:</Text>
                    <Text style={{ flex: 1 }}>{clo.description}</Text>
                    <Tag color="blue">{clo.weight}%</Tag>
                  </Space>
                </List.Item>
              )}
            />
          </Card>
        )}

        {(syllabus as any).prerequisites && (syllabus as any).prerequisites.length > 0 && (
          <Card title="Môn học tiên quyết">
            <List
              dataSource={(syllabus as any).prerequisites}
              renderItem={(item: any) => <List.Item>{item}</List.Item>}
            />
          </Card>
        )}

        {(syllabus as any).references && (syllabus as any).references.length > 0 && (
          <Card title="Tài liệu tham khảo">
            <List
              dataSource={(syllabus as any).references}
              renderItem={(item: any, index: number) => (
                <List.Item>
                  <Text>
                    [{index + 1}] {item}
                  </Text>
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
        >
          <List
            dataSource={comments}
            renderItem={(comment) => (
              <List.Item>
                <List.Item.Meta
                  avatar={<Avatar icon={<UserOutlined />} />}
                  title={
                    <Space>
                      <Text strong>{comment.author}</Text>
                      <Text type="secondary" style={{ fontSize: '12px' }}>
                        {comment.timestamp}
                      </Text>
                    </Space>
                  }
                  description={comment.content}
                />
              </List.Item>
            )}
          />

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
