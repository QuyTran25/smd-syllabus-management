import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Input, Space, Table, Typography, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import axiosClient from '../../api/axiosClient'; // Đảm bảo đường dẫn import đúng

const { Title, Text } = Typography;

// 1. Định nghĩa kiểu dữ liệu khớp với Backend trả về
interface Subject {
  id: string;
  code: string;
  currentNameVi: string; // Tên môn học trong DB là currentNameVi
  defaultCredits: number; // Số tín chỉ là defaultCredits
  departmentId: string;   // Dùng để debug liên kết khoa
  isActive: boolean;
}

export default function AdminSubjectsPage() {
  // State lưu dữ liệu thật từ API
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');

  // 2. Hàm gọi API lấy danh sách môn học
  const fetchSubjects = async () => {
    setLoading(true);
    try {
      const response = await axiosClient.get('/subjects');
      // Xử lý dữ liệu trả về (mảng hoặc object bọc data)
      const data = Array.isArray(response.data) ? response.data : (response.data?.data || []);
      setSubjects(data);
    } catch (error) {
      console.error("Lỗi tải môn học:", error);
      message.error('Không thể tải danh sách môn học.');
    } finally {
      setLoading(false);
    }
  };

  // Gọi API khi trang vừa load
  useEffect(() => {
    fetchSubjects();
  }, []);

  // 3. Logic tìm kiếm (Client-side filtering)
  const filteredData = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return subjects;
    return subjects.filter(
      (s) =>
        (s.code?.toLowerCase().includes(q)) || 
        (s.currentNameVi?.toLowerCase().includes(q))
    );
  }, [search, subjects]);

  // 4. Cấu hình cột hiển thị
  const columns: ColumnsType<Subject> = [
    { 
      title: 'Mã môn', 
      dataIndex: 'code', 
      key: 'code', 
      width: 120,
      render: (text) => <strong style={{ color: '#1890ff' }}>{text}</strong>
    },
    { 
      title: 'Tên môn học', 
      dataIndex: 'currentNameVi', 
      key: 'currentNameVi' 
    },
    { 
      title: 'Tín chỉ', 
      dataIndex: 'defaultCredits', 
      key: 'defaultCredits', 
      width: 100, 
      align: 'center' 
    },
    {
      title: 'ID Khoa (Debug)', // Cột này quan trọng để bạn check lỗi Backend
      dataIndex: 'departmentId',
      key: 'departmentId',
      width: 200,
      render: (id) => <Text type="secondary" style={{ fontSize: 12 }}>{id}</Text>
    },
    {
      title: 'Trạng thái',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 120,
      align: 'center',
      render: (isActive) => (
        <Tag color={isActive ? 'success' : 'error'}>
          {isActive ? 'Hoạt động' : 'Đã khóa'}
        </Tag>
      )
    }
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {/* Header Trang */}
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
        <div>
          <Title level={2} style={{ margin: 0 }}>Quản lý Môn học</Title>
          <Text type="secondary">Danh sách môn học từ Database ({subjects.length} bản ghi)</Text>
        </div>
        <Space>
            <Button icon={<ReloadOutlined />} onClick={fetchSubjects}>Làm mới</Button>
            <Button type="primary" icon={<PlusOutlined />}>
            Tạo môn học
            </Button>
        </Space>
      </div>

      {/* Bộ lọc và Bảng */}
      <Card>
        <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
          <Input
            style={{ width: 320, maxWidth: '100%' }}
            placeholder="Tìm theo mã hoặc tên môn..."
            prefix={<SearchOutlined />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            allowClear
          />
        </Space>

        <Table
          rowKey="id"
          columns={columns}
          dataSource={filteredData} // Dùng dữ liệu đã lọc
          loading={loading} // Hiệu ứng xoay khi đang tải
          pagination={{ pageSize: 10, showSizeChanger: true }}
          scroll={{ x: 800 }}
        />
      </Card>
    </Space>
  );
}