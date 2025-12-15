import React, { useState } from 'react';
import { Card, Table, Tag, Button, Space, Select, Badge, Row, Col, Statistic } from 'antd';
import {
  EditOutlined,
  EyeOutlined,
  InfoCircleOutlined,
  CommentOutlined,
  WarningOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  TeamOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import RejectionReasonModal from './dashboard/components/RejectionReasonModal';

type TaskType = 'DRAFT' | 'REJECTED' | 'REVIEW' | 'FEEDBACK';
type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'SUBMITTED' | 'COMPLETED' | 'REJECTED';

interface LecturerTask {
  id: string;
  type: TaskType;
  subjectCode: string;
  subjectName: string;
  deadline: string;
  status: TaskStatus;
  syllabusId: number;
  rejectionReason?: string;
  rejectionType?: 'HOD' | 'AA' | 'PRINCIPAL';
  assignedBy?: string;
}

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [selectedTaskType, setSelectedTaskType] = useState<string>('ALL');
  const [rejectionModal, setRejectionModal] = useState<{
    open: boolean;
    syllabusId: number;
    syllabusCode: string;
    syllabusName: string;
    reason: string;
    type: 'HOD' | 'AA' | 'PRINCIPAL';
  }>({
    open: false,
    syllabusId: 0,
    syllabusCode: '',
    syllabusName: '',
    reason: '',
    type: 'HOD',
  });

  // Mock data: Tất cả nhiệm vụ của giảng viên
  const allTasks: LecturerTask[] = [
    {
      id: '1',
      type: 'DRAFT',
      subjectCode: 'CS101',
      subjectName: 'Lập trình căn bản',
      deadline: '2024-12-20',
      status: 'IN_PROGRESS',
      syllabusId: 1,
    },
    {
      id: '2',
      type: 'REJECTED',
      subjectCode: 'CS301',
      subjectName: 'Cơ sở dữ liệu',
      deadline: '2024-12-18',
      status: 'REJECTED',
      syllabusId: 2,
      rejectionReason:
        'CLO số 3 không rõ ràng về phương pháp đánh giá. Vui lòng bổ sung thang điểm chi tiết và phân bổ % điểm cho từng hoạt động đánh giá.',
      rejectionType: 'HOD',
    },
    {
      id: '3',
      type: 'REVIEW',
      subjectCode: 'CS401',
      subjectName: 'Trí tuệ nhân tạo',
      deadline: '2024-12-22',
      status: 'PENDING',
      syllabusId: 3,
      assignedBy: 'TS. Nguyễn Văn A',
    },
    {
      id: '4',
      type: 'FEEDBACK',
      subjectCode: 'CS201',
      subjectName: 'Cấu trúc dữ liệu',
      deadline: '2024-12-25',
      status: 'PENDING',
      syllabusId: 4,
    },
    {
      id: '5',
      type: 'REJECTED',
      subjectCode: 'CS501',
      subjectName: 'Học máy',
      deadline: '2024-12-19',
      status: 'REJECTED',
      syllabusId: 5,
      rejectionReason:
        'Chưa có mapping PLO cho CLO 4 và CLO 5. Tài liệu tham khảo cần bổ sung ít nhất 2 tài liệu tiếng Anh xuất bản sau năm 2020.',
      rejectionType: 'AA',
    },
    {
      id: '6',
      type: 'REVIEW',
      subjectCode: 'CS302',
      subjectName: 'Mạng máy tính',
      deadline: '2024-12-21',
      status: 'IN_PROGRESS',
      syllabusId: 6,
      assignedBy: 'PGS. Trần Thị B',
    },
  ];

  const filteredTasks =
    selectedTaskType === 'ALL'
      ? allTasks
      : allTasks.filter((task) => task.type === selectedTaskType);

  const taskTypeLabels: Record<TaskType, string> = {
    DRAFT: 'Soạn đề cương',
    REJECTED: 'Từ chối',
    REVIEW: 'Review đề cương',
    FEEDBACK: 'Yêu cầu cập nhật',
  };

  const taskTypeColors: Record<TaskType, string> = {
    DRAFT: 'blue',
    REJECTED: 'red',
    REVIEW: 'orange',
    FEEDBACK: 'gold',
  };

  const taskStatusLabels: Record<TaskStatus, string> = {
    PENDING: 'Chưa bắt đầu',
    IN_PROGRESS: 'Đang thực hiện',
    SUBMITTED: 'Đã gửi',
    COMPLETED: 'Hoàn thành',
    REJECTED: 'Bị từ chối',
  };

  const taskStatusColors: Record<TaskStatus, string> = {
    PENDING: 'default',
    IN_PROGRESS: 'processing',
    SUBMITTED: 'success',
    COMPLETED: 'success',
    REJECTED: 'error',
  };

  const handleViewRejectionReason = (task: LecturerTask) => {
    if (task.rejectionReason && task.rejectionType) {
      setRejectionModal({
        open: true,
        syllabusId: task.syllabusId,
        syllabusCode: task.subjectCode,
        syllabusName: task.subjectName,
        reason: task.rejectionReason,
        type: task.rejectionType,
      });
    }
  };

  const handleAction = (task: LecturerTask) => {
    switch (task.type) {
      case 'DRAFT':
        navigate(`/lecturer/syllabi/edit/${task.syllabusId}`);
        break;
      case 'REJECTED':
        navigate(`/lecturer/syllabi/edit/${task.syllabusId}`);
        break;
      case 'REVIEW':
        navigate(`/lecturer/syllabi/${task.syllabusId}`);
        break;
      case 'FEEDBACK':
        navigate(`/lecturer/syllabi/edit/${task.syllabusId}`);
        break;
    }
  };

  const getActionButton = (task: LecturerTask) => {
    switch (task.type) {
      case 'DRAFT':
        return (
          <Button type="primary" icon={<EditOutlined />} onClick={() => handleAction(task)}>
            Tiếp tục soạn
          </Button>
        );
      case 'REJECTED':
        return (
          <Button type="primary" danger icon={<EditOutlined />} onClick={() => handleAction(task)}>
            Sửa ngay
          </Button>
        );
      case 'REVIEW':
        return (
          <Button type="default" icon={<CommentOutlined />} onClick={() => handleAction(task)}>
            Xem & Góp ý
          </Button>
        );
      case 'FEEDBACK':
        return (
          <Button type="primary" icon={<EditOutlined />} onClick={() => handleAction(task)}>
            Cập nhật
          </Button>
        );
    }
  };

  const columns = [
    {
      title: 'Loại task',
      dataIndex: 'type',
      width: 140,
      render: (type: TaskType) => (
        <Tag color={taskTypeColors[type]}>{taskTypeLabels[type]}</Tag>
      ),
    },
    {
      title: 'Mã MH',
      dataIndex: 'subjectCode',
      width: 100,
    },
    {
      title: 'Tên môn học',
      dataIndex: 'subjectName',
      ellipsis: true,
    },
    {
      title: 'Deadline',
      dataIndex: 'deadline',
      width: 120,
      sorter: (a: LecturerTask, b: LecturerTask) =>
        new Date(a.deadline).getTime() - new Date(b.deadline).getTime(),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 160,
      render: (status: TaskStatus, record: LecturerTask) => (
        <Space>
          <Tag color={taskStatusColors[status]}>{taskStatusLabels[status]}</Tag>
          {record.type === 'REJECTED' && record.rejectionReason && (
            <InfoCircleOutlined
              style={{ color: '#ff4d4f', cursor: 'pointer', fontSize: 16 }}
              onClick={() => handleViewRejectionReason(record)}
            />
          )}
          {record.type === 'FEEDBACK' && (
            <WarningOutlined style={{ color: '#faad14', fontSize: 16 }} />
          )}
        </Space>
      ),
    },
    {
      title: 'Hành động',
      key: 'action',
      width: 160,
      render: (_: any, record: LecturerTask) => getActionButton(record),
    },
  ];

  const taskTypeOptions = [
    { label: 'Tất cả', value: 'ALL' },
    { label: 'Soạn đề cương', value: 'DRAFT' },
    { label: 'Từ chối', value: 'REJECTED' },
    { label: 'Review', value: 'REVIEW' },
    { label: 'Yêu cầu cập nhật', value: 'FEEDBACK' },
  ];

  const pendingCount = allTasks.filter((t) => t.status === 'PENDING').length;

  // Calculate stats
  const stats = {
    total: allTasks.length,
    draft: allTasks.filter((t) => t.type === 'DRAFT').length,
    review: allTasks.filter((t) => t.type === 'REVIEW').length,
    rejected: allTasks.filter((t) => t.type === 'REJECTED').length,
  };

  return (
    <div style={{ padding: '24px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <h1 style={{ margin: 0 }}>Dashboard Giảng viên</h1>

        {/* Stats Overview */}
        <Row gutter={16}>
          <Col span={6}>
            <Card>
              <Statistic
                title="Tổng nhiệm vụ"
                value={stats.total}
                prefix={<FileTextOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Đang soạn"
                value={stats.draft}
                prefix={<ClockCircleOutlined />}
                valueStyle={{ color: '#faad14' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Đang review"
                value={stats.review}
                prefix={<TeamOutlined />}
                valueStyle={{ color: '#13c2c2' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Cần sửa"
                value={stats.rejected}
                prefix={<ExclamationCircleOutlined />}
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Card>
          </Col>
        </Row>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ margin: 0 }}>
            Nhiệm vụ của tôi{' '}
            {pendingCount > 0 && (
              <Badge count={pendingCount} style={{ backgroundColor: '#faad14' }} />
            )}
          </h2>
          <Select
            value={selectedTaskType}
            onChange={setSelectedTaskType}
            options={taskTypeOptions}
            style={{ width: 200 }}
          />
        </div>

        <Card>
          <Table
            dataSource={filteredTasks}
            columns={columns}
            rowKey="id"
            pagination={{
              pageSize: 10,
              showTotal: (total) => `Tổng ${total} nhiệm vụ`,
            }}
          />
        </Card>
      </Space>

      <RejectionReasonModal
        open={rejectionModal.open}
        onClose={() => setRejectionModal({ ...rejectionModal, open: false })}
        syllabusId={rejectionModal.syllabusId}
        syllabusCode={rejectionModal.syllabusCode}
        syllabusName={rejectionModal.syllabusName}
        rejectionReason={rejectionModal.reason}
        rejectionType={rejectionModal.type}
      />
    </div>
  );
};

export default DashboardPage;
