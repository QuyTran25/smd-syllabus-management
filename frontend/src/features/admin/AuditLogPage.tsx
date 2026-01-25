import React, { useState } from 'react';
import {
  Card,
  Table,
  Space,
  Select,
  DatePicker,
  Input,
  Tag,
  Button,
  Typography,
  message,
  Tooltip,
} from 'antd';
import {
  SearchOutlined,
  DownloadOutlined,
  HistoryOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import type { TablePaginationConfig } from 'antd/es/table';
import dayjs from 'dayjs';

// 🔥 FIX: Import service thật (Điều chỉnh đường dẫn tương đối theo cấu trúc folder của bạn)
// Ví dụ: nếu file này ở features/admin/AuditLogPage.tsx thì đường dẫn này là đúng
import { auditLogService, AuditLog } from '../../services/auditlog.service';
// HOẶC dùng alias nếu cấu hình: import { auditLogService, AuditLog } from '@/services/auditlog.service';

const { RangePicker } = DatePicker;
const { Option } = Select;
const { Text } = Typography;

// Role display mapping
const ROLE_LABELS: Record<string, string> = {
  ADMIN: 'Admin',
  Administrator: 'Admin',
  PRINCIPAL: 'Hiệu trưởng',
  Principal: 'Hiệu trưởng',
  HOD: 'Trưởng BM',
  'Head of Department': 'Trưởng BM',
  AA: 'Phòng ĐT',
  'Academic Affairs': 'Phòng ĐT',
  LECTURER: 'Giảng viên',
  Lecturer: 'Giảng viên',
  STUDENT: 'Sinh viên',
  Student: 'Sinh viên',
};

// Action labels in Vietnamese
const ACTION_LABELS: Record<string, string> = {
  CREATE: 'Tạo mới',
  UPDATE: 'Cập nhật',
  DELETE: 'Xóa',
  APPROVE: 'Phê duyệt',
  REJECT: 'Từ chối',
  SUBMIT: 'Gửi',
  LOGIN: 'Đăng nhập',
  LOGOUT: 'Đăng xuất',
  EXPORT: 'Xuất file',
  PUBLISH: 'Xuất bản',
  UNPUBLISH: 'Hủy xuất bản',
};

// Entity labels
const ENTITY_LABELS: Record<string, string> = {
  User: 'Người dùng',
  Syllabus: 'Đề cương',
  PLO: 'PLO',
  CLO: 'CLO',
  Subject: 'Môn học',
  Semester: 'Học kỳ',
  System: 'Hệ thống',
  SystemConfig: 'Cấu hình',
};

export default function AuditLogPage() {
  const [searchText, setSearchText] = useState('');
  const [selectedAction, setSelectedAction] = useState<string>('all');
  const [selectedEntity, setSelectedEntity] = useState<string>('all');
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 20 });

  // Fetch audit logs from API
  const {
    data: logsData,
    isLoading,
    refetch,
    error,
  } = useQuery({
    queryKey: ['audit-logs', selectedAction, selectedEntity, dateRange, pagination],
    queryFn: async () => {
      // Chuẩn bị filter object
      const filters = {
        action: selectedAction !== 'all' ? selectedAction : undefined,
        entityName: selectedEntity !== 'all' ? selectedEntity : undefined,
        startDate: dateRange?.[0]?.toISOString(),
        endDate: dateRange?.[1]?.toISOString(),
        page: pagination.current - 1, // API trang bắt đầu từ 0
        size: pagination.pageSize,
      };

      // 🔥 FIX: Logic chọn API
      // Nếu có filter tìm kiếm -> gọi search, ngược lại gọi get all
      const hasSearchFilters =
        filters.action || filters.entityName || filters.startDate || filters.endDate;

      const result = hasSearchFilters
        ? await auditLogService.searchAuditLogs(filters)
        : await auditLogService.getAuditLogs(filters);

      return result;
    },
    retry: 1,
    refetchOnWindowFocus: true, // Auto refresh khi quay lại tab
  });

  // Show error if any
  React.useEffect(() => {
    if (error) {
      message.error('Không thể tải nhật ký hoạt động. Vui lòng thử lại.');
      console.error('Audit log error:', error);
    }
  }, [error]);

  // Filter locally by search text (client-side filter cho text search đơn giản)
  const filteredLogs = React.useMemo(() => {
    if (!logsData?.content) return [];
    if (!searchText) return logsData.content;

    const searchLower = searchText.toLowerCase();
    return logsData.content.filter(
      (log) =>
        log.actorName?.toLowerCase().includes(searchLower) ||
        log.description?.toLowerCase().includes(searchLower) ||
        log.entityName?.toLowerCase().includes(searchLower) ||
        log.ipAddress?.toLowerCase().includes(searchLower)
    );
  }, [logsData?.content, searchText]);

  const handleTableChange = (paginationConfig: TablePaginationConfig) => {
    setPagination({
      current: paginationConfig.current || 1,
      pageSize: paginationConfig.pageSize || 20,
    });
  };

  const handleExport = () => {
    message.info('Chức năng export đang được phát triển');
  };

  const handleReset = () => {
    setSearchText('');
    setSelectedAction('all');
    setSelectedEntity('all');
    setDateRange(null);
    setPagination({ current: 1, pageSize: 20 });
  };

  const columns: ColumnsType<AuditLog> = [
    {
      title: 'Thời gian',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 150,
      align: 'center',
      fixed: 'left',
      // 🔥 FIX: Bỏ sorter client-side nếu muốn server-side sort hoàn toàn,
      // nhưng giữ lại để user có thể sort trang hiện tại cũng tốt
      sorter: (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
      render: (createdAt) => {
        const date = dayjs(createdAt);
        return (
          <Space direction="vertical" size={0} style={{ textAlign: 'center' }}>
            <Text strong style={{ fontSize: '13px' }}>
              {date.format('HH:mm:ss')}
            </Text>
            <Text type="secondary" style={{ fontSize: '11px' }}>
              {date.format('DD/MM/YYYY')}
            </Text>
          </Space>
        );
      },
    },
    {
      title: 'Người dùng',
      key: 'user',
      width: 200,
      ellipsis: true,
      render: (_, record) => {
        const actorName = record.actorName || record.actorEmail || 'Hệ thống';
        const actorRole = record.actorRole || 'SYSTEM';
        const roleLabel = ROLE_LABELS[actorRole] || actorRole;

        return (
          <Space direction="vertical" size={2}>
            <Text strong style={{ fontSize: '13px' }}>
              {actorName}
            </Text>
            <Space size={4}>
              <Tag color="blue" style={{ fontSize: '10px', margin: 0 }}>
                {roleLabel}
              </Tag>
            </Space>
          </Space>
        );
      },
    },
    {
      title: 'Hành động',
      dataIndex: 'action',
      key: 'action',
      width: 120,
      align: 'center',
      render: (action) => {
        const colors: Record<string, string> = {
          CREATE: 'green',
          UPDATE: 'blue',
          DELETE: 'red',
          APPROVE: 'cyan',
          REJECT: 'orange',
          SUBMIT: 'purple',
          LOGIN: 'default',
          LOGOUT: 'default',
          EXPORT: 'geekblue',
          PUBLISH: 'green',
          UNPUBLISH: 'volcano',
        };
        return <Tag color={colors[action] || 'default'}>{ACTION_LABELS[action] || action}</Tag>;
      },
    },
    {
      title: 'Tài nguyên',
      key: 'resource',
      width: 150,
      align: 'center',
      ellipsis: true,
      render: (_, record) => (
        <Space direction="vertical" size={0} style={{ textAlign: 'center' }}>
          <Text style={{ fontSize: '13px' }}>
            {ENTITY_LABELS[record.entityName] || record.entityName || '-'}
          </Text>
          {record.entityId && (
            <Tooltip title={record.entityId}>
              <Text type="secondary" style={{ fontSize: '10px', cursor: 'help' }}>
                #{record.entityId.substring(0, 8)}...
              </Text>
            </Tooltip>
          )}
        </Space>
      ),
    },
    {
      title: 'Chi tiết',
      dataIndex: 'description',
      key: 'description',
      minWidth: 300,
      ellipsis: { showTitle: false },
      render: (description) => (
        <Tooltip title={description}>
          <Text style={{ fontSize: '13px' }}>{description || '-'}</Text>
        </Tooltip>
      ),
    },
    {
      title: 'Kết quả',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      align: 'center',
      render: (status) => (
        <Tag color={status === 'SUCCESS' ? 'success' : 'error'}>
          {status === 'SUCCESS' ? 'Thành công' : 'Lỗi'}
        </Tag>
      ),
    },
    {
      title: 'IP',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 130,
      align: 'center',
      render: (ip) => (
        <Text type="secondary" style={{ fontSize: '12px', fontFamily: 'monospace' }}>
          {ip || '-'}
        </Text>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 24,
        }}
      >
        <h2 style={{ margin: 0, fontSize: '24px', fontWeight: 600 }}>
          <HistoryOutlined style={{ marginRight: 8 }} />
          Nhật ký Hoạt động
        </h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => refetch()} loading={isLoading}>
            Làm mới
          </Button>
          <Button icon={<DownloadOutlined />} type="primary" onClick={handleExport}>
            Export Log
          </Button>
        </Space>
      </div>

      {/* Filters Card */}
      <Card style={{ marginBottom: 16 }}>
        <Space wrap size={12} style={{ width: '100%' }}>
          <Input
            placeholder="Tìm theo tên, chi tiết, IP..."
            prefix={<SearchOutlined />}
            style={{ width: 280 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />

          <Select
            value={selectedAction}
            onChange={setSelectedAction}
            style={{ width: 180 }}
            showSearch
            optionFilterProp="children"
          >
            <Option value="all">Tất cả hành động</Option>
            <Option value="CREATE">Tạo mới</Option>
            <Option value="UPDATE">Cập nhật</Option>
            <Option value="DELETE">Xóa</Option>
            <Option value="APPROVE">Phê duyệt</Option>
            <Option value="REJECT">Từ chối</Option>
            <Option value="SUBMIT">Gửi</Option>
            <Option value="LOGIN">Đăng nhập</Option>
            <Option value="LOGOUT">Đăng xuất</Option>
            <Option value="PUBLISH">Xuất bản</Option>
            <Option value="EXPORT">Xuất file</Option>
          </Select>

          <Select
            value={selectedEntity}
            onChange={setSelectedEntity}
            style={{ width: 180 }}
            showSearch
            optionFilterProp="children"
          >
            <Option value="all">Tất cả tài nguyên</Option>
            <Option value="User">Người dùng</Option>
            <Option value="Syllabus">Đề cương</Option>
            <Option value="PLO">PLO</Option>
            <Option value="CLO">CLO</Option>
            <Option value="Subject">Môn học</Option>
            <Option value="Semester">Học kỳ</Option>
            <Option value="System">Hệ thống</Option>
            <Option value="SystemConfig">Cấu hình</Option>
          </Select>

          <RangePicker
            value={dateRange}
            onChange={(dates) => setDateRange(dates as [dayjs.Dayjs, dayjs.Dayjs] | null)}
            format="DD/MM/YYYY"
            placeholder={['Từ ngày', 'Đến ngày']}
            style={{ width: 260 }}
          />

          <Button onClick={handleReset}>Đặt lại</Button>
        </Space>

        {/* Summary info */}
        <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid #f0f0f0' }}>
          <Space size={24}>
            <Text type="secondary" style={{ fontSize: '13px' }}>
              📊 Tổng: <Text strong>{logsData?.totalElements || 0}</Text> bản ghi
            </Text>
            <Text type="secondary" style={{ fontSize: '13px' }}>
              📄 Trang:{' '}
              <Text strong>{logsData?.number !== undefined ? logsData.number + 1 : 1}</Text>/
              {logsData?.totalPages || 1}
            </Text>
            {searchText && (
              <Text type="secondary" style={{ fontSize: '13px' }}>
                🔍 Tìm thấy: <Text strong>{filteredLogs.length}</Text> kết quả
              </Text>
            )}
          </Space>
        </div>
      </Card>

      {/* Table Card */}
      <Card>
        <Table
          columns={columns}
          dataSource={filteredLogs}
          rowKey="id"
          loading={isLoading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: logsData?.totalElements || 0,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} bản ghi`,
            pageSizeOptions: ['10', '20', '50', '100'],
            position: ['bottomCenter'],
          }}
          onChange={handleTableChange}
          scroll={{ x: 1300 }}
          size="middle"
          rowClassName={(record) => (record.status === 'FAILED' ? 'audit-log-failed' : '')}
        />
      </Card>

      {/* Add custom CSS for failed rows */}
      <style>{`
        .audit-log-failed {
          background-color: #fff1f0;
        }
        .audit-log-failed:hover > td {
          background-color: #ffccc7 !important;
        }
      `}</style>
    </div>
  );
}
