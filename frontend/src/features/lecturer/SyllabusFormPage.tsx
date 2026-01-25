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
  Row,
  Col,
  Divider,
  Tag,
  Spin,
  List,
  Avatar,
  Typography,
  Alert,
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  SaveOutlined,
  SendOutlined,
  ArrowLeftOutlined,
  CommentOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams, useLocation, useSearchParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { syllabusService, revisionService } from '@/services';
import { SyllabusStatus } from '@/types';
import type { ColumnsType } from 'antd/es/table';

const { TextArea } = Input;
const { Option } = Select;
const { Text } = Typography;

// Interfaces
interface CLO {
  id: string;
  code: string;
  description: string;
  bloomLevel: string;
  weight: number;
  mappedPLOs: string[];
}

interface AssessmentMethod {
  id: string;
  method: string;
  form: string;
  clos: string[];
  criteria: string;
  weight: number;
}

interface PrerequisiteCourse {
  id: string;
  code: string;
  name: string;
  type: 'required' | 'recommended';
}

// Mock data (will move to API later)
const departments = [
  { id: '1', name: 'Khoa Công nghệ Thông tin' },
  { id: '2', name: 'Khoa Điện - Điện tử' },
];

const semesters = [
  { id: '1', name: 'HK1 2024-2025', year: '2024-2025', semester: 1 },
  { id: '2', name: 'HK2 2024-2025', year: '2024-2025', semester: 2 },
];

const bloomLevels = ['Nhớ', 'Hiểu', 'Áp dụng', 'Phân tích', 'Đánh giá', 'Sáng tạo'];

const SyllabusFormPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [searchParams] = useSearchParams();

  // Get assignmentId from query params
  const assignmentId = searchParams.get('assignmentId');

  // Detect mode
  const isCreateMode = location.pathname.includes('/create');
  const isEditMode = location.pathname.includes('/edit');
  const isViewMode = !isCreateMode && !isEditMode && !!id;
  const mode = isCreateMode ? 'create' : isEditMode ? 'edit' : 'view';

  const [clos, setCLOs] = useState<CLO[]>([]);
  const [assessmentMethods, setAssessmentMethods] = useState<AssessmentMethod[]>([]);
  const [prerequisites, setPrerequisites] = useState<PrerequisiteCourse[]>([]);
  const [newComment, setNewComment] = useState('');

  // Auto-create syllabus from teaching assignment
  const { data: autoCreatedSyllabus, isLoading: isAutoCreating, error: autoCreateError } = useQuery({
    queryKey: ['create-syllabus-from-assignment', assignmentId],
    queryFn: () => syllabusService.createSyllabusFromAssignment(assignmentId!),
    enabled: !!assignmentId && isCreateMode,
    retry: false,
  });

  // Handle auto-create result - redirect to edit mode
  useEffect(() => {
    if (autoCreatedSyllabus && assignmentId && isCreateMode) {
      message.success('Đã tạo bản nháp đề cương từ nhiệm vụ được giao');
      navigate(`/lecturer/syllabi/edit/${autoCreatedSyllabus.id}`, { replace: true });
    }
  }, [autoCreatedSyllabus, assignmentId, isCreateMode, navigate]);

  // Handle auto-create error
  useEffect(() => {
    if (autoCreateError) {
      message.error('Không thể tạo đề cương từ nhiệm vụ');
    }
  }, [autoCreateError]);

  // Fetch syllabus data (edit/view mode)
  const { data: syllabus, isLoading: isSyllabusLoading } = useQuery({
    queryKey: ['syllabus', id],
    queryFn: () => syllabusService.getSyllabusById(id!),
    enabled: !!id && !isCreateMode,
  });

  // Fetch active revision session (if any)
  const { data: activeRevisionSession } = useQuery({
    queryKey: ['active-revision-session', id],
    queryFn: () => revisionService.getActiveRevisionSession(id!),
    enabled: !!id && !isCreateMode,
  });

  // Fetch comments (view/edit mode)
  const { data: comments = [], isLoading: isCommentsLoading } = useQuery({
    queryKey: ['syllabus-comments', id],
    queryFn: () => syllabusService.getComments(id!),
    enabled: !!id,
  });

  // Add comment mutation
  const addCommentMutation = useMutation({
    mutationFn: (content: string) => syllabusService.addComment(id!, content),
    onSuccess: () => {
      message.success('Góp ý đã được gửi');
      queryClient.invalidateQueries({ queryKey: ['syllabus-comments', id] });
      setNewComment('');
    },
    onError: () => {
      message.error('Gửi góp ý thất bại');
    },
  });

  // Create syllabus mutation
  const createMutation = useMutation({
    mutationFn: (data: any) => syllabusService.createSyllabus(data),
    onSuccess: (response) => {
      message.success('Đề cương đã được tạo');
      navigate(`/lecturer/syllabi/${response.id}`);
    },
    onError: () => {
      message.error('Tạo đề cương thất bại');
    },
  });

  // Update syllabus mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) =>
      syllabusService.updateSyllabus(id, data),
    onSuccess: () => {
      message.success('Đề cương đã được cập nhật');
      queryClient.invalidateQueries({ queryKey: ['syllabus', id] });
    },
    onError: () => {
      message.error('Cập nhật đề cương thất bại');
    },
  });

  // Submit for approval mutation (for DRAFT/REJECTED status)
  const submitMutation = useMutation({
    mutationFn: (syllabusId: string) => syllabusService.submitForApproval(syllabusId),
    onSuccess: () => {
      message.success('Đề cương đã được gửi phê duyệt');
      queryClient.invalidateQueries({ queryKey: ['syllabus', id] });
      navigate('/lecturer/syllabi');
    },
    onError: () => {
      message.error('Gửi phê duyệt thất bại');
    },
  });

  // Submit revision mutation (for REVISION_IN_PROGRESS status)
  const submitRevisionMutation = useMutation({
    mutationFn: ({ sessionId, summary }: { sessionId: string; summary?: string }) =>
      revisionService.submitRevision({
        revisionSessionId: sessionId,
        summary,
      }),
    onSuccess: () => {
      message.success('Đã gửi revision cho Trưởng Bộ môn duyệt');
      queryClient.invalidateQueries({ queryKey: ['syllabus', id] });
      queryClient.invalidateQueries({ queryKey: ['active-revision-session', id] });
      navigate('/lecturer/syllabi');
    },
    onError: () => {
      message.error('Gửi revision thất bại');
    },
  });

  // Load syllabus data into form
  useEffect(() => {
    if (syllabus && (isEditMode || isViewMode)) {
      // Parse content from JSONB - cast to any since content not in type
      const syllabusAny = syllabus as any;
      const content = syllabusAny.content || {};

      form.setFieldsValue({
        subjectCode: syllabus.subjectCode,
        subjectName: syllabus.subjectNameVi,
        credits: syllabus.creditCount,
        semester: syllabus.semester,
        academicYear: syllabus.academicYear,
        department: syllabus.department,
        description: syllabus.description || content.description || '',
        objectives: syllabus.objectives?.join('\n') || content.objectives || '',
        teachingMethod: content.teachingMethods || '',
        assessmentMethod: typeof content.gradingPolicy === 'object' && content.gradingPolicy
          ? Object.entries(content.gradingPolicy)
              .map(([key, value]) => {
                const label = key === 'midterm' ? 'Giữa kỳ' 
                  : key === 'final' ? 'Cuối kỳ'
                  : key === 'assignments' ? 'Bài tập'
                  : key === 'project' ? 'Dự án'
                  : key;
                return `${label}: ${value}%`;
              })
              .join(', ')
          : content.gradingPolicy || '', // If saved as string, use directly
        textbooks: Array.isArray(content.textbooks)
          ? content.textbooks.map((book: any) => `${book.title} - ${book.authors} (${book.year})`).join('\n')
          : content.textbooks || '',
        references: content.references || '',
        studentDuties: content.studentDuties || '',
      });

      // Load CLOs from content
      if (content.clos && Array.isArray(content.clos)) {
        setCLOs(content.clos);
      } else if (syllabus.clos && Array.isArray(syllabus.clos)) {
        setCLOs(
          syllabus.clos.map((clo: any) => ({
            id: clo.id || Date.now().toString(),
            code: clo.code,
            description: clo.description,
            bloomLevel: clo.bloomLevel || 'Hiểu',
            weight: Number(clo.weight) || 0,
            mappedPLOs: [],
          }))
        );
      }

      // Load assessment methods
      if (content.assessmentMethods && Array.isArray(content.assessmentMethods)) {
        setAssessmentMethods(
          content.assessmentMethods.map((method: any, index: number) => ({
            id: method.id || `am-${index}`,
            method: method.name || method.method || '',
            form: method.type || method.form || '',
            weight: Number(method.weight) || 0,
            description: method.description || '',
          }))
        );
      }

      // Load prerequisites
      if (content.prerequisites && Array.isArray(content.prerequisites)) {
        setPrerequisites(content.prerequisites);
      }
    }
  }, [syllabus, isEditMode, isViewMode, form]);

  // CLO functions
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

  // Assessment functions
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
    setAssessmentMethods(
      assessmentMethods.map((method) => (method.id === id ? { ...method, [field]: value } : method))
    );
  };

  const deleteAssessmentMethod = (id: string) => {
    setAssessmentMethods(assessmentMethods.filter((method) => method.id !== id));
  };

  // Handle save
  const handleSave = async (shouldSubmit: boolean = false) => {
    try {
      const values = await form.validateFields();

      // Validate CLOs
      if (clos.length === 0) {
        message.error('Vui lòng thêm ít nhất 1 CLO');
        return;
      }

      const totalWeight = clos.reduce((sum, clo) => sum + clo.weight, 0);
      if (totalWeight !== 100) {
        message.error(`Tổng trọng số CLO phải bằng 100% (hiện tại: ${totalWeight}%)`);
        return;
      }

      // Build content object - match JSONB structure from V31 migration
      // Auto-suggest PLO mappings if not manually set
      let ploMappings: Array<{
        cloCode: string;
        ploCode: string;
        contributionLevel: string;
      }> = [];
      
      // Check if any CLO has manual PLO mappings
      const hasManualMappings = clos.some(clo => clo.mappedPLOs && clo.mappedPLOs.length > 0);
      
      if (hasManualMappings) {
        // Use manual mappings from form
        clos.forEach(clo => {
          if (clo.mappedPLOs && clo.mappedPLOs.length > 0) {
            clo.mappedPLOs.forEach(ploCode => {
              ploMappings.push({
                cloCode: clo.code,
                ploCode: ploCode,
                contributionLevel: 'M' // Default to Main
              });
            });
          }
        });
      } else {
        // Auto-suggest PLO mappings from backend
        try {
          const response = await fetch('/api/syllabi/suggest-plo-mappings', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(clos)
          });
          
          if (response.ok) {
            const result = await response.json();
            ploMappings = result.data || [];
          } else {
            console.warn('⚠️ Failed to auto-suggest PLO mappings, continuing without them');
          }
        } catch (error) {
          console.error('❌ Error auto-suggesting PLO mappings:', error);
          // Continue without PLO mappings rather than failing
        }
      }

      const content = {
        description: values.description,
        objectives: values.objectives,
        teachingMethods: values.teachingMethod, // Match JSONB field name (plural)
        gradingPolicy: values.assessmentMethod, // Store as string for now, can parse later
        textbooks: values.textbooks?.split('\n').filter((line: string) => line.trim()).map((line: string) => {
          // Parse "Title - Author (Year)" format
          const match = line.match(/^(.+?)\s*-\s*(.+?)\s*\((\d{4})\)$/);
          if (match) {
            return { title: match[1].trim(), authors: match[2].trim(), year: match[3] };
          }
          return { title: line.trim(), authors: '', year: '' };
        }) || [],
        references: values.references,
        studentDuties: values.studentDuties,
        clos,
        assessmentMethods,
        prerequisites,
        ploMappings, // Add PLO mappings extracted from CLOs
      };


      const payload = {
        subjectId: (syllabus as any)?.subjectId || (syllabus as any)?.id || 'temp-subject-id',
        versionNo: `v${syllabus?.version || 1}`,
        content,
        description: values.description,
        objectives: values.objectives,
        teachingAssignmentId: assignmentId || undefined, // Link to teaching assignment if from notification
      };

      // Handle different scenarios based on create/edit mode and revision status
      if (isCreateMode) {
        // Create new syllabus
        const response = await createMutation.mutateAsync(payload);
        if (shouldSubmit && response.id) {
          await submitMutation.mutateAsync(response.id);
        }
      } else if (id) {
        // Update existing syllabus
        await updateMutation.mutateAsync({ id, data: payload });
        
        // If shouldSubmit, check if this is a revision or normal submit
        if (shouldSubmit) {
          if (activeRevisionSession) {
            // Submit revision to HOD for re-approval
            await submitRevisionMutation.mutateAsync({
              sessionId: activeRevisionSession.id,
              summary: 'Đã hoàn thành chỉnh sửa theo phản hồi',
            });
          } else {
            // Normal submit for approval (DRAFT → PENDING_HOD)
            await submitMutation.mutateAsync(id);
          }
        }
      }
    } catch (error) {
      console.error('Save error:', error);
    }
  };

  const handleAddComment = () => {
    if (newComment.trim()) {
      addCommentMutation.mutate(newComment);
    }
  };

  // Calculate totals
  const totalWeight = clos.reduce((sum, clo) => sum + clo.weight, 0);
  const totalAssessmentWeight = assessmentMethods.reduce((sum, m) => sum + m.weight, 0);

  // CLO Table Columns
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
          placeholder="Mô tả CLO..."
          rows={2}
        />
      ),
    },
    {
      title: 'Mức Bloom',
      dataIndex: 'bloomLevel',
      width: 150,
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
      title: 'PLO tương ứng',
      dataIndex: 'mappedPLOs',
      width: 180,
      render: (_, record) => (
        <Select
          mode="multiple"
          value={record.mappedPLOs}
          onChange={(value) => updateCLO(record.id, 'mappedPLOs', value)}
          placeholder="Chọn PLO"
          style={{ width: '100%' }}
        >
          <Option key="PLO1" value="PLO1">PLO1</Option>
          <Option key="PLO2" value="PLO2">PLO2</Option>
          <Option key="PLO3" value="PLO3">PLO3</Option>
          <Option key="PLO4" value="PLO4">PLO4</Option>
          <Option key="PLO5" value="PLO5">PLO5</Option>
          <Option key="PLO6" value="PLO6">PLO6</Option>
        </Select>
      ),
    },
    {
      title: '',
      key: 'action',
      width: 60,
      render: (_, record) => (
        <Button
          type="text"
          danger
          size="small"
          icon={<DeleteOutlined />}
          onClick={() => deleteCLO(record.id)}
        />
      ),
    },
  ];

  // Assessment Table Columns
  const assessmentColumns: ColumnsType<AssessmentMethod> = [
    {
      title: 'Phương pháp',
      dataIndex: 'method',
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
          onChange={(value) => updateAssessmentMethod(record.id, 'weight', value || 0)}
          min={0}
          max={100}
          style={{ width: '100%' }}
        />
      ),
    },
    {
      title: '',
      key: 'action',
      width: 60,
      render: (_, record) => (
        <Button
          type="text"
          danger
          size="small"
          icon={<DeleteOutlined />}
          onClick={() => deleteAssessmentMethod(record.id)}
        />
      ),
    },
  ];

  // Show loading when fetching syllabus or auto-creating from assignment
  if (isSyllabusLoading || isAutoCreating) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', minHeight: '400px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Spin spinning={true} size="large">
          <div style={{ padding: '50px' }}>
            Đang tải đề cương...
          </div>
        </Spin>
      </div>
    );
  }

  const canEdit =
    isCreateMode ||
    (syllabus &&
      (syllabus.status === SyllabusStatus.DRAFT ||
        syllabus.status === SyllabusStatus.REJECTED ||
        syllabus.status === SyllabusStatus.REVISION_IN_PROGRESS));

  return (
    <div style={{ padding: '24px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/lecturer/syllabi')}>
          Quay lại
        </Button>

        {/* Show feedbacks if in revision mode */}
        {activeRevisionSession && activeRevisionSession.feedbacks && activeRevisionSession.feedbacks.length > 0 && (
          <Alert
            message={`Có ${activeRevisionSession.feedbacks.length} lỗi cần chỉnh sửa`}
            description={
              <div>
                <p style={{ marginBottom: 12 }}>
                  Admin đã yêu cầu chỉnh sửa đề cương dựa trên phản hồi từ sinh viên:
                </p>
                <List
                  size="small"
                  dataSource={activeRevisionSession.feedbacks}
                  renderItem={(fb: any, index: number) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={
                          <Tag color="red">{index + 1}</Tag>
                        }
                        title={
                          <Space>
                            <Tag color="orange">{fb.type}</Tag>
                            <strong>{fb.title}</strong>
                          </Space>
                        }
                        description={
                          <div>
                            <div><strong>Phần:</strong> {fb.section}</div>
                            <div><strong>Mô tả:</strong> {fb.description}</div>
                            <div style={{ color: '#888', fontSize: 12 }}>
                              <strong>Từ sinh viên:</strong> {fb.studentName}
                            </div>
                          </div>
                        }
                      />
                    </List.Item>
                  )}
                />
              </div>
            }
            type="warning"
            showIcon
            closable={false}
          />
        )}

        <Card
          title={
            isCreateMode
              ? 'Tạo Đề cương mới'
              : isEditMode
              ? 'Chỉnh sửa Đề cương'
              : 'Chi tiết Đề cương'
          }
        >
          <style>{`
            .syllabus-form .ant-input[disabled],
            .syllabus-form .ant-input-number-input[disabled],
            .syllabus-form .ant-select-disabled .ant-select-selection-item,
            .syllabus-form textarea[disabled] {
              color: rgba(0, 0, 0, 0.88) !important;
              background-color: #fafafa !important;
              cursor: default !important;
            }
          `}</style>
          <Form form={form} layout="vertical" className="syllabus-form" disabled={!canEdit}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="Mã học phần" name="subjectCode">
                  <Input placeholder="CS401" disabled />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="Tên học phần" name="subjectName">
                  <Input placeholder="Trí tuệ nhân tạo" disabled />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item label="Số tín chỉ" name="credits">
                  <InputNumber min={1} max={10} style={{ width: '100%' }} disabled />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="Học kỳ" name="semester">
                  <Select placeholder="Chọn học kỳ" disabled>
                    {semesters.map((s) => (
                      <Option key={s.id} value={s.name}>
                        {s.name}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="Năm học" name="academicYear">
                  <Input placeholder="2024-2025" disabled />
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
              <TextArea rows={2} placeholder="Thi giữa kỳ 30%, cuối kỳ 40%..." />
            </Form.Item>

            <Form.Item label="Giáo trình chính" name="textbooks">
              <TextArea rows={2} placeholder="Danh sách giáo trình..." />
            </Form.Item>

            <Form.Item label="Tài liệu tham khảo" name="references">
              <TextArea rows={2} placeholder="Danh sách tài liệu tham khảo..." />
            </Form.Item>

            <Form.Item label="Nhiệm vụ của Sinh viên" name="studentDuties">
              <TextArea rows={4} placeholder="Nhiệm vụ của sinh viên..." />
            </Form.Item>

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

            {canEdit && (
              <Button type="dashed" icon={<PlusOutlined />} onClick={addCLO} block>
                Thêm CLO
              </Button>
            )}

            <Divider orientation="left">
              Ma trận Đánh giá
              {assessmentMethods.length > 0 && (
                <Tag
                  color={totalAssessmentWeight === 100 ? 'success' : 'error'}
                  style={{ marginLeft: 8 }}
                >
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

            {canEdit && (
              <Button
                type="dashed"
                icon={<PlusOutlined />}
                onClick={addAssessmentMethod}
                block
              >
                Thêm phương pháp đánh giá
              </Button>
            )}

            <Divider />

            {canEdit && (
              <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                <Button onClick={() => navigate('/lecturer/syllabi')}>Hủy</Button>
                <Button
                  icon={<SaveOutlined />}
                  onClick={() => handleSave(false)}
                  loading={createMutation.isPending || updateMutation.isPending}
                >
                  Lưu bản nháp
                </Button>
                <Button
                  type="primary"
                  icon={<SendOutlined />}
                  onClick={() => handleSave(true)}
                  loading={submitMutation.isPending}
                >
                  Gửi phê duyệt
                </Button>
              </Space>
            )}
          </Form>
        </Card>

        {/* Comments Section - Show in view/edit mode */}
        {!isCreateMode && (
          <Card
            title={
              <Space>
                <CommentOutlined />
                <span>Góp ý & Thảo luận ({comments.length})</span>
              </Space>
            }
            loading={isCommentsLoading}
          >
            {comments.length > 0 ? (
              <List
                dataSource={comments}
                renderItem={(comment: any) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<Avatar icon={<UserOutlined />} />}
                      title={
                        <Space>
                          <Text strong>{comment.createdByName}</Text>
                          <Text type="secondary" style={{ fontSize: '12px' }}>
                            {new Date(comment.createdAt).toLocaleString('vi-VN')}
                          </Text>
                        </Space>
                      }
                      description={
                        <div>
                          {comment.section && (
                            <Tag color="blue" style={{ marginBottom: 8 }}>
                              {comment.section}
                            </Tag>
                          )}
                          <div>{comment.content}</div>
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <Text type="secondary">Chưa có góp ý nào</Text>
            )}

            <Divider />

            <Space direction="vertical" style={{ width: '100%' }}>
              <TextArea
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                placeholder="Nhập góp ý của bạn..."
                rows={3}
                onPressEnter={(e) => {
                  if (e.ctrlKey) {
                    handleAddComment();
                  }
                }}
              />
              <div style={{ textAlign: 'right' }}>
                <Button
                  type="primary"
                  icon={<SendOutlined />}
                  onClick={handleAddComment}
                  disabled={!newComment.trim()}
                  loading={addCommentMutation.isPending}
                >
                  Gửi góp ý
                </Button>
              </div>
            </Space>
          </Card>
        )}
      </Space>
    </div>
  );
};

export default SyllabusFormPage;
