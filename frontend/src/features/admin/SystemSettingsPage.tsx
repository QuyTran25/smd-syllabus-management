import React from 'react';
import { Card, Tabs, Form, Input, Button, Table, Space, message, InputNumber, Switch, Tag, DatePicker } from 'antd';
import { PlusOutlined, DeleteOutlined, SaveOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { semesterService } from '@/services';
import { Semester } from '@/types';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { TextArea } = Input;
const { RangePicker } = DatePicker;

export const SystemSettingsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [semesterForm] = Form.useForm();

  // Fetch semesters using React Query
  const { data: semesters, isLoading: loadingSemesters } = useQuery({
    queryKey: ['semesters'],
    queryFn: () => semesterService.getSemesters(),
  });

  // Create semester mutation
  const createSemesterMutation = useMutation({
    mutationFn: (data: Omit<Semester, 'id' | 'createdAt' | 'updatedAt'>) =>
      semesterService.createSemester(data),
    onSuccess: () => {
      message.success('Tạo học kỳ thành công');
      queryClient.invalidateQueries({ queryKey: ['semesters'] });
      semesterForm.resetFields();
    },
    onError: () => {
      message.error('Tạo học kỳ thất bại');
    },
  });

  // Delete semester mutation
  const deleteSemesterMutation = useMutation({
    mutationFn: (id: string) => semesterService.deleteSemester(id),
    onSuccess: () => {
      message.success('Xóa học kỳ thành công');
      queryClient.invalidateQueries({ queryKey: ['semesters'] });
    },
    onError: (error: Error) => {
      message.error(error.message || 'Xóa học kỳ thất bại');
    },
  });

  // Set active semester mutation
  const setActiveMutation = useMutation({
    mutationFn: (id: string) => semesterService.setActiveSemester(id),
    onSuccess: () => {
      message.success('Đã kích hoạt học kỳ');
      queryClient.invalidateQueries({ queryKey: ['semesters'] });
    },
  });

  const handleCreateSemester = (values: any) => {
    const [startDate, endDate] = values.dateRange;
    
    // Extract academic year from dates
    const startYear = startDate.year();
    const endYear = endDate.year();
    const academicYear = startYear !== endYear ? `${startYear}-${endYear}` : `${startYear}`;

    createSemesterMutation.mutate({
      code: values.code,
      name: values.name,
      startDate: startDate.format('YYYY-MM-DD'),
      endDate: endDate.format('YYYY-MM-DD'),
      academicYear,
      isActive: false,
      createdBy: 'admin-1', // TODO: Get from auth context
    });
  };

  const semesterColumns: ColumnsType<Semester> = [
    { 
      title: 'Mã', 
      dataIndex: 'code', 
      key: 'code', 
      width: 100,
      render: (text, record) => (
        <Space size="small">
          {text}
          {record.isActive && <Tag color="green" icon={<CheckCircleOutlined />}>Hoạt động</Tag>}
        </Space>
      ),
    },
    { 
      title: 'Tên học kỳ', 
      dataIndex: 'name', 
      key: 'name',
      ellipsis: { showTitle: false },
    },
    { 
      title: 'Năm học', 
      dataIndex: 'academicYear', 
      key: 'academicYear', 
      width: 90 
    },
    { 
      title: 'Bắt đầu', 
      dataIndex: 'startDate', 
      key: 'startDate', 
      width: 90,
      render: (date) => dayjs(date).format('DD/MM/YY'),
    },
    { 
      title: 'Kết thúc', 
      dataIndex: 'endDate', 
      key: 'endDate', 
      width: 90,
      render: (date) => dayjs(date).format('DD/MM/YY'),
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 140,
      render: (_, record) => (
        <Space>
          {!record.isActive && (
            <Button
              type="link"
              size="small"
              onClick={() => setActiveMutation.mutate(record.id)}
            >
              Kích hoạt
            </Button>
          )}
          <Button
            type="text"
            danger
            size="small"
            icon={<DeleteOutlined />}
            onClick={() => deleteSemesterMutation.mutate(record.id)}
            disabled={record.isActive}
          />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2>Cấu hình Hệ thống</h2>

      <Tabs
        items={[
          {
            key: 'semester',
            label: 'Học kỳ',
            children: (
              <Card>
                <Form
                  form={semesterForm}
                  layout="vertical"
                  onFinish={handleCreateSemester}
                  style={{ marginBottom: 16 }}
                >
                  <Space size="large" align="start" style={{ width: '100%' }}>
                    <Form.Item 
                      name="code" 
                      label="Mã học kỳ"
                      rules={[{ required: true, message: 'Nhập mã học kỳ' }]}
                      style={{ marginBottom: 0 }}
                    >
                      <Input placeholder="HK1-2025" style={{ width: 150 }} />
                    </Form.Item>
                    <Form.Item 
                      name="name" 
                      label="Tên học kỳ"
                      rules={[{ required: true, message: 'Nhập tên học kỳ' }]}
                      style={{ marginBottom: 0 }}
                    >
                      <Input placeholder="Học kỳ 1 năm 2025-2026" style={{ width: 250 }} />
                    </Form.Item>
                    <Form.Item 
                      name="dateRange" 
                      label="Thời gian"
                      rules={[{ required: true, message: 'Chọn thời gian' }]}
                      style={{ marginBottom: 0 }}
                    >
                      <RangePicker format="DD/MM/YYYY" />
                    </Form.Item>
                    <Form.Item label=" " style={{ marginBottom: 0 }}>
                      <Button 
                        type="primary" 
                        htmlType="submit" 
                        icon={<PlusOutlined />}
                        loading={createSemesterMutation.isPending}
                      >
                        Thêm
                      </Button>
                    </Form.Item>
                  </Space>
                </Form>

                <Table 
                  columns={semesterColumns} 
                  dataSource={semesters || []} 
                  rowKey="id" 
                  pagination={false}
                  loading={loadingSemesters}
                />
              </Card>
            ),
          },
          {
            key: 'workflow',
            label: 'Quy tắc Nghiệp vụ',
            children: (
              <Card>
                <Form layout="vertical">
                  <Form.Item label="Thời gian phê duyệt tối đa (ngày)">
                    <Space>
                      <InputNumber min={1} defaultValue={3} addonBefore="Trưởng BM" />
                      <InputNumber min={1} defaultValue={5} addonBefore="Phòng ĐT" />
                      <InputNumber min={1} defaultValue={7} addonBefore="Hiệu trưởng" />
                    </Space>
                  </Form.Item>
                  <Form.Item label="Tự động từ chối nếu quá hạn">
                    <Switch defaultChecked />
                  </Form.Item>
                  <Form.Item label="Yêu cầu số lượng CLO tối thiểu">
                    <InputNumber min={1} max={10} defaultValue={4} />
                  </Form.Item>
                  <Form.Item label="Yêu cầu ánh xạ PLO">
                    <Switch defaultChecked />
                  </Form.Item>
                  <Form.Item label="Cho phép Giảng viên tự xuất bản">
                    <Switch />
                  </Form.Item>
                  <Button type="primary" icon={<SaveOutlined />}>Lưu quy tắc</Button>
                </Form>
              </Card>
            ),
          },
          {
            key: 'ui',
            label: 'Giao diện',
            children: (
              <Card>
                <Form layout="vertical">
                  <Form.Item label="Màu chủ đạo">
                    <Input type="color" defaultValue="#018486" style={{ width: 100 }} />
                  </Form.Item>
                  <Form.Item label="Logo hệ thống (URL)">
                    <Input placeholder="https://example.com/logo.png" />
                  </Form.Item>
                  <Form.Item label="Tiêu đề trang chủ">
                    <Input defaultValue="SMD - Hệ thống Quản lý và Số hóa Giáo trình" />
                  </Form.Item>
                  <Form.Item label="Nội dung Footer">
                    <TextArea rows={3} defaultValue="© 2024 SMD. All rights reserved." />
                  </Form.Item>
                  <Button type="primary" icon={<SaveOutlined />}>Lưu cấu hình</Button>
                </Form>
              </Card>
            ),
          },
        ]}
      />
    </div>
  );
};
