import React, { useState } from 'react';
import { Card, Table, Button, Space, message, Popconfirm } from 'antd';
import { CheckOutlined, FileTextOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { syllabusService } from '@/services';
import { SyllabusStatus, type Syllabus } from '@/types';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';

export const BatchApprovalPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  // Fetch pending syllabi
  const { data, isLoading } = useQuery({
    queryKey: ['syllabi', { status: [SyllabusStatus.PENDING_PRINCIPAL] }],
    queryFn: () =>
      syllabusService.getSyllabi({ status: [SyllabusStatus.PENDING_PRINCIPAL] }, { page: 1, pageSize: 100 }),
  });

  // Batch approve mutation
  const batchApproveMutation = useMutation({
    mutationFn: async (ids: string[]) => {
      // Approve each syllabus sequentially
      for (const id of ids) {
        await syllabusService.approveSyllabus({
          syllabusId: id,
          action: 'APPROVE',
        });
      }
    },
    onSuccess: () => {
      message.success(`Phê duyệt thành công ${selectedRowKeys.length} đề cương`);
      setSelectedRowKeys([]);
      queryClient.invalidateQueries({ queryKey: ['syllabi'] });
    },
    onError: () => {
      message.error('Phê duyệt thất bại');
    },
  });

  const columns: ColumnsType<Syllabus> = [
    {
      title: 'Mã môn',
      dataIndex: 'subjectCode',
      key: 'subjectCode',
      width: 100,
    },
    {
      title: 'Tên môn học',
      dataIndex: 'subjectNameVi',
      key: 'subjectNameVi',
      render: (text, record) => (
        <a onClick={() => navigate(`/syllabi/${record.id}`)}>{text}</a>
      ),
    },
    {
      title: 'Tín chỉ',
      dataIndex: 'creditCount',
      key: 'creditCount',
      width: 70,
      align: 'center',
    },
    {
      title: 'Loại',
      dataIndex: 'courseType',
      key: 'courseType',
      width: 100,
      render: (type: string) => {
        const typeMap = { required: 'BB', elective: 'TC', free: 'TCTD' };
        return typeMap[type as keyof typeof typeMap] || '-';
      },
    },
    {
      title: 'Khoa',
      dataIndex: 'faculty',
      key: 'faculty',
      width: 150,
    },
    {
      title: 'Bộ môn',
      dataIndex: 'department',
      key: 'department',
      width: 150,
    },
    {
      title: 'Giảng viên',
      dataIndex: 'ownerName',
      key: 'ownerName',
      width: 150,
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semester',
      key: 'semester',
      width: 120,
    },
    {
      title: 'Ngày nộp',
      dataIndex: 'submittedAt',
      key: 'submittedAt',
      width: 120,
      render: (date) => (date ? new Date(date).toLocaleDateString('vi-VN') : '-'),
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 100,
      render: (_, record) => (
        <Button
          type="link"
          icon={<FileTextOutlined />}
          onClick={() => navigate(`/syllabi/${record.id}`)}
          style={{ fontSize: '12px' }}
        >
          Xem chi tiết
        </Button>
      ),
    },
  ];

  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: React.Key[]) => {
      setSelectedRowKeys(newSelectedRowKeys as string[]);
    },
  };

  return (
    <div>
      <h2>Phê duyệt Hàng loạt (Batch Approval)</h2>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Popconfirm
            title="Phê duyệt hàng loạt"
            description={`Bạn có chắc muốn phê duyệt ${selectedRowKeys.length} đề cương đã chọn?`}
            onConfirm={() => batchApproveMutation.mutate(selectedRowKeys)}
            okText="Xác nhận"
            cancelText="Hủy"
            disabled={selectedRowKeys.length === 0}
          >
            <Button
              type="primary"
              icon={<CheckOutlined />}
              disabled={selectedRowKeys.length === 0}
              loading={batchApproveMutation.isPending}
            >
              Phê duyệt {selectedRowKeys.length > 0 && `(${selectedRowKeys.length})`}
            </Button>
          </Popconfirm>
          <span style={{ color: '#999' }}>
            {data?.data.length || 0} đề cương đang chờ phê duyệt
          </span>
        </Space>

        <Table
          rowSelection={rowSelection}
          columns={columns}
          dataSource={data?.data || []}
          rowKey="id"
          loading={isLoading}
          scroll={{ x: 1000 }}
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} đề cương`,
          }}
        />
      </Card>
    </div>
  );
};
