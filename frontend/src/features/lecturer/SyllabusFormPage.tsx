import React, { useState, useEffect } from 'react';
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Space,
  Table,
  InputNumber,
  message,
  Modal,
  Row,
  Col,
  Divider,
  Tag,
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  SaveOutlined,
  CopyOutlined,
  SendOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';

const { TextArea } = Input;
const { Option } = Select;

interface CLO {
  id: string;
  code: string;
  description: string;
  bloomLevel: string;
  weight: number;
  mappedPLOs: string[];
  piMappings?: { ploId: string; piCode: string; level: 'H' | 'M' | 'L' }[]; // Ánh xạ chi tiết đến PI
}

interface PrerequisiteCourse {
  id: string;
  code: string;
  name: string;
  type: 'required' | 'recommended';
}

interface TimeAllocation {
  theory: number; // Số tiết lý thuyết
  practice: number; // Số tiết thực hành
  selfStudy: number; // Số tiết tự học
}

interface AssessmentMethod {
  id: string;
  method: string; // Tên phương pháp đánh giá
  form: string; // Hình thức (Cá nhân/Nhóm/Kiểm tra)
  clos: string[]; // Mã CLO áp dụng
  criteria: string; // Tiêu chí (VD: A1.2, A2.1)
  weight: number; // Tỷ trọng %
}

// Mock data
const departments = [
  { id: '1', name: 'Khoa Công nghệ Thông tin' },
  { id: '2', name: 'Khoa Điện - Điện tử' },
  { id: '3', name: 'Khoa Cơ khí' },
];

const programs = [
  { id: '1', name: 'Khoa học Máy tính', departmentId: '1' },
  { id: '2', name: 'Hệ thống Thông tin', departmentId: '1' },
  { id: '3', name: 'Kỹ thuật Điện tử', departmentId: '2' },
];

const semesters = [
  { id: '1', name: 'HK1 2024-2025', year: '2024-2025', semester: 1 },
  { id: '2', name: 'HK2 2024-2025', year: '2024-2025', semester: 2 },
  { id: '3', name: 'HK1 2025-2026', year: '2025-2026', semester: 1 },
];

const plos = [
  { 
    id: '1', 
    code: 'PLO1', 
    description: 'Kiến thức nền tảng',
    pis: [
      { code: 'PI1.1', description: 'Kiến thức cơ bản' },
      { code: 'PI1.2', description: 'Kiến thức chuyên ngành' },
    ]
  },
  { 
    id: '2', 
    code: 'PLO2', 
    description: 'Kỹ năng chuyên môn',
    pis: [
      { code: 'PI2.1', description: 'Phân tích vấn đề' },
      { code: 'PI2.2', description: 'Thiết kế giải pháp' },
      { code: 'PI2.3', description: 'Triển khai hệ thống' },
    ]
  },
  { 
    id: '3', 
    code: 'PLO3', 
    description: 'Kỹ năng mềm',
    pis: [
      { code: 'PI3.1', description: 'Giao tiếp hiệu quả' },
      { code: 'PI3.2', description: 'Làm việc nhóm' },
    ]
  },
  { 
    id: '4', 
    code: 'PLO4', 
    description: 'Tư duy phản biện',
    pis: [
      { code: 'PI4.1', description: 'Tư duy logic' },
      { code: 'PI4.2', description: 'Giải quyết vấn đề' },
    ]
  },
  { 
    id: '5', 
    code: 'PLO5', 
    description: 'Làm việc nhóm',
    pis: [
      { code: 'PI5.1', description: 'Hợp tác' },
      { code: 'PI5.2', description: 'Lãnh đạo' },
    ]
  },
];

const bloomLevels = ['Nhớ', 'Hiểu', 'Áp dụng', 'Phân tích', 'Đánh giá', 'Sáng tạo'];

const availableCourses = [
  { id: '1', code: 'CS101', name: 'Lập trình căn bản', departmentId: '1' },
  { id: '2', code: 'CS201', name: 'Cấu trúc dữ liệu', departmentId: '1' },
  { id: '3', code: 'CS202', name: 'Toán rời rạc', departmentId: '1' },
  { id: '4', code: 'CS301', name: 'Cơ sở dữ liệu', departmentId: '1' },
  { id: '5', code: 'CS302', name: 'Mạng máy tính', departmentId: '1' },
];

const existingSyllabi = [
  {
    id: '1',
    subjectCode: 'CS101',
    subjectName: 'Lập trình căn bản',
    semester: 'HK1 2024-2025',
    version: 'v2.0',
    departmentId: '1',
  },
  {
    id: '2',
    subjectCode: 'CS201',
    subjectName: 'Cấu trúc dữ liệu',
    semester: 'HK1 2024-2025',
    version: 'v1.5',
    departmentId: '1',
  },
];

const SyllabusFormPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [clos, setCLOs] = useState<CLO[]>([]);
  const [prerequisites, setPrerequisites] = useState<PrerequisiteCourse[]>([]);
  const [assessmentMethods, setAssessmentMethods] = useState<AssessmentMethod[]>([]);
  const [selectedDepartment, setSelectedDepartment] = useState<string>('');
  const [copyModalVisible, setCopyModalVisible] = useState(false);
  const [piMappingModalVisible, setPiMappingModalVisible] = useState(false);
  const [currentCLOForMapping, setCurrentCLOForMapping] = useState<CLO | null>(null);

  useEffect(() => {
    if (id) {
      // Load existing syllabus for editing
      loadSyllabus(id);
    }
  }, [id]);

  const loadSyllabus = async (_syllabusId: string) => {
    setLoading(true);
    // Mock load
    setTimeout(() => {
      form.setFieldsValue({
        departmentId: '1',
        programId: '1',
        subjectCode: 'CS301',
        subjectName: 'Cơ sở dữ liệu',
        credits: 3,
        semesterId: '2',
        academicYear: '2024-2025',
        description: 'Môn học về cơ sở dữ liệu quan hệ...',
        objectives: 'Sinh viên nắm vững các khái niệm...',
      });
      setCLOs([
        {
          id: '1',
          code: 'CLO1',
          description: 'Hiểu các khái niệm cơ bản về CSDL',
          bloomLevel: 'Hiểu',
          weight: 20,
          mappedPLOs: ['1', '2'],
        },
      ]);
      setSelectedDepartment('1');
      setLoading(false);
    }, 500);
  };

  const handleCopyFromExisting = (syllabusId: string) => {
    // Copy all fields from existing syllabus
    const selected = existingSyllabi.find((s) => s.id === syllabusId);
    if (selected) {
      message.success(`Đã copy từ ${selected.subjectCode} - ${selected.version}`);
      setCopyModalVisible(false);
      // Mock copy data
      form.setFieldsValue({
        subjectCode: selected.subjectCode,
        subjectName: selected.subjectName,
        description: 'Nội dung được copy từ đề cương cũ...',
      });
      setCLOs([
        {
          id: '1',
          code: 'CLO1',
          description: 'CLO được copy từ đề cương cũ',
          bloomLevel: 'Áp dụng',
          weight: 25,
          mappedPLOs: ['1'],
        },
      ]);
    }
  };

  const addCLO = () => {
    const newCLO: CLO = {
      id: Date.now().toString(),
      code: `CLO${clos.length + 1}`,
      description: '',
      bloomLevel: 'Hiểu',
      weight: 0,
      mappedPLOs: [],
    };
    setCLOs([...clos, newCLO]);
  };

  const updateCLO = (id: string, field: keyof CLO, value: any) => {
    setCLOs(clos.map((clo) => (clo.id === id ? { ...clo, [field]: value } : clo)));
  };

  const deleteCLO = (id: string) => {
    setCLOs(clos.filter((clo) => clo.id !== id));
  };

  // Load prerequisites from course when course code is selected
  const handleCourseSelect = (courseCode: string) => {
    const course = availableCourses.find((c) => c.code === courseCode);
    if (course) {
      // Auto-fill course name (read-only)
      form.setFieldsValue({
        subjectName: course.name,
      });

      // Load prerequisites from course (set by AA)
      // Mock prerequisites - in real app, fetch from courseService.getCourseByCode()
      const mockCoursePrerequisites: PrerequisiteCourse[] = [];
      
      // Example: CS201 has CS101 as prerequisite
      if (courseCode === 'CS201') {
        mockCoursePrerequisites.push({
          id: 'prereq-1',
          code: 'CS101',
          name: 'Lập trình căn bản',
          type: 'required',
        });
      }
      // Example: CS301 has CS201 (required) and CS202 (recommended)
      else if (courseCode === 'CS301') {
        mockCoursePrerequisites.push(
          {
            id: 'prereq-2',
            code: 'CS201',
            name: 'Cấu trúc dữ liệu',
            type: 'required',
          },
          {
            id: 'prereq-3',
            code: 'CS202',
            name: 'Toán rời rạc',
            type: 'recommended',
          }
        );
      }
      // Example: CS302 has CS101 as prerequisite
      else if (courseCode === 'CS302') {
        mockCoursePrerequisites.push({
          id: 'prereq-4',
          code: 'CS101',
          name: 'Lập trình căn bản',
          type: 'required',
        });
      }

      if (mockCoursePrerequisites.length > 0) {
        setPrerequisites(mockCoursePrerequisites);
        message.info(`Đã tải ${mockCoursePrerequisites.length} môn tiên quyết (do Phòng Đào tạo thiết lập)`);
      } else {
        setPrerequisites([]);
      }
    }
  };

  const addPrerequisite = (courseId: string, type: 'required' | 'recommended') => {
    const course = availableCourses.find((c) => c.id === courseId);
    if (course && !prerequisites.find((p) => p.id === courseId)) {
      setPrerequisites([...prerequisites, { ...course, type }]);
      message.success(`Đã thêm ${course.code} - ${course.name}`);
    }
  };

  const removePrerequisite = (id: string) => {
    setPrerequisites(prerequisites.filter((p) => p.id !== id));
  };

  const changePrerequisiteType = (id: string, type: 'required' | 'recommended') => {
    setPrerequisites(prerequisites.map((p) => (p.id === id ? { ...p, type } : p)));
  };

  const addAssessmentMethod = () => {
    const newMethod: AssessmentMethod = {
      id: Date.now().toString(),
      method: '',
      form: 'Cá nhân',
      clos: [],
      criteria: '',
      weight: 0,
    };
    setAssessmentMethods([...assessmentMethods, newMethod]);
  };

  const updateAssessmentMethod = (id: string, field: keyof AssessmentMethod, value: any) => {
    setAssessmentMethods(assessmentMethods.map((method) => 
      method.id === id ? { ...method, [field]: value } : method
    ));
  };

  const deleteAssessmentMethod = (id: string) => {
    setAssessmentMethods(assessmentMethods.filter((method) => method.id !== id));
  };

  const openPiMappingModal = (clo: CLO) => {
    setCurrentCLOForMapping(clo);
    setPiMappingModalVisible(true);
  };

  const savePiMappings = (piMappings: { ploId: string; piCode: string; level: 'H' | 'M' | 'L' }[]) => {
    if (currentCLOForMapping) {
      setCLOs(clos.map((clo) => 
        clo.id === currentCLOForMapping.id 
          ? { ...clo, piMappings, mappedPLOs: [...new Set(piMappings.map(m => m.ploId))] }
          : clo
      ));
      setPiMappingModalVisible(false);
      setCurrentCLOForMapping(null);
      message.success('Đã lưu ánh xạ PI');
    }
  };

  const handleSave = async (status: 'DRAFT' | 'SUBMIT') => {
    try {
      await form.validateFields();
      setLoading(true);

      // Validate CLOs
      if (clos.length === 0) {
        message.error('Vui lòng thêm ít nhất 1 CLO');
        setLoading(false);
        return;
      }

      const totalWeight = clos.reduce((sum, clo) => sum + clo.weight, 0);
      if (totalWeight !== 100) {
        message.error(`Tổng trọng số CLO phải bằng 100% (hiện tại: ${totalWeight}%)`);
        setLoading(false);
        return;
      }

      // Validate Assessment Methods
      if (assessmentMethods.length > 0) {
        const totalAssessmentWeight = assessmentMethods.reduce((sum, method) => sum + method.weight, 0);
        if (totalAssessmentWeight !== 100) {
          message.error(`Tổng trọng số đánh giá phải bằng 100% (hiện tại: ${totalAssessmentWeight}%)`);
          setLoading(false);
          return;
        }
      }

      // Mock save
      setTimeout(() => {
        if (status === 'SUBMIT') {
          message.success('Đã gửi đề cương cho Trưởng Bộ môn phê duyệt!');
        } else {
          message.success('Đã lưu bản nháp!');
        }
        setLoading(false);
        navigate('/lecturer/syllabi');
      }, 1000);
    } catch (error) {
      setLoading(false);
    }
  };

  const cloColumns: ColumnsType<CLO> = [
    {
      title: 'Mã CLO',
      dataIndex: 'code',
      width: 100,
      render: (_, record) => (
        <Input
          value={record.code}
          onChange={(e) => updateCLO(record.id, 'code', e.target.value)}
          placeholder="CLO1"
        />
      ),
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      render: (_, record) => (
        <TextArea
          value={record.description}
          onChange={(e) => updateCLO(record.id, 'description', e.target.value)}
          placeholder="Sinh viên có khả năng..."
          rows={2}
        />
      ),
    },
    {
      title: 'Bloom Level',
      dataIndex: 'bloomLevel',
      width: 130,
      render: (_, record) => (
        <Select
          value={record.bloomLevel}
          onChange={(value) => updateCLO(record.id, 'bloomLevel', value)}
          style={{ width: '100%' }}
        >
          {bloomLevels.map((level) => (
            <Option key={level} value={level}>
              {level}
            </Option>
          ))}
        </Select>
      ),
    },
    {
      title: 'Trọng số (%)',
      dataIndex: 'weight',
      width: 120,
      render: (_, record) => (
        <InputNumber
          value={record.weight}
          onChange={(value) => updateCLO(record.id, 'weight', value || 0)}
          min={0}
          max={100}
          style={{ width: '100%' }}
        />
      ),
    },
    {
      title: 'Ánh xạ PLO-PI',
      dataIndex: 'mappedPLOs',
      width: 250,
      render: (_, record) => (
        <Space direction="vertical" style={{ width: '100%' }}>
          <Button 
            size="small" 
            onClick={() => openPiMappingModal(record)}
            type={record.piMappings && record.piMappings.length > 0 ? 'primary' : 'default'}
          >
            {record.piMappings && record.piMappings.length > 0 
              ? `${record.piMappings.length} PI đã chọn` 
              : 'Chọn PLO-PI'}
          </Button>
          {record.piMappings && record.piMappings.length > 0 && (
            <div style={{ fontSize: '11px', color: '#666' }}>
              {record.piMappings.map(m => {
                const plo = plos.find(p => p.id === m.ploId);
                return `${plo?.code}/${m.piCode}(${m.level})`;
              }).join(', ')}
            </div>
          )}
        </Space>
      ),
    },
    {
      title: '',
      key: 'actions',
      width: 50,
      render: (_, record) => (
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          onClick={() => deleteCLO(record.id)}
        />
      ),
    },
  ];

  const assessmentColumns: ColumnsType<AssessmentMethod> = [
    {
      title: 'Phương pháp đánh giá',
      dataIndex: 'method',
      width: 200,
      render: (_, record) => (
        <Input
          value={record.method}
          onChange={(e) => updateAssessmentMethod(record.id, 'method', e.target.value)}
          placeholder="Thi giữa kỳ"
        />
      ),
    },
    {
      title: 'Hình thức',
      dataIndex: 'form',
      width: 150,
      render: (_, record) => (
        <Select
          value={record.form}
          onChange={(value) => updateAssessmentMethod(record.id, 'form', value)}
          style={{ width: '100%' }}
        >
          <Option value="Cá nhân">Cá nhân</Option>
          <Option value="Nhóm">Nhóm</Option>
          <Option value="Kiểm tra">Kiểm tra</Option>
          <Option value="Bài tập">Bài tập</Option>
          <Option value="Thi">Thi</Option>
        </Select>
      ),
    },
    {
      title: 'CLO đánh giá',
      dataIndex: 'clos',
      width: 180,
      render: (_, record) => (
        <Select
          mode="multiple"
          value={record.clos}
          onChange={(value) => updateAssessmentMethod(record.id, 'clos', value)}
          placeholder="Chọn CLO"
          style={{ width: '100%' }}
        >
          {clos.map((clo) => (
            <Option key={clo.code} value={clo.code}>
              {clo.code}
            </Option>
          ))}
        </Select>
      ),
    },
    {
      title: 'Tiêu chí',
      dataIndex: 'criteria',
      width: 150,
      render: (_, record) => (
        <Input
          value={record.criteria}
          onChange={(e) => updateAssessmentMethod(record.id, 'criteria', e.target.value)}
          placeholder="A1.2, A2.1"
        />
      ),
    },
    {
      title: 'Trọng số (%)',
      dataIndex: 'weight',
      width: 120,
      render: (_, record) => (
        <InputNumber
          value={record.weight}
          onChange={(value) => updateAssessmentMethod(record.id, 'weight', value || 0)}
          min={0}
          max={100}
          style={{ width: '100%' }}
        />
      ),
    },
    {
      title: '',
      key: 'actions',
      width: 50,
      render: (_, record) => (
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          onClick={() => deleteAssessmentMethod(record.id)}
        />
      ),
    },
  ];

  const totalWeight = clos.reduce((sum, clo) => sum + clo.weight, 0);
  const totalAssessmentWeight = assessmentMethods.reduce((sum, method) => sum + method.weight, 0);

  return (
    <div style={{ padding: '24px' }}>
      <Card
        title={id ? 'Chỉnh sửa Đề cương' : 'Tạo Đề cương mới'}
        extra={
          !id && (
            <Button icon={<CopyOutlined />} onClick={() => setCopyModalVisible(true)}>
              Copy từ đề cương có sẵn
            </Button>
          )
        }
      >
        <Form form={form} layout="vertical" disabled={loading}>
          <Divider orientation="left">Thông tin Học phần</Divider>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Khoa/Bộ môn"
                name="departmentId"
                rules={[{ required: true, message: 'Vui lòng chọn khoa' }]}
              >
                <Select
                  placeholder="Chọn khoa"
                  onChange={(value) => setSelectedDepartment(value)}
                >
                  {departments.map((dept) => (
                    <Option key={dept.id} value={dept.id}>
                      {dept.name}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="Chương trình đào tạo"
                name="programId"
                rules={[{ required: true, message: 'Vui lòng chọn chương trình' }]}
              >
                <Select placeholder="Chọn chương trình">
                  {programs
                    .filter((p) => p.departmentId === selectedDepartment)
                    .map((program) => (
                      <Option key={program.id} value={program.id}>
                        {program.name}
                      </Option>
                    ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                label="Mã học phần"
                name="subjectCode"
                rules={[{ required: true, message: 'Vui lòng chọn mã học phần' }]}
              >
                <Select 
                  placeholder="Chọn môn học"
                  onChange={handleCourseSelect}
                  showSearch
                  filterOption={(input, option) =>
                    (option?.children as string).toLowerCase().includes(input.toLowerCase())
                  }
                >
                  {availableCourses
                    .filter((c) => c.departmentId === selectedDepartment || !selectedDepartment)
                    .map((course) => (
                      <Option key={course.id} value={course.code}>
                        {course.code} - {course.name}
                      </Option>
                    ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="Tên học phần"
                name="subjectName"
                rules={[{ required: true, message: 'Vui lòng nhập tên học phần' }]}
              >
                <Input placeholder="Cơ sở dữ liệu" disabled />
              </Form.Item>
            </Col>
            <Col span={4}>
              <Form.Item
                label="Số tín chỉ"
                name="credits"
                rules={[{ required: true, message: 'Nhập số tín chỉ' }]}
              >
                <InputNumber min={1} max={10} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Tên tiếng Anh"
                name="subjectNameEN"
                rules={[{ required: true, message: 'Vui lòng nhập tên tiếng Anh' }]}
              >
                <Input placeholder="Database Systems" />
              </Form.Item>
            </Col>
            <Col span={6}>
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
            </Col>
            <Col span={6}>
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
            </Col>
          </Row>

          <Divider orientation="left">Phân bổ Thời gian</Divider>
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item
                label="Lý thuyết (tiết)"
                name={['timeAllocation', 'theory']}
                rules={[{ required: true, message: 'Nhập số tiết LT' }]}
              >
                <InputNumber min={0} style={{ width: '100%' }} placeholder="30" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                label="Thực hành (tiết)"
                name={['timeAllocation', 'practice']}
                rules={[{ required: true, message: 'Nhập số tiết TH' }]}
              >
                <InputNumber min={0} style={{ width: '100%' }} placeholder="30" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                label="Tự học (tiết)"
                name={['timeAllocation', 'selfStudy']}
                rules={[{ required: true, message: 'Nhập số tiết tự học' }]}
              >
                <InputNumber min={0} style={{ width: '100%' }} placeholder="90" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                label="Thang điểm"
                name="gradeScale"
                initialValue={10}
              >
                <Select>
                  <Option value={10}>Thang điểm 10</Option>
                  <Option value={4}>Thang điểm 4</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Học kỳ"
                name="semesterId"
                rules={[{ required: true, message: 'Vui lòng chọn học kỳ' }]}
              >
                <Select placeholder="Chọn học kỳ">
                  {semesters.map((sem) => (
                    <Option key={sem.id} value={sem.id}>
                      {sem.name}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Năm học" name="academicYear">
                <Input placeholder="2024-2025" />
              </Form.Item>
            </Col>
          </Row>

          <Divider orientation="left">Nội dung Đề cương</Divider>

          <Form.Item label="Mô tả học phần" name="description">
            <TextArea rows={3} placeholder="Mô tả tổng quan về học phần..." />
          </Form.Item>

          <Form.Item label="Mục tiêu học phần" name="objectives">
            <TextArea rows={4} placeholder="Mục tiêu sau khi hoàn thành học phần..." />
          </Form.Item>

          <Form.Item label="Phương pháp giảng dạy" name="teachingMethod">
            <TextArea rows={2} placeholder="Giảng lý thuyết, thực hành..." />
          </Form.Item>

          <Form.Item label="Phương pháp đánh giá" name="assessmentMethod">
            <TextArea rows={2} placeholder="Thi giữa kỳ 30%, cuối kỳ 40%, thực hành 30%..." />
          </Form.Item>

          <Form.Item label="Giáo trình chính" name="textbooks">
            <TextArea rows={2} placeholder="Danh sách giáo trình..." />
          </Form.Item>

          <Form.Item label="Tài liệu tham khảo" name="references">
            <TextArea rows={2} placeholder="Danh sách tài liệu tham khảo..." />
          </Form.Item>

          <Form.Item label="Nhiệm vụ của Sinh viên" name="studentDuties">
            <TextArea 
              rows={4} 
              placeholder="- Tham gia đầy đủ các buổi học (tối thiểu 80% số tiết)&#10;- Hoàn thành đầy đủ các bài tập được giao&#10;- Tham gia tích cực vào các hoạt động học tập nhóm&#10;- Tự nghiên cứu và chuẩn bị trước nội dung bài học..." 
            />
          </Form.Item>

          <Divider orientation="left">Học phần Tiên quyết</Divider>

          <div style={{ marginBottom: 16, padding: '12px', background: '#e6f7ff', borderRadius: '4px', border: '1px solid #91d5ff' }}>
            <Space>
              <span style={{ fontSize: '14px', color: '#0050b3' }}>
                ℹ️ <strong>Lưu ý:</strong> Các môn tiên quyết được tự động tải từ thông tin môn học (do Phòng Đào tạo thiết lập khi tạo môn học).
              </span>
            </Space>
          </div>

          <Space direction="vertical" style={{ width: '100%', marginBottom: 16 }}>
            <div style={{ color: '#8c8c8c', fontSize: '13px', marginBottom: 8 }}>
              Nếu cần thêm môn học khuyến nghị, bạn có thể chọn thêm bên dưới:
            </div>
            <Space wrap>
              <Select
                placeholder="Thêm học phần bắt buộc (nếu cần)"
                style={{ width: 300 }}
                onChange={(value) => addPrerequisite(value, 'required')}
                value={undefined}
              >
                {availableCourses
                  .filter((c) => c.departmentId === selectedDepartment)
                  .filter((c) => !prerequisites.find((p) => p.id === c.id))
                  .map((course) => (
                    <Option key={course.id} value={course.id}>
                      {course.code} - {course.name}
                    </Option>
                  ))}
              </Select>
              <Select
                placeholder="Thêm học phần khuyến nghị"
                style={{ width: 300 }}
                onChange={(value) => addPrerequisite(value, 'recommended')}
                value={undefined}
              >
                {availableCourses
                  .filter((c) => c.departmentId === selectedDepartment)
                  .filter((c) => !prerequisites.find((p) => p.id === c.id))
                  .map((course) => (
                    <Option key={course.id} value={course.id}>
                      {course.code} - {course.name}
                    </Option>
                  ))}
              </Select>
            </Space>

            {prerequisites.length > 0 && (
              <Table
                dataSource={prerequisites}
                rowKey="id"
                size="small"
                pagination={false}
                columns={[
                  { title: 'Mã HP', dataIndex: 'code', width: 100 },
                  { title: 'Tên học phần', dataIndex: 'name' },
                  {
                    title: 'Loại',
                    dataIndex: 'type',
                    width: 150,
                    render: (type, record) => (
                      <Select
                        value={type}
                        onChange={(value) => changePrerequisiteType(record.id, value)}
                        size="small"
                        style={{ width: '100%' }}
                        disabled={record.id.startsWith('prereq-')} // Disable nếu là prereq từ course data
                      >
                        <Option value="required">
                          <Tag color="red">Bắt buộc</Tag>
                        </Option>
                        <Option value="recommended">
                          <Tag color="blue">Khuyến nghị</Tag>
                        </Option>
                      </Select>
                    ),
                  },
                  {
                    title: '',
                    key: 'action',
                    width: 60,
                    render: (_, record) => {
                      const isFromCourseData = record.id.startsWith('prereq-');
                      return (
                        <Button
                          type="text"
                          danger
                          size="small"
                          icon={<DeleteOutlined />}
                          onClick={() => removePrerequisite(record.id)}
                          disabled={isFromCourseData}
                          title={isFromCourseData ? 'Môn này do Phòng Đào tạo thiết lập, không thể xóa' : 'Xóa môn tiên quyết'}
                        />
                      );
                    },
                  },
                ]}
              />
            )}
          </Space>

          <Divider orientation="left">
            Chuẩn đầu ra học phần (CLO)
            <Tag color={totalWeight === 100 ? 'success' : 'error'} style={{ marginLeft: 8 }}>
              Tổng: {totalWeight}%
            </Tag>
          </Divider>

          <Table
            columns={cloColumns}
            dataSource={clos}
            rowKey="id"
            pagination={false}
            size="small"
            style={{ marginBottom: 16 }}
          />

          <Button type="dashed" icon={<PlusOutlined />} onClick={addCLO} block>
            Thêm CLO
          </Button>

          <Divider orientation="left">
            Ma trận Đánh giá
            {assessmentMethods.length > 0 && (
              <Tag color={totalAssessmentWeight === 100 ? 'success' : 'error'} style={{ marginLeft: 8 }}>
                Tổng: {totalAssessmentWeight}%
              </Tag>
            )}
          </Divider>

          <Table
            columns={assessmentColumns}
            dataSource={assessmentMethods}
            rowKey="id"
            pagination={false}
            size="small"
            style={{ marginBottom: 16 }}
          />

          <Button type="dashed" icon={<PlusOutlined />} onClick={addAssessmentMethod} block>
            Thêm phương pháp đánh giá
          </Button>

          <Divider />

          <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
            <Button onClick={() => navigate('/lecturer/syllabi')}>Hủy</Button>
            <Button icon={<SaveOutlined />} onClick={() => handleSave('DRAFT')} loading={loading}>
              Lưu bản nháp
            </Button>
            <Button
              type="primary"
              icon={<SendOutlined />}
              onClick={() => handleSave('SUBMIT')}
              loading={loading}
            >
              Gửi phê duyệt
            </Button>
          </Space>
        </Form>
      </Card>

      {/* Copy Modal */}
      <Modal
        title="Copy từ đề cương có sẵn"
        open={copyModalVisible}
        onCancel={() => setCopyModalVisible(false)}
        footer={null}
        width={700}
      >
        <p style={{ marginBottom: 16 }}>
          Chọn đề cương cùng khoa/bộ môn để copy nội dung:
        </p>
        <Table
          dataSource={existingSyllabi.filter((s) => s.departmentId === selectedDepartment)}
          rowKey="id"
          pagination={false}
          size="small"
          columns={[
            { title: 'Mã HP', dataIndex: 'subjectCode', width: 100 },
            { title: 'Tên học phần', dataIndex: 'subjectName' },
            { title: 'Học kỳ', dataIndex: 'semester', width: 150 },
            { title: 'Phiên bản', dataIndex: 'version', width: 100 },
            {
              title: '',
              key: 'action',
              width: 80,
              render: (_, record) => (
                <Button
                  type="primary"
                  size="small"
                  icon={<CopyOutlined />}
                  onClick={() => handleCopyFromExisting(record.id)}
                >
                  Copy
                </Button>
              ),
            },
          ]}
        />
      </Modal>

      {/* PI Mapping Modal */}
      <PIMappingModal
        visible={piMappingModalVisible}
        onCancel={() => {
          setPiMappingModalVisible(false);
          setCurrentCLOForMapping(null);
        }}
        onSave={savePiMappings}
        clo={currentCLOForMapping}
        plos={plos}
      />
    </div>
  );
};

// PI Mapping Modal Component
interface PIMappingModalProps {
  visible: boolean;
  onCancel: () => void;
  onSave: (mappings: { ploId: string; piCode: string; level: 'H' | 'M' | 'L' }[]) => void;
  clo: CLO | null;
  plos: any[];
}

const PIMappingModal: React.FC<PIMappingModalProps> = ({ visible, onCancel, onSave, clo, plos }) => {
  const [mappings, setMappings] = useState<{ ploId: string; piCode: string; level: 'H' | 'M' | 'L' }[]>([]);

  useEffect(() => {
    if (clo && clo.piMappings) {
      setMappings(clo.piMappings);
    } else {
      setMappings([]);
    }
  }, [clo]);

  const toggleMapping = (ploId: string, piCode: string) => {
    const existing = mappings.find(m => m.ploId === ploId && m.piCode === piCode);
    if (existing) {
      setMappings(mappings.filter(m => !(m.ploId === ploId && m.piCode === piCode)));
    } else {
      setMappings([...mappings, { ploId, piCode, level: 'M' }]);
    }
  };

  const updateLevel = (ploId: string, piCode: string, level: 'H' | 'M' | 'L') => {
    setMappings(mappings.map(m => 
      m.ploId === ploId && m.piCode === piCode ? { ...m, level } : m
    ));
  };

  const isSelected = (ploId: string, piCode: string) => {
    return mappings.some(m => m.ploId === ploId && m.piCode === piCode);
  };

  const getLevel = (ploId: string, piCode: string) => {
    return mappings.find(m => m.ploId === ploId && m.piCode === piCode)?.level || 'M';
  };

  return (
    <Modal
      title={`Ánh xạ PLO-PI cho ${clo?.code || ''}`}
      open={visible}
      onCancel={onCancel}
      onOk={() => onSave(mappings)}
      width={900}
      okText="Lưu"
      cancelText="Hủy"
    >
      <p style={{ marginBottom: 16, color: '#666' }}>
        Chọn các Performance Indicator (PI) cụ thể và mức độ đóng góp (H: Cao, M: Trung bình, L: Thấp)
      </p>
      
      {plos.map(plo => (
        <Card key={plo.id} size="small" style={{ marginBottom: 12 }}>
          <div style={{ fontWeight: 'bold', marginBottom: 8 }}>
            {plo.code} - {plo.description}
          </div>
          <Space wrap>
            {plo.pis.map((pi: any) => {
              const selected = isSelected(plo.id, pi.code);
              const level = getLevel(plo.id, pi.code);
              return (
                <div key={pi.code} style={{ marginBottom: 8 }}>
                  <Space>
                    <Button
                      size="small"
                      type={selected ? 'primary' : 'default'}
                      onClick={() => toggleMapping(plo.id, pi.code)}
                    >
                      {pi.code}
                    </Button>
                    {selected && (
                      <Select
                        size="small"
                        value={level}
                        onChange={(value) => updateLevel(plo.id, pi.code, value)}
                        style={{ width: 60 }}
                      >
                        <Option value="H">H</Option>
                        <Option value="M">M</Option>
                        <Option value="L">L</Option>
                      </Select>
                    )}
                  </Space>
                  <div style={{ fontSize: '11px', color: '#999', marginTop: 2 }}>
                    {pi.description}
                  </div>
                </div>
              );
            })}
          </Space>
        </Card>
      ))}

      {mappings.length > 0 && (
        <div style={{ marginTop: 16, padding: 12, background: '#f0f2f5', borderRadius: 4 }}>
          <strong>Đã chọn ({mappings.length}):</strong>{' '}
          {mappings.map(m => {
            const plo = plos.find(p => p.id === m.ploId);
            return `${plo?.code}/${m.piCode}(${m.level})`;
          }).join(', ')}
        </div>
      )}
    </Modal>
  );
};

export default SyllabusFormPage;
