import React, { useEffect, useState } from 'react';
import { Card, Table, Tag, Button, Space, Typography, Badge } from 'antd';
import { EditOutlined, FormOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { syllabusService } from '@/services/syllabus.service';

const { Title } = Typography;

// Định nghĩa kiểu dữ liệu cho bảng Task
interface TaskData {
  id: string;
  type: string;       
  subjectCode: string;
  subjectName: string;
  deadline: string;   
  status: string;
  action: 'CONTINUE' | 'FIX' | 'UPDATE' | 'VIEW';
}

const LecturerDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [tasks, setTasks] = useState<TaskData[]>([]);

  const mapSyllabusToTask = (syllabus: any): TaskData | null => {
    const status = String(syllabus.status); // Ép kiểu string cho chắc chắn
    
    // --- LOGIC MỚI: Chỉ lấy DRAFT, REJECTED, REVISION ---
    if (status === 'DRAFT') {
      return {
        id: syllabus.id,
        type: 'Soạn đề cương',
        subjectCode: syllabus.subjectCode,
        subjectName: syllabus.subjectNameVi || syllabus.subjectName,
        deadline: '---', 
        status: 'DRAFT',
        action: 'CONTINUE',
      };
    }
    // Support cả code cũ (HOD_REJECTED) và mới (REJECTED)
    if (['REJECTED', 'HOD_REJECTED', 'AA_REJECTED', 'PRINCIPAL_REJECTED'].includes(status)) {
      return {
        id: syllabus.id,
        type: 'Từ chối',
        subjectCode: syllabus.subjectCode,
        subjectName: syllabus.subjectNameVi || syllabus.subjectName,
        deadline: syllabus.updatedAt ? new Date(syllabus.updatedAt).toLocaleDateString('vi-VN') : '---',
        status: 'REJECTED',
        action: 'FIX',
      };
    }
    if (status === 'PENDING_HOD_REVISION') {
      return {
        id: syllabus.id,
        type: 'Yêu cầu cập nhật',
        subjectCode: syllabus.subjectCode,
        subjectName: syllabus.subjectNameVi || syllabus.subjectName,
        deadline: syllabus.updatedAt ? new Date(syllabus.updatedAt).toLocaleDateString('vi-VN') : '---',
        status: 'REVISION',
        action: 'UPDATE',
      };
    }
    return null; // Các task khác (Review, Approved...) sẽ bị ẩn đi
  };

  useEffect(() => {
    const fetchTasks = async () => {
      setLoading(true);
      try {
        // GỌI API THẬT
        const data = await syllabusService.getMySyllabuses();
        
        const actionableTasks = (data || [])
          .map(mapSyllabusToTask)
          .filter((t): t is TaskData => t !== null);

        setTasks(actionableTasks);
      } catch (error) {
        console.error('Lỗi tải dashboard:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchTasks();
  }, []);

  const columns = [
    {
      title: 'Loại task',
      dataIndex: 'type',
      key: 'type',
      render: (text: string) => {
        let color = 'default';
        if (text === 'Soạn đề cương') color = 'blue';
        if (text === 'Từ chối') color = 'red';
        if (text === 'Yêu cầu cập nhật') color = 'orange';
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: 'Mã MH',
      dataIndex: 'subjectCode',
      key: 'subjectCode',
      render: (text: string) => <strong>{text}</strong>,
    },
    {
      title: 'Tên môn học',
      dataIndex: 'subjectName',
      key: 'subjectName',
    },
    {
      title: 'Deadline',
      dataIndex: 'deadline',
      key: 'deadline',
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        if (status === 'DRAFT') return <Tag color="processing">Đang thực hiện</Tag>;
        if (status === 'REJECTED') return <Tag color="error">Bị từ chối</Tag>;
        if (status === 'REVISION') return <Tag color="warning">Cần sửa đổi</Tag>;
        return <Tag>{status}</Tag>;
      },
    },
    {
      title: 'Hành động',
      key: 'action',
      render: (_: any, record: TaskData) => {
        if (record.action === 'CONTINUE') {
          return (
            <Button 
              type="primary" 
              style={{ backgroundColor: '#00897B' }} 
              icon={<FormOutlined />}
              onClick={() => navigate(`/lecturer/syllabi/edit/${record.id}`)}
            >
              Tiếp tục soạn
            </Button>
          );
        }
        if (record.action === 'FIX' || record.action === 'UPDATE') {
          return (
            <Button 
              type="primary" 
              danger 
              icon={<EditOutlined />}
              onClick={() => navigate(`/lecturer/syllabi/edit/${record.id}`)}
            >
              Sửa ngay
            </Button>
          );
        }
        return <Button icon={<EyeOutlined />}>Xem</Button>;
      },
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>
          Nhiệm vụ của tôi <Badge count={tasks.length} style={{ backgroundColor: '#f5222d' }} />
        </Title>
      </div>

      <Card loading={loading} bordered={false} style={{ borderRadius: 8 }}>
        <Table
          columns={columns}
          dataSource={tasks}
          rowKey="id"
          pagination={{ pageSize: 5 }}
          locale={{ emptyText: 'Bạn hiện không có nhiệm vụ nào cần xử lý.' }}
        />
      </Card>
    </div>
  );
};

export default LecturerDashboard;