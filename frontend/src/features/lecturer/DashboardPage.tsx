import React, { useState, useEffect } from 'react';
import { Card, Table, Tag, Button, Space, Select, Badge, Row, Col, Statistic, Typography, App } from 'antd';
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

const { Title } = Typography;
import { useNavigate } from 'react-router-dom';
import RejectionReasonModal from './dashboard/components/RejectionReasonModal';
import { useAuth } from '@/features/auth/AuthContext';
import { syllabusService } from '@/services/syllabus.service';
import { teachingAssignmentService } from '@/services/teaching-assignment.service';
import { collaborationService } from '@/services/collaboration.service';
import { Syllabus, SyllabusStatus } from '@/types';

type TaskType = 'DRAFT' | 'REJECTED' | 'REVIEW' | 'FEEDBACK';
type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'SUBMITTED' | 'COMPLETED' | 'REJECTED';

interface LecturerTask {
  id: string;
  type: TaskType;
  subjectCode: string;
  subjectName: string;
  deadline: string;
  status: TaskStatus;
  syllabusId: string;
  rejectionReason?: string;
  rejectionType?: 'HOD' | 'AA' | 'PRINCIPAL';
  assignedBy?: string;
}

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { message } = App.useApp(); // ✅ Dùng App context thay vì static
  const [selectedTaskType, setSelectedTaskType] = useState<string>('ALL');
  const [loading, setLoading] = useState(false);
  const [allTasks, setAllTasks] = useState<LecturerTask[]>([]);
  const [rejectionModal, setRejectionModal] = useState<{
    open: boolean;
    syllabusId: string;
    syllabusCode: string;
    syllabusName: string;
    reason: string;
    type: 'HOD' | 'AA' | 'PRINCIPAL';
  }>({
    open: false,
    syllabusId: '',
    syllabusCode: '',
    syllabusName: '',
    reason: '',
    type: 'HOD',
  });

  // Fetch real data on component mount
  useEffect(() => {
    if (user) {
      fetchLecturerTasks();
    }
  }, [user]);

  const fetchLecturerTasks = async () => {
    if (!user) return;

    try {
      setLoading(true);
      const tasks: LecturerTask[] = [];

      // 1. Fetch teaching assignments (wrap trong try-catch riêng)
      let assignments: any[] = [];
      try {
        assignments = await teachingAssignmentService.getByLecturerId(user.id);
      } catch (error) {
        console.warn('⚠️ Could not fetch teaching assignments (user may not have any):', error);
        // KHÔNG throw error, tiếp tục load các data khác
      }
      
      // 2. Fetch syllabi where user is the creator (DRAFT, PENDING_HOD, REJECTED, REVISION_IN_PROGRESS)
      const { data: syllabi } = await syllabusService.getSyllabi(
        {
          status: [
            SyllabusStatus.DRAFT,
            SyllabusStatus.PENDING_HOD,
            SyllabusStatus.REJECTED,
            SyllabusStatus.REVISION_IN_PROGRESS,
          ],
        },
        { page: 1, pageSize: 100 }
      );

      // Filter syllabi where current user is the creator
      const mySyllabi = syllabi.filter(s => s.createdBy === user.id);

      // Map syllabi to tasks
      mySyllabi.forEach((syllabus) => {
        let taskType: TaskType = 'DRAFT';
        let taskStatus: TaskStatus = 'IN_PROGRESS';

        if (syllabus.status === SyllabusStatus.DRAFT) {
          taskType = 'DRAFT';
          taskStatus = 'IN_PROGRESS';
        } else if (syllabus.status === SyllabusStatus.PENDING_HOD) {
          taskType = 'DRAFT';
          taskStatus = 'PENDING'; // Chờ TBM duyệt
        } else if (syllabus.status === SyllabusStatus.REJECTED) {
          taskType = 'REJECTED';
          taskStatus = 'REJECTED';
        } else if (syllabus.status === SyllabusStatus.REVISION_IN_PROGRESS) {
          taskType = 'FEEDBACK';
          taskStatus = 'IN_PROGRESS';
        }

        // Find corresponding teaching assignment for deadline
        const assignment = assignments.find(a => a.syllabusId === syllabus.id);

        tasks.push({
          id: syllabus.id,
          type: taskType,
          subjectCode: syllabus.subjectCode,
          subjectName: syllabus.subjectNameVi,
          deadline: assignment?.deadline || syllabus.updatedAt,
          status: taskStatus,
          syllabusId: syllabus.id,
          rejectionReason: undefined, // Will fetch from approval history if needed
          rejectionType: undefined,
        });
      });

      // 3. Fetch syllabi where user is a collaborator (for REVIEW tasks)
      try {
        const collaborations = await collaborationService.getMyCollaborations(user.id);
        
        for (const collab of collaborations) {
          try {
            const syllabus = await syllabusService.getSyllabusById(collab.syllabusVersionId);
            
            // Only show DRAFT syllabi for review
            if (syllabus.status === SyllabusStatus.DRAFT) {
              tasks.push({
                id: syllabus.id,
                type: 'REVIEW',
                subjectCode: syllabus.subjectCode,
                subjectName: syllabus.subjectNameVi,
                deadline: syllabus.updatedAt,
                status: 'PENDING',
                syllabusId: syllabus.id,
                assignedBy: syllabus.ownerName, // Giảng viên chính
              });
            }
          } catch (error) {
            console.error(`Error fetching syllabus for collaboration ${collab.syllabusVersionId}:`, error);
          }
        }
      } catch (error) {
        console.warn('Could not fetch collaborations:', error);
      }

      setAllTasks(tasks);
    } catch (error: any) {
      message.error('Không thể tải dữ liệu: ' + (error.message || 'Lỗi không xác định'));
      console.error('Error fetching lecturer tasks:', error);
    } finally {
      setLoading(false);
    }
  };

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
          <Button type="primary" icon={<EditOutlined />} onClick={() => handleAction(task)} style={{ fontSize: '12px' }}>
            Tiếp tục soạn
          </Button>
        );
      case 'REJECTED':
        return (
          <Button type="primary" danger icon={<EditOutlined />} onClick={() => handleAction(task)} style={{ fontSize: '12px' }}>
            Sửa ngay
          </Button>
        );
      case 'REVIEW':
        return (
          <Button type="default" icon={<CommentOutlined />} onClick={() => handleAction(task)} style={{ fontSize: '12px' }}>
            Xem & Góp ý
          </Button>
        );
      case 'FEEDBACK':
        return (
          <Button type="primary" icon={<EditOutlined />} onClick={() => handleAction(task)} style={{ fontSize: '12px' }}>
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
    draft: allTasks.filter((t) => t.type === 'DRAFT' && t.status === 'IN_PROGRESS').length,
    pending: allTasks.filter((t) => t.status === 'PENDING').length, // Chờ duyệt
    review: allTasks.filter((t) => t.type === 'REVIEW').length,
    rejected: allTasks.filter((t) => t.type === 'REJECTED').length + allTasks.filter((t) => t.type === 'FEEDBACK').length, // Cần sửa
  };

  return (
    <div>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Title level={2} style={{ margin: 0, marginBottom: 24 }}>Dashboard Giảng viên</Title>

        {/* Stats Overview - Responsive Grid */}
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Tổng nhiệm vụ"
                value={stats.total}
                prefix={<FileTextOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Đang soạn"
                value={stats.draft}
                prefix={<ClockCircleOutlined />}
                valueStyle={{ color: '#faad14' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Chờ phê duyệt"
                value={stats.pending}
                prefix={<ClockCircleOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Đang review"
                value={stats.review}
                prefix={<TeamOutlined />}
                valueStyle={{ color: '#13c2c2' }}
              />
            </Card>
          </Col>
        </Row>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
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
            style={{ width: 200, minWidth: 150 }}
          />
        </div>

        <Card>
          <Table
            dataSource={filteredTasks}
            columns={columns}
            rowKey="id"
            loading={loading}
            scroll={{ x: 800 }}
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
