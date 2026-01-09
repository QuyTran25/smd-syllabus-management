import React, { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Input,
  Select,
  Modal,
  Form,
  Upload,
  message,
  Popconfirm,
  Avatar,
  Typography,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  UploadOutlined,
  EditOutlined,
  DeleteOutlined,
  LockOutlined,
  UnlockOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userService } from '@/services';
import facultyService from '@/services/faculty.service';
import { User, UserRole } from '@/types';
import type { ColumnsType } from 'antd/es/table';

const { Title, Text } = Typography;
const { Option } = Select;

export const UserManagementPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchText, setSearchText] = useState('');
  const [roleFilter, setRoleFilter] = useState<UserRole | undefined>();
  const [statusFilter, setStatusFilter] = useState<boolean | undefined>();
  const [isCreateModalVisible, setIsCreateModalVisible] = useState(false);
  const [isEditModalVisible, setIsEditModalVisible] = useState(false);
  const [isImportModalVisible, setIsImportModalVisible] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [selectedRole, setSelectedRole] = useState<UserRole | undefined>();
  const [selectedDepartment, setSelectedDepartment] = useState<string | undefined>();
  const [selectedFacultyId, setSelectedFacultyId] = useState<string | undefined>();
  const [form] = Form.useForm();

  // Fetch users
  const { data: users, isLoading } = useQuery({
    queryKey: ['users', roleFilter, statusFilter, searchText],
    queryFn: () => userService.getUsers({ role: roleFilter, isActive: statusFilter, search: searchText }),
  });

  // Fetch all faculties for dropdown
  const { data: faculties } = useQuery({
    queryKey: ['faculties'],
    queryFn: () => facultyService.getAllFaculties(),
  });

  // Fetch departments for selected faculty
  const { data: departments } = useQuery({
    queryKey: ['departments', selectedFacultyId],
    queryFn: () => facultyService.getDepartmentsByFaculty(selectedFacultyId!),
    enabled: !!selectedFacultyId,
  });

  // Fetch HODs for manager dropdown (filtered by department)
  const { data: hods } = useQuery({
    queryKey: ['users', UserRole.HOD, selectedDepartment],
    queryFn: () => userService.getUsers({ role: UserRole.HOD }),
    select: (data) => {
      if (!selectedDepartment) return data;
      return data.filter(user => user.department === selectedDepartment);
    },
  });

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (data: Omit<User, 'id' | 'createdAt' | 'lastLogin'>) => userService.createUser(data),
    onSuccess: () => {
      message.success('Tạo người dùng thành công');
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsCreateModalVisible(false);
      form.resetFields();
    },
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<User> }) => userService.updateUser(id, data),
    onSuccess: () => {
      message.success('Cập nhật người dùng thành công');
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsEditModalVisible(false);
      setSelectedUser(null);
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id: string) => userService.deleteUser(id),
    onSuccess: () => {
      message.success('Xóa người dùng thành công');
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  // Toggle status mutation
  const toggleStatusMutation = useMutation({
    mutationFn: (id: string) => userService.toggleUserStatus(id),
    onSuccess: (user) => {
      message.success(`${user.isActive ? 'Mở khóa' : 'Khóa'} tài khoản thành công`);
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  // Import mutation
  const importMutation = useMutation({
    mutationFn: (file: File) => userService.importUsers(file),
    onSuccess: (result) => {
      if (result.errors && result.errors.length > 0) {
        Modal.warning({
          title: `Import hoàn tất: ${result.success} thành công, ${result.failed} lỗi`,
          content: (
            <div>
              <p>Các lỗi gặp phải:</p>
              <ul style={{ maxHeight: '300px', overflow: 'auto' }}>
                {result.errors.map((err, idx) => (
                  <li key={idx} style={{ color: 'red', fontSize: '12px' }}>{err}</li>
                ))}
              </ul>
            </div>
          ),
          width: 600,
        });
      } else {
        message.success(`Import thành công ${result.success} người dùng`);
      }
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsImportModalVisible(false);
    },
    onError: (error: Error) => {
      message.error(error.message || 'Import thất bại');
    },
  });

  const handleCreate = (values: any) => {
    createMutation.mutate(values);
  };

  const handleEdit = (values: any) => {
    if (!selectedUser) return;
    updateMutation.mutate({ id: selectedUser.id, data: values });
  };

  const handleDelete = (id: string) => {
    deleteMutation.mutate(id);
  };

  const handleToggleStatus = (id: string) => {
    toggleStatusMutation.mutate(id);
  };

  const handleImport = (info: any) => {
    if (info.file.status === 'done') {
      importMutation.mutate(info.file.originFileObj);
    }
  };

  const openEditModal = (user: User) => {
    setSelectedUser(user);
    setSelectedRole(user.role);
    setSelectedDepartment(user.department);
    form.setFieldsValue(user);
    setIsEditModalVisible(true);
  };

  const roleLabels: Record<UserRole, string> = {
    [UserRole.ADMIN]: 'Quản trị viên',
    [UserRole.LECTURER]: 'Giảng viên',
    [UserRole.HOD]: 'Trưởng Bộ môn',
    [UserRole.AA]: 'Phòng Đào tạo',
    [UserRole.PRINCIPAL]: 'Hiệu trưởng',
    [UserRole.STUDENT]: 'Sinh viên',
  };

  const columns: ColumnsType<User> = [
    {
      title: 'Người dùng',
      key: 'user',
      width: 250,
      ellipsis: { showTitle: false },
      render: (_, record) => (
        <Space size="small">
          <Avatar size="small" src={record.avatar} icon={<UserOutlined />} />
          <Text strong>{record.fullName}</Text>
        </Space>
      ),
    },
    {
      title: 'Vai trò',
      dataIndex: 'role',
      key: 'role',
      width: 115,
      render: (role: UserRole) => {
        const colors: Record<UserRole, string> = {
          ADMIN: 'red',
          PRINCIPAL: 'purple',
          AA: 'blue',
          HOD: 'green',
          LECTURER: 'orange',
          STUDENT: 'default',
        };
        return <Tag color={colors[role]}>{roleLabels[role]}</Tag>;
      },
    },
    {
      title: 'Khoa/Bộ môn',
      key: 'department',
      width: 200,
      ellipsis: { showTitle: false },
      render: (_, record) => (
        <Text>
          {record.faculty}{record.department ? ` - ${record.department}` : ''}
        </Text>
      ),
    },
    {
      title: 'SĐT',
      dataIndex: 'phone',
      key: 'phone',
      width: 110,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 95,
      render: (isActive: boolean) => (
        <Tag color={isActive ? 'success' : 'error'}>{isActive ? 'Hoạt động' : 'Khóa'}</Tag>
      ),
    },
    {
      title: 'Đăng nhập',
      dataIndex: 'lastLogin',
      key: 'lastLogin',
      width: 90,
      render: (date?: string) => date ? new Date(date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }) : '-',
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 110,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => openEditModal(record)}
          />
          <Button
            type="text"
            icon={record.isActive ? <LockOutlined /> : <UnlockOutlined />}
            onClick={() => handleToggleStatus(record.id)}
            style={{ color: record.isActive ? '#ff4d4f' : '#52c41a' }}
          />
          <Popconfirm
            title="Xóa người dùng này?"
            onConfirm={() => handleDelete(record.id)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button type="text" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={2} style={{ margin: 0 }}>
          Quản lý Người dùng
        </Title>
        <Space>
          <Button icon={<UploadOutlined />} onClick={() => setIsImportModalVisible(true)}>
            Import CSV
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsCreateModalVisible(true)}>
            Tạo người dùng
          </Button>
        </Space>
      </div>

      <Card>
        <Space style={{ marginBottom: 16 }} size="middle">
          <Input
            placeholder="Tìm theo tên hoặc email..."
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            style={{ width: 300 }}
            allowClear
          />
          <Select
            placeholder="Lọc theo vai trò"
            style={{ width: 200 }}
            onChange={setRoleFilter}
            allowClear
          >
            {Object.entries(roleLabels).map(([key, label]) => (
              <Option key={key} value={key}>{label}</Option>
            ))}
          </Select>
          <Select
            placeholder="Lọc theo trạng thái"
            style={{ width: 150 }}
            onChange={setStatusFilter}
            allowClear
          >
            <Option value={true}>Hoạt động</Option>
            <Option value={false}>Bị khóa</Option>
          </Select>
        </Space>

        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={isLoading}
          scroll={{ x: 1000 }}
        />
      </Card>

      {/* Create Modal */}
      <Modal
        title="Tạo người dùng mới"
        open={isCreateModalVisible}
        onCancel={() => {
          setIsCreateModalVisible(false);
          setSelectedRole(undefined);
          setSelectedDepartment(undefined);
          setSelectedFacultyId(undefined);
          form.resetFields();
        }}
        onOk={() => form.submit()}
        confirmLoading={createMutation.isPending}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="fullName" label="Họ và tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="role" label="Vai trò" rules={[{ required: true }]}>
            <Select onChange={(value) => setSelectedRole(value)}>
              {Object.entries(roleLabels).map(([key, label]) => (
                <Option key={key} value={key}>{label}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="phone" label="Số điện thoại">
            <Input />
          </Form.Item>
          <Form.Item 
            name="facultyId" 
            label="Khoa"
            rules={[
              { 
                required: selectedRole === UserRole.LECTURER || selectedRole === UserRole.HOD, 
                message: 'Vui lòng chọn khoa' 
              }
            ]}
          >
            <Select 
              placeholder="Chọn khoa"
              showSearch
              allowClear
              onChange={(value) => {
                setSelectedFacultyId(value);
                form.setFieldValue('departmentId', undefined);
              }}
              options={faculties?.map(f => ({
                value: f.id,
                label: f.name,
              }))}
              filterOption={(input, option) =>
                String(option?.label || '').toLowerCase().includes(input.toLowerCase())
              }
            />
          </Form.Item>
          <Form.Item 
            name="departmentId" 
            label="Bộ môn"
            rules={[
              { 
                required: selectedRole === UserRole.LECTURER || selectedRole === UserRole.HOD, 
                message: 'Vui lòng chọn bộ môn' 
              }
            ]}
          >
            <Select 
              placeholder="Chọn bộ môn"
              showSearch
              allowClear
              disabled={!selectedFacultyId}
              onChange={(value) => {
                const dept = departments?.find(d => d.id === value);
                setSelectedDepartment(dept?.name);
              }}
              options={departments?.map(d => ({
                value: d.id,
                label: d.name,
              }))}
              filterOption={(input, option) =>
                String(option?.label || '').toLowerCase().includes(input.toLowerCase())
              }
            />
          </Form.Item>
          {selectedRole === UserRole.LECTURER && (
            <Form.Item 
              name="managerId" 
              label="Trưởng bộ môn quản lý trực tiếp" 
              rules={[{ required: true, message: 'Vui lòng chọn Trưởng bộ môn' }]}
            >
              <Select 
                placeholder="Chọn Trưởng bộ môn"
                showSearch
                options={hods?.map(hod => ({
                  value: hod.id,
                  label: `${hod.fullName}${hod.department ? ` (${hod.department})` : ''}`,
                }))}
                filterOption={(input, option) =>
                  String(option?.label || '').toLowerCase().includes(input.toLowerCase())
                }
              />
            </Form.Item>
          )}
          <Form.Item name="isActive" label="Trạng thái" initialValue={true}>
            <Select>
              <Option value={true}>Hoạt động</Option>
              <Option value={false}>Khóa</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* Edit Modal */}
      <Modal
        title="Chỉnh sửa người dùng"
        open={isEditModalVisible}
        onCancel={() => {
          setIsEditModalVisible(false);
          setSelectedUser(null);
          setSelectedRole(undefined);
          setSelectedDepartment(undefined);
          setSelectedFacultyId(undefined);
          form.resetFields();
        }}
        onOk={() => form.submit()}
        confirmLoading={updateMutation.isPending}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleEdit}>
          <Form.Item name="fullName" label="Họ và tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="role" label="Vai trò" rules={[{ required: true }]}>
            <Select onChange={(value) => setSelectedRole(value)}>
              {Object.entries(roleLabels).map(([key, label]) => (
                <Option key={key} value={key}>{label}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="phone" label="Số điện thoại">
            <Input />
          </Form.Item>
          <Form.Item 
            name="facultyId" 
            label="Khoa"
            rules={[
              { 
                required: selectedRole === UserRole.LECTURER || selectedRole === UserRole.HOD || selectedUser?.role === UserRole.LECTURER || selectedUser?.role === UserRole.HOD, 
                message: 'Vui lòng chọn khoa' 
              }
            ]}
          >
            <Select 
              placeholder="Chọn khoa"
              showSearch
              allowClear
              onChange={(value) => {
                setSelectedFacultyId(value);
                form.setFieldValue('departmentId', undefined);
              }}
              options={faculties?.map(f => ({
                value: f.id,
                label: f.name,
              }))}
              filterOption={(input, option) =>
                String(option?.label || '').toLowerCase().includes(input.toLowerCase())
              }
            />
          </Form.Item>
          <Form.Item 
            name="departmentId" 
            label="Bộ môn"
            rules={[
              { 
                required: selectedRole === UserRole.LECTURER || selectedRole === UserRole.HOD || selectedUser?.role === UserRole.LECTURER || selectedUser?.role === UserRole.HOD, 
                message: 'Vui lòng chọn bộ môn' 
              }
            ]}
          >
            <Select 
              placeholder="Chọn bộ môn"
              showSearch
              allowClear
              disabled={!selectedFacultyId}
              onChange={(value) => {
                const dept = departments?.find(d => d.id === value);
                setSelectedDepartment(dept?.name);
              }}
              options={departments?.map(d => ({
                value: d.id,
                label: d.name,
              }))}
              filterOption={(input, option) =>
                String(option?.label || '').toLowerCase().includes(input.toLowerCase())
              }
            />
          </Form.Item>
          {(selectedRole === UserRole.LECTURER || selectedUser?.role === UserRole.LECTURER) && (
            <Form.Item 
              name="managerId" 
              label="Trưởng bộ môn quản lý trực tiếp" 
              rules={[{ required: true, message: 'Vui lòng chọn Trưởng bộ môn' }]}
            >
              <Select 
                placeholder="Chọn Trưởng bộ môn"
                showSearch
                options={hods?.map(hod => ({
                  value: hod.id,
                  label: `${hod.fullName}${hod.department ? ` (${hod.department})` : ''}`,
                }))}
                filterOption={(input, option) =>
                  String(option?.label || '').toLowerCase().includes(input.toLowerCase())
                }
              />
            </Form.Item>
          )}
          <Form.Item name="isActive" label="Trạng thái">
            <Select>
              <Option value={true}>Hoạt động</Option>
              <Option value={false}>Khóa</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* Import Modal */}
      <Modal
        title="Import người dùng từ CSV"
        open={isImportModalVisible}
        onCancel={() => setIsImportModalVisible(false)}
        footer={null}
      >
        <Upload
          accept=".csv"
          maxCount={1}
          onChange={handleImport}
          beforeUpload={() => false}
        >
          <Button icon={<UploadOutlined />} block>Chọn file CSV</Button>
        </Upload>
        <div style={{ marginTop: 16, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
          <p style={{ margin: 0, fontWeight: 500 }}>Format CSV:</p>
          <code style={{ fontSize: '0.85rem' }}>
            fullName,email,role,phone,faculty,department
          </code>
        </div>
      </Modal>
    </div>
  );
};
