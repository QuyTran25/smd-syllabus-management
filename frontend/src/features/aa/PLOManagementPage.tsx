import React, { useState, useMemo } from 'react';
import { Card, Table, Space, Select, Tag, Alert, Button, Modal, Form, Input, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import { ploService, PLO } from '../../services/plo.service';

const { Option } = Select;
const { TextArea } = Input;

// Interface for display (mapped from API)
interface PLODisplay {
  id: string;
  code: string;
  description: string;
  category: 'Knowledge' | 'Skills' | 'Competence' | 'Attitude';
  curriculumId: string;
  curriculumCode: string;
  curriculumName: string;
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
  const [curriculumFilter, setCurriculumFilter] = useState<string | undefined>(undefined);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPLO, setEditingPLO] = useState<PLODisplay | null>(null);
  const [form] = Form.useForm();
  const queryClient = useQueryClient();

  // Fetch PLOs from API
  const { data: plosRaw, isLoading, error } = useQuery({
    queryKey: ['plos'],
    queryFn: () => ploService.getAllPLOs(),
  });

  // Create PLO mutation
  const createPLOMutation = useMutation({
    mutationFn: (values: any) => ploService.createPLO(values),
    onSuccess: () => {
      message.success('Thêm PLO thành công');
      queryClient.invalidateQueries({ queryKey: ['plos'] });
      setIsModalOpen(false);
      form.resetFields();
    },
    onError: () => {
      message.error('Thêm PLO thất bại');
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
    onError: () => {
      message.error('Cập nhật PLO thất bại');
    },
  });

  // Delete PLO mutation
  const deletePLOMutation = useMutation({
    mutationFn: (id: string) => ploService.deletePLO(id),
    onSuccess: () => {
      message.success('Xóa PLO thành công');
      queryClient.invalidateQueries({ queryKey: ['plos'] });
    },
    onError: () => {
      message.error('Xóa PLO thất bại');
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
      curriculumId: p.curriculumId,
      curriculumCode: p.curriculumCode,
      curriculumName: p.curriculumName,
    }));
  }, [plosRaw]);

  // Extract unique curriculums for filter
  const curriculums = useMemo(() => {
    const unique = new Map<string, { id: string; code: string; name: string }>();
    plos.forEach((p) => {
      if (!unique.has(p.curriculumId)) {
        unique.set(p.curriculumId, {
          id: p.curriculumId,
          code: p.curriculumCode,
          name: p.curriculumName,
        });
      }
    });
    return Array.from(unique.values());
  }, [plos]);

  const ploColumns: ColumnsType<PLODisplay> = [
    {
      title: 'Chương trình đào tạo',
      key: 'curriculum',
      width: 250,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span style={{ fontWeight: 500 }}>{record.curriculumCode}</span>
          <span style={{ fontSize: '12px', color: '#666' }}>{record.curriculumName}</span>
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
                curriculumId: record.curriculumId,
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

  // Filter PLOs by curriculum
  const filteredPLOs = curriculumFilter
    ? plos?.filter((p) => p.curriculumId === curriculumFilter)
    : plos;

  if (error) {
    return (
      <div style={{ padding: 24 }}>
        <Alert
          type="error"
          message="Lỗi tải dữ liệu"
          description="Không thể tải danh sách PLO. Vui lòng thử lại sau."
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
            placeholder="Lọc theo chương trình đào tạo"
            style={{ width: 350 }}
            allowClear
            value={curriculumFilter}
            onChange={(value) => setCurriculumFilter(value)}
          >
            {curriculums.map((c) => (
              <Option key={c.id} value={c.id}>
                {c.code} - {c.name}
              </Option>
            ))}
          </Select>
          <span style={{ marginLeft: 16, color: '#666' }}>
            Tổng: <strong>{filteredPLOs?.length || 0}</strong> PLO
            {curriculumFilter && (
              <> (của CTĐT <strong>{curriculums.find(c => c.id === curriculumFilter)?.code}</strong>)</>
            )}
          </span>
        </div>

        <Table
          columns={ploColumns}
          dataSource={filteredPLOs || []}
          rowKey="id"
          loading={isLoading}
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
            name="curriculumId"
            rules={[{ required: true, message: 'Vui lòng chọn chương trình đào tạo' }]}
          >
            <Select placeholder="Chọn chương trình đào tạo">
              {curriculums.map((c) => (
                <Option key={c.id} value={c.id}>
                  {c.code} - {c.name}
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
