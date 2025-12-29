import React, { useState } from 'react';
import { Card, Table, Space, Select, DatePicker, Input, Tag, Button, Typography } from 'antd';
import { SearchOutlined, DownloadOutlined, HistoryOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import type { TablePaginationConfig } from 'antd/es/table';
import dayjs from 'dayjs';
import { auditLogService, AuditLog } from '@/services/auditlog.service';

const { RangePicker } = DatePicker;
const { Option } = Select;
const { Text } = Typography;

// Role display mapping
const ROLE_LABELS: Record<string, string> = {
  ADMIN: 'Admin',
  PRINCIPAL: 'Hiệu trưởng',
  HOD: 'Trưởng BM',
  AA: 'Phòng ĐT',
  LECTURER: 'Giảng viên',
  STUDENT: 'Sinh viên',
};

export const AuditLogPage: React.FC = () => {
  const [searchText, setSearchText] = useState('');
  const [selectedAction, setSelectedAction] = useState<string>('all');
  const [selectedEntity, setSelectedEntity] = useState<string>('all');
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 20 });

  // Fetch audit logs from API
  const { data: logsData, isLoading } = useQuery({
    queryKey: ['audit-logs', selectedAction, selectedEntity, dateRange, pagination],
    queryFn: async () => {
      const filters = {
        action: selectedAction !== 'all' ? selectedAction : undefined,
        entityName: selectedEntity !== 'all' ? selectedEntity : undefined,
        startDate: dateRange?.[0]?.toISOString(),
        endDate: dateRange?.[1]?.toISOString(),
        page: pagination.current - 1,
        size: pagination.pageSize,
      };
      return auditLogService.searchAuditLogs(filters);
    },
  });

  // Filter locally by search text (for name/description)
  const filteredLogs = React.useMemo(() => {
    if (!logsData?.content) return [];
    if (!searchText) return logsData.content;
    
    const searchLower = searchText.toLowerCase();
    return logsData.content.filter(
      (log) =>
        log.actorName?.toLowerCase().includes(searchLower) ||
        log.description?.toLowerCase().includes(searchLower)
    );
  }, [logsData?.content, searchText]);

  const handleTableChange = (paginationConfig: TablePaginationConfig) => {
    setPagination({
      current: paginationConfig.current || 1,
      pageSize: paginationConfig.pageSize || 20,
    });
  };

  const columns: ColumnsType<AuditLog> = [
    {
      title: 'Thời gian',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 130,
      align: 'center',
      sorter: (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
      render: (createdAt) => {
        const date = dayjs(createdAt);
        return (
          <Space direction="vertical" size={0}>
            <Text strong>{date.format('HH:mm:ss')}</Text>
            <Text type="secondary" style={{ fontSize: '11px' }}>{date.format('DD/MM/YYYY')}</Text>
          </Space>
        );
      },
    },
    {
      title: 'Người dùng',
      key: 'user',
      width: 180,
      ellipsis: { showTitle: false },
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.actorName || 'Hệ thống'}</Text>
          <Tag color="blue" style={{ fontSize: '10px' }}>
            {ROLE_LABELS[record.actorRole] || record.actorRole || 'System'}
          </Tag>
        </Space>
      ),
    },
    {
      title: 'Hành động',
      dataIndex: 'action',
      key: 'action',
      width: 100,
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
        return <Tag color={colors[action] || 'default'}>{action}</Tag>;
      },
    },
    {
      title: 'Tài nguyên',
      key: 'resource',
      width: 150,
      align: 'center',
      ellipsis: { showTitle: false },
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text>{record.entityName || '-'}</Text>
          {record.entityId && (
            <Text type="secondary" style={{ fontSize: '10px' }}>
              #{record.entityId.substring(0, 8)}...
            </Text>
          )}
        </Space>
      ),
    },
    {
      title: 'Chi tiết',
      dataIndex: 'description',
      key: 'description',
      ellipsis: { showTitle: false },
      minWidth: 250,
      render: (description) => description || '-',
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
      width: 120,
      align: 'center',
      render: (ip) => <Text type="secondary">{ip || '-'}</Text>,
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2><HistoryOutlined /> Nhật ký Hoạt động (Audit Log)</h2>
        <Button icon={<DownloadOutlined />} type="primary">
          Export Log
        </Button>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap size={12}>
          <Input
            placeholder="Tìm theo tên hoặc chi tiết..."
            prefix={<SearchOutlined />}
            style={{ width: 280 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />

          <Select value={selectedAction} onChange={setSelectedAction} style={{ width: 160 }}>
            <Option value="all">Tất cả hành động</Option>
            <Option value="CREATE">CREATE</Option>
            <Option value="UPDATE">UPDATE</Option>
            <Option value="DELETE">DELETE</Option>
            <Option value="APPROVE">APPROVE</Option>
            <Option value="REJECT">REJECT</Option>
            <Option value="SUBMIT">SUBMIT</Option>
            <Option value="LOGIN">LOGIN</Option>
            <Option value="LOGOUT">LOGOUT</Option>
            <Option value="PUBLISH">PUBLISH</Option>
            <Option value="EXPORT">EXPORT</Option>
          </Select>

          <Select value={selectedEntity} onChange={setSelectedEntity} style={{ width: 160 }}>
            <Option value="all">Tất cả tài nguyên</Option>
            <Option value="User">User</Option>
            <Option value="Syllabus">Syllabus</Option>
            <Option value="PLO">PLO</Option>
            <Option value="CLO">CLO</Option>
            <Option value="Subject">Subject</Option>
            <Option value="Semester">Semester</Option>
            <Option value="System">System</Option>
          </Select>

          <RangePicker
            value={dateRange}
            onChange={(dates) => setDateRange(dates as [dayjs.Dayjs, dayjs.Dayjs] | null)}
            format="DD/MM/YYYY"
            placeholder={['Từ ngày', 'Đến ngày']}
          />
        </Space>
      </Card>

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
          }}
          onChange={handleTableChange}
          scroll={{ x: 1100 }}
        />
      </Card>
    </div>
  );
};
