import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  Tag,
  Dropdown,
  Modal,
  message,
  Typography,
  Alert,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  EyeOutlined,
  MoreOutlined,
  SendOutlined,
  DeleteOutlined,
  HistoryOutlined,
  DiffOutlined,
  StarOutlined,
  StarFilled,
  InfoCircleOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import VersionComparisonModal from './VersionComparisonModal';
import RejectionReasonModal from './dashboard/components/RejectionReasonModal';
import { useAuth } from '@/features/auth/AuthContext';
import { syllabusService } from '@/services/syllabus.service';
import { revisionService } from '@/services';
import { SyllabusStatus } from '@/types';

const { Search } = Input;
const { Option } = Select;
const { Title } = Typography;

interface Syllabus {
  id: string;
  subjectCode: string;
  subjectName: string;
  semester: string;
  status: string;
  currentVersion: string;
  totalVersions: number;
  lastModified: string;
  submittedAt?: string;
  approvedBy?: string;
  isFollowing?: boolean;
  rejectionReason?: string;
  rejectionType?: 'HOD' | 'AA' | 'PRINCIPAL';
}

interface Syllabus {
  id: string;
  subjectCode: string;
  subjectName: string;
  semester: string;
  status: string;
  currentVersion: string;
  totalVersions: number;
  lastModified: string;
  submittedAt?: string;
  approvedBy?: string;
  isFollowing?: boolean;
  rejectionReason?: string;
  rejectionType?: 'HOD' | 'AA' | 'PRINCIPAL';
  ownerId?: string;
}

const statusColors: Record<string, string> = {
  DRAFT: 'default',
  PENDING_HOD: 'processing',
  PENDING_AA: 'processing',
  PENDING_PRINCIPAL: 'processing',
  REJECTED: 'error',
  REVISION_IN_PROGRESS: 'warning',
  APPROVED: 'success',
  PUBLISHED: 'success',
  ARCHIVED: 'default',
};

const statusLabels: Record<string, string> = {
  DRAFT: 'Bản nháp',
  PENDING_HOD: 'Chờ TBM duyệt',
  PENDING_AA: 'Chờ AA duyệt',
  PENDING_PRINCIPAL: 'Chờ Hiệu trưởng',
  REJECTED: 'Bị từ chối',
  REVISION_IN_PROGRESS: 'Đang chỉnh sửa',
  APPROVED: 'Đã duyệt',
  PUBLISHED: 'Đã xuất bản',
  ARCHIVED: 'Đã lưu trữ',
};

const ManageSyllabiPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [syllabi, setSyllabi] = useState<Syllabus[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [semesterFilter, setSemesterFilter] = useState<string>('');
  const [selectedSyllabus, setSelectedSyllabus] = useState<Syllabus | null>(null);
  const [historyModalVisible, setHistoryModalVisible] = useState(false);
  const [compareModalVisible, setCompareModalVisible] = useState(false);
  const [rejectionModal, setRejectionModal] = useState<{
    open: boolean;
    syllabusId: number;
    syllabusCode: string;
    syllabusName: string;
    reason: string;
    type: 'HOD' | 'AA' | 'PRINCIPAL';
  }>({
    open: false,
    syllabusId: 0,
    syllabusCode: '',
    syllabusName: '',
    reason: '',
    type: 'HOD',
  });

  // Fetch syllabi on mount and filter changes
  useEffect(() => {
    if (user) {
      fetchSyllabi();
    }
  }, [user, statusFilter, semesterFilter]);

  const fetchSyllabi = async () => {
    if (!user) return;

    try {
      setLoading(true);
      
      // Fetch syllabi where user is the creator
      // Lecturer chỉ thấy các trạng thái: DRAFT, PENDING_HOD, REJECTED, REVISION_IN_PROGRESS
      const filters: any = {
        status: statusFilter ? [statusFilter as SyllabusStatus] : [
          SyllabusStatus.DRAFT,
          SyllabusStatus.PENDING_HOD,
          SyllabusStatus.REJECTED,
          SyllabusStatus.REVISION_IN_PROGRESS,
        ],
      };

      if (searchText) {
        filters.search = searchText;
      }

      const { data } = await syllabusService.getSyllabi(filters, { page: 1, pageSize: 100 });

      // Filter by current user (creator)
      const mySyllabi = data.filter(s => s.createdBy === user.id);

      // Map to local interface
      const mappedSyllabi: Syllabus[] = mySyllabi.map(s => ({
        id: s.id,
        subjectCode: s.subjectCode,
        subjectName: s.subjectNameVi,
        semester: s.semester,
        status: s.status,
        currentVersion: `v${s.version}.0`,
        totalVersions: s.version,
        lastModified: new Date(s.updatedAt).toLocaleString('vi-VN'),
        submittedAt: s.submittedAt ? new Date(s.submittedAt).toLocaleString('vi-VN') : undefined,
        approvedBy: s.hodApprovedBy || s.aaApprovedBy || s.principalApprovedBy,
        isFollowing: false, // TODO: Implement follow feature
        rejectionReason: undefined, // TODO: Fetch from approval_history
        rejectionType: s.status === SyllabusStatus.REJECTED ? 'HOD' : undefined,
        ownerId: s.ownerId,
      }));

      setSyllabi(mappedSyllabi);
    } catch (error: any) {
      message.error('Không thể tải danh sách đề cương: ' + (error.message || 'Lỗi không xác định'));
      console.error('Error fetching syllabi:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewRejectionReason = (syllabus: Syllabus) => {
    if (syllabus.rejectionReason && syllabus.rejectionType) {
      setRejectionModal({
        open: true,
        syllabusId: Number(syllabus.id),
        syllabusCode: syllabus.subjectCode,
        syllabusName: syllabus.subjectName,
        reason: syllabus.rejectionReason,
        type: syllabus.rejectionType,
      });
    }
  };

  const handleSearch = (value: string) => {
    setSearchText(value);
    fetchSyllabi();
  };

  const handleSubmit = (id: string) => {
    const syllabus = syllabi.find(s => s.id === id);
    if (!syllabus) return;

    // Check if this is a revision submission
    const isRevision = syllabus.status === 'REVISION_IN_PROGRESS';

    Modal.confirm({
      title: isRevision ? 'Gửi revision cho TBM duyệt' : 'Gửi đề cương phê duyệt',
      content: isRevision
        ? 'Bạn có chắc muốn gửi đề cương đã chỉnh sửa cho Trưởng Bộ môn duyệt?'
        : 'Bạn có chắc muốn gửi đề cương này cho Trưởng Bộ môn phê duyệt?',
      onOk: async () => {
        try {
          setLoading(true);
          
          if (isRevision) {
            // Get active revision session
            const session = await revisionService.getActiveRevisionSession(id);
            if (session) {
              await revisionService.submitRevision({
                revisionSessionId: session.id,
                summary: 'Đã hoàn thành chỉnh sửa theo phản hồi',
              });
              message.success('Đã gửi revision cho TBM duyệt!');
            } else {
              message.error('Không tìm thấy revision session');
            }
          } else {
            // Normal approval workflow
            await syllabusService.submitForApproval(id);
            message.success('Đã gửi đề cương thành công!');
          }
          
          await fetchSyllabi();
        } catch (error: any) {
          message.error('Không thể gửi đề cương: ' + (error.message || 'Lỗi không xác định'));
        } finally {
          setLoading(false);
        }
      },
    });
  };

  const handleDelete = (id: string) => {
    Modal.confirm({
      title: 'Xóa đề cương',
      content: 'Bạn có chắc muốn xóa đề cương này?',
      okType: 'danger',
      onOk: async () => {
        try {
          setLoading(true);
          await syllabusService.deleteSyllabus(id);
          await fetchSyllabi();
          message.success('Đã xóa đề cương!');
        } catch (error: any) {
          message.error('Không thể xóa đề cương: ' + (error.message || 'Lỗi không xác định'));
        } finally {
          setLoading(false);
        }
      },
    });
  };

  const showHistory = (syllabus: Syllabus) => {
    setSelectedSyllabus(syllabus);
    setHistoryModalVisible(true);
  };

  const toggleFollow = (id: string) => {
    setSyllabi(
      syllabi.map((s) =>
        s.id === id ? { ...s, isFollowing: !s.isFollowing } : s
      )
    );
    const syllabus = syllabi.find((s) => s.id === id);
    if (syllabus?.isFollowing) {
      message.success('Đã hủy theo dõi đề cương');
    } else {
      message.success('Đã theo dõi đề cương. Bạn sẽ nhận thông báo khi có cập nhật.');
    }
  };

  const columns: ColumnsType<Syllabus> = [
    {
      title: 'Mã HP',
      dataIndex: 'subjectCode',
      width: 100,
      align: 'center',
      sorter: (a, b) => a.subjectCode.localeCompare(b.subjectCode),
    },
    {
      title: 'Tên học phần',
      dataIndex: 'subjectName',
      width: 200,
      align: 'center',
      sorter: (a, b) => a.subjectName.localeCompare(b.subjectName),
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semester',
      width: 150,
      align: 'center',
      sorter: (a, b) => a.semester.localeCompare(b.semester),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 150,
      align: 'center',
      render: (status, record) => (
        <Space>
          <Tag color={statusColors[status]}>{statusLabels[status] || status}</Tag>
          {status === 'REJECTED' && record.rejectionReason && (
            <InfoCircleOutlined
              style={{ color: '#ff4d4f', cursor: 'pointer', fontSize: 16 }}
              onClick={() => handleViewRejectionReason(record)}
            />
          )}
        </Space>
      ),
    },
    {
      title: 'Phiên bản',
      dataIndex: 'currentVersion',
      width: 100,
      align: 'center',
      render: (version, record) => (
        <Space>
          <span>{version}</span>
          <Button
            type="link"
            size="small"
            icon={<HistoryOutlined />}
            onClick={() => showHistory(record)}
          >
            ({record.totalVersions})
          </Button>
        </Space>
      ),
    },
    {
      title: 'Cập nhật lần cuối',
      dataIndex: 'lastModified',
      width: 180,
      align: 'center',
      sorter: (a, b) => new Date(a.lastModified).getTime() - new Date(b.lastModified).getTime(),
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 250,
      fixed: 'right',
      align: 'center',
      render: (_, record) => (
        <Space>
          <Button
            type={record.isFollowing ? 'default' : 'text'}
            size="small"
            icon={record.isFollowing ? <StarFilled /> : <StarOutlined />}
            onClick={() => toggleFollow(record.id)}
            style={record.isFollowing ? { color: '#faad14' } : {}}
          >
            {record.isFollowing ? 'Đang theo dõi' : 'Theo dõi'}
          </Button>
          <Button
            type="primary"
            size="small"
            icon={<EditOutlined />}
            onClick={() => navigate(`/lecturer/syllabi/edit/${record.id}`)}
            disabled={
              record.status !== 'DRAFT' &&
              record.status !== 'HOD_REJECTED' &&
              record.status !== 'PUBLISHED' &&
              record.status !== 'REVISION_IN_PROGRESS'
            }
          >
            Sửa
          </Button>
          <Button
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/lecturer/syllabi/${record.id}`)}
          >
            Xem
          </Button>
          <Dropdown
            menu={{
              items: [
                {
                  key: 'submit',
                  label: 'Gửi phê duyệt',
                  icon: <SendOutlined />,
                  disabled: 
                    record.status !== 'DRAFT' && 
                    record.status !== 'HOD_REJECTED' && 
                    record.status !== 'REVISION_IN_PROGRESS',
                  onClick: () => handleSubmit(record.id),
                },
                {
                  key: 'compare',
                  label: 'So sánh phiên bản',
                  icon: <DiffOutlined />,
                  onClick: () => {
                    setSelectedSyllabus(record);
                    setCompareModalVisible(true);
                  },
                },
                {
                  key: 'delete',
                  label: 'Xóa',
                  icon: <DeleteOutlined />,
                  danger: true,
                  disabled: record.status !== 'DRAFT',
                  onClick: () => handleDelete(record.id),
                },
              ],
            }}
          >
            <Button size="small" icon={<MoreOutlined />} />
          </Dropdown>
        </Space>
      ),
    },
  ];

  const filteredData = syllabi.filter((item) => {
    const matchSearch =
      item.subjectCode.toLowerCase().includes(searchText.toLowerCase()) ||
      item.subjectName.toLowerCase().includes(searchText.toLowerCase());
    const matchStatus = !statusFilter || item.status === statusFilter;
    const matchSemester = !semesterFilter || item.semester === semesterFilter;
    return matchSearch && matchStatus && matchSemester;
  });

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, padding: '24px 24px 0' }}>
        <Title level={2} style={{ margin: 0 }}>Quản lý Đề cương của tôi</Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => navigate('/lecturer/syllabi/create')}
        >
          Tạo đề cương mới
        </Button>
      </div>
      
      {/* Revision Alert */}
      {syllabi.filter(s => s.status === 'REVISION_IN_PROGRESS').length > 0 && (
        <Alert
          message={`Bạn có ${syllabi.filter(s => s.status === 'REVISION_IN_PROGRESS').length} đề cương cần chỉnh sửa`}
          description={
            <div>
              Admin đã yêu cầu bạn chỉnh sửa một số đề cương dựa trên phản hồi từ sinh viên. 
              Vui lòng kiểm tra <strong>Thông báo</strong> để xem chi tiết các lỗi cần sửa.
            </div>
          }
          type="warning"
          showIcon
          closable
          style={{ margin: '0 24px 24px' }}
        />
      )}
      
      <Card
      >
        <Space style={{ marginBottom: 16, width: '100%' }} size="middle">
          <Search
            placeholder="Tìm theo mã hoặc tên học phần"
            onSearch={handleSearch}
            onChange={(e) => handleSearch(e.target.value)}
            style={{ width: 300 }}
          />
          <Select
            placeholder="Lọc theo trạng thái"
            style={{ width: 180 }}
            allowClear
            onChange={setStatusFilter}
          >
            <Option value="DRAFT">Bản nháp</Option>
            <Option value="WAITING_HOD">Chờ TBM duyệt</Option>
            <Option value="HOD_REJECTED">TBM từ chối</Option>
            <Option value="PUBLISHED">Đã xuất bản</Option>
          </Select>
          <Select
            placeholder="Lọc theo học kỳ"
            style={{ width: 180 }}
            allowClear
            onChange={setSemesterFilter}
          >
            <Option value="HK1 2024-2025">HK1 2024-2025</Option>
            <Option value="HK2 2024-2025">HK2 2024-2025</Option>
            <Option value="HK1 2025-2026">HK1 2025-2026</Option>
          </Select>
        </Space>

        <Table
          columns={columns}
          dataSource={filteredData}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1150 }}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `Tổng ${total} đề cương`,
            showSizeChanger: true,
          }}
        />
      </Card>

      {/* Rejection Reason Modal */}
      <RejectionReasonModal
        open={rejectionModal.open}
        onClose={() => setRejectionModal({ ...rejectionModal, open: false })}
        syllabusId={rejectionModal.syllabusId}
        syllabusCode={rejectionModal.syllabusCode}
        syllabusName={rejectionModal.syllabusName}
        rejectionReason={rejectionModal.reason}
        rejectionType={rejectionModal.type}
      />

      {/* Version History Modal */}
      <Modal
        title={`Lịch sử phiên bản - ${selectedSyllabus?.subjectCode}`}
        open={historyModalVisible}
        onCancel={() => setHistoryModalVisible(false)}
        footer={null}
        width={800}
      >
        <Table
          dataSource={[
            {
              id: '1',
              version: 'v3.0',
              createdAt: '2024-12-08 14:30',
              createdBy: 'GV. Trần Thị B',
              status: 'DRAFT',
              changes: 'Cập nhật CLO3, thêm tài liệu tham khảo',
            },
            {
              id: '2',
              version: 'v2.1',
              createdAt: '2024-12-05 10:00',
              createdBy: 'GV. Trần Thị B',
              status: 'HOD_APPROVED',
              changes: 'Sửa phương pháp đánh giá',
            },
            {
              id: '3',
              version: 'v2.0',
              createdAt: '2024-11-20 09:15',
              createdBy: 'GV. Trần Thị B',
              status: 'PUBLISHED',
              changes: 'Phiên bản chính thức HK2 2024-2025',
            },
          ]}
          rowKey="id"
          pagination={false}
          size="small"
          columns={[
            { title: 'Phiên bản', dataIndex: 'version', width: 100 },
            { title: 'Ngày tạo', dataIndex: 'createdAt', width: 150 },
            { title: 'Người tạo', dataIndex: 'createdBy', width: 150 },
            {
              title: 'Trạng thái',
              dataIndex: 'status',
              width: 120,
              render: (status) => <Tag color={statusColors[status]}>{statusLabels[status]}</Tag>,
            },
            { title: 'Thay đổi', dataIndex: 'changes' },
          ]}
        />
      </Modal>

      {/* Version Comparison Modal */}
      {selectedSyllabus && (
        <VersionComparisonModal
          visible={compareModalVisible}
          onClose={() => setCompareModalVisible(false)}
          syllabusId={selectedSyllabus.id}
          currentVersion={selectedSyllabus.currentVersion}
        />
      )}
    </div>
  );
};

export default ManageSyllabiPage;
