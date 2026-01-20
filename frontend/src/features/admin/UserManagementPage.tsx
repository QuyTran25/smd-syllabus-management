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
  Row,
  Col,
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
  const [form] = Form.useForm();

  // --- States ---
  const [searchText, setSearchText] = useState('');
  const [roleFilter, setRoleFilter] = useState<UserRole | undefined>();
  const [statusFilter, setStatusFilter] = useState<boolean | undefined>();

  // Pagination
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });

  // Modal visibility
  const [isCreateModalVisible, setIsCreateModalVisible] = useState(false);
  const [isEditModalVisible, setIsEditModalVisible] = useState(false);
  const [isImportModalVisible, setIsImportModalVisible] = useState(false);

  // Selected Data for Edit/Dependent Dropdowns
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [selectedRole, setSelectedRole] = useState<string | undefined>();
  const [selectedFacultyId, setSelectedFacultyId] = useState<string | undefined>();

  // --- Queries ---

  // 1. Fetch Users (Server-side Pagination & Filter)
  const { data: userResponse, isLoading } = useQuery({
    queryKey: [
      'users',
      pagination.current,
      pagination.pageSize,
      roleFilter,
      statusFilter,
      searchText,
    ],
    queryFn: () =>
      userService.getUsers({
        page: pagination.current - 1, // Backend 0-based
        size: pagination.pageSize,
        role: roleFilter,
        isActive: statusFilter,
        search: searchText,
      }),
  });

  // 2. Fetch Faculties
  const { data: faculties } = useQuery({
    queryKey: ['faculties'],
    queryFn: () => facultyService.getAllFaculties(),
  });

  // 3. Fetch Departments (Dependent on selectedFacultyId)
  const { data: departments } = useQuery({
    queryKey: ['departments', selectedFacultyId],
    queryFn: () => facultyService.getDepartmentsByFaculty(selectedFacultyId!),
    enabled: !!selectedFacultyId,
  });

  // --- Mutations ---

  const createMutation = useMutation({
    mutationFn: (values: any) => userService.createUser(values),
    onSuccess: () => {
      message.success('Tạo người dùng thành công');
      setIsCreateModalVisible(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (err: any) => message.error(err.response?.data?.message || 'Tạo thất bại'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) => userService.updateUser(id, data),
    onSuccess: () => {
      message.success('Cập nhật thành công');
      setIsEditModalVisible(false);
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (err: any) => message.error(err.response?.data?.message || 'Cập nhật thất bại'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => userService.deleteUser(id),
    onSuccess: () => {
      message.success('Xóa người dùng thành công');
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  const toggleStatusMutation = useMutation({
    mutationFn: (id: string) => userService.toggleUserStatus(id),
    onSuccess: (user) => {
      message.success(`Đã ${user.isActive ? 'mở khóa' : 'khóa'} tài khoản`);
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  const importMutation = useMutation({
    mutationFn: (file: File) => userService.importUsers(file),
    onSuccess: (res: any) => {
      if (res.failed > 0) {
        Modal.warning({
          title: `Import: ${res.success} thành công, ${res.failed} lỗi`,
          content: (
            <div style={{ maxHeight: 300, overflow: 'auto' }}>
              <ul style={{ paddingLeft: 20 }}>
                {res.errors.map((e: string, i: number) => (
                  <li key={i} style={{ color: 'red' }}>
                    {e}
                  </li>
                ))}
              </ul>
            </div>
          ),
          width: 600,
        });
      } else {
        message.success(`Import thành công ${res.success} dòng!`);
      }
      setIsImportModalVisible(false);
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: () => message.error('Lỗi khi import file'),
  });

  // --- Handlers ---

  const handleOpenCreate = () => {
    form.resetFields();
    setSelectedRole(undefined);
    setSelectedFacultyId(undefined);
    setIsCreateModalVisible(true);
  };

  const handleOpenEdit = (user: User) => {
    setSelectedUser(user);

    // Set state để trigger load departments và hiển thị đúng logic
    // Lưu ý: role trong user là Enum, cần convert sang string khớp với value của Select
    const roleStr = user.role?.toString();
    setSelectedRole(roleStr);
    setSelectedFacultyId(user.facultyId);

    // Fill form
    form.setFieldsValue({
      fullName: user.fullName,
      email: user.email,
      phone: user.phone,
      role: roleStr,
      facultyId: user.facultyId,
      departmentId: user.departmentId,
      isActive: user.isActive,
    });

    setIsEditModalVisible(true);
  };

  const roleLabels: Record<string, string> = {
    ADMIN: 'Quản trị viên',
    LECTURER: 'Giảng viên',
    HOD: 'Trưởng bộ môn',
    AA: 'Giáo vụ',
    PRINCIPAL: 'Hiệu trưởng',
    STUDENT: 'Sinh viên',
  };

  const roleColors: Record<string, string> = {
    ADMIN: 'red',
    LECTURER: 'blue',
    HOD: 'green',
    AA: 'purple',
    PRINCIPAL: 'gold',
    STUDENT: 'default',
  };

  // --- Columns ---
  const columns: ColumnsType<User> = [
    {
      title: 'Người dùng',
      key: 'user',
      width: 250,
      render: (_, r) => (
        <Space>
          <Avatar icon={<UserOutlined />} src={r.avatar} />
          <div>
            <Text strong>{r.fullName}</Text> <br />
            <Text type="secondary" style={{ fontSize: 12 }}>
              {r.email}
            </Text>
          </div>
        </Space>
      ),
    },
    {
      title: 'Vai trò',
      dataIndex: 'role',
      width: 150,
      render: (role) => <Tag color={roleColors[role] || 'default'}>{roleLabels[role] || role}</Tag>,
    },
    {
      title: 'Đơn vị',
      key: 'org',
      width: 200,
      render: (_, r) => (
        <div>
          {r.faculty && (
            <div>
              <Text type="secondary">K: </Text>
              {r.faculty}
            </div>
          )}
          {r.department && <div style={{ fontSize: 12, color: '#666' }}>BM: {r.department}</div>}
        </div>
      ),
    },
    {
      title: 'SĐT',
      dataIndex: 'phone',
      width: 120,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'isActive',
      width: 100,
      render: (active) => (
        <Tag color={active ? 'success' : 'error'}>{active ? 'Active' : 'Locked'}</Tag>
      ),
    },
    {
      title: 'Hành động',
      key: 'action',
      width: 120,
      fixed: 'right',
      render: (_, r) => (
        <Space>
          <Button icon={<EditOutlined />} type="text" onClick={() => handleOpenEdit(r)} />
          <Button
            icon={r.isActive ? <LockOutlined /> : <UnlockOutlined />}
            type="text"
            danger={r.isActive}
            style={{ color: !r.isActive ? '#52c41a' : undefined }}
            onClick={() => toggleStatusMutation.mutate(r.id)}
          />
          <Popconfirm title="Xóa người dùng?" onConfirm={() => deleteMutation.mutate(r.id)}>
            <Button icon={<DeleteOutlined />} type="text" danger />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // --- Form Content (Reusable) ---
  const renderFormContent = () => (
    <>
      <Row gutter={16}>
        <Col span={12}>
          <Form.Item
            name="fullName"
            label="Họ và tên"
            rules={[{ required: true, message: 'Nhập họ tên' }]}
          >
            <Input placeholder="Nguyễn Văn A" />
          </Form.Item>
        </Col>
        <Col span={12}>
          <Form.Item
            name="email"
            label="Email"
            rules={[{ required: true, type: 'email', message: 'Email không hợp lệ' }]}
          >
            <Input placeholder="email@smd.edu.vn" />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={12}>
          <Form.Item
            name="role"
            label="Vai trò"
            rules={[{ required: true, message: 'Chọn vai trò' }]}
          >
            <Select onChange={setSelectedRole} placeholder="Chọn vai trò">
              {Object.entries(roleLabels).map(([key, label]) => (
                <Option key={key} value={key}>
                  {label}
                </Option>
              ))}
            </Select>
          </Form.Item>
        </Col>
        <Col span={12}>
          <Form.Item name="phone" label="Số điện thoại">
            <Input />
          </Form.Item>
        </Col>
      </Row>

      {/* Chỉ hiện Khoa/Bộ môn nếu Role là LECTURER hoặc HOD */}
      {(selectedRole === 'LECTURER' || selectedRole === 'HOD') && (
        <Row
          gutter={16}
          style={{
            background: '#f5f5f5',
            padding: '10px',
            borderRadius: '8px',
            marginBottom: '16px',
          }}
        >
          <Col span={24}>
            <Text type="secondary" style={{ fontSize: 12, marginBottom: 8, display: 'block' }}>
              Thông tin đơn vị công tác:
            </Text>
          </Col>
          <Col span={12}>
            <Form.Item
              name="facultyId"
              label="Khoa"
              rules={[{ required: true, message: 'Vui lòng chọn Khoa' }]}
            >
              <Select
                placeholder="Chọn Khoa"
                showSearch
                optionFilterProp="label"
                options={faculties?.map((f) => ({ label: f.name, value: f.id }))}
                onChange={(val) => {
                  setSelectedFacultyId(val);
                  form.setFieldValue('departmentId', undefined); // Reset bộ môn khi đổi khoa
                }}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="departmentId"
              label="Bộ môn"
              rules={[{ required: true, message: 'Vui lòng chọn Bộ môn' }]}
            >
              <Select
                placeholder={!selectedFacultyId ? 'Chọn Khoa trước' : 'Chọn Bộ môn'}
                showSearch
                optionFilterProp="label"
                disabled={!selectedFacultyId}
                options={departments?.map((d) => ({ label: d.name, value: d.id }))}
              />
            </Form.Item>
          </Col>
        </Row>
      )}

      {!selectedUser && (
        <Form.Item
          name="password"
          label="Mật khẩu khởi tạo"
          initialValue="Smd@123456"
          help="Mặc định: Smd@123456"
        >
          <Input.Password />
        </Form.Item>
      )}

      {selectedUser && (
        <Form.Item name="isActive" label="Trạng thái" valuePropName="checked" initialValue={true}>
          <Select>
            <Option value={true}>Hoạt động</Option>
            <Option value={false}>Đang khóa</Option>
          </Select>
        </Form.Item>
      )}
    </>
  );

  return (
    <div style={{ padding: 24 }}>
      <div
        style={{
          marginBottom: 16,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Title level={4} style={{ margin: 0 }}>
          Quản lý người dùng
        </Title>
        <Space>
          <Button icon={<UploadOutlined />} onClick={() => setIsImportModalVisible(true)}>
            Import CSV
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleOpenCreate}>
            Thêm mới
          </Button>
        </Space>
      </div>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input
            placeholder="Tìm tên hoặc email..."
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            style={{ width: 250 }}
            allowClear
          />
          <Select
            placeholder="Lọc theo vai trò"
            allowClear
            onChange={setRoleFilter}
            style={{ width: 180 }}
          >
            {Object.entries(roleLabels).map(([key, label]) => (
              <Option key={key} value={key}>
                {label}
              </Option>
            ))}
          </Select>
          <Select
            placeholder="Trạng thái"
            allowClear
            onChange={setStatusFilter}
            style={{ width: 120 }}
          >
            <Option value={true}>Active</Option>
            <Option value={false}>Locked</Option>
          </Select>
        </Space>

        <Table
          columns={columns}
          dataSource={userResponse?.data || []}
          loading={isLoading}
          rowKey="id"
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: userResponse?.total || 0,
            showSizeChanger: true,
            onChange: (page, size) => setPagination({ current: page, pageSize: size }),
            showTotal: (total) => `Tổng ${total} user`,
          }}
          scroll={{ x: 1000 }}
        />
      </Card>

      {/* Create Modal */}
      <Modal
        title="Thêm người dùng mới"
        open={isCreateModalVisible}
        onCancel={() => setIsCreateModalVisible(false)}
        onOk={() => form.submit()}
        confirmLoading={createMutation.isPending}
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={createMutation.mutate}>
          {renderFormContent()}
        </Form>
      </Modal>

      {/* Edit Modal */}
      <Modal
        title="Cập nhật thông tin"
        open={isEditModalVisible}
        onCancel={() => setIsEditModalVisible(false)}
        onOk={() => form.submit()}
        confirmLoading={updateMutation.isPending}
        width={700}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => updateMutation.mutate({ id: selectedUser!.id, data: values })}
        >
          {renderFormContent()}
        </Form>
      </Modal>

      {/* Import Modal */}
      <Modal
        title="Import Users từ CSV"
        open={isImportModalVisible}
        onCancel={() => setIsImportModalVisible(false)}
        footer={null}
      >
        <Upload.Dragger
          accept=".csv"
          beforeUpload={(file) => {
            importMutation.mutate(file);
            return false;
          }}
          showUploadList={false}
          disabled={importMutation.isPending}
        >
          <p className="ant-upload-drag-icon">
            <UploadOutlined />
          </p>
          <p className="ant-upload-text">Kéo thả hoặc nhấn để chọn file CSV</p>
          <p className="ant-upload-hint">
            Format yêu cầu: email, fullName, role, phone (không có header)
          </p>
        </Upload.Dragger>
      </Modal>
    </div>
  );
};
