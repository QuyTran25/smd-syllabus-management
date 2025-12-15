import React, { useState } from 'react';
import { Modal, Select, Row, Col, Descriptions, Tag, Divider, Empty, Table } from 'antd';
import { DiffOutlined } from '@ant-design/icons';

const { Option } = Select;

interface VersionComparisonModalProps {
  visible: boolean;
  onClose: () => void;
  syllabusId: string;
  currentVersion?: string;
}

interface VersionData {
  version: string;
  date: string;
  author: string;
  status: string;
  subjectCode: string;
  subjectName: string;
  credits: number;
  description: string;
  objectives: string;
  clos: Array<{
    code: string;
    description: string;
    bloomLevel: string;
    weight: number;
  }>;
  prerequisites: string[];
}

// Mock version data
const mockVersions: Record<string, VersionData[]> = {
  '1': [
    {
      version: 'v3.0',
      date: '2024-12-08',
      author: 'GV. Nguyễn Văn A',
      status: 'DRAFT',
      subjectCode: 'CS301',
      subjectName: 'Cơ sở dữ liệu',
      credits: 3,
      description: 'Môn học về thiết kế và quản lý cơ sở dữ liệu quan hệ. Bổ sung thêm phần NoSQL.',
      objectives: 'Sinh viên nắm vững lý thuyết và thực hành CSDL, bao gồm cả NoSQL.',
      clos: [
        { code: 'CLO1', description: 'Hiểu các khái niệm CSDL quan hệ và NoSQL', bloomLevel: 'Hiểu', weight: 25 },
        { code: 'CLO2', description: 'Thiết kế CSDL chuẩn hóa', bloomLevel: 'Áp dụng', weight: 30 },
        { code: 'CLO3', description: 'Viết truy vấn SQL phức tạp', bloomLevel: 'Áp dụng', weight: 25 },
        { code: 'CLO4', description: 'Tối ưu hóa hiệu năng CSDL', bloomLevel: 'Phân tích', weight: 20 },
      ],
      prerequisites: ['CS201 - Cấu trúc dữ liệu'],
    },
    {
      version: 'v2.0',
      date: '2024-09-15',
      author: 'GV. Nguyễn Văn A',
      status: 'PUBLISHED',
      subjectCode: 'CS301',
      subjectName: 'Cơ sở dữ liệu',
      credits: 3,
      description: 'Môn học về thiết kế và quản lý cơ sở dữ liệu quan hệ.',
      objectives: 'Sinh viên nắm vững lý thuyết và thực hành CSDL.',
      clos: [
        { code: 'CLO1', description: 'Hiểu các khái niệm CSDL quan hệ', bloomLevel: 'Hiểu', weight: 30 },
        { code: 'CLO2', description: 'Thiết kế CSDL chuẩn hóa', bloomLevel: 'Áp dụng', weight: 35 },
        { code: 'CLO3', description: 'Viết truy vấn SQL', bloomLevel: 'Áp dụng', weight: 35 },
      ],
      prerequisites: ['CS201 - Cấu trúc dữ liệu'],
    },
    {
      version: 'v1.0',
      date: '2023-08-10',
      author: 'GV. Trần Thị B',
      status: 'PUBLISHED',
      subjectCode: 'CS301',
      subjectName: 'Cơ sở dữ liệu',
      credits: 3,
      description: 'Môn học cơ bản về cơ sở dữ liệu.',
      objectives: 'Sinh viên hiểu cơ bản về CSDL.',
      clos: [
        { code: 'CLO1', description: 'Hiểu CSDL', bloomLevel: 'Hiểu', weight: 50 },
        { code: 'CLO2', description: 'Viết SQL đơn giản', bloomLevel: 'Áp dụng', weight: 50 },
      ],
      prerequisites: [],
    },
  ],
};

const VersionComparisonModal: React.FC<VersionComparisonModalProps> = ({
  visible,
  onClose,
  syllabusId,
  currentVersion,
}) => {
  const versions = mockVersions[syllabusId] || [];
  const [leftVersion, setLeftVersion] = useState<string>(currentVersion || versions[0]?.version || '');
  const [rightVersion, setRightVersion] = useState<string>(versions[1]?.version || '');

  const leftData = versions.find((v) => v.version === leftVersion);
  const rightData = versions.find((v) => v.version === rightVersion);

  const getDiffColor = (left: any, right: any) => {
    if (JSON.stringify(left) !== JSON.stringify(right)) {
      return '#fff7e6'; // Light yellow for differences
    }
    return 'transparent';
  };

  return (
    <Modal
      title={
        <span>
          <DiffOutlined /> So sánh phiên bản đề cương
        </span>
      }
      open={visible}
      onCancel={onClose}
      width={1200}
      footer={null}
    >
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={12}>
          <Select
            value={leftVersion}
            onChange={setLeftVersion}
            style={{ width: '100%' }}
            placeholder="Chọn phiên bản"
          >
            {versions.map((v) => (
              <Option key={v.version} value={v.version}>
                {v.version} - {v.date} - <Tag color="blue">{v.status}</Tag>
              </Option>
            ))}
          </Select>
        </Col>
        <Col span={12}>
          <Select
            value={rightVersion}
            onChange={setRightVersion}
            style={{ width: '100%' }}
            placeholder="Chọn phiên bản để so sánh"
          >
            {versions.map((v) => (
              <Option key={v.version} value={v.version} disabled={v.version === leftVersion}>
                {v.version} - {v.date} - <Tag color="blue">{v.status}</Tag>
              </Option>
            ))}
          </Select>
        </Col>
      </Row>

      {leftData && rightData ? (
        <>
          <Row gutter={16}>
            <Col span={12}>
              <Descriptions
                bordered
                size="small"
                column={1}
                labelStyle={{ width: 150, backgroundColor: getDiffColor(leftData.subjectCode, rightData.subjectCode) }}
              >
                <Descriptions.Item label="Mã học phần">{leftData.subjectCode}</Descriptions.Item>
                <Descriptions.Item
                  label="Tên học phần"
                  labelStyle={{ backgroundColor: getDiffColor(leftData.subjectName, rightData.subjectName) }}
                >
                  {leftData.subjectName}
                </Descriptions.Item>
                <Descriptions.Item
                  label="Số tín chỉ"
                  labelStyle={{ backgroundColor: getDiffColor(leftData.credits, rightData.credits) }}
                >
                  {leftData.credits}
                </Descriptions.Item>
                <Descriptions.Item
                  label="Mô tả"
                  labelStyle={{ backgroundColor: getDiffColor(leftData.description, rightData.description) }}
                >
                  {leftData.description}
                </Descriptions.Item>
                <Descriptions.Item
                  label="Mục tiêu"
                  labelStyle={{ backgroundColor: getDiffColor(leftData.objectives, rightData.objectives) }}
                >
                  {leftData.objectives}
                </Descriptions.Item>
              </Descriptions>
            </Col>
            <Col span={12}>
              <Descriptions
                bordered
                size="small"
                column={1}
                labelStyle={{ width: 150, backgroundColor: getDiffColor(rightData.subjectCode, leftData.subjectCode) }}
              >
                <Descriptions.Item label="Mã học phần">{rightData.subjectCode}</Descriptions.Item>
                <Descriptions.Item
                  label="Tên học phần"
                  labelStyle={{ backgroundColor: getDiffColor(rightData.subjectName, leftData.subjectName) }}
                >
                  {rightData.subjectName}
                </Descriptions.Item>
                <Descriptions.Item
                  label="Số tín chỉ"
                  labelStyle={{ backgroundColor: getDiffColor(rightData.credits, leftData.credits) }}
                >
                  {rightData.credits}
                </Descriptions.Item>
                <Descriptions.Item
                  label="Mô tả"
                  labelStyle={{ backgroundColor: getDiffColor(rightData.description, leftData.description) }}
                >
                  {rightData.description}
                </Descriptions.Item>
                <Descriptions.Item
                  label="Mục tiêu"
                  labelStyle={{ backgroundColor: getDiffColor(rightData.objectives, leftData.objectives) }}
                >
                  {rightData.objectives}
                </Descriptions.Item>
              </Descriptions>
            </Col>
          </Row>

          <Divider>Học phần Tiên quyết</Divider>
          <Row gutter={16}>
            <Col span={12}>
              {leftData.prerequisites.length > 0 ? (
                <ul>
                  {leftData.prerequisites.map((p, i) => (
                    <li key={i}>{p}</li>
                  ))}
                </ul>
              ) : (
                <Empty description="Không có" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Col>
            <Col span={12}>
              {rightData.prerequisites.length > 0 ? (
                <ul>
                  {rightData.prerequisites.map((p, i) => (
                    <li key={i}>{p}</li>
                  ))}
                </ul>
              ) : (
                <Empty description="Không có" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Col>
          </Row>

          <Divider>CLO Comparison</Divider>
          <Row gutter={16}>
            <Col span={12}>
              <Table
                dataSource={leftData.clos}
                rowKey="code"
                size="small"
                pagination={false}
                columns={[
                  { title: 'CLO', dataIndex: 'code', width: 70 },
                  { title: 'Mô tả', dataIndex: 'description' },
                  { title: 'Bloom', dataIndex: 'bloomLevel', width: 100 },
                  { title: 'Trọng số', dataIndex: 'weight', width: 80, render: (w) => `${w}%` },
                ]}
              />
            </Col>
            <Col span={12}>
              <Table
                dataSource={rightData.clos}
                rowKey="code"
                size="small"
                pagination={false}
                columns={[
                  { title: 'CLO', dataIndex: 'code', width: 70 },
                  { title: 'Mô tả', dataIndex: 'description' },
                  { title: 'Bloom', dataIndex: 'bloomLevel', width: 100 },
                  { title: 'Trọng số', dataIndex: 'weight', width: 80, render: (w) => `${w}%` },
                ]}
              />
            </Col>
          </Row>
        </>
      ) : (
        <Empty description="Vui lòng chọn 2 phiên bản để so sánh" />
      )}
    </Modal>
  );
};

export default VersionComparisonModal;
