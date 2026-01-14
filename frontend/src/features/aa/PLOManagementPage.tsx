import React, { useState, useMemo } from 'react';
import { Card, Table, Space, Select, Tag, Alert, Button, Modal, Form, Input, App, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import { ploService, PLO } from '../../services/plo.service';
import { subjectService } from '../../services/subject.service';
import { AxiosError } from 'axios';

const { Option } = Select;
const { TextArea } = Input;

// Interface for display (mapped from API)
interface PLODisplay {
  id: string;
  code: string;
  description: string;
  category: 'Knowledge' | 'Skills' | 'Competence' | 'Attitude';
  subjectId: string;
  subjectCode: string;
  subjectName: string;
}

// Map API category to display category
const mapCategory = (apiCategory: PLO['category']): PLODisplay['category'] => {
  const mapping: Record<string, PLODisplay['category']> = {
    KNOWLEDGE: 'Knowledge',
    SKILLS: 'Skills',
    COMPETENCE: 'Competence',
    ATTITUDE: 'Attitude',
  };
  return mapping[apiCategory] || 'Knowledge';
};

export const PLOManagementPage: React.FC = () => {
  const [subjectFilter, setSubjectFilter] = useState<string | undefined>(undefined);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPLO, setEditingPLO] = useState<PLODisplay | null>(null);
  const [form] = Form.useForm();
  const queryClient = useQueryClient();
  const { message } = App.useApp();

  // Fetch PLOs from API
  const { data: plosRaw, isLoading: isLoadingPlos, error: errorPlos } = useQuery({
    queryKey: ['plos'],
    queryFn: () => ploService.getAllPLOs(),
  });

  // Fetch Subjects from API
  const { data: subjects, isLoading: isLoadingSubjects, error: errorSubjects } = useQuery({
    queryKey: ['subjects'],
    queryFn: () => subjectService.getAllSubjects(),
  });

  const handleApiError = (error: unknown, defaultMessage: string) => {
    console.error(defaultMessage, error);
    if (error instanceof AxiosError) {
      const backendMessage = error.response?.data?.message;
      message.error(backendMessage || defaultMessage);
    } else {
      message.error(defaultMessage);
    }
  };

  // Create PLO mutation
  const createPLOMutation = useMutation({
    mutationFn: (values: any) => ploService.createPLO(values),
    onSuccess: () => {
      message.success('Thêm PLO thành công');
      queryClient.invalidateQueries({ queryKey: ['plos'] });
      setIsModalOpen(false);
      form.resetFields();
    },
    onError: (error) => {
      handleApiError(error, 'Thêm PLO thất bại');
    },
  });

  // Update PLO mutation
  const updatePLOMutation = useMutation({
    mutationFn: ({ id, values }: { id: string; values: any }) => ploService.updatePLO(id, values),
    onSuccess: () => {
      message.success('Cập nhật PLO thành công');
      queryClient.invalidateQueries({ queryKey: ['plos'] });
      setIsModalOpen(false);
      setEditingPLO(null);
      form.resetFields();
    },
    onError: (error) => {
      handleApiError(error, 'Cập nhật PLO thất bại');
    },
  });

  // Delete PLO mutation
  const deletePLOMutation = useMutation({
    mutationFn: (id: string) => ploService.deletePLO(id),
    onSuccess: () => {
      message.success('Xóa PLO thành công');
      queryClient.invalidateQueries({ queryKey: ['plos'] });
    },
    onError: (error) => {
      handleApiError(error, 'Xóa PLO thất bại');
    },
  });

  // Map API response to display format
  const plos: PLODisplay[] = useMemo(() => {
    if (!plosRaw) return [];
    return plosRaw.map((p) => ({
      id: p.id,
      code: p.code,
      description: p.description,
      category: mapCategory(p.category),
      subjectId: p.subjectId,
      subjectCode: p.subjectCode,
      subjectName: p.subjectName,
    }));
  }, [plosRaw]);

  const ploColumns: ColumnsType<PLODisplay> = [
    {
      title: 'Môn học',
      key: 'subject',
      width: 250,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span style={{ fontWeight: 500 }}>{record.subjectCode}</span>
          <span style={{ fontSize: '12px', color: '#666' }}>{record.subjectName}</span>
        </Space>
      ),
    },
    {
      title: 'Mã PLO',
      dataIndex: 'code',
      key: 'code',
      width: 100,
      render: (text) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: 'Danh mục',
      dataIndex: 'category',
      key: 'category',
      width: 150,
      render: (category) => {
        const config = {
          Knowledge: { color: 'blue', text: 'Kiến thức' },
          Skills: { color: 'green', text: 'Kỹ năng' },
          Competence: { color: 'orange', text: 'Năng lực' },
          Attitude: { color: 'purple', text: 'Thái độ' },
        };
        const cfg = config[category as keyof typeof config] || { color: 'default', text: category };
        return <Tag color={cfg.color}>{cfg.text}</Tag>;
      },
    },
    {
      title: 'Hành động',
      key: 'action',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setEditingPLO(record);
              form.setFieldsValue({
                subjectId: record.subjectId,
                code: record.code,
                description: record.description,
                category: record.category.toUpperCase(),
              });
              setIsModalOpen(true);
            }}
          >
            Sửa
          </Button>
          <Popconfirm
            title="Xóa PLO"
            description="Bạn có chắc muốn xóa PLO này?"
            onConfirm={() => deletePLOMutation.mutate(record.id)}
            okText="Xóa"
            cancelText="Hủy"
            okButtonProps={{ danger: true }}
          >
            <Button type="link" danger size="small" icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // Filter PLOs by subject
  const filteredPLOs = subjectFilter
    ? plos?.filter((p) => p.subjectId === subjectFilter)
    : plos;

  if (errorPlos || errorSubjects) {
    return (
      <div style={{ padding: 24 }}>
        <Alert
          type="error"
          message="Lỗi tải dữ liệu"
          description="Không thể tải danh sách PLO hoặc môn học. Vui lòng thử lại sau."
        />
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Quản lý PLO (Chuẩn đầu ra)</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>
          Thêm PLO
        </Button>
      </div>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Select
            placeholder="Lọc theo môn học"
            style={{ width: 350 }}
            allowClear
            value={subjectFilter}
            onChange={(value) => setSubjectFilter(value)}
            loading={isLoadingSubjects}
          >
            {subjects?.map((s) => (
              <Option key={s.id} value={s.id}>
                {s.code} - {s.currentNameVi}
              </Option>
            ))}
          </Select>
          <span style={{ marginLeft: 16, color: '#666' }}>
            Tổng: <strong>{filteredPLOs?.length || 0}</strong> PLO
            {subjectFilter && (
              <> (của môn <strong>{subjects?.find(s => s.id === subjectFilter)?.code}</strong>)</>
            )}
          </span>
        </div>

        <Table
          columns={ploColumns}
          dataSource={filteredPLOs || []}
          rowKey="id"
          loading={isLoadingPlos}
          scroll={{ x: 900 }}
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} PLO`,
          }}
        />
      </Card>

      {/* Modal thêm/sửa PLO */}
      <Modal
        title={editingPLO ? 'Chỉnh sửa PLO' : 'Thêm PLO mới'}
        open={isModalOpen}
        onOk={() => form.submit()}
        onCancel={() => {
          setIsModalOpen(false);
          setEditingPLO(null);
          form.resetFields();
        }}
        confirmLoading={createPLOMutation.isPending || updatePLOMutation.isPending}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => {
            if (editingPLO) {
              updatePLOMutation.mutate({ id: editingPLO.id, values });
            } else {
              createPLOMutation.mutate(values);
            }
          }}
        >
          <Form.Item
            label="Môn học"
            name="subjectId"
            rules={[{ required: true, message: 'Vui lòng chọn môn học' }]}
          >
            <Select placeholder="Chọn môn học" loading={isLoadingSubjects}>
              {subjects?.map((s) => (
                <Option key={s.id} value={s.id}>
                  {s.code} - {s.currentNameVi}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="Mã PLO"
            name="code"
            rules={[{ required: true, message: 'Vui lòng nhập mã PLO' }]}
          >
            <Input placeholder="Ví dụ: PLO1" />
          </Form.Item>

          <Form.Item
            label="Mô tả"
            name="description"
            rules={[{ required: true, message: 'Vui lòng nhập mô tả' }]}
          >
            <TextArea rows={4} placeholder="Mô tả chuẩn đầu ra..." />
          </Form.Item>

          <Form.Item
            label="Danh mục"
            name="category"
            rules={[{ required: true, message: 'Vui lòng chọn danh mục' }]}
          >
            <Select placeholder="Chọn danh mục">
              <Option value="KNOWLEDGE">Kiến thức</Option>
              <Option value="SKILLS">Kỹ năng</Option>
              <Option value="COMPETENCE">Năng lực</Option>
              <Option value="ATTITUDE">Thái độ</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
