import React, { useState, useMemo } from 'react';
import {
  Card,
  Table,
  Space,
  Tag,
  Alert,
  Select,
  Descriptions,
  Modal,
  Typography,
} from 'antd';
import { EyeOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import { subjectService, Subject } from '../../services/subject.service';

const { Option } = Select;
const { Text } = Typography;

// Interface for display (mapped from API)
interface CourseDisplay {
  id: string;
  code: string;
  name: string;
  credits: number;
  departmentName?: string;
  subjectType?: string;
  component?: string;
  theoryHours: number;
  practiceHours: number;
  selfStudyHours: number;
  isActive: boolean;
}

export const CourseManagementPage: React.FC = () => {
  const [departmentFilter, setDepartmentFilter] = useState<string | undefined>(undefined);
  const [selectedCourse, setSelectedCourse] = useState<CourseDisplay | null>(null);
  const [isDetailModalVisible, setIsDetailModalVisible] = useState(false);

  // Fetch courses from API
  const { data: subjectsRaw, isLoading, error } = useQuery({
    queryKey: ['subjects'],
    queryFn: () => subjectService.getAllSubjects(),
  });

  // Map API response to display format
  const courses: CourseDisplay[] = useMemo(() => {
    if (!subjectsRaw) return [];
    return subjectsRaw.map((s: Subject) => ({
      id: s.id,
      code: s.code,
      name: s.currentNameVi,
      credits: s.defaultCredits,
      departmentName: s.departmentName,
      subjectType: s.subjectType,
      component: s.component,
      theoryHours: s.defaultTheoryHours,
      practiceHours: s.defaultPracticeHours,
      selfStudyHours: s.defaultSelfStudyHours,
      isActive: s.isActive,
    }));
  }, [subjectsRaw]);

  // Extract unique departments for filter
  const departments = useMemo(() => {
    const unique = new Set<string>();
    courses.forEach((c) => {
      if (c.departmentName) unique.add(c.departmentName);
    });
    return Array.from(unique);
  }, [courses]);

  // Filter courses by department
  const filteredCourses = departmentFilter
    ? courses.filter((c) => c.departmentName === departmentFilter)
    : courses;

  const courseColumns: ColumnsType<CourseDisplay> = [
    {
      title: 'Mã môn',
      dataIndex: 'code',
      key: 'code',
      width: 120,
      fixed: 'left',
      sorter: (a, b) => a.code.localeCompare(b.code),
    },
    {
      title: 'Tên môn học',
      dataIndex: 'name',
      key: 'name',
      width: 280,
    },
    {
      title: 'Tín chỉ',
      dataIndex: 'credits',
      key: 'credits',
      width: 80,
      align: 'center',
      sorter: (a, b) => a.credits - b.credits,
    },
    {
      title: 'Loại',
      dataIndex: 'subjectType',
      key: 'subjectType',
      width: 140,
      render: (type) => {
        if (!type) return <span style={{ color: '#999' }}>-</span>;
        const config: Record<string, { color: string; text: string }> = {
          REQUIRED: { color: 'red', text: 'Bắt buộc' },
          ELECTIVE: { color: 'blue', text: 'Tự chọn' },
          FREE_ELECTIVE: { color: 'green', text: 'Tự chọn tự do' },
        };
        const cfg = config[type] || { color: 'default', text: type };
        return <Tag color={cfg.color}>{cfg.text}</Tag>;
      },
    },
    {
      title: 'Thành phần',
      dataIndex: 'component',
      key: 'component',
      width: 130,
      render: (type) => {
        if (!type) return <span style={{ color: '#999' }}>-</span>;
        const config: Record<string, string> = {
          MAJOR: 'Chuyên ngành',
          FOUNDATION: 'Cơ sở ngành',
          GENERAL: 'Đại cương',
          THESIS: 'Khóa luận',
        };
        return config[type] || type;
      },
    },
    {
      title: 'Bộ môn',
      dataIndex: 'departmentName',
      key: 'departmentName',
      width: 200,
      render: (text) => text || <span style={{ color: '#999' }}>-</span>,
    },
    {
      title: 'Số tiết',
      key: 'hours',
      width: 180,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text type="secondary" style={{ fontSize: 12 }}>
            LT: {record.theoryHours} | TH: {record.practiceHours} | TH: {record.selfStudyHours}
          </Text>
        </Space>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 100,
      render: (isActive) => (
        <Tag color={isActive ? 'green' : 'default'}>
          {isActive ? 'Hoạt động' : 'Ẩn'}
        </Tag>
      ),
    },
    {
      title: 'Chi tiết',
      key: 'actions',
      width: 100,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <EyeOutlined
            style={{ cursor: 'pointer', color: '#1890ff', fontSize: 16 }}
            onClick={() => {
              setSelectedCourse(record);
              setIsDetailModalVisible(true);
            }}
          />
        </Space>
      ),
    },
  ];

  if (error) {
    return (
      <div style={{ padding: 24 }}>
        <Alert
          type="error"
          message="Lỗi tải dữ liệu"
          description="Không thể tải danh sách môn học. Vui lòng thử lại sau."
        />
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Quản lý Môn học</h2>
        <Tag color="blue">Chế độ xem - Read Only</Tag>
      </div>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Select
            placeholder="Lọc theo bộ môn"
            style={{ width: 350 }}
            allowClear
            value={departmentFilter}
            onChange={(value) => setDepartmentFilter(value)}
          >
            {departments.map((d) => (
              <Option key={d} value={d}>
                {d}
              </Option>
            ))}
          </Select>
          <span style={{ marginLeft: 16, color: '#666' }}>
            Tổng: <strong>{filteredCourses?.length || 0}</strong> môn học
          </span>
        </div>

        <Table
          columns={courseColumns}
          dataSource={filteredCourses || []}
          rowKey="id"
          loading={isLoading}
          scroll={{ x: 1400 }}
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} môn học`,
          }}
        />
      </Card>

      {/* Course Detail Modal */}
      <Modal
        title={`Chi tiết môn học: ${selectedCourse?.code}`}
        open={isDetailModalVisible}
        onCancel={() => {
          setIsDetailModalVisible(false);
          setSelectedCourse(null);
        }}
        footer={null}
        width={600}
      >
        {selectedCourse && (
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="Mã môn">{selectedCourse.code}</Descriptions.Item>
            <Descriptions.Item label="Tên môn">{selectedCourse.name}</Descriptions.Item>
            <Descriptions.Item label="Số tín chỉ">{selectedCourse.credits}</Descriptions.Item>
            <Descriptions.Item label="Bộ môn">{selectedCourse.departmentName || '-'}</Descriptions.Item>
            <Descriptions.Item label="Loại">{selectedCourse.subjectType || '-'}</Descriptions.Item>
            <Descriptions.Item label="Thành phần">{selectedCourse.component || '-'}</Descriptions.Item>
            <Descriptions.Item label="Số tiết lý thuyết">{selectedCourse.theoryHours}</Descriptions.Item>
            <Descriptions.Item label="Số tiết thực hành">{selectedCourse.practiceHours}</Descriptions.Item>
            <Descriptions.Item label="Số tiết tự học">{selectedCourse.selfStudyHours}</Descriptions.Item>
            <Descriptions.Item label="Trạng thái">
              <Tag color={selectedCourse.isActive ? 'green' : 'default'}>
                {selectedCourse.isActive ? 'Hoạt động' : 'Ẩn'}
              </Tag>
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};
