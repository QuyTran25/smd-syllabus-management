import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Descriptions,
  Tag,
  Button,
  Space,
  Typography,
  Tabs,
  Table,
  Timeline,
  Modal,
  Input,
  message,
  Spin,
  Divider,
  DatePicker,
  Popconfirm,
} from 'antd';
import {
  CheckOutlined,
  CloseOutlined,
  ArrowLeftOutlined,
  StopOutlined,
  InboxOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { syllabusService, aiService } from '@/services';
import { SyllabusStatus, UserRole, ApprovalAction } from '@/types';
import { useAuth } from '../auth';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { VersionComparisonModal } from '../hod/VersionComparisonModal';

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;

export const SyllabusDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const [isApproveModalVisible, setIsApproveModalVisible] = useState(false);
  const [isRejectModalVisible, setIsRejectModalVisible] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [effectiveDate, setEffectiveDate] = useState<string | null>(null);
  const [isVersionCompareVisible, setIsVersionCompareVisible] = useState(false);
  const [isPLOComplianceVisible, setIsPLOComplianceVisible] = useState(false);

  // Fetch syllabus detail
  const { data: syllabus, isLoading } = useQuery({
    queryKey: ['syllabus', id],
    queryFn: () => syllabusService.getSyllabusById(id!),
    enabled: !!id,
  });

  // Fetch comments
  const { data: comments } = useQuery({
    queryKey: ['syllabus-comments', id],
    queryFn: () => syllabusService.getComments(id!),
    enabled: !!id,
  });

  // Fetch PLO compliance (AA only)
  const { data: ploCompliance, isLoading: isComplianceLoading } = useQuery({
    queryKey: ['plo-compliance', id],
    queryFn: () => aiService.checkPLOCompliance(id!),
    enabled: !!id && isPLOComplianceVisible && user?.role === UserRole.AA,
  });

  // Approval mutation
  const approveMutation = useMutation({
    mutationFn: (action: ApprovalAction) => syllabusService.approveSyllabus(action),
    onSuccess: () => {
      message.success('Phê duyệt thành công');
      queryClient.invalidateQueries({ queryKey: ['syllabus', id] });
      queryClient.invalidateQueries({ queryKey: ['syllabi'] });
      setIsApproveModalVisible(false);
    },
    onError: () => {
      message.error('Phê duyệt thất bại');
    },
  });

  // Reject mutation
  const rejectMutation = useMutation({
    mutationFn: (action: ApprovalAction) => syllabusService.rejectSyllabus(action),
    onSuccess: () => {
      message.success('Đã từ chối đề cương');
      queryClient.invalidateQueries({ queryKey: ['syllabus', id] });
      queryClient.invalidateQueries({ queryKey: ['syllabi'] });
      setIsRejectModalVisible(false);
      setRejectReason('');
    },
    onError: () => {
      message.error('Từ chối thất bại');
    },
  });

  // Unpublish mutation (Admin only)
  const unpublishMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      syllabusService.unpublishSyllabus(id, reason),
    onSuccess: () => {
      message.success('Hủy xuất bản thành công');
      queryClient.invalidateQueries({ queryKey: ['syllabus', id] });
      queryClient.invalidateQueries({ queryKey: ['syllabi'] });
    },
    onError: () => {
      message.error('Hủy xuất bản thất bại');
    },
  });

  // Archive mutation (Admin only)
  const archiveMutation = useMutation({
    mutationFn: (id: string) => syllabusService.archiveSyllabus(id),
    onSuccess: () => {
      message.success('Lưu trữ thành công');
      queryClient.invalidateQueries({ queryKey: ['syllabus', id] });
      queryClient.invalidateQueries({ queryKey: ['syllabi'] });
    },
    onError: () => {
      message.error('Lưu trữ thất bại');
    },
  });

  // Update effective date mutation (Admin only)
  const updateEffectiveDateMutation = useMutation({
    mutationFn: ({ id, date }: { id: string; date: string }) =>
      syllabusService.updateEffectiveDate(id, date),
    onSuccess: () => {
      message.success('Cập nhật ngày hiệu lực thành công');
      queryClient.invalidateQueries({ queryKey: ['syllabus', id] });
      setEffectiveDate(null);
    },
    onError: () => {
      message.error('Cập nhật ngày hiệu lực thất bại');
    },
  });

  // Check if user can approve/reject
  const canApprove = () => {
    if (!syllabus || !user) return false;

    // Principal can only approve/reject PENDING_PRINCIPAL status
    if (user.role === UserRole.PRINCIPAL) {
      return syllabus.status === SyllabusStatus.PENDING_PRINCIPAL;
    }

    const roleStatusMap = {
      [UserRole.HOD]: SyllabusStatus.PENDING_HOD,
      [UserRole.AA]: SyllabusStatus.PENDING_AA,
      [UserRole.ADMIN]: SyllabusStatus.APPROVED, // Admin can publish
    };

    return syllabus.status === roleStatusMap[user.role as keyof typeof roleStatusMap];
  };

  const handleApprove = () => {
    if (!id) return;
    approveMutation.mutate({
      syllabusId: id,
      action: 'APPROVE',
    });
  };

  const handleReject = () => {
    if (!id || !rejectReason.trim()) {
      message.warning('Vui lòng nhập lý do từ chối');
      return;
    }

    rejectMutation.mutate({
      syllabusId: id,
      action: 'REJECT',
      reason: rejectReason,
    });
  };

  if (isLoading || !syllabus) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  // CLO Table columns
  const cloColumns: ColumnsType<any> = [
    { title: 'Mã', dataIndex: 'code', key: 'code', width: 80 },
    { title: 'Mô tả', dataIndex: 'description', key: 'description' },
    { title: 'Bloom Level', dataIndex: 'bloomLevel', key: 'bloomLevel', width: 120 },
    { title: 'Trọng số (%)', dataIndex: 'weight', key: 'weight', width: 100, align: 'center' },
  ];

  // Assessment columns
  const assessmentColumns: ColumnsType<any> = [
    { title: 'Tên', dataIndex: 'name', key: 'name' },
    { title: 'Phương pháp', dataIndex: 'method', key: 'method', width: 150 },
    { title: 'Trọng số (%)', dataIndex: 'weight', key: 'weight', width: 100, align: 'center' },
  ];

  const statusConfig = {
    [SyllabusStatus.DRAFT]: { color: 'default', text: 'Nháp' },
    [SyllabusStatus.PENDING_HOD]: { color: 'orange', text: 'Chờ Trưởng Bộ môn' },
    [SyllabusStatus.PENDING_HOD_REVISION]: { color: 'gold', text: 'Chờ Trưởng Bộ môn (Sửa lỗi)' },
    [SyllabusStatus.PENDING_AA]: { color: 'blue', text: 'Chờ Phòng Đào tạo' },
    [SyllabusStatus.PENDING_PRINCIPAL]: { color: 'purple', text: 'Chờ Hiệu trưởng' },
    [SyllabusStatus.APPROVED]: { color: 'green', text: 'Đã phê duyệt' },
    [SyllabusStatus.PENDING_ADMIN_REPUBLISH]: { color: 'lime', text: 'Chờ xuất bản lại' },
    [SyllabusStatus.PUBLISHED]: { color: 'cyan', text: 'Đã xuất bản' },
    [SyllabusStatus.REJECTED]: { color: 'red', text: 'Bị từ chối' },
    [SyllabusStatus.REVISION_IN_PROGRESS]: { color: 'volcano', text: 'Đang chỉnh sửa' },
    [SyllabusStatus.INACTIVE]: { color: 'default', text: 'Không hoạt động' },
    [SyllabusStatus.ARCHIVED]: { color: 'default', text: 'Đã lưu trữ' },
  };

  const currentStatus = statusConfig[syllabus.status] || { color: 'default', text: syllabus.status };

  return (
    <div>
      {/* Header */}
      <div style={{ marginBottom: 24 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/admin/syllabi')} style={{ marginBottom: 16 }}>
          Quay lại
        </Button>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <Title level={2} style={{ margin: 0 }}>
              {syllabus.subjectNameVi}
            </Title>
            <Space style={{ marginTop: 8 }}>
              <Text type="secondary">Mã môn: {syllabus.subjectCode}</Text>
              <Tag color={currentStatus.color}>{currentStatus.text}</Tag>
            </Space>
          </div>

          {/* HOD and Principal approval buttons */}
          {canApprove() && user?.role !== UserRole.AA && user?.role !== UserRole.HOD && (
            <Space>
              <Button
                type="primary"
                icon={<CheckOutlined />}
                onClick={() => setIsApproveModalVisible(true)}
                loading={approveMutation.isPending}
              >
                {user?.role === UserRole.ADMIN ? 'Xuất bản' : user?.role === UserRole.PRINCIPAL ? 'Duyệt' : 'Phê duyệt'}
              </Button>
              <Button
                danger
                icon={<CloseOutlined />}
                onClick={() => setIsRejectModalVisible(true)}
                loading={rejectMutation.isPending}
              >
                Từ chối
              </Button>
            </Space>
          )}

          {/* HoD Approval Section */}
          {user?.role === UserRole.HOD && 
           (syllabus.status === SyllabusStatus.PENDING_HOD || syllabus.status === SyllabusStatus.PENDING_HOD_REVISION) && (
            <Space>
              <Button
                type="primary"
                icon={<CheckOutlined />}
                onClick={() => {
                  const isRevision = syllabus.status === SyllabusStatus.PENDING_HOD_REVISION;
                  Modal.confirm({
                    title: 'Xác nhận duyệt đề cương',
                    content: (
                      <Space direction="vertical">
                        <Text>Bạn có chắc muốn duyệt đề cương này?</Text>
                        <Text strong>{syllabus.subjectCode} - {syllabus.subjectNameVi}</Text>
                        {isRevision ? (
                          <Text type="secondary">Đề cương sẽ được gửi cho Admin để xuất hành lại.</Text>
                        ) : (
                          <Text type="secondary">Đề cương sẽ được gửi lên Phòng Đào tạo để xét duyệt.</Text>
                        )}
                      </Space>
                    ),
                    okText: 'Duyệt',
                    cancelText: 'Hủy',
                    onOk: handleApprove,
                  });
                }}
              >
                Duyệt đề cương
              </Button>
              <Popconfirm
                title="Từ chối đề cương"
                description={
                  <div style={{ width: 300 }}>
                    <Text type="danger" strong>Lý do từ chối (bắt buộc):</Text>
                    <TextArea
                      rows={4}
                      value={rejectReason}
                      onChange={(e) => setRejectReason(e.target.value)}
                      placeholder="Nhập lý do từ chối cụ thể..."
                      style={{ marginTop: 8 }}
                    />
                  </div>
                }
                onConfirm={handleReject}
                okText="Từ chối"
                cancelText="Hủy"
                okButtonProps={{ danger: true }}
              >
                <Button danger icon={<CloseOutlined />}>
                  Từ chối
                </Button>
              </Popconfirm>
              {syllabus.previousVersionId && (
                <Button type="dashed" onClick={() => setIsVersionCompareVisible(true)}>
                  So sánh Phiên bản (AI)
                </Button>
              )}
            </Space>
          )}

          {/* Admin-only actions for published syllabi */}
          {user?.role === UserRole.ADMIN && syllabus.status === SyllabusStatus.PUBLISHED && (
            <Space>
              <Popconfirm
                title="Hủy xuất bản"
                description="Bạn có chắc muốn hủy xuất bản đề cương này?"
                onConfirm={() => unpublishMutation.mutate({ id: syllabus.id, reason: 'Admin unpublish' })}
                okText="Xác nhận"
                cancelText="Hủy"
              >
                <Button icon={<StopOutlined />} loading={unpublishMutation.isPending}>
                  Hủy xuất bản
                </Button>
              </Popconfirm>
              <Popconfirm
                title="Lưu trữ"
                description="Bạn có chắc muốn lưu trữ đề cương này?"
                onConfirm={() => archiveMutation.mutate(syllabus.id)}
                okText="Xác nhận"
                cancelText="Hủy"
              >
                <Button icon={<InboxOutlined />} loading={archiveMutation.isPending}>
                  Lưu trữ
                </Button>
              </Popconfirm>
            </Space>
          )}

          {/* AA Approval Section */}
          {user?.role === UserRole.AA && syllabus.status === SyllabusStatus.PENDING_AA && (
            <Space>
              <Button
                type="primary"
                icon={<CheckOutlined />}
                onClick={() => {
                  Modal.confirm({
                    title: 'Xác nhận duyệt đề cương',
                    content: (
                      <Space direction="vertical">
                        <Text>Bạn có chắc muốn duyệt đề cương này?</Text>
                        <Text strong>{syllabus.subjectCode} - {syllabus.subjectNameVi}</Text>
                        <Text type="secondary">Đề cương sẽ được gửi lên Hiệu trưởng để phê duyệt cuối cùng.</Text>
                      </Space>
                    ),
                    okText: 'Duyệt',
                    cancelText: 'Hủy',
                    onOk: handleApprove,
                  });
                }}
              >
                Duyệt đề cương
              </Button>
              <Popconfirm
                title="Từ chối đề cương"
                description={
                  <div style={{ width: 300 }}>
                    <Text type="danger" strong>Lý do từ chối (bắt buộc):</Text>
                    <TextArea
                      rows={4}
                      value={rejectReason}
                      onChange={(e) => setRejectReason(e.target.value)}
                      placeholder="Nhập lý do từ chối cụ thể..."
                      style={{ marginTop: 8 }}
                    />
                  </div>
                }
                onConfirm={handleReject}
                okText="Từ chối"
                cancelText="Hủy"
                okButtonProps={{ danger: true }}
              >
                <Button danger icon={<CloseOutlined />}>
                  Từ chối
                </Button>
              </Popconfirm>
              <Button type="dashed" onClick={() => setIsPLOComplianceVisible(true)}>
                Kiểm tra Tuân thủ PLO (AI)
              </Button>
            </Space>
          )}

          {/* AA PLO Compliance Check (for non-pending status) */}
          {user?.role === UserRole.AA && syllabus.status !== SyllabusStatus.PENDING_AA && (
            <Button type="dashed" onClick={() => setIsPLOComplianceVisible(true)}>
              Kiểm tra Tuân thủ PLO (AI)
            </Button>
          )}
        </div>
      </div>

      {/* Effective Date Section (Admin only, for approved/published) */}
      {user?.role === UserRole.ADMIN &&
        (syllabus.status === SyllabusStatus.APPROVED || syllabus.status === SyllabusStatus.PUBLISHED) && (
          <Card style={{ marginBottom: 24 }}>
            <Space>
              <Text strong>Ngày hiệu lực:</Text>
              {syllabus.effectiveDate ? (
                <Tag color="blue">{dayjs(syllabus.effectiveDate).format('DD/MM/YYYY')}</Tag>
              ) : (
                <Tag>Chưa thiết lập</Tag>
              )}
              <DatePicker
                value={effectiveDate ? dayjs(effectiveDate) : undefined}
                onChange={(date) => setEffectiveDate(date ? date.toISOString() : null)}
                format="DD/MM/YYYY"
              />
              <Button
                type="primary"
                size="small"
                disabled={!effectiveDate}
                loading={updateEffectiveDateMutation.isPending}
                onClick={() => effectiveDate && updateEffectiveDateMutation.mutate({ id: syllabus.id, date: effectiveDate })}
              >
                Cập nhật
              </Button>
            </Space>
          </Card>
        )}

      {/* Main Content */}
      <Tabs
        defaultActiveKey="info"
        items={[
          {
            key: 'info',
            label: 'Thông tin chung',
            children: (
              <Card>
                <Descriptions bordered column={2}>
                  <Descriptions.Item label="Tên môn học (Tiếng Việt)" span={2}>
                    {syllabus.subjectNameVi}
                  </Descriptions.Item>
                  <Descriptions.Item label="Tên môn học (Tiếng Anh)" span={2}>
                    {syllabus.subjectNameEn || <Text type="secondary">Chưa cập nhật</Text>}
                  </Descriptions.Item>
                  <Descriptions.Item label="Mã môn học">{syllabus.subjectCode}</Descriptions.Item>
                  <Descriptions.Item label="Số tín chỉ">{syllabus.creditCount}</Descriptions.Item>
                  <Descriptions.Item label="Loại học phần">
                    {syllabus.courseType === 'required' && <Tag color="red">Bắt buộc</Tag>}
                    {syllabus.courseType === 'elective' && <Tag color="blue">Tự chọn</Tag>}
                    {syllabus.courseType === 'free' && <Tag color="green">Tự chọn tự do</Tag>}
                    {!syllabus.courseType && <Text type="secondary">Chưa xác định</Text>}
                  </Descriptions.Item>
                  <Descriptions.Item label="Thành phần">
                    {syllabus.componentType === 'major' && 'Chuyên ngành'}
                    {syllabus.componentType === 'foundation' && 'Cơ sở ngành'}
                    {syllabus.componentType === 'general' && 'Đại cương'}
                    {syllabus.componentType === 'thesis' && 'Khóa luận/Thực tập'}
                    {!syllabus.componentType && <Text type="secondary">Chưa xác định</Text>}
                  </Descriptions.Item>
                  <Descriptions.Item label="Khoa">{syllabus.faculty}</Descriptions.Item>
                  <Descriptions.Item label="Bộ môn">{syllabus.department}</Descriptions.Item>
                  <Descriptions.Item label="Học kỳ">{syllabus.semester}</Descriptions.Item>
                  <Descriptions.Item label="Năm học">{syllabus.academicYear}</Descriptions.Item>
                  <Descriptions.Item label="Giảng viên">{syllabus.ownerName}</Descriptions.Item>
                  <Descriptions.Item label="Trạng thái">
                    <Tag color={currentStatus.color}>{currentStatus.text}</Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="Thang điểm">{syllabus.gradeScale || 10}</Descriptions.Item>
                  <Descriptions.Item label="Phiên bản">{syllabus.versionNumber || 1}</Descriptions.Item>
                  {syllabus.status === SyllabusStatus.REJECTED && comments && comments.length > 0 && (
                    <Descriptions.Item label="Lý do từ chối" span={2}>
                      <div style={{ padding: '12px', backgroundColor: '#fff2e8', border: '1px solid #ffbb96', borderRadius: '4px' }}>
                        <Text type="danger">
                          {comments.find(c => c.type === 'OFFICIAL' && c.content)?.content || 'Không có lý do cụ thể'}
                        </Text>
                      </div>
                    </Descriptions.Item>
                  )}
                  <Descriptions.Item label="Mục tiêu" span={2}>
                    <ul>
                      {(syllabus.objectives || []).map((obj, idx) => (
                        <li key={idx}>{obj}</li>
                      ))}
                    </ul>
                  </Descriptions.Item>
                  {syllabus.studentDuties && (
                    <Descriptions.Item label="Nhiệm vụ Sinh viên" span={2}>
                      <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{syllabus.studentDuties}</Paragraph>
                    </Descriptions.Item>
                  )}
                </Descriptions>

                <Divider />

                <Title level={4}>Phân bổ Thời gian</Title>
                <Descriptions bordered column={2}>
                  <Descriptions.Item label="Lý thuyết">
                    {syllabus.timeAllocation?.theory || syllabus.theoryHours || 0} tiết
                  </Descriptions.Item>
                  <Descriptions.Item label="Thực hành">
                    {syllabus.timeAllocation?.practice || syllabus.practiceHours || 0} tiết
                  </Descriptions.Item>
                  <Descriptions.Item label="Tự học">
                    {syllabus.timeAllocation?.selfStudy || syllabus.selfStudyHours || 0} tiết
                  </Descriptions.Item>
                  <Descriptions.Item label="Tổng số giờ">
                    {((syllabus.timeAllocation?.theory || 0) + 
                      (syllabus.timeAllocation?.practice || 0) + 
                      (syllabus.timeAllocation?.selfStudy || 0)) || syllabus.totalStudyHours || 0} tiết
                  </Descriptions.Item>
                </Descriptions>
              </Card>
            ),
          },
          {
            key: 'clo',
            label: 'CLO & PLO',
            children: (
              <Card>
                <Title level={4}>Chuẩn đầu ra môn học (CLO)</Title>
                <Table
                  columns={cloColumns}
                  dataSource={syllabus.clos || []}
                  rowKey="id"
                  pagination={false}
                  style={{ marginBottom: 24 }}
                />

                <Title level={4}>Ánh xạ CLO - PLO</Title>
                <Table
                  dataSource={(syllabus.ploMappings || []).filter(m => m.cloCode && m.ploCode)}
                  rowKey={(record, index) => record.cloCode && record.ploCode ? `${record.cloCode}-${record.ploCode}` : `mapping-${index}`}
                  pagination={false}
                  locale={{ emptyText: 'Chưa có ánh xạ CLO-PLO. Giảng viên cần cập nhật đề cương để tự động tạo ánh xạ.' }}
                  columns={[
                    { title: 'CLO', dataIndex: 'cloCode', key: 'cloCode' },
                    { title: 'PLO', dataIndex: 'ploCode', key: 'ploCode' },
                    {
                      title: 'Mức độ đóng góp',
                      dataIndex: 'contributionLevel',
                      key: 'contributionLevel',
                      render: (level: string) => {
                        const colors = { I: 'blue', R: 'orange', M: 'green' };
                        const labels = { I: 'I - Giới thiệu', R: 'R - Củng cố', M: 'M - Thành thạo' };
                        return <Tag color={colors[level as keyof typeof colors] || 'default'}>{labels[level as keyof typeof labels] || level}</Tag>;
                      },
                    },
                  ]}
                />
              </Card>
            ),
          },
          {
            key: 'assessment',
            label: 'Đánh giá',
            children: (
              <Card>
                <Title level={4}>Ma trận Đánh giá</Title>
                {syllabus.assessmentMethods && syllabus.assessmentMethods.length > 0 ? (
                  <Table
                    dataSource={syllabus.assessmentMethods}
                    rowKey="id"
                    pagination={false}
                    columns={[
                      { title: 'Phương pháp', dataIndex: 'method', key: 'method' },
                      { title: 'Hình thức', dataIndex: 'form', key: 'form' },
                      {
                        title: 'CLO đánh giá',
                        dataIndex: 'clos',
                        key: 'clos',
                        render: (clos: string[]) => (
                          <Space size="small" wrap>
                            {(clos || []).map((clo) => (
                              <Tag key={clo} color="blue">{clo}</Tag>
                            ))}
                          </Space>
                        ),
                      },
                      { title: 'Tiêu chí', dataIndex: 'criteria', key: 'criteria' },
                      {
                        title: 'Trọng số',
                        dataIndex: 'weight',
                        key: 'weight',
                        render: (weight: number) => `${weight}%`,
                      },
                    ]}
                    summary={(data) => {
                      const total = data.reduce((sum, item) => sum + item.weight, 0);
                      return (
                        <Table.Summary fixed>
                          <Table.Summary.Row>
                            <Table.Summary.Cell index={0} colSpan={4}>
                              <Text strong>Tổng</Text>
                            </Table.Summary.Cell>
                            <Table.Summary.Cell index={1}>
                              <Text strong>{total}%</Text>
                            </Table.Summary.Cell>
                          </Table.Summary.Row>
                        </Table.Summary>
                      );
                    }}
                  />
                ) : (
                  <>
                    <Title level={4}>Tiêu chí đánh giá (Cũ)</Title>
                    <Table
                      columns={assessmentColumns}
                      dataSource={syllabus.assessmentCriteria || []}
                      rowKey="id"
                      pagination={false}
                    />
                  </>
                )}
              </Card>
            ),
          },
          {
            key: 'history',
            label: 'Lịch sử phê duyệt',
            children: (
              <Card>
                <Timeline
                  items={[
                    syllabus.createdAt ? {
                      color: 'gray',
                      children: (
                        <>
                          <Text strong>Tạo đề cương</Text>
                          <br />
                          <Text type="secondary">
                            {new Date(syllabus.createdAt).toLocaleString('vi-VN')}
                          </Text>
                        </>
                      ),
                    } : null,
                    syllabus.submittedAt ? {
                      color: 'blue',
                      children: (
                        <>
                          <Text strong>Gửi phê duyệt</Text>
                          <br />
                          <Text type="secondary">
                            {new Date(syllabus.submittedAt).toLocaleString('vi-VN')}
                          </Text>
                        </>
                      ),
                    } : null,
                    syllabus.hodApprovedAt ? {
                      color: 'green',
                      children: (
                        <>
                          <Text strong>Trưởng Bộ môn phê duyệt</Text>
                          <br />
                          <Text type="secondary">
                            {new Date(syllabus.hodApprovedAt).toLocaleString('vi-VN')}
                          </Text>
                        </>
                      ),
                    } : null,
                    syllabus.aaApprovedAt ? {
                      color: 'green',
                      children: (
                        <>
                          <Text strong>Phòng Đào tạo phê duyệt</Text>
                          <br />
                          <Text type="secondary">
                            {new Date(syllabus.aaApprovedAt).toLocaleString('vi-VN')}
                          </Text>
                        </>
                      ),
                    } : null,
                    syllabus.principalApprovedAt ? {
                      color: 'green',
                      children: (
                        <>
                          <Text strong>Hiệu trưởng phê duyệt</Text>
                          <br />
                          <Text type="secondary">
                            {new Date(syllabus.principalApprovedAt).toLocaleString('vi-VN')}
                          </Text>
                        </>
                      ),
                    } : null,
                    syllabus.publishedAt ? {
                      color: 'cyan',
                      children: (
                        <>
                          <Text strong>Xuất bản</Text>
                          <br />
                          <Text type="secondary">
                            {new Date(syllabus.publishedAt).toLocaleString('vi-VN')}
                          </Text>
                        </>
                      ),
                    } : null,
                  ].filter((item): item is { color: string; children: JSX.Element } => item !== null)}
                />

                {comments && comments.length > 0 && (
                  <>
                    <Divider />
                    <Title level={4}>Nhận xét & Phản hồi</Title>
                    {comments.map((comment) => (
                      <Card key={comment.id} size="small" style={{ marginBottom: 8 }}>
                        <Space direction="vertical" style={{ width: '100%' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Text strong>{comment.userName}</Text>
                            <Text type="secondary">
                              {new Date(comment.createdAt).toLocaleString('vi-VN')}
                            </Text>
                          </div>
                          {comment.section && <Tag>{comment.section}</Tag>}
                          <Text>{comment.content}</Text>
                        </Space>
                      </Card>
                    ))}
                  </>
                )}
              </Card>
            ),
          },
        ]}
      />

      {/* Approve Modal */}
      <Modal
        title={user?.role === UserRole.PRINCIPAL ? "Xác nhận duyệt" : "Xác nhận phê duyệt"}
        open={isApproveModalVisible}
        onOk={handleApprove}
        onCancel={() => setIsApproveModalVisible(false)}
        okText={user?.role === UserRole.PRINCIPAL ? "Duyệt" : "Phê duyệt"}
        cancelText="Hủy"
        confirmLoading={approveMutation.isPending}
      >
        <p>
          Bạn có chắc chắn muốn {user?.role === UserRole.PRINCIPAL ? "duyệt" : "phê duyệt"} đề cương <strong>{syllabus.courseName}</strong>?
        </p>
      </Modal>

      {/* Reject Modal */}
      <Modal
        title="Từ chối đề cương"
        open={isRejectModalVisible}
        onOk={handleReject}
        onCancel={() => {
          setIsRejectModalVisible(false);
          setRejectReason('');
        }}
        okText="Từ chối"
        cancelText="Hủy"
        okButtonProps={{ danger: true }}
        confirmLoading={rejectMutation.isPending}
      >
        <p>Vui lòng nhập lý do từ chối:</p>
        <TextArea
          rows={4}
          value={rejectReason}
          onChange={(e) => setRejectReason(e.target.value)}
          placeholder="Nhập lý do từ chối đề cương..."
        />
      </Modal>

      {/* Version Comparison Modal (HoD) */}
      {syllabus.previousVersionId && (
        <VersionComparisonModal
          visible={isVersionCompareVisible}
          onClose={() => setIsVersionCompareVisible(false)}
          syllabusId={syllabus.id}
          oldVersionId={syllabus.previousVersionId}
        />
      )}

      {/* PLO Compliance Modal (AA) */}
      <Modal
        title="Kiểm tra Tuân thủ PLO (AI Analysis)"
        open={isPLOComplianceVisible}
        onCancel={() => setIsPLOComplianceVisible(false)}
        footer={null}
        width={800}
      >
        {isComplianceLoading ? (
          <div style={{ textAlign: 'center', padding: 40 }}>
            <Spin size="large" />
            <p style={{ marginTop: 16, color: '#999' }}>AI đang phân tích tuân thủ PLO...</p>
          </div>
        ) : ploCompliance ? (
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            {/* Compliance Status */}
            <Card size="small">
              <Space>
                <strong>Trạng thái:</strong>
                {ploCompliance.compliant ? (
                  <Tag color="green" icon={<CheckOutlined />}>Đạt yêu cầu</Tag>
                ) : (
                  <Tag color="red" icon={<CloseOutlined />}>Chưa đạt yêu cầu</Tag>
                )}
              </Space>
            </Card>

            {/* Issues */}
            {ploCompliance.issues.length > 0 && (
              <Card title="Vấn đề phát hiện" size="small">
                <Space direction="vertical" size={12} style={{ width: '100%' }}>
                  {ploCompliance.issues.map((issue, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: 12,
                        backgroundColor: issue.severity === 'error' ? '#fff2f0' : '#fffbe6',
                        border: `1px solid ${issue.severity === 'error' ? '#ffccc7' : '#ffe58f'}`,
                        borderRadius: 4,
                      }}
                    >
                      <Space>
                        {issue.severity === 'error' ? (
                          <CloseOutlined style={{ color: '#ff4d4f' }} />
                        ) : (
                          <ExclamationCircleOutlined style={{ color: '#faad14' }} />
                        )}
                        <strong>{issue.plo}:</strong>
                        <span>{issue.message}</span>
                      </Space>
                    </div>
                  ))}
                </Space>
              </Card>
            )}

            {/* AI Suggestions */}
            {ploCompliance.suggestions.length > 0 && (
              <Card title="Đề xuất cải thiện (AI)" size="small">
                <ul style={{ marginBottom: 0 }}>
                  {ploCompliance.suggestions.map((suggestion, idx) => (
                    <li key={idx} style={{ marginBottom: 8 }}>
                      {suggestion}
                    </li>
                  ))}
                </ul>
              </Card>
            )}
          </Space>
        ) : (
          <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
            Không có dữ liệu
          </div>
        )}
      </Modal>
    </div>
  );
};
