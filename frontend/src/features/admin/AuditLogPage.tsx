import React, { useState } from 'react';
import { Card, Table, Space, Select, DatePicker, Input, Tag, Button } from 'antd';
import { SearchOutlined, DownloadOutlined, HistoryOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;
const { Option } = Select;

interface AuditLog {
  id: string;
  timestamp: string;
  userId: string;
  userName: string;
  userRole: string;
  action: string;
  resource: string;
  resourceId: string;
  details: string;
  ipAddress: string;
  status: 'success' | 'failed';
}

// Mock audit logs
const mockAuditLogs: AuditLog[] = [
  {
    id: '1',
    timestamp: '2024-12-08 14:30:25',
    userId: 'u001',
    userName: 'Nguyễn Văn A',
    userRole: 'Admin',
    action: 'CREATE',
    resource: 'User',
    resourceId: 'u025',
    details: 'Tạo tài khoản mới cho giảng viên Trần Thị B',
    ipAddress: '192.168.1.100',
    status: 'success',
  },
  {
    id: '2',
    timestamp: '2024-12-08 14:25:10',
    userId: 'u002',
    userName: 'Lê Văn C',
    userRole: 'HoD',
    action: 'APPROVE',
    resource: 'Syllabus',
    resourceId: 's042',
    details: 'Phê duyệt đề cương CS301 - Cấu trúc dữ liệu',
    ipAddress: '192.168.1.105',
    status: 'success',
  },
  {
    id: '3',
    timestamp: '2024-12-08 14:20:45',
    userId: 'u003',
    userName: 'Phạm Thị D',
    userRole: 'AA',
    action: 'UPDATE',
    resource: 'PLO',
    resourceId: 'plo05',
    details: 'Cập nhật mô tả PLO5 - Năng lực tự học',
    ipAddress: '192.168.1.110',
    status: 'success',
  },
  {
    id: '4',
    timestamp: '2024-12-08 14:15:30',
    userId: 'u004',
    userName: 'Hoàng Văn E',
    userRole: 'Lecturer',
    action: 'SUBMIT',
    resource: 'Syllabus',
    resourceId: 's043',
    details: 'Nộp đề cương CS401 - Hệ điều hành để phê duyệt',
    ipAddress: '192.168.1.115',
    status: 'success',
  },
  {
    id: '5',
    timestamp: '2024-12-08 14:10:15',
    userId: 'u001',
    userName: 'Nguyễn Văn A',
    userRole: 'Admin',
    action: 'DELETE',
    resource: 'User',
    resourceId: 'u020',
    details: 'Xóa tài khoản người dùng không hoạt động',
    ipAddress: '192.168.1.100',
    status: 'success',
  },
  {
    id: '6',
    timestamp: '2024-12-08 14:05:00',
    userId: 'u005',
    userName: 'Vũ Thị F',
    userRole: 'Principal',
    action: 'APPROVE',
    resource: 'Syllabus',
    resourceId: 's038',
    details: 'Phê duyệt cuối cùng 5 đề cương (batch approval)',
    ipAddress: '192.168.1.120',
    status: 'success',
  },
  {
    id: '7',
    timestamp: '2024-12-08 13:58:30',
    userId: 'u002',
    userName: 'Lê Văn C',
    userRole: 'HoD',
    action: 'REJECT',
    resource: 'Syllabus',
    resourceId: 's044',
    details: 'Từ chối đề cương CS501 - Thiếu CLO ánh xạ PLO',
    ipAddress: '192.168.1.105',
    status: 'success',
  },
  {
    id: '8',
    timestamp: '2024-12-08 13:55:20',
    userId: 'u006',
    userName: 'Đỗ Văn G',
    userRole: 'Lecturer',
    action: 'LOGIN',
    resource: 'System',
    resourceId: '-',
    details: 'Đăng nhập hệ thống',
    ipAddress: '192.168.1.125',
    status: 'success',
  },
  {
    id: '9',
    timestamp: '2024-12-08 13:50:10',
    userId: 'u001',
    userName: 'Nguyễn Văn A',
    userRole: 'Admin',
    action: 'UPDATE',
    resource: 'SystemConfig',
    resourceId: 'cfg01',
    details: 'Cập nhật thời gian phê duyệt tối đa: HoD=3 ngày, AA=5 ngày',
    ipAddress: '192.168.1.100',
    status: 'success',
  },
  {
    id: '10',
    timestamp: '2024-12-08 13:45:00',
    userId: 'u007',
    userName: 'Trần Văn H',
    userRole: 'Lecturer',
    action: 'EXPORT',
    resource: 'Syllabus',
    resourceId: 's040',
    details: 'Xuất file CSV danh sách đề cương',
    ipAddress: '192.168.1.130',
    status: 'success',
  },
];

export const AuditLogPage: React.FC = () => {
  const [searchText, setSearchText] = useState('');
  const [selectedAction, setSelectedAction] = useState<string>('all');
  const [selectedRole, setSelectedRole] = useState<string>('all');
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);

  // Mock query
  const { data: logs, isLoading } = useQuery({
    queryKey: ['audit-logs', searchText, selectedAction, selectedRole, dateRange],
    queryFn: async () => {
      await new Promise((resolve) => setTimeout(resolve, 500));
      let filteredLogs = [...mockAuditLogs];

      if (searchText) {
        filteredLogs = filteredLogs.filter(
          (log) =>
            log.userName.toLowerCase().includes(searchText.toLowerCase()) ||
            log.details.toLowerCase().includes(searchText.toLowerCase())
        );
      }

      if (selectedAction !== 'all') {
        filteredLogs = filteredLogs.filter((log) => log.action === selectedAction);
      }

      if (selectedRole !== 'all') {
        filteredLogs = filteredLogs.filter((log) => log.userRole === selectedRole);
      }

      return filteredLogs;
    },
  });

  const columns: ColumnsType<AuditLog> = [
    {
      title: 'Thời gian',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 110,
      align: 'center',
      sorter: (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime(),
      render: (timestamp) => {
        const date = new Date(timestamp);
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')} ${date.getDate().toString().padStart(2, '0')}/${(date.getMonth()+1).toString().padStart(2, '0')}`;
      },
    },
    {
      title: 'Người dùng',
      key: 'user',
      width: 200,
      align: 'center',
      ellipsis: { showTitle: false },
      render: (_, record) => (
        <Space size="small">
          <strong>{record.userName}</strong>
          <Tag color="blue">{record.userRole}</Tag>
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
          EXPORT: 'geekblue',
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
      render: (_, record) => `${record.resource}${record.resourceId !== '-' ? ` #${record.resourceId}` : ''}`,
    },
    {
      title: 'Chi tiết',
      dataIndex: 'details',
      key: 'details',
      align: 'center',
      ellipsis: { showTitle: false },
      minWidth: 200,
    },
    {
      title: 'Kết quả',
      dataIndex: 'status',
      key: 'status',
      width: 110,
      align: 'center',
      render: (status) => (
        <Tag color={status === 'success' ? 'success' : 'error'}>
          {status === 'success' ? 'Thành công' : 'Lỗi'}
        </Tag>
      ),
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
            style={{ width: 300 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />

          <Select value={selectedAction} onChange={setSelectedAction} style={{ width: 180 }}>
            <Option value="all">Tất cả hành động</Option>
            <Option value="CREATE">CREATE</Option>
            <Option value="UPDATE">UPDATE</Option>
            <Option value="DELETE">DELETE</Option>
            <Option value="APPROVE">APPROVE</Option>
            <Option value="REJECT">REJECT</Option>
            <Option value="SUBMIT">SUBMIT</Option>
            <Option value="LOGIN">LOGIN</Option>
            <Option value="EXPORT">EXPORT</Option>
          </Select>

          <Select value={selectedRole} onChange={setSelectedRole} style={{ width: 160 }}>
            <Option value="all">Tất cả vai trò</Option>
            <Option value="Admin">Admin</Option>
            <Option value="Principal">Principal</Option>
            <Option value="HoD">HoD</Option>
            <Option value="AA">AA</Option>
            <Option value="Lecturer">Lecturer</Option>
          </Select>

          <RangePicker
            value={dateRange}
            onChange={(dates) => dates && setDateRange(dates as [dayjs.Dayjs, dayjs.Dayjs])}
            format="DD/MM/YYYY"
            placeholder={['Từ ngày', 'Đến ngày']}
          />
        </Space>
      </Card>

      <Card>
        <Table
          columns={columns}
          dataSource={logs || []}
          rowKey="id"
          loading={isLoading}
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} bản ghi`,
          }}
          scroll={{ x: 950 }}
        />
      </Card>
    </div>
  );
};
