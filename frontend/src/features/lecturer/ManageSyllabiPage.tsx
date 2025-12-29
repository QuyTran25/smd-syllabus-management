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
import { syllabusService } from '@/services/syllabus.service';
import { semesterService } from '@/services/semester.service';
import { Syllabus } from '@/types';

const { Search } = Input;
const { Option } = Select;
const { Title } = Typography;

// Đã xóa interface cục bộ để dùng Syllabus global

// Status mappings: support both old and new enum strings from backend
const statusColors: Record<string, string> = {
  DRAFT: 'default',
  WAITING_HOD: 'processing',
  HOD_APPROVED: 'success',
  HOD_REJECTED: 'error',
  WAITING_AA: 'processing',
  AA_APPROVED: 'success',
  AA_REJECTED: 'error',
  WAITING_PRINCIPAL: 'processing',
  PUBLISHED: 'success',
};

const statusLabels: Record<string, string> = {
  DRAFT: 'Bản nháp',
  WAITING_HOD: 'Chờ TBM duyệt',
  HOD_APPROVED: 'TBM đã duyệt',
  HOD_REJECTED: 'TBM từ chối',
  WAITING_AA: 'Chờ AA duyệt',
  AA_APPROVED: 'AA đã duyệt',
  AA_REJECTED: 'AA từ chối',
  WAITING_PRINCIPAL: 'Chờ Hiệu trưởng',
  PUBLISHED: 'Đã xuất bản',
};

// Normalize backend status values to the keys used above. Backend may return PENDING_HOD / REJECTED / APPROVED etc.
const normalizeStatusKey = (raw?: string) => {
  if (!raw) return '';
  const s = String(raw).toUpperCase();
  if (s === 'DRAFT') return 'DRAFT';
  if (s.includes('PENDING') || s.includes('WAITING')) {
    if (s.includes('AA')) return 'WAITING_AA';
    if (s.includes('PRINCIPAL')) return 'WAITING_PRINCIPAL';
    return 'WAITING_HOD';
  }
  if (s.includes('REJECT')) {
    // map generic REJECTED to HOD_REJECTED as a safe default; specific ones like AA_REJECTED are preserved
    if (s.includes('AA')) return 'AA_REJECTED';
    if (s.includes('PRINCIPAL')) return 'HOD_REJECTED';
    return s === 'REJECTED' ? 'HOD_REJECTED' : s;
  }
  if (s.includes('APPROV')) {
    if (s.includes('AA')) return 'AA_APPROVED';
    return 'HOD_APPROVED';
  }
  if (s === 'PUBLISHED') return 'PUBLISHED';
  return s;
};

const ManageSyllabiPage: React.FC = () => {
  const navigate = useNavigate();
  const [syllabi, setSyllabi] = useState<Syllabus[]>([]);
  const [loading, setLoading] = useState(false);
  const [semesters, setSemesters] = useState<any[]>([]);
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

  const handleViewRejectionReason = (syllabus: Syllabus) => {
    // Ép kiểu as any để tránh lỗi TS nếu type thiếu trường
    const s = syllabus as any;
    if (s.rejectionReason && s.rejectionType) {
      setRejectionModal({
        open: true,
        syllabusId: Number(s.id),
        syllabusCode: s.subjectCode,
        syllabusName: s.subjectNameVi || s.subjectName || '',
        reason: s.rejectionReason,
        type: s.rejectionType,
      });
    }
  };

  const handleSearch = (value: string) => {
    setSearchText(value);
  };

  // Hoisted reusable fetchData: tải syllabi và semesters, chịu lỗi cho semesters
  async function fetchData() {
    setLoading(true);
    try {
      const syllabiRes = await syllabusService.getMySyllabuses();
      setSyllabi(syllabiRes || []);
    } catch (err) {
      console.error('Lỗi tải Syllabi:', err);
      message.error('Không thể tải danh sách đề cương');
    }

    try {
      const semestersRes = await semesterService.getSemesters();
      if (Array.isArray(semestersRes)) setSemesters(semestersRes);
      else setSemesters((semestersRes as any)?.data || (semestersRes as any)?.rows || []);
    } catch (err) {
      console.error('Lỗi tải Semester (Backend đang lỗi 500):', err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    // Hàm fetch dữ liệu độc lập

    fetchData(); // Call the hoisted fetchData function
  }, []);

  const handleSubmit = (id: string) => {
    Modal.confirm({
      title: 'Gửi đề cương phê duyệt',
      content: 'Bạn có chắc muốn gửi đề cương này cho Trưởng Bộ môn phê duyệt?',
      onOk: async () => {
        setLoading(true);
        try {
          const updated = await (syllabusService as any).submitSyllabus(id);
          if (updated) {
            setSyllabi((prev) => prev.map((s) => (s.id === id ? { ...s, ...updated } : s)));
          } else {
            // Fallback reload nếu API không trả về object
            const syllabiRes = await syllabusService.getMySyllabuses();
            setSyllabi(syllabiRes || []);
          }
          message.success('Đã gửi đề cương thành công!');
        } catch (error) {
          console.error(error);
          message.error('Lỗi khi gửi đề cương');
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
        setLoading(true);
        try {
          await syllabusService.deleteSyllabus(id);
          setSyllabi((prev) => prev.filter((s) => s.id !== id));
          message.success('Đã xóa đề cương!');
        } catch (error) {
          console.error(error);
          message.error('Lỗi khi xóa đề cương');
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
        s.id === id ? { ...s, isFollowing: !(s as any).isFollowing } : s
      )
    );
    const syllabus = syllabi.find((s) => s.id === id);
    if ((syllabus as any)?.isFollowing) {
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
      // Fix lỗi TS: ép kiểu as any cho a và b
      sorter: (a, b) => ((a as any).subjectCode || '').localeCompare((b as any).subjectCode || ''),
    },
    {
      title: 'Tên học phần',
      dataIndex: 'subjectNameVi',
      width: 200,
      align: 'center',
      sorter: (a, b) => ((a as any).subjectNameVi || '').localeCompare((b as any).subjectNameVi || ''),
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semester',
      width: 150,
      align: 'center',
      sorter: (a, b) => ((a as any).semester || '').localeCompare((b as any).semester || ''),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 150,
      align: 'center',
      render: (status, record) => {
        const key = normalizeStatusKey(status);
        const showRejectionIcon = ['HOD_REJECTED', 'AA_REJECTED', 'PRINCIPAL_REJECTED'].includes(key) || String(status).toUpperCase() === 'REJECTED';
        return (
          <Space>
            <Tag color={statusColors[key]}>{statusLabels[key] || status}</Tag>
            {showRejectionIcon && (record as any).rejectionReason && (
              <InfoCircleOutlined
                style={{ color: '#ff4d4f', cursor: 'pointer', fontSize: 16 }}
                onClick={() => handleViewRejectionReason(record)}
              />
            )}
          </Space>
        );
      },
    },
    {
      title: 'Phiên bản',
      dataIndex: 'currentVersion',
      width: 100,
      align: 'center',
      render: (version, record) => (
        <Space>
          <span>{(version as any) || (record as any).currentVersion}</span>
          <Button
            type="link"
            size="small"
            icon={<HistoryOutlined />}
            onClick={() => showHistory(record)}
          >
            ({(record as any).totalVersions || ''})
          </Button>
        </Space>
      ),
    },
    {
      title: 'Cập nhật lần cuối',
      dataIndex: 'updatedAt',
      width: 180,
      align: 'center',
      sorter: (a, b) => new Date((a as any).updatedAt || '').getTime() - new Date((b as any).updatedAt || '').getTime(),
      render: (text) => text ? new Date(text).toLocaleDateString('vi-VN') : '',
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 250,
      fixed: 'right',
      align: 'center',
      render: (_, record) => {
        const key = normalizeStatusKey((record as any).status);
        const editable = ['DRAFT', 'HOD_REJECTED', 'PUBLISHED'].includes(key);
        const canSubmit = ['DRAFT', 'HOD_REJECTED'].includes(key);
        const canDelete = ['DRAFT'].includes(key);
        return (
          <Space>
            <Button
              type={(record as any).isFollowing ? 'default' : 'text'}
              size="small"
              icon={(record as any).isFollowing ? <StarFilled /> : <StarOutlined />}
              onClick={() => toggleFollow(record.id)}
              style={(record as any).isFollowing ? { color: '#faad14' } : {}}
            >
              {(record as any).isFollowing ? 'Đang theo dõi' : 'Theo dõi'}
            </Button>
            <Button
              type="primary"
              size="small"
              icon={<EditOutlined />}
              onClick={() => navigate(`/lecturer/syllabi/edit/${record.id}`)}
              disabled={!editable}
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
                    disabled: !canSubmit,
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
                    disabled: !canDelete,
                    onClick: () => handleDelete(record.id),
                  },
                ],
              }}
            >
              <Button size="small" icon={<MoreOutlined />} />
            </Dropdown>
          </Space>
        );
      },
    },
  ];

  const filteredData = syllabi.filter((item) => {
    const s = item as any; // Cast để search không lỗi
    const matchSearch =
      (s.subjectCode || '').toLowerCase().includes(searchText.toLowerCase()) ||
      (s.subjectNameVi || '').toLowerCase().includes(searchText.toLowerCase());
    const matchStatus = !statusFilter || normalizeStatusKey((item as any).status) === normalizeStatusKey(statusFilter);
    const matchSemester = !semesterFilter || s.semester === semesterFilter;
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
      <Card>
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
            {semesters.map((s) => (
              <Option key={s.id} value={s.name}>
                {s.name}
              </Option>
            ))}
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
        title={`Lịch sử phiên bản - ${(selectedSyllabus as any)?.subjectCode}`}
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
            }
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
              render: (status) => {
                const key = normalizeStatusKey(status);
                return <Tag color={statusColors[key]}>{statusLabels[key] || status}</Tag>;
              },
            },
            { title: 'Thay đổi', dataIndex: 'changes' },
          ]}
        />
      </Modal>

      {selectedSyllabus && (
        <VersionComparisonModal
          visible={compareModalVisible}
          onClose={() => setCompareModalVisible(false)}
          syllabusId={selectedSyllabus.id}
          currentVersion={(selectedSyllabus as any).currentVersion}
        />
      )}
    </div>
  );
};

export default ManageSyllabiPage;