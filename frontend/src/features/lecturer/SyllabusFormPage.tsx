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
// --- SỬA 1: Import axiosClient thay vì axios thường ---
import axiosClient from '../../api/axiosClient'; 
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

// --- Interfaces ---
interface CLO {
  id: string;
  code: string;
  description: string;
  bloomLevel: string;
  weight: number;
  mappedPLOs: string[];
  piMappings?: { ploId: string; piCode: string; level: 'H' | 'M' | 'L' }[];
}

interface PrerequisiteCourse {
  id: string;
  code: string;
  name: string;
  type: 'required' | 'recommended';
}

interface AssessmentMethod {
  id: string;
  method: string;
  form: string;
  clos: string[];
  criteria: string;
  weight: number;
}

// --- Static Data ---
const bloomLevels = ['Nhớ', 'Hiểu', 'Áp dụng', 'Phân tích', 'Đánh giá', 'Sáng tạo'];

// Mock PLO
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
    ]
  },
];

// Mock Syllabus cũ
const existingSyllabi = [
  {
    id: '1',
    subjectCode: 'CS101',
    subjectName: 'Lập trình căn bản',
    semester: 'HK1 2024-2025',
    version: 'v2.0',
    departmentId: '1',
  },
];

// --- Main Component ---
const SyllabusFormPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  
  // State dữ liệu từ DB
  const [dbDepartments, setDbDepartments] = useState<any[]>([]);
  const [dbCourses, setDbCourses] = useState<any[]>([]);
  const [dbSemesters, setDbSemesters] = useState<any[]>([]);
  
  // State quản lý Form
  const [clos, setCLOs] = useState<CLO[]>([]);
  const [prerequisites, setPrerequisites] = useState<PrerequisiteCourse[]>([]);
  const [assessmentMethods, setAssessmentMethods] = useState<AssessmentMethod[]>([]);
  const [selectedDepartment, setSelectedDepartment] = useState<string>('');
  const [copyModalVisible, setCopyModalVisible] = useState(false);
  const [piMappingModalVisible, setPiMappingModalVisible] = useState(false);
  const [currentCLOForMapping, setCurrentCLOForMapping] = useState<CLO | null>(null);

  // 1. Gọi API lấy dữ liệu thật (SỬA: Dùng axiosClient)
  useEffect(() => {
    const fetchMetadata = async () => {
      try {
        // axiosClient đã có baseURL và tự động gắn Token
        const [deptRes, courseRes, semRes] = await Promise.all([
          axiosClient.get('/departments'),
          // Use public endpoints provided by backend
          axiosClient.get('/public/subjects'),
          axiosClient.get('/public/semesters')
        ]);
        // Debug: show full responses to help diagnose empty lists
        console.log('DEPARTMENTS RESPONSE (full):', deptRes);
        console.log('SUBJECTS RESPONSE (full):', courseRes);
        console.log('SEMESTERS RESPONSE (full):', semRes);

        // 1) Departments: API may return paged result { success, data: { content: [...] } }
        const deptContent = Array.isArray(deptRes?.data?.data?.content)
          ? deptRes.data.data.content
          : Array.isArray(deptRes?.data)
            ? deptRes.data
            : [];
        setDbDepartments(deptContent);
        console.log('Đã load danh sách bộ môn (normalized):', deptContent);

        // 2) Subjects (courses): handle either paged ({ success, data: { content }}) or plain list
        const courseContent = Array.isArray(courseRes?.data?.data?.content)
          ? courseRes.data.data.content
          : Array.isArray(courseRes?.data)
            ? courseRes.data
            : Array.isArray(courseRes?.data?.data)
              ? courseRes.data.data
              : [];
        setDbCourses(courseContent);
        console.log('Đã load danh sách môn học (normalized):', courseContent);

        // 3) Semesters: similar handling
        const semContent = Array.isArray(semRes?.data?.data?.content)
          ? semRes.data.data.content
          : Array.isArray(semRes?.data)
            ? semRes.data
            : Array.isArray(semRes?.data?.data)
              ? semRes.data.data
              : [];
        setDbSemesters(semContent);
        console.log('Đã load danh sách học kỳ (normalized):', semContent);
      } catch (error) {
        console.error("Lỗi tải dữ liệu danh mục:", error);
        // Không spam message error ở đây để tránh khó chịu khi load trang
      }
    };
    fetchMetadata();
  }, []);

  // 2. Logic chọn môn học (Select trả về subjectId UUID)
  const handleCourseSelect = (subjectId: string) => {
    const course = dbCourses.find((c: any) => c.id === subjectId);
    if (course) {
      form.setFieldsValue({
        subjectName: course.currentNameVi || course.name,
        credits: course.defaultCredits || course.credits,
      });
      setPrerequisites([]);
    }
  };

  // --- Các hàm Logic giữ nguyên ---
  const handleCopyFromExisting = (syllabusId: string) => {
    const selected = existingSyllabi.find((s) => s.id === syllabusId);
    if (selected) {
      message.success(`Đã copy từ ${selected.subjectCode}`);
      setCopyModalVisible(false);
      // Try to find subject by code and set subjectId if available
      const found = dbCourses.find((c: any) => c.code === selected.subjectCode);
      form.setFieldsValue({
        subjectId: found ? found.id : undefined,
        subjectName: selected.subjectName,
        description: 'Nội dung được copy từ đề cương cũ...',
      });
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

  // --- Logic Lưu dữ liệu vào DB ---
  const handleSave = async (status: 'DRAFT' | 'SUBMIT') => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      // Validate
      if (clos.length === 0) {
        message.error('Vui lòng thêm ít nhất 1 CLO');
        setLoading(false); return;
      }
      const totalWeight = clos.reduce((sum, clo) => sum + clo.weight, 0);
      if (totalWeight !== 100) {
        message.error(`Tổng trọng số CLO phải bằng 100% (hiện tại: ${totalWeight}%)`);
        setLoading(false); return;
      }

      // Build payload matching backend SyllabusRequest
      const payload = {
        subjectId: values.subjectId,
        academicTermId: values.semesterId,
        versionNo: values.versionNo || '1.0',
        status: status === 'DRAFT' ? 'DRAFT' : 'PENDING_APPROVAL',
        // content - nested JSON to be stored as JSONB
        content: {
          description: values.description,
          objectives: values.objectives,
          programId: values.programId,
          academicYear: values.academicYear,
          clos: clos,
          assessmentMethods: assessmentMethods,
          prerequisites: prerequisites,
        }
      };

      // Call backend create endpoint (plural path)
      await axiosClient.post('/syllabus', payload);

      message.success(status === 'SUBMIT' ? 'Đã gửi đề cương thành công!' : 'Đã lưu nháp thành công!');
      navigate('/lecturer/syllabi');

    } catch (error) {
      console.error(error);
      message.error('Lỗi khi lưu đề cương. Vui lòng thử lại!');
    } finally {
      setLoading(false);
    }
  };

  // --- Columns Configuration ---
  const cloColumns: ColumnsType<CLO> = [
    {
      title: 'Mã CLO',
      dataIndex: 'code',
      width: 100,
      render: (_, record) => (
        <Input value={record.code} onChange={(e) => updateCLO(record.id, 'code', e.target.value)} />
      ),
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      render: (_, record) => (
        <TextArea value={record.description} onChange={(e) => updateCLO(record.id, 'description', e.target.value)} />
      ),
    },
    {
      title: 'Bloom',
      dataIndex: 'bloomLevel',
      width: 130,
      render: (_, record) => (
        <Select value={record.bloomLevel} onChange={(value) => updateCLO(record.id, 'bloomLevel', value)}>
          {bloomLevels.map(l => <Option key={l} value={l}>{l}</Option>)}
        </Select>
      ),
    },
    {
      title: 'Trọng số (%)',
      dataIndex: 'weight',
      width: 100,
      render: (_, record) => (
        <InputNumber value={record.weight} onChange={(value) => updateCLO(record.id, 'weight', value || 0)} min={0} max={100} />
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
          </Space>
        ),
    },
    {
      title: '',
      width: 50,
      render: (_, record) => (
        <Button type="text" danger icon={<DeleteOutlined />} onClick={() => deleteCLO(record.id)} />
      ),
    },
  ];

  const assessmentColumns: ColumnsType<AssessmentMethod> = [
    {
      title: 'Phương pháp',
      dataIndex: 'method',
      render: (_, record) => (
        <Input value={record.method} onChange={(e) => updateAssessmentMethod(record.id, 'method', e.target.value)} />
      ),
    },
    {
        title: 'Trọng số (%)',
        dataIndex: 'weight',
        width: 120,
        render: (_, record) => (
          <InputNumber value={record.weight} onChange={(value) => updateAssessmentMethod(record.id, 'weight', value || 0)} min={0} max={100} />
        ),
    },
    {
      title: '',
      width: 50,
      render: (_, record) => (
        <Button type="text" danger icon={<DeleteOutlined />} onClick={() => deleteAssessmentMethod(record.id)} />
      ),
    },
  ];

  const totalWeight = clos.reduce((sum, clo) => sum + clo.weight, 0);
  const totalAssessmentWeight = assessmentMethods.reduce((sum, method) => sum + method.weight, 0);

  return (
    <div style={{ padding: '24px' }}>
      <Card
        title={id ? 'Chỉnh sửa Đề cương' : 'Tạo Đề cương mới'}
        extra={!id && <Button icon={<CopyOutlined />} onClick={() => setCopyModalVisible(true)}>Copy mẫu</Button>}
      >
        <Form form={form} layout="vertical" disabled={loading}>
          <Divider orientation="left">Thông tin Học phần</Divider>
          
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="Khoa/Bộ môn" name="departmentId" rules={[{ required: true, message: 'Vui lòng chọn khoa' }]}>
                <Select placeholder="Chọn khoa" onChange={(value) => setSelectedDepartment(value)}>
                  {dbDepartments.map((dept: any) => (
                    <Option key={dept.id} value={dept.id}>{dept.name}</Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Chương trình đào tạo" name="programId">
                 <Select placeholder="Chọn chương trình" disabled>
                    <Option value="1">Hệ thống thông tin</Option>
                 </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="Mã học phần" name="subjectId" rules={[{ required: true, message: 'Vui lòng chọn môn học' }]}>
                <Select 
                  placeholder="Chọn môn học" 
                  onChange={handleCourseSelect} 
                  showSearch
                  filterOption={(input, option) =>
                    (option?.children as unknown as string).toLowerCase().includes(input.toLowerCase())
                  }
                >
                  {dbCourses
                    // Include courses when no department selected, or when departmentId matches,
                    // also include courses with null/undefined departmentId to avoid empty lists.
                    .filter((c: any) => !selectedDepartment || c.departmentId === selectedDepartment || c.departmentId == null)
                    .map((course: any) => (
                      // IMPORTANT: value is course.id (UUID)
                      <Option key={course.id} value={course.id}>
                        {course.code} - {course.currentNameVi || course.name}
                      </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Tên học phần" name="subjectName" rules={[{ required: true }]}>
                <Input disabled />
              </Form.Item>
            </Col>
            <Col span={4}>
              <Form.Item label="Số tín chỉ" name="credits" rules={[{ required: true }]}>
                <InputNumber style={{ width: '100%' }} min={1} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="Học kỳ" name="semesterId" rules={[{ required: true, message: 'Vui lòng chọn học kỳ' }]}>
                <Select placeholder="Chọn học kỳ">
                  {dbSemesters.map((sem: any) => (
                    <Option key={sem.id} value={sem.id}>
                      {sem.name || sem.termName || sem.code} {sem.academicYear ? `(${sem.academicYear})` : ''}
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
          <Form.Item label="Mô tả học phần" name="description"><TextArea rows={3} /></Form.Item>
          <Form.Item label="Mục tiêu" name="objectives"><TextArea rows={3} /></Form.Item>

          <Divider orientation="left">
            Chuẩn đầu ra (CLO) <Tag color={totalWeight === 100 ? 'success' : 'error'}>Tổng: {totalWeight}%</Tag>
          </Divider>
          <Table columns={cloColumns} dataSource={clos} rowKey="id" pagination={false} size="small" />
          <Button type="dashed" onClick={addCLO} block icon={<PlusOutlined />} style={{ marginTop: 8 }}>Thêm CLO</Button>

          <Divider orientation="left">
            Đánh giá <Tag color={totalAssessmentWeight === 100 ? 'success' : 'error'}>Tổng: {totalAssessmentWeight}%</Tag>
          </Divider>
          <Table columns={assessmentColumns} dataSource={assessmentMethods} rowKey="id" pagination={false} size="small" />
          <Button type="dashed" onClick={addAssessmentMethod} block icon={<PlusOutlined />} style={{ marginTop: 8 }}>Thêm Đánh giá</Button>

          <Divider />
          <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
            <Button onClick={() => navigate('/lecturer/syllabi')}>Hủy</Button>
            <Button icon={<SaveOutlined />} onClick={() => handleSave('DRAFT')} loading={loading}>Lưu nháp</Button>
            <Button type="primary" icon={<SendOutlined />} onClick={() => handleSave('SUBMIT')} loading={loading}>Gửi phê duyệt</Button>
          </Space>
        </Form>
      </Card>

      {/* Copy Modal */}
      <Modal title="Copy từ đề cương cũ" open={copyModalVisible} onCancel={() => setCopyModalVisible(false)} footer={null}>
        <Table 
            dataSource={existingSyllabi} 
            rowKey="id" 
            size="small"
            columns={[
                { title: 'Mã HP', dataIndex: 'subjectCode' },
                { title: 'Tên HP', dataIndex: 'subjectName' },
                { title: 'Action', render: (_, r) => <Button size="small" onClick={() => handleCopyFromExisting(r.id)}>Chọn</Button> }
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

// --- Sub-Component: PI Mapping Modal ---
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
    </Modal>
  );
};

export default SyllabusFormPage;