import React, { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  message,
  Tag,
  Descriptions,
  Tabs,
  Typography,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  LinkOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';

const { Option } = Select;
const { TextArea } = Input;
const { Text } = Typography;

interface Semester {
  id: string;
  code: string;
  name: string;
  startDate: string;
  endDate: string;
  cloCount: number;
  ploCount: number;
  gradingScale: '10' | '4' | 'letter';
  isActive: boolean;
}

interface Course {
  id: string;
  code: string;
  name: string;
  credits: number;
  semesterId: string;
  semesterName: string;
  faculty: string;
  department: string;
  courseType: 'required' | 'elective' | 'free';
  componentType: 'major' | 'foundation' | 'general' | 'thesis';
  theoryHours: number;
  practiceHours: number;
  selfStudyHours: number;
  cloCount: number; // Auto-filled from semester
  ploCount: number; // Auto-filled from semester
  gradingScale: string; // Auto-filled from semester
  description?: string;
  prerequisites: CourseRelation[];
  corequisites: CourseRelation[];
  createdAt: string;
  updatedAt: string;
}

interface CourseRelation {
  courseId: string;
  courseCode: string;
  courseName: string;
  type: 'required' | 'recommended';
}

// Mock semesters (from Admin config)
const mockSemesters: Semester[] = [
  {
    id: 's1',
    code: 'HK1-2024',
    name: 'Học kỳ 1 năm 2024-2025',
    startDate: '2024-09-01',
    endDate: '2025-01-15',
    cloCount: 6,
    ploCount: 12,
    gradingScale: '10',
    isActive: true,
  },
  {
    id: 's2',
    code: 'HK2-2024',
    name: 'Học kỳ 2 năm 2024-2025',
    startDate: '2025-02-01',
    endDate: '2025-06-15',
    cloCount: 5,
    ploCount: 10,
    gradingScale: '4',
    isActive: false,
  },
];

// Mock data
const mockCourses: Course[] = [
  {
    id: 'c1',
    code: 'CS101',
    name: 'Nhập môn Lập trình',
    credits: 3,
    semesterId: 's1',
    semesterName: 'Học kỳ 1 năm 2024-2025',
    faculty: 'Khoa CNTT',
    department: 'Bộ môn Khoa học Máy tính',
    courseType: 'required',
    componentType: 'foundation',
    theoryHours: 30,
    practiceHours: 30,
    selfStudyHours: 60,
    cloCount: 6,
    ploCount: 12,
    gradingScale: 'Thang 10',
    prerequisites: [],
    corequisites: [],
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01',
  },
  {
    id: 'c2',
    code: 'CS201',
    name: 'Cấu trúc Dữ liệu và Giải thuật',
    credits: 4,
    semesterId: 's1',
    semesterName: 'Học kỳ 1 năm 2024-2025',
    faculty: 'Khoa CNTT',
    department: 'Bộ môn Khoa học Máy tính',
    courseType: 'required',
    componentType: 'foundation',
    theoryHours: 45,
    practiceHours: 30,
    selfStudyHours: 75,
    cloCount: 6,
    ploCount: 12,
    gradingScale: 'Thang 10',
    prerequisites: [
      { courseId: 'c1', courseCode: 'CS101', courseName: 'Nhập môn Lập trình', type: 'required' },
    ],
    corequisites: [],
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01',
  },
];

const mockFaculties = ['Khoa CNTT', 'Khoa Điện - Điện tử', 'Khoa Cơ khí', 'Khoa Kinh tế'];
const mockDepartments = {
  'Khoa CNTT': ['Bộ môn Khoa học Máy tính', 'Bộ môn Kỹ thuật Phần mềm', 'Bộ môn Mạng và An ninh'],
  'Khoa Điện - Điện tử': ['Bộ môn Điện tử', 'Bộ môn Tự động hóa'],
  'Khoa Cơ khí': ['Bộ môn Cơ khí chế tạo', 'Bộ môn Cơ điện tử'],
  'Khoa Kinh tế': ['Bộ môn Quản trị', 'Bộ môn Kế toán'],
};

export const CourseManagementPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isRelationModalVisible, setIsRelationModalVisible] = useState(false);
  const [editingCourse, setEditingCourse] = useState<Course | null>(null);
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [form] = Form.useForm();
  const [relationForm] = Form.useForm();
  const [selectedFaculty, setSelectedFaculty] = useState<string>('');
  const [selectedSemester, setSelectedSemester] = useState<Semester | null>(null);

  // Fetch courses
  const { data: courses, isLoading } = useQuery({
    queryKey: ['courses'],
    queryFn: async () => {
      await new Promise((resolve) => setTimeout(resolve, 500));
      return mockCourses;
    },
  });

  // Handle semester selection - auto-fill config
  const handleSemesterChange = (semesterId: string) => {
    const semester = mockSemesters.find((s) => s.id === semesterId);
    if (semester) {
      setSelectedSemester(semester);
      form.setFieldsValue({
        cloCount: semester.cloCount,
        ploCount: semester.ploCount,
        gradingScale: semester.gradingScale === '10' ? 'Thang 10' : semester.gradingScale === '4' ? 'Thang 4' : 'Chữ',
      });
    }
  };

  // Create/Update course mutation
  const saveCourseMutation = useMutation({
    mutationFn: async (values: any) => {
      await new Promise((resolve) => setTimeout(resolve, 800));
      return values;
    },
    onSuccess: () => {
      message.success(editingCourse ? 'Cập nhật môn học thành công' : 'Thêm môn học thành công');
      setIsModalVisible(false);
      setEditingCourse(null);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['courses'] });
    },
  });

  // Delete course mutation
  const deleteCourseMutation = useMutation({
    mutationFn: async (id: string) => {
      await new Promise((resolve) => setTimeout(resolve, 500));
      return id;
    },
    onSuccess: () => {
      message.success('Xóa môn học thành công');
      queryClient.invalidateQueries({ queryKey: ['courses'] });
    },
  });

  // Add prerequisite/corequisite
  const addRelationMutation = useMutation({
    mutationFn: async (values: any) => {
      await new Promise((resolve) => setTimeout(resolve, 500));
      return values;
    },
    onSuccess: () => {
      message.success('Thêm quan hệ môn học thành công');
      setIsRelationModalVisible(false);
      relationForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ['courses'] });
    },
  });

  const courseColumns: ColumnsType<Course> = [
    {
      title: 'Mã môn',
      dataIndex: 'code',
      key: 'code',
      width: 100,
      fixed: 'left',
    },
    {
      title: 'Tên môn học',
      dataIndex: 'name',
      key: 'name',
      width: 250,
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semesterName',
      key: 'semesterName',
      width: 180,
      render: (text) => <Tag color="purple">{text}</Tag>,
    },
    {
      title: 'Tín chỉ',
      dataIndex: 'credits',
      key: 'credits',
      width: 80,
      align: 'center',
    },
    {
      title: 'Loại',
      dataIndex: 'courseType',
      key: 'courseType',
      width: 120,
      render: (type) => {
        const config = {
          required: { color: 'red', text: 'Bắt buộc' },
          elective: { color: 'blue', text: 'Tự chọn' },
          free: { color: 'green', text: 'Tự chọn tự do' },
        };
        const { color, text } = config[type as keyof typeof config];
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: 'Thành phần',
      dataIndex: 'componentType',
      key: 'componentType',
      width: 130,
      render: (type) => {
        const config = {
          major: 'Chuyên ngành',
          foundation: 'Cơ sở ngành',
          general: 'Đại cương',
          thesis: 'Khóa luận',
        };
        return config[type as keyof typeof config];
      },
    },
    {
      title: 'Khoa',
      dataIndex: 'faculty',
      key: 'faculty',
      width: 150,
    },
    {
      title: 'Bộ môn',
      dataIndex: 'department',
      key: 'department',
      width: 180,
    },
    {
      title: 'Môn tiên quyết',
      key: 'prerequisites',
      width: 150,
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          {record.prerequisites.length > 0 ? (
            record.prerequisites.map((p) => (
              <Tag key={p.courseId} color={p.type === 'required' ? 'orange' : 'blue'}>
                {p.courseCode}
              </Tag>
            ))
          ) : (
            <span style={{ color: '#999' }}>-</span>
          )}
        </Space>
      ),
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setEditingCourse(record);
              setSelectedFaculty(record.faculty);
              form.setFieldsValue(record);
              setIsModalVisible(true);
            }}
          >
            Sửa
          </Button>
          <Button
            size="small"
            icon={<LinkOutlined />}
            onClick={() => {
              setSelectedCourse(record);
              setIsRelationModalVisible(true);
            }}
          >
            Quan hệ
          </Button>
          <Button
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => {
              Modal.confirm({
                title: 'Xóa môn học',
                content: `Bạn có chắc muốn xóa môn học "${record.name}"?`,
                okText: 'Xóa',
                cancelText: 'Hủy',
                onOk: () => deleteCourseMutation.mutate(record.id),
              });
            }}
          />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Quản lý Môn học</h2>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          size="large"
          onClick={() => {
            setEditingCourse(null);
            form.resetFields();
            setSelectedFaculty('');
            setIsModalVisible(true);
          }}
        >
          Thêm Môn học
        </Button>
      </div>

      <Card>
        <Table
          columns={courseColumns}
          dataSource={courses || []}
          rowKey="id"
          loading={isLoading}
          scroll={{ x: 1600 }}
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} môn học`,
          }}
        />
      </Card>

      {/* Add/Edit Course Modal */}
      <Modal
        title={editingCourse ? 'Chỉnh sửa Môn học' : 'Thêm Môn học mới'}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          setEditingCourse(null);
          form.resetFields();
          setSelectedFaculty('');
        }}
        footer={null}
        width={800}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => saveCourseMutation.mutate(values)}
        >
          <Tabs
            items={[
              {
                key: 'basic',
                label: 'Thông tin cơ bản',
                children: (
                  <>
                    <Form.Item
                      label="Học kỳ"
                      name="semesterId"
                      rules={[{ required: true, message: 'Chọn học kỳ' }]}
                    >
                      <Select
                        placeholder="Chọn học kỳ (tự động điền CLO, PLO, Thang điểm)"
                        onChange={handleSemesterChange}
                      >
                        {mockSemesters.map((s) => (
                          <Option key={s.id} value={s.id}>
                            {s.name} {s.isActive && <Tag color="green" style={{ marginLeft: 8 }}>Đang hoạt động</Tag>}
                          </Option>
                        ))}
                      </Select>
                    </Form.Item>

                    {selectedSemester && (
                      <Card size="small" style={{ marginBottom: 16, backgroundColor: '#f0f5ff' }}>
                        <Space direction="vertical" size={4}>
                          <Text type="secondary">
                            <strong>Thông số học kỳ (tự động):</strong>
                          </Text>
                          <Text>• Số CLO: <strong>{selectedSemester.cloCount}</strong></Text>
                          <Text>• Số PLO: <strong>{selectedSemester.ploCount}</strong></Text>
                          <Text>• Thang điểm: <strong>{selectedSemester.gradingScale === '10' ? 'Thang 10' : selectedSemester.gradingScale === '4' ? 'Thang 4' : 'Chữ'}</strong></Text>
                        </Space>
                      </Card>
                    )}

                    <Form.Item
                      label="Mã môn học"
                      name="code"
                      rules={[{ required: true, message: 'Nhập mã môn học' }]}
                    >
                      <Input placeholder="VD: CS101" />
                    </Form.Item>

                    <Form.Item
                      label="Tên môn học"
                      name="name"
                      rules={[{ required: true, message: 'Nhập tên môn học' }]}
                    >
                      <Input placeholder="VD: Nhập môn Lập trình" />
                    </Form.Item>

                    <Form.Item
                      label="Số tín chỉ"
                      name="credits"
                      rules={[{ required: true, message: 'Nhập số tín chỉ' }]}
                    >
                      <InputNumber min={1} max={10} style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item
                      label="Loại học phần"
                      name="courseType"
                      rules={[{ required: true, message: 'Chọn loại học phần' }]}
                    >
                      <Select placeholder="Chọn loại">
                        <Option value="required">Bắt buộc</Option>
                        <Option value="elective">Tự chọn</Option>
                        <Option value="free">Tự chọn tự do</Option>
                      </Select>
                    </Form.Item>

                    <Form.Item
                      label="Thành phần"
                      name="componentType"
                      rules={[{ required: true, message: 'Chọn thành phần' }]}
                    >
                      <Select placeholder="Chọn thành phần">
                        <Option value="major">Chuyên ngành</Option>
                        <Option value="foundation">Cơ sở ngành</Option>
                        <Option value="general">Đại cương</Option>
                        <Option value="thesis">Khóa luận/Thực tập</Option>
                      </Select>
                    </Form.Item>

                    <Form.Item
                      label="Mô tả"
                      name="description"
                    >
                      <TextArea rows={3} placeholder="Mô tả ngắn về môn học..." />
                    </Form.Item>

                    {/* Hidden fields - auto-filled from semester */}
                    <Form.Item name="cloCount" hidden>
                      <InputNumber />
                    </Form.Item>
                    <Form.Item name="ploCount" hidden>
                      <InputNumber />
                    </Form.Item>
                    <Form.Item name="gradingScale" hidden>
                      <Input />
                    </Form.Item>
                  </>
                ),
              },
              {
                key: 'organization',
                label: 'Tổ chức',
                children: (
                  <>
                    <Form.Item
                      label="Khoa"
                      name="faculty"
                      rules={[{ required: true, message: 'Chọn khoa' }]}
                    >
                      <Select
                        placeholder="Chọn khoa"
                        onChange={(value) => {
                          setSelectedFaculty(value);
                          form.setFieldValue('department', undefined);
                        }}
                      >
                        {mockFaculties.map((f) => (
                          <Option key={f} value={f}>
                            {f}
                          </Option>
                        ))}
                      </Select>
                    </Form.Item>

                    <Form.Item
                      label="Bộ môn"
                      name="department"
                      rules={[{ required: true, message: 'Chọn bộ môn' }]}
                    >
                      <Select placeholder="Chọn bộ môn" disabled={!selectedFaculty}>
                        {selectedFaculty &&
                          mockDepartments[selectedFaculty as keyof typeof mockDepartments]?.map((d) => (
                            <Option key={d} value={d}>
                              {d}
                            </Option>
                          ))}
                      </Select>
                    </Form.Item>
                  </>
                ),
              },
              {
                key: 'hours',
                label: 'Phân bổ thời gian',
                children: (
                  <>
                    <Form.Item
                      label="Số tiết Lý thuyết"
                      name="theoryHours"
                      rules={[{ required: true, message: 'Nhập số tiết lý thuyết' }]}
                    >
                      <InputNumber min={0} style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item
                      label="Số tiết Thực hành"
                      name="practiceHours"
                      rules={[{ required: true, message: 'Nhập số tiết thực hành' }]}
                    >
                      <InputNumber min={0} style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item
                      label="Số tiết Tự học"
                      name="selfStudyHours"
                      rules={[{ required: true, message: 'Nhập số tiết tự học' }]}
                    >
                      <InputNumber min={0} style={{ width: '100%' }} />
                    </Form.Item>
                  </>
                ),
              },
            ]}
          />

          <Form.Item style={{ marginBottom: 0, marginTop: 16 }}>
            <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
              <Button onClick={() => setIsModalVisible(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit" loading={saveCourseMutation.isPending}>
                {editingCourse ? 'Cập nhật' : 'Thêm mới'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Course Relations Modal */}
      <Modal
        title={
          <Space>
            <LinkOutlined />
            <span>Quản lý Quan hệ Môn học: {selectedCourse?.code}</span>
          </Space>
        }
        open={isRelationModalVisible}
        onCancel={() => {
          setIsRelationModalVisible(false);
          setSelectedCourse(null);
          relationForm.resetFields();
        }}
        footer={null}
        width={700}
      >
        {selectedCourse && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="Môn học">
                <strong>{selectedCourse.name}</strong> ({selectedCourse.code})
              </Descriptions.Item>
            </Descriptions>

            <Card title="Môn học Tiên quyết" size="small">
              {selectedCourse.prerequisites.length > 0 ? (
                <Space direction="vertical" style={{ width: '100%' }}>
                  {selectedCourse.prerequisites.map((p) => (
                    <Tag key={p.courseId} color={p.type === 'required' ? 'orange' : 'blue'}>
                      {p.courseCode} - {p.courseName} ({p.type === 'required' ? 'Bắt buộc' : 'Khuyến nghị'})
                    </Tag>
                  ))}
                </Space>
              ) : (
                <span style={{ color: '#999' }}>Chưa có môn tiên quyết</span>
              )}
            </Card>

            <Card title="Môn học Song hành" size="small">
              {selectedCourse.corequisites.length > 0 ? (
                <Space direction="vertical" style={{ width: '100%' }}>
                  {selectedCourse.corequisites.map((c) => (
                    <Tag key={c.courseId} color="cyan">
                      {c.courseCode} - {c.courseName}
                    </Tag>
                  ))}
                </Space>
              ) : (
                <span style={{ color: '#999' }}>Chưa có môn song hành</span>
              )}
            </Card>

            <Form form={relationForm} layout="vertical" onFinish={(values) => addRelationMutation.mutate(values)}>
              <Form.Item
                label="Loại quan hệ"
                name="relationType"
                rules={[{ required: true, message: 'Chọn loại quan hệ' }]}
              >
                <Select placeholder="Chọn loại">
                  <Option value="prerequisite-required">Tiên quyết (Bắt buộc)</Option>
                  <Option value="prerequisite-recommended">Tiên quyết (Khuyến nghị)</Option>
                  <Option value="corequisite">Song hành</Option>
                </Select>
              </Form.Item>

              <Form.Item
                label="Chọn môn học"
                name="relatedCourseId"
                rules={[{ required: true, message: 'Chọn môn học' }]}
              >
                <Select
                  showSearch
                  placeholder="Tìm môn học..."
                  optionFilterProp="children"
                  filterOption={(input, option) =>
                    String(option?.label || '').toLowerCase().includes(input.toLowerCase())
                  }
                >
                  {courses
                    ?.filter((c) => c.id !== selectedCourse.id)
                    .map((c) => (
                      <Option key={c.id} value={c.id} label={`${c.code} - ${c.name}`}>
                        {c.code} - {c.name}
                      </Option>
                    ))}
                </Select>
              </Form.Item>

              <Form.Item style={{ marginBottom: 0 }}>
                <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                  <Button onClick={() => relationForm.resetFields()}>Xóa</Button>
                  <Button type="primary" htmlType="submit" loading={addRelationMutation.isPending}>
                    Thêm quan hệ
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Space>
        )}
      </Modal>
    </div>
  );
};
