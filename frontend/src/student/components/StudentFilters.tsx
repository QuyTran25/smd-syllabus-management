import React from 'react';
import { Card, Input, Row, Col, Select, Typography } from 'antd';
import type { StudentSyllabiFilters } from '../types';

const { Text } = Typography;

type Props = {
  value: StudentSyllabiFilters;
  faculties: string[];
  programs: string[];
  terms: string[];
  onChange: (next: StudentSyllabiFilters) => void;
};

export const StudentFilters: React.FC<Props> = ({
  value,
  faculties,
  programs,
  terms,
  onChange,
}) => {
  return (
    <Card
      size="small"
      style={{
        borderRadius: 10,
        boxShadow: '0 2px 10px rgba(0,0,0,0.04)',
      }}
      styles={{ body: { padding: 14 } }}
    >
      <div style={{ marginBottom: 10 }}>
        <Text strong>Tổng quan về đề cương</Text>
      </div>

      {/* Row 1: scope + search + sort */}
      <Row gutter={[10, 10]} align="middle">
        <Col xs={24} md={6}>
          <Select
            value={value.scope}
            style={{ width: '100%' }}
            options={[
              { value: 'ALL', label: 'Tất cả đề cương' },
              { value: 'TRACKED', label: 'Đề cương đang theo dõi' },
            ]}
            onChange={(scope) => onChange({ ...value, scope })}
          />
        </Col>

        <Col xs={24} md={12}>
          <Input
            value={value.q}
            allowClear
            placeholder="Tìm theo mã HP, tên học phần, hoặc giảng viên..."
            onChange={(e) => onChange({ ...value, q: e.target.value })}
          />
        </Col>

        <Col xs={24} md={6}>
          <Select
            value={value.sort}
            style={{ width: '100%' }}
            options={[
              { value: 'newest', label: 'Gần đây nhất' },
              { value: 'oldest', label: 'Cũ nhất' },
            ]}
            onChange={(sort) => onChange({ ...value, sort })}
          />
        </Col>
      </Row>

      {/* Row 2: faculty + program + term */}
      <Row gutter={[10, 10]} style={{ marginTop: 10 }}>
        <Col xs={24} md={8}>
          <Select
            allowClear
            placeholder="Lọc theo Khoa/Bộ môn"
            style={{ width: '100%' }}
            value={value.faculty}
            options={faculties.map((x) => ({ value: x, label: x }))}
            onChange={(faculty) => onChange({ ...value, faculty })}
          />
        </Col>

        <Col xs={24} md={8}>
          <Select
            allowClear
            placeholder="Lọc theo Chương trình"
            style={{ width: '100%' }}
            value={value.program}
            options={programs.map((x) => ({ value: x, label: x }))}
            onChange={(program) => onChange({ ...value, program })}
          />
        </Col>

        <Col xs={24} md={8}>
          <Select
            allowClear
            placeholder="Lọc theo Học kỳ"
            style={{ width: '100%' }}
            value={value.term}
            options={terms.map((x) => ({ value: x, label: x }))}
            onChange={(term) => onChange({ ...value, term })}
          />
        </Col>
      </Row>
    </Card>
  );
};
