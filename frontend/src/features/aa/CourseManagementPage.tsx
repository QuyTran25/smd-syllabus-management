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
  Tabs,
  Switch,
} from 'antd';
import { EyeOutlined, EditOutlined, DeleteOutlined, PlusOutlined, LinkOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import { subjectService, Subject } from '../../services/subject.service';
import { academicTermService, AcademicTerm } from '../../services/academic-term.service';
import { apiClient as api } from '@/config/api-config';

const { Option } = Select;
const { Text } = Typography;
const { TextArea } = Input;

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
  const [isRelationModalVisible, setIsRelationModalVisible] = useState(false);
  const [editingCourse, setEditingCourse] = useState<CourseDisplay | null>(null);
  const [selectedAcademicTerm, setSelectedAcademicTerm] = useState<AcademicTerm | null>(null);
  const [selectedFacultyId, setSelectedFacultyId] = useState<string | undefined>(undefined);
  const [calculatedCredits, setCalculatedCredits] = useState<number | null>(null);
  const [creditsWarning, setCreditsWarning] = useState<string>('');
  const [form] = Form.useForm();
  const [relationForm] = Form.useForm();
  const queryClient = useQueryClient();

  // Fetch courses from API (sorted by createdAt descending - newest first)
  const { data: subjectsRaw, isLoading, error } = useQuery({
    queryKey: ['subjects'],
    queryFn: async () => {
      const subjects = await subjectService.getAllSubjects();
      // Sort by createdAt descending so newest subjects appear first
      return subjects.sort((a, b) => {
        const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
        const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
        return dateB - dateA;
      });
    },
  });

  // Fetch academic terms
  const { data: academicTerms } = useQuery({
    queryKey: ['academicTerms'],
    queryFn: () => academicTermService.getAllTerms(),
  });

  // Fetch faculties (Khoa)
  const { data: faculties } = useQuery({
    queryKey: ['faculties'],
    queryFn: async () => {
      const response = await api.get('/api/faculties/all');
      return response.data.data;
    },
  });

  // Fetch departments by faculty
  const { data: departmentsByFaculty } = useQuery({
    queryKey: ['departments', selectedFacultyId],
    queryFn: async () => {
      if (!selectedFacultyId) return [];
      const response = await api.get(`/api/faculties/${selectedFacultyId}/departments`);
      return response.data.data;
    },
    enabled: !!selectedFacultyId,
  });

  // Handle academic term selection
  const handleAcademicTermChange = (termId: string) => {
    const term = academicTerms?.find((t: AcademicTerm) => t.id === termId);
    setSelectedAcademicTerm(term || null);
    form.setFieldValue('academicTermId', termId);
  };

  // Auto-calculate hours when credits change
  const handleCreditsChange = (credits: number | null) => {
    if (!credits || credits <= 0) return;
    
    // Gợi ý: LT = 30/tín chỉ, TH = 30/tín chỉ
    const suggestedTheory = credits * 15 + 15;
    const suggestedPractice = credits * 15 + 15;
    const suggestedSelfStudy = (suggestedTheory + suggestedPractice) * 2;
    
    form.setFieldsValue({
      theoryHours: suggestedTheory,
      practiceHours: suggestedPractice,
      selfStudyHours: suggestedSelfStudy,
    });
    
    setCalculatedCredits(credits);
    setCreditsWarning('');
  };

  // Auto-calculate credits when hours change
  const handleHoursChange = () => {
    const theoryHours = form.getFieldValue('theoryHours') || 0;
    const practiceHours = form.getFieldValue('practiceHours') || 0;
    
    if (theoryHours === 0 && practiceHours === 0) {
      setCalculatedCredits(null);
      setCreditsWarning('');
      return;
    }
    
    // Công thức: Credits = LT/15 + TH/30
    const calculated = theoryHours / 15 + practiceHours / 30;
    const rounded = Math.floor(calculated * 2) / 2; // Làm tròn xuống 0.5
    
    setCalculatedCredits(rounded);
    
    // Kiểm tra có hợp lệ không (chỉ chấp nhận số nguyên hoặc .5)
    const isValid = calculated === rounded || Math.abs(calculated - rounded) < 0.01;
    
    if (!isValid) {
      setCreditsWarning(
        `⚠️ Số tiết không chuẩn! Tính ra ${calculated.toFixed(2)} tín chỉ, đã làm tròn xuống ${rounded} tín chỉ.`
      );
    } else {
      setCreditsWarning('');
    }
    
    // Auto-update credits field
    form.setFieldValue('credits', rounded);
  };

  // Validate mã môn học
  const validateSubjectCode = async (code: string) => {
    if (!code) return;
    
    // Regex: 2-6 ký tự HOA + 2 chữ số
    const regex = /^[A-Z]{2,6}[0-9]{2}$/;
    
    if (!regex.test(code)) {
      throw new Error('Mã môn học phải có dạng: 2-6 chữ IN HOA + 2 chữ số (VD: CSDL26)');
    }
    
    // Check trùng mã
    try {
      const exists = courses.some(c => c.code.toUpperCase() === code.toUpperCase() && c.id !== editingCourse?.id);
      if (exists) {
        throw new Error('Mã môn học đã tồn tại!');
      }
    } catch (error) {
      throw error;
    }
  };

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

  // Check cycle mutation
  const checkCycleMutation = useMutation({
    mutationFn: async (data: { subjectId: string; prerequisiteId: string }) => {
      // Tạm thời check đơn giản: không cho prerequisite chính nó
      if (data.subjectId === data.prerequisiteId) {
        throw new Error('Không thể chọn chính môn học này làm học phần tiên quyết!');
      }
      
      // TODO: Gọi backend API để check cycle bằng DFS
      // const response = await api.get(`/api/subjects/check-cycle`, {
      //   params: { subjectId: data.subjectId, prerequisiteId: data.prerequisiteId }
      // });
      // if (response.data.hasCycle) {
      //   throw new Error('Phát hiện vòng lặp phụ thuộc! Không thể thêm quan hệ này.');
      // }
      
      return { hasCycle: false };
    },
  });

  // Add prerequisite mutation
  const addPrerequisiteMutation = useMutation({
    mutationFn: async ({ subjectId, prerequisiteData }: { subjectId: string; prerequisiteData: any }) => {
      // Check cycle trước khi thêm
      await checkCycleMutation.mutateAsync({
        subjectId,
        prerequisiteId: prerequisiteData.relatedSubjectId,
      });
      
      const response = await api.post(`/api/subjects/${subjectId}/prerequisites`, prerequisiteData);
      return response.data;
    },
    onSuccess: () => {
      message.success('Thêm quan hệ môn học thành công');
      setIsRelationModalVisible(false);
      relationForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ['subjects'] });
    },
    onError: (error: any) => {
      message.error(error?.message || error?.response?.data?.message || 'Thêm quan hệ thất bại');
    },
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
      title: 'Hành động',
      key: 'actions',
      width: 220,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={async () => {
              setEditingCourse(record);
              
              // Fetch full subject details to get departmentId and other fields
              try {
                const fullSubject = await subjectService.getSubjectById(record.id);
                if (!fullSubject) {
                  message.error('Không tìm thấy môn học');
                  return;
                }
                
                // Set faculty ID from department's parent organization
                let facultyId = undefined;
                if (fullSubject.departmentId) {
                  // Fetch department to get its parent faculty
                  const deptResponse = await api.get(`/api/departments/${fullSubject.departmentId}`);
                  const department = deptResponse.data.data;
                  // API trả về facultyId, không phải parentId
                  if (department.facultyId) {
                    facultyId = department.facultyId;
                    setSelectedFacultyId(department.facultyId);
                  }
                }
                
                // Get active academic term for edit (if not stored with subject)
                const activeTermId = academicTerms?.find((t: AcademicTerm) => t.isActive)?.id;
                
                // Mở modal trước
                setIsFormModalVisible(true);
                
                // Đợi một chút để departmentsByFaculty được load sau khi setSelectedFacultyId
                setTimeout(() => {
                  form.setFieldsValue({
                    academicTermId: activeTermId, // Mặc định học kỳ đang hoạt động
                    code: fullSubject.code,
                    name: fullSubject.currentNameVi,
                    nameEn: fullSubject.currentNameEn,
                    credits: fullSubject.defaultCredits,
                    subjectType: fullSubject.subjectType || 'REQUIRED',
                    component: fullSubject.component || 'BOTH',
                    departmentId: fullSubject.departmentId,
                    facultyId: facultyId,
                    theoryHours: fullSubject.defaultTheoryHours || 0,
                    practiceHours: fullSubject.defaultPracticeHours || 0,
                    selfStudyHours: fullSubject.defaultSelfStudyHours || 0,
                    description: fullSubject.description,
                    recommendedTerm: fullSubject.recommendedTerm,
                    isActive: fullSubject.isActive !== false,
                  });
                }, 300);
              } catch (error) {
                console.error('Error loading subject details:', error);
                message.error('Không thể tải thông tin môn học');
              }
            }}
          >
            Sửa
          </Button>
          <Button
            type="link"
            size="small"
            icon={<LinkOutlined />}
            onClick={() => {
              setSelectedCourse(record);
              setIsRelationModalVisible(true);
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
            <Button type="link" danger size="small" icon={<DeleteOutlined />}>
              Xóa
            </Button>
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
          scroll={{ x: 1500 }}
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
        onCancel={() => {
          setIsFormModalVisible(false);
          setEditingCourse(null);
          setSelectedAcademicTerm(null);
          setSelectedFacultyId(undefined);
          form.resetFields();
        }}
        footer={null}
        width={800}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={async (values) => {
            // Prevent double submit
            if (createMutation.isPending || updateMutation.isPending) {
              console.log('Mutation is pending, skipping...');
              return;
            }
            
            // Map form values to backend format
            const payload = {
              code: values.code,
              departmentId: values.departmentId,
              academicTermId: values.academicTermId, // Thêm academicTermId
              currentNameVi: values.name,
              currentNameEn: values.nameEn,
              defaultCredits: values.credits,
              subjectType: values.subjectType,
              component: values.component,
              defaultTheoryHours: values.theoryHours,
              defaultPracticeHours: values.practiceHours,
              defaultSelfStudyHours: values.selfStudyHours,
              description: values.description,
              recommendedTerm: values.recommendedTerm,
              isActive: values.isActive !== false,
            };

            try {
              if (editingCourse) {
                await updateMutation.mutateAsync({ id: editingCourse.id, values: payload });
              } else {
                await createMutation.mutateAsync(payload);
              }
            } catch (error) {
              // Error handled by mutation onError
              console.error('Submit error:', error);
            }
          }}
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
                      name="academicTermId"
                      rules={[{ required: true, message: 'Chọn học kỳ' }]}
                    >
                      <Select
                        placeholder="Chọn học kỳ (tự động điền thông số)"
                        onChange={handleAcademicTermChange}
                      >
                        {academicTerms?.map((term: AcademicTerm) => (
                          <Option key={term.id} value={term.id}>
                            {term.name}{' '}
                            {term.isActive && (
                              <Tag color="green" style={{ marginLeft: 8 }}>
                                Đang hoạt động
                              </Tag>
                            )}
                          </Option>
                        ))}
                      </Select>
                    </Form.Item>

                    {selectedAcademicTerm && (
                      <Card size="small" style={{ marginBottom: 16, backgroundColor: '#f0f5ff' }}>
                        <Space direction="vertical" size={4}>
                          <Text type="secondary">
                            <strong>Thông số học kỳ (tham khảo):</strong>
                          </Text>
                          <Text>
                            • Năm học: <strong>{selectedAcademicTerm.academicYear}</strong>
                          </Text>
                          <Text>
                            • Từ: <strong>{selectedAcademicTerm.startDate}</strong> đến{' '}
                            <strong>{selectedAcademicTerm.endDate}</strong>
                          </Text>
                        </Space>
                      </Card>
                    )}

                    <Form.Item
                      label="Mã môn học"
                      name="code"
                      rules={[
                        { required: true, message: 'Nhập mã môn học' },
                        {
                          validator: async (_, value) => {
                            if (value) {
                              await validateSubjectCode(value);
                            }
                          },
                        },
                      ]}
                      extra="Format: 2-6 chữ IN HOA + 2 chữ số năm (VD: CSDL26, TTNT26)"
                    >
                      <Input
                        placeholder="VD: CSDL26"
                        disabled={!!editingCourse}
                        maxLength={8}
                        style={{ textTransform: 'uppercase' }}
                      />
                    </Form.Item>

                    <Form.Item
                      label="Tên môn học (Tiếng Việt)"
                      name="name"
                      rules={[{ required: true, message: 'Nhập tên môn học' }]}
                    >
                      <Input placeholder="VD: Nhập môn Lập trình" />
                    </Form.Item>

                    <Form.Item label="Tên môn học (Tiếng Anh)" name="nameEn">
                      <Input placeholder="VD: Introduction to Programming" />
                    </Form.Item>

                    <Form.Item
                      label="Số tín chỉ"
                      name="credits"
                      rules={[{ required: true, message: 'Nhập số tín chỉ' }]}
                      extra="Nhập số tín chỉ để hệ thống gợi ý số tiết tự động"
                    >
                      <InputNumber
                        min={1}
                        max={10}
                        step={0.5}
                        style={{ width: '100%' }}
                        onChange={handleCreditsChange}
                        placeholder="VD: 3"
                      />
                    </Form.Item>

                    <Form.Item
                      label="Loại học phần"
                      name="subjectType"
                      rules={[{ required: true, message: 'Chọn loại học phần' }]}
                    >
                      <Select placeholder="Chọn loại">
                        <Option value="REQUIRED">Bắt buộc</Option>
                        <Option value="ELECTIVE">Tự chọn</Option>
                      </Select>
                    </Form.Item>

                    <Form.Item
                      label="Thành phần"
                      name="component"
                      rules={[{ required: true, message: 'Chọn thành phần' }]}
                    >
                      <Select placeholder="Chọn thành phần">
                        <Option value="THEORY">Lý thuyết</Option>
                        <Option value="PRACTICE">Thực hành</Option>
                        <Option value="BOTH">Lý thuyết + Thực hành</Option>
                      </Select>
                    </Form.Item>

                    <Form.Item label="Mô tả" name="description">
                      <TextArea rows={3} placeholder="Mô tả ngắn về môn học..." />
                    </Form.Item>

                    <Form.Item label="Kỳ học khuyến nghị" name="recommendedTerm">
                      <InputNumber min={1} max={10} placeholder="VD: 3 (học kỳ 3)" style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item 
                      label="Trạng thái" 
                      name="isActive" 
                      valuePropName="checked"
                      initialValue={true}
                    >
                      <Switch 
                        checkedChildren="Hoạt động" 
                        unCheckedChildren="Ẩn" 
                      />
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
                      name="facultyId"
                      rules={[{ required: true, message: 'Chọn khoa' }]}
                    >
                      <Select
                        placeholder="Chọn khoa"
                        onChange={(value) => {
                          setSelectedFacultyId(value);
                          form.setFieldValue('departmentId', undefined);
                        }}
                      >
                        {faculties?.map((faculty: any) => (
                          <Option key={faculty.id} value={faculty.id}>
                            {faculty.name}
                          </Option>
                        ))}
                      </Select>
                    </Form.Item>

                    <Form.Item
                      label="Bộ môn"
                      name="departmentId"
                      rules={[{ required: true, message: 'Chọn bộ môn' }]}
                    >
                      <Select placeholder="Chọn bộ môn" disabled={!selectedFacultyId}>
                        {departmentsByFaculty?.map((dept: any) => (
                          <Option key={dept.id} value={dept.id}>
                            {dept.name}
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
                    {creditsWarning && (
                      <Alert
                        message={creditsWarning}
                        type="warning"
                        showIcon
                        style={{ marginBottom: 16 }}
                      />
                    )}
                    
                    <Form.Item
                      label="Số tiết Lý thuyết"
                      name="theoryHours"
                      rules={[{ required: true, message: 'Nhập số tiết lý thuyết' }]}
                      extra={calculatedCredits ? `Tính theo công thức: ≈ ${form.getFieldValue('theoryHours') || 0}/15 tín chỉ` : ''}
                    >
                      <InputNumber
                        min={0}
                        style={{ width: '100%' }}
                        placeholder="VD: 30"
                        onChange={handleHoursChange}
                      />
                    </Form.Item>

                    <Form.Item
                      label="Số tiết Thực hành"
                      name="practiceHours"
                      rules={[{ required: true, message: 'Nhập số tiết thực hành' }]}
                      extra={calculatedCredits ? `Tính theo công thức: ≈ ${form.getFieldValue('practiceHours') || 0}/30 tín chỉ` : ''}
                    >
                      <InputNumber
                        min={0}
                        style={{ width: '100%' }}
                        placeholder="VD: 30"
                        onChange={handleHoursChange}
                      />
                    </Form.Item>

                    <Form.Item
                      label="Số tiết Tự học"
                      name="selfStudyHours"
                      rules={[{ required: true, message: 'Nhập số tiết tự học' }]}
                    >
                      <InputNumber min={0} style={{ width: '100%' }} placeholder="VD: 60" />
                    </Form.Item>

                    <Alert
                      message="Lưu ý"
                      description={
                        <div>
                          <p>• Tổng số tiết = Lý thuyết + Thực hành + Tự học</p>
                          <p>• Thông thường: 1 tín chỉ = 15 tiết lý thuyết hoặc 30 tiết thực hành</p>
                        </div>
                      }
                      type="info"
                      showIcon
                    />
                  </>
                ),
              },
            ]}
          />

          <Form.Item style={{ marginBottom: 0, marginTop: 16 }}>
            <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
              <Button
                onClick={() => {
                  setIsFormModalVisible(false);
                  setEditingCourse(null);
                  setSelectedAcademicTerm(null);
                  setSelectedFacultyId(undefined);
                  form.resetFields();
                }}
              >
                Hủy
              </Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={createMutation.isPending || updateMutation.isPending}
              >
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
              {selectedCourse.prerequisites && selectedCourse.prerequisites.length > 0 ? (
                <Space direction="vertical" style={{ width: '100%' }}>
                  {selectedCourse.prerequisites.split(',').map((p, idx) => (
                    <Tag key={idx} color="orange">
                      {p.trim()}
                    </Tag>
                  ))}
                </Space>
              ) : (
                <span style={{ color: '#999' }}>Chưa có môn tiên quyết</span>
              )}
            </Card>

            <Card title="Môn học Song hành" size="small">
              <span style={{ color: '#999' }}>Chưa có môn song hành</span>
            </Card>

            <Form
              form={relationForm}
              layout="vertical"
              onFinish={(values) => {
                if (!selectedCourse) return;

                // Map form values to backend enum: PREREQUISITE, CO_REQUISITE
                const prerequisiteData = {
                  relatedSubjectId: values.relatedCourseId,
                  type: values.relationType, // Trực tiếp dùng PREREQUISITE hoặc CO_REQUISITE
                };

                addPrerequisiteMutation.mutate({
                  subjectId: selectedCourse.id,
                  prerequisiteData,
                });
              }}
            >
              <Form.Item
                label="Loại quan hệ"
                name="relationType"
                rules={[{ required: true, message: 'Chọn loại quan hệ' }]}
              >
                <Select placeholder="Chọn loại">
                  <Option value="PREREQUISITE">Tiên quyết</Option>
                  <Option value="CO_REQUISITE">Song hành</Option>
                </Select>
              </Form.Item>

              <Form.Item
                label="Chọn môn học"
                name="relatedCourseId"
                rules={[{ required: true, message: 'Chọn môn học' }]}
              >
                <Select
                  placeholder="Tìm môn học..."
                  showSearch
                  filterOption={(input, option) =>
                    (option?.children as string).toLowerCase().includes(input.toLowerCase())
                  }
                >
                  {courses
                    .filter((c) => c.id !== selectedCourse.id)
                    .map((course) => (
                      <Option key={course.id} value={course.id}>
                        {course.code} - {course.name}
                      </Option>
                    ))}
                </Select>
              </Form.Item>

              <Form.Item style={{ marginBottom: 0 }}>
                <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                  <Button onClick={() => relationForm.resetFields()}>Xóa</Button>
                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={addPrerequisiteMutation.isPending}
                  >
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
