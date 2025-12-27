import React, { useState, useMemo } from 'react';
import { Card, Table, Space, Select, Tag, Alert, Spin } from 'antd';
import { useQuery } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import { ploService, PLO } from '../../services/plo.service';

const { Option } = Select;

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

  // Fetch PLOs from API
  const { data: plosRaw, isLoading, error } = useQuery({
    queryKey: ['plos'],
    queryFn: () => ploService.getAllPLOs(),
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
        <Tag color="blue">Chế độ xem - Read Only</Tag>
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
    </div>
  );
};
