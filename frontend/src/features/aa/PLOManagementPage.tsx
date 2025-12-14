import React, { useState } from 'react';
import { Card, Table, Button, Space, Modal, Form, Input, Select, Tag, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';

const { TextArea } = Input;
const { Option } = Select;

interface PLO {
  id: string;
  code: string;
  description: string;
  category: 'Knowledge' | 'Skills' | 'Competence' | 'Attitude';
  courseId: string;
  courseCode: string;
  courseName: string;
}

interface Course {
  id: string;
  code: string;
  name: string;
}

// Mock courses
const mockCourses: Course[] = [
  { id: 'c1', code: 'CS101', name: 'Nhập môn Lập trình' },
  { id: 'c2', code: 'CS201', name: 'Cấu trúc Dữ liệu và Giải thuật' },
  { id: 'c3', code: 'CS301', name: 'Hệ điều hành' },
  { id: 'c4', code: 'CS401', name: 'Mạng máy tính' },
];

// Mock PLOs
const mockPLOs: PLO[] = [
  { id: 'p1', code: 'PLO1', description: 'Vận dụng kiến thức nền tảng về toán, khoa học tự nhiên và kỹ thuật', category: 'Knowledge', courseId: 'c1', courseCode: 'CS101', courseName: 'Nhập môn Lập trình' },
  { id: 'p2', code: 'PLO2', description: 'Phân tích và giải quyết các vấn đề phức tạp trong lĩnh vực CNTT', category: 'Skills', courseId: 'c1', courseCode: 'CS101', courseName: 'Nhập môn Lập trình' },
  { id: 'p3', code: 'PLO1', description: 'Thiết kế và triển khai các cấu trúc dữ liệu hiệu quả', category: 'Skills', courseId: 'c2', courseCode: 'CS201', courseName: 'Cấu trúc Dữ liệu và Giải thuật' },
  { id: 'p4', code: 'PLO2', description: 'Phân tích độ phức tạp thuật toán', category: 'Knowledge', courseId: 'c2', courseCode: 'CS201', courseName: 'Cấu trúc Dữ liệu và Giải thuật' },
  { id: 'p5', code: 'PLO3', description: 'Làm việc hiệu quả trong môi trường nhóm', category: 'Competence', courseId: 'c2', courseCode: 'CS201', courseName: 'Cấu trúc Dữ liệu và Giải thuật' },
  { id: 'p6', code: 'PLO1', description: 'Hiểu cấu trúc và hoạt động của hệ điều hành', category: 'Knowledge', courseId: 'c3', courseCode: 'CS301', courseName: 'Hệ điều hành' },
];

export const PLOManagementPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingPLO, setEditingPLO] = useState<PLO | null>(null);
  const [courseFilter, setCourseFilter] = useState<string | undefined>(undefined);
  const [form] = Form.useForm();

  // Fetch PLOs
  const { data: plos, isLoading } = useQuery({
    queryKey: ['plos'],
    queryFn: async () => {
      await new Promise((resolve) => setTimeout(resolve, 500));
      return mockPLOs;
    },
  });

  // Create/Update PLO mutation
  const savePLOMutation = useMutation({
    mutationFn: async (values: any) => {
      await new Promise((resolve) => setTimeout(resolve, 800));
      return values;
    },
    onSuccess: () => {
      message.success(editingPLO ? 'Cập nhật PLO thành công' : 'Thêm PLO thành công');
      setIsModalVisible(false);
      setEditingPLO(null);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['plos'] });
    },
  });

  // Delete PLO mutation
  const deletePLOMutation = useMutation({
    mutationFn: async (id: string) => {
      await new Promise((resolve) => setTimeout(resolve, 500));
      return id;
    },
    onSuccess: () => {
      message.success('Xóa PLO thành công');
      queryClient.invalidateQueries({ queryKey: ['plos'] });
    },
  });

  const ploColumns: ColumnsType<PLO> = [
    {
      title: 'Môn học',
      key: 'course',
      width: 250,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span style={{ fontWeight: 500 }}>{record.courseCode}</span>
          <span style={{ fontSize: '12px', color: '#666' }}>{record.courseName}</span>
        </Space>
      ),
    },
    {
      title: 'Mã PLO',
      dataIndex: 'code',
      key: 'code',
      width: 100,
      render: (text) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: 'Danh mục',
      dataIndex: 'category',
      key: 'category',
      width: 150,
      render: (category) => {
        const config = {
          Knowledge: { color: 'blue', text: 'Kiến thức' },
          Skills: { color: 'green', text: 'Kỹ năng' },
          Competence: { color: 'orange', text: 'Năng lực' },
          Attitude: { color: 'purple', text: 'Thái độ' },
        };
        const { color, text} = config[category as keyof typeof config];
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setEditingPLO(record);
              form.setFieldsValue(record);
              setIsModalVisible(true);
            }}
          >
            Sửa
          </Button>
          <Button
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => {
              Modal.confirm({
                title: 'Xóa PLO',
                content: `Bạn có chắc muốn xóa PLO "${record.code}"?`,
                okText: 'Xóa',
                cancelText: 'Hủy',
                okType: 'danger',
                onOk: () => deletePLOMutation.mutate(record.id),
              });
            }}
          />
        </Space>
      ),
    },
  ];

  // Filter PLOs by course
  const filteredPLOs = courseFilter
    ? plos?.filter((p) => p.courseId === courseFilter)
    : plos;

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Quản lý PLO</h2>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          size="large"
          onClick={() => {
            setEditingPLO(null);
            form.resetFields();
            setIsModalVisible(true);
          }}
        >
          Thêm PLO
        </Button>
      </div>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Select
            placeholder="Lọc theo môn học"
            style={{ width: 300 }}
            allowClear
            value={courseFilter}
            onChange={(value) => setCourseFilter(value)}
          >
            {mockCourses.map((c) => (
              <Option key={c.id} value={c.id}>
                {c.code} - {c.name}
              </Option>
            ))}
          </Select>
          <span style={{ marginLeft: 16, color: '#666' }}>
            Tổng: <strong>{filteredPLOs?.length || 0}</strong> PLO
            {courseFilter && (
              <> (của môn <strong>{mockCourses.find(c => c.id === courseFilter)?.code}</strong>)</>
            )}
          </span>
        </div>

        <Table
          columns={ploColumns}
          dataSource={filteredPLOs || []}
          rowKey="id"
          loading={isLoading}
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} PLO`,
          }}
        />
      </Card>

      {/* Add/Edit PLO Modal */}
      <Modal
        title={editingPLO ? 'Chỉnh sửa PLO' : 'Thêm PLO mới'}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          setEditingPLO(null);
          form.resetFields();
        }}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => {
            const course = mockCourses.find((c) => c.id === values.courseId);
            savePLOMutation.mutate({
              ...values,
              courseCode: course?.code,
              courseName: course?.name,
            });
          }}
        >
          <Form.Item
            label="Môn học"
            name="courseId"
            rules={[{ required: true, message: 'Chọn môn học' }]}
          >
            <Select placeholder="Chọn môn học" showSearch optionFilterProp="children">
              {mockCourses.map((c) => (
                <Option key={c.id} value={c.id}>
                  {c.code} - {c.name}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Mã PLO"
            name="code"
            rules={[{ required: true, message: 'Nhập mã PLO' }]}
          >
            <Input placeholder="VD: PLO1" />
          </Form.Item>

          <Form.Item
            label="Mô tả"
            name="description"
            rules={[{ required: true, message: 'Nhập mô tả PLO' }]}
          >
            <TextArea rows={3} placeholder="Mô tả chi tiết PLO..." />
          </Form.Item>

          <Form.Item
            label="Danh mục"
            name="category"
            rules={[{ required: true, message: 'Chọn danh mục' }]}
          >
            <Select placeholder="Chọn danh mục">
              <Option value="Knowledge">Knowledge (Kiến thức)</Option>
              <Option value="Skills">Skills (Kỹ năng)</Option>
              <Option value="Competence">Competence (Năng lực)</Option>
              <Option value="Attitude">Attitude (Thái độ)</Option>
            </Select>
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, marginTop: 16 }}>
            <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
              <Button onClick={() => setIsModalVisible(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit" loading={savePLOMutation.isPending}>
                {editingPLO ? 'Cập nhật' : 'Thêm mới'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
