import React, { useState } from 'react';
import { Card, Descriptions, Tag, Button, Space, Input, List, Avatar, Typography, Divider } from 'antd';
import { ArrowLeftOutlined, CommentOutlined, SendOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';

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
  const [comments, setComments] = useState<Comment[]>([
    {
      id: '1',
      author: 'TS. Nguyễn Văn A',
      content: 'CLO 2 cần bổ sung thêm phương pháp đánh giá cụ thể hơn.',
      timestamp: '2024-12-10 14:30',
    },
    {
      id: '2',
      author: 'ThS. Trần Thị B',
      content: 'Tài liệu tham khảo số 3 đã cũ, nên thay bằng phiên bản 2023.',
      timestamp: '2024-12-10 15:45',
    },
  ]);

  // Mock data - in real app, fetch from API based on id
  const syllabus: SyllabusDetail = {
    id: id || '1',
    subjectCode: 'CS401',
    subjectName: 'Trí tuệ nhân tạo',
    credits: 3,
    semester: 'HK2 2024-2025',
    department: 'Công nghệ Thông tin',
    status: 'PENDING_REVIEW',
    version: 'v2.0',
    description:
      'Môn học cung cấp kiến thức cơ bản về trí tuệ nhân tạo, bao gồm các thuật toán tìm kiếm, học máy, xử lý ngôn ngữ tự nhiên và thị giác máy tính.',
    objectives: [
      'Hiểu các khái niệm cơ bản về AI',
      'Áp dụng các thuật toán AI vào bài toán thực tế',
      'Phát triển ứng dụng AI đơn giản',
    ],
    clos: [
      { code: 'CLO1', description: 'Giải thích các khái niệm cơ bản về AI', weight: 20 },
      { code: 'CLO2', description: 'Vận dụng thuật toán tìm kiếm', weight: 30 },
      { code: 'CLO3', description: 'Xây dựng mô hình học máy cơ bản', weight: 30 },
      { code: 'CLO4', description: 'Phát triển ứng dụng AI hoàn chỉnh', weight: 20 },
    ],
    plos: ['PLO1', 'PLO2', 'PLO5', 'PLO6'],
    prerequisites: ['CS301 - Cơ sở dữ liệu', 'CS201 - Cấu trúc dữ liệu'],
    teachingMethod: 'Kết hợp giảng dạy lý thuyết và thực hành',
    assessment: 'Thi cuối kỳ: 50%, Giữa kỳ: 20%, Bài tập: 20%, Dự án: 10%',
    references: [
      'Stuart Russell, Peter Norvig - Artificial Intelligence: A Modern Approach (2020)',
      'Ian Goodfellow - Deep Learning (2016)',
    ],
    mainLecturer: 'PGS.TS Lê Văn C',
    coLecturers: ['ThS. Nguyễn Thị D', 'TS. Trần Văn E'],
  };

  const handleAddComment = () => {
    if (newComment.trim()) {
      const comment: Comment = {
        id: String(comments.length + 1),
        author: 'Tôi', // Current user
        content: newComment,
        timestamp: new Date().toLocaleString('vi-VN'),
      };
      setComments([...comments, comment]);
      setNewComment('');
    }
  };

  const statusColors: Record<string, string> = {
    DRAFT: 'default',
    PENDING_REVIEW: 'processing',
    WAITING_HOD: 'processing',
    PUBLISHED: 'success',
    HOD_REJECTED: 'error',
  };

  const statusLabels: Record<string, string> = {
    DRAFT: 'Bản nháp',
    PENDING_REVIEW: 'Đang review',
    WAITING_HOD: 'Chờ TBM duyệt',
    PUBLISHED: 'Đã xuất bản',
    HOD_REJECTED: 'TBM từ chối',
  };

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
                {syllabus.subjectCode} - {syllabus.subjectName}
              </span>
              <Tag color={statusColors[syllabus.status]}>
                {statusLabels[syllabus.status] || syllabus.status}
              </Tag>
              <Tag color="blue">{syllabus.version}</Tag>
            </Space>
          }
        >
          <Descriptions bordered column={2}>
            <Descriptions.Item label="Số tín chỉ">{syllabus.credits}</Descriptions.Item>
            <Descriptions.Item label="Học kỳ">{syllabus.semester}</Descriptions.Item>
            <Descriptions.Item label="Khoa/Bộ môn" span={2}>
              {syllabus.department}
            </Descriptions.Item>
            <Descriptions.Item label="Giảng viên chính" span={2}>
              {syllabus.mainLecturer}
            </Descriptions.Item>
            <Descriptions.Item label="Giảng viên cộng tác" span={2}>
              {syllabus.coLecturers.join(', ')}
            </Descriptions.Item>
          </Descriptions>
        </Card>

        <Card title="Mô tả môn học">
          <Text>{syllabus.description}</Text>
        </Card>

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

        <Card title="Chuẩn đầu ra môn học (CLO)">
          <List
            dataSource={syllabus.clos}
            renderItem={(clo) => (
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

        <Card title="Mapping với PLO">
          <Space wrap>
            {syllabus.plos.map((plo) => (
              <Tag key={plo} color="green">
                {plo}
              </Tag>
            ))}
          </Space>
        </Card>

        <Card title="Môn học tiên quyết">
          {syllabus.prerequisites.length > 0 ? (
            <List
              dataSource={syllabus.prerequisites}
              renderItem={(item) => <List.Item>{item}</List.Item>}
            />
          ) : (
            <Text type="secondary">Không có</Text>
          )}
        </Card>

        <Card title="Phương pháp giảng dạy">
          <Text>{syllabus.teachingMethod}</Text>
        </Card>

        <Card title="Phương pháp đánh giá">
          <Text>{syllabus.assessment}</Text>
        </Card>

        <Card title="Tài liệu tham khảo">
          <List
            dataSource={syllabus.references}
            renderItem={(item, index) => (
              <List.Item>
                <Text>
                  [{index + 1}] {item}
                </Text>
              </List.Item>
            )}
          />
        </Card>

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
