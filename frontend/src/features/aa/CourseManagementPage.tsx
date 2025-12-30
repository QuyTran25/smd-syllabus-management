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
  Button,
  Form,
  Input,
  InputNumber,
  message,
  Popconfirm,
} from 'antd';
import { EyeOutlined, EditOutlined, DeleteOutlined, PlusOutlined, LinkOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
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
  semester?: string;
  departmentName?: string;
  facultyName?: string;
  prerequisites?: string;
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
  const [isFormModalVisible, setIsFormModalVisible] = useState(false);
  const [editingCourse, setEditingCourse] = useState<CourseDisplay | null>(null);
  const [form] = Form.useForm();
  const queryClient = useQueryClient();

  // Fetch courses from API
  const { data: subjectsRaw, isLoading, error } = useQuery({
    queryKey: ['subjects'],
    queryFn: () => subjectService.getAllSubjects(),
  });

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (values: any) => subjectService.createSubject(values),
    onSuccess: () => {
      message.success('Thêm môn học thành công');
      queryClient.invalidateQueries({ queryKey: ['subjects'] });
      setIsFormModalVisible(false);
      form.resetFields();
    },
    onError: () => message.error('Thêm môn học thất bại'),
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, values }: { id: string; values: any }) => 
      subjectService.updateSubject(id, values),
    onSuccess: () => {
      message.success('Cập nhật môn học thành công');
      queryClient.invalidateQueries({ queryKey: ['subjects'] });
      setIsFormModalVisible(false);
      setEditingCourse(null);
      form.resetFields();
    },
    onError: () => message.error('Cập nhật môn học thất bại'),
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id: string) => subjectService.deleteSubject(id),
    onSuccess: () => {
      message.success('Xóa môn học thành công');
      queryClient.invalidateQueries({ queryKey: ['subjects'] });
    },
    onError: () => message.error('Xóa môn học thất bại'),
  });

  // Map API response to display format
  const courses: CourseDisplay[] = useMemo(() => {
    if (!subjectsRaw) return [];
    return subjectsRaw.map((s: Subject) => ({
      id: s.id,
      code: s.code,
      name: s.currentNameVi,
      credits: s.defaultCredits,
      semester: s.semester,
      departmentName: s.departmentName,
      facultyName: s.facultyName,
      prerequisites: s.prerequisites,
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
      width: 200,
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semester',
      key: 'semester',
      width: 80,
      align: 'center',
      render: (text) => text || <span style={{ color: '#999' }}>-</span>,
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
      title: 'Khoa',
      dataIndex: 'facultyName',
      key: 'facultyName',
      width: 150,
      render: (text) => text || <span style={{ color: '#999' }}>-</span>,
    },
    {
      title: 'Bộ môn',
      dataIndex: 'departmentName',
      key: 'departmentName',
      width: 200,
      render: (text) => text || <span style={{ color: '#999' }}>-</span>,
    },
    {
      title: 'Môn tiên quyết',
      dataIndex: 'prerequisites',
      key: 'prerequisites',
      width: 150,
      render: (text) => text || <span style={{ color: '#999' }}>-</span>,
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setEditingCourse(record);
              form.setFieldsValue({
                code: record.code,
                name: record.name,
                credits: record.credits,
              });
              setIsFormModalVisible(true);
            }}
          >
            Sửa
          </Button>
          <Button
            type="link"
            size="small"
            icon={<LinkOutlined />}
            onClick={() => {
              // TODO: Open relationship modal
              message.info('Chức năng Quan hệ đang phát triển');
            }}
          >
            Quan hệ
          </Button>
          <Popconfirm
            title="Xóa môn học"
            description="Bạn có chắc muốn xóa môn học này?"
            onConfirm={() => deleteMutation.mutate(record.id)}
            okText="Xóa"
            cancelText="Hủy"
            okButtonProps={{ danger: true }}
          >
            <Button type="link" danger size="small" icon={<DeleteOutlined />} />
          </Popconfirm>
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
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => {
            setEditingCourse(null);
            form.resetFields();
            setIsFormModalVisible(true);
          }}
        >
          Thêm Môn học
        </Button>
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
          scroll={{ x: 1300 }}
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

      {/* Add/Edit Form Modal */}
      <Modal
        title={editingCourse ? 'Chỉnh sửa môn học' : 'Thêm môn học mới'}
        open={isFormModalVisible}
        onOk={() => form.submit()}
        onCancel={() => {
          setIsFormModalVisible(false);
          setEditingCourse(null);
          form.resetFields();
        }}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        width={500}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => {
            if (editingCourse) {
              updateMutation.mutate({ id: editingCourse.id, values });
            } else {
              createMutation.mutate(values);
            }
          }}
        >
          <Form.Item
            label="Mã môn học"
            name="code"
            rules={[{ required: true, message: 'Vui lòng nhập mã môn học' }]}
          >
            <Input placeholder="Ví dụ: CS101" disabled={!!editingCourse} />
          </Form.Item>

          <Form.Item
            label="Tên môn học"
            name="name"
            rules={[{ required: true, message: 'Vui lòng nhập tên môn học' }]}
          >
            <Input placeholder="Nhập tên môn học" />
          </Form.Item>

          <Form.Item
            label="Số tín chỉ"
            name="credits"
            rules={[{ required: true, message: 'Vui lòng nhập số tín chỉ' }]}
          >
            <InputNumber min={1} max={10} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
