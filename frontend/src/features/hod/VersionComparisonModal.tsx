import React from 'react';
import { Modal, Spin, Alert, Typography, Tag, Space, Empty } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { aiService, type VersionDiff } from '@/services/ai.service';
import { PlusOutlined, MinusOutlined, EditOutlined } from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

interface VersionComparisonModalProps {
  visible: boolean;
  onClose: () => void;
  syllabusId: string;
  oldVersionId: string;
}

export const VersionComparisonModal: React.FC<VersionComparisonModalProps> = ({
  visible,
  onClose,
  syllabusId,
  oldVersionId,
}) => {
  const { data: comparison, isLoading } = useQuery({
    queryKey: ['ai-version-comparison', syllabusId, oldVersionId],
    queryFn: () => aiService.compareVersions(syllabusId, oldVersionId),
    enabled: visible,
  });

  const getDiffIcon = (type: VersionDiff['type']) => {
    switch (type) {
      case 'added':
        return <PlusOutlined style={{ color: '#52c41a' }} />;
      case 'removed':
        return <MinusOutlined style={{ color: '#ff4d4f' }} />;
      case 'modified':
        return <EditOutlined style={{ color: '#1890ff' }} />;
    }
  };

  const getDiffColor = (type: VersionDiff['type']) => {
    switch (type) {
      case 'added':
        return '#f6ffed';
      case 'removed':
        return '#fff2f0';
      case 'modified':
        return '#e6f7ff';
    }
  };

  const getImpactTag = (level: string) => {
    const config = {
      low: { color: 'green', text: 'Thay đổi nhỏ' },
      medium: { color: 'orange', text: 'Thay đổi vừa' },
      high: { color: 'red', text: 'Thay đổi lớn' },
    };
    const { color, text } = config[level as keyof typeof config];
    return <Tag color={color}>{text}</Tag>;
  };

  return (
    <Modal
      title="So sánh Phiên bản (AI Analysis)"
      open={visible}
      onCancel={onClose}
      width={800}
      footer={null}
    >
      {isLoading ? (
        <div style={{ textAlign: 'center', padding: 40 }}>
          <Spin size="large" />
          <p style={{ marginTop: 16, color: '#999' }}>AI đang phân tích sự khác biệt...</p>
        </div>
      ) : comparison ? (
        <>
          <Alert
            message={
              <Space>
                <Text strong>Tóm tắt thay đổi:</Text>
                {getImpactTag(comparison.impactLevel)}
              </Space>
            }
            description={comparison.summary}
            type="info"
            showIcon
            style={{ marginBottom: 24 }}
          />

          <Title level={5}>Chi tiết thay đổi ({comparison.changes.length})</Title>

          {comparison.changes.map((change, index) => (
            <div
              key={index}
              style={{
                padding: 12,
                marginBottom: 12,
                backgroundColor: getDiffColor(change.type),
                border: '1px solid #d9d9d9',
                borderRadius: 4,
              }}
            >
              <Space style={{ marginBottom: 8 }}>
                {getDiffIcon(change.type)}
                <Text strong>{change.section}</Text>
                <Tag>{change.type === 'added' ? 'Thêm mới' : change.type === 'removed' ? 'Xóa' : 'Chỉnh sửa'}</Tag>
              </Space>

              {change.oldContent && (
                <div style={{ marginTop: 8 }}>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    Trước:
                  </Text>
                  <Paragraph
                    style={{
                      marginTop: 4,
                      padding: 8,
                      backgroundColor: '#fff',
                      borderLeft: '3px solid #ff4d4f',
                    }}
                  >
                    {change.oldContent}
                  </Paragraph>
                </div>
              )}

              {change.newContent && (
                <div style={{ marginTop: 8 }}>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    Sau:
                  </Text>
                  <Paragraph
                    style={{
                      marginTop: 4,
                      padding: 8,
                      backgroundColor: '#fff',
                      borderLeft: '3px solid #52c41a',
                    }}
                  >
                    {change.newContent}
                  </Paragraph>
                </div>
              )}
            </div>
          ))}
        </>
      ) : (
        <Empty description="Không có dữ liệu so sánh" />
      )}
    </Modal>
  );
};
