import React from 'react';
import { Modal, Alert, Card, Typography, Space } from 'antd';

const { Text } = Typography;

type Props = {
  open: boolean;
  onClose: () => void;
  summary: {
    overview: string;
    highlights: string[];
    recommendations: string[];
  };
};

export const AISummaryModal: React.FC<Props> = ({ open, onClose, summary }) => {
  return (
    <Modal
      open={open}
      onCancel={onClose}
      onOk={onClose}
      okText="Đóng"
      cancelButtonProps={{ style: { display: 'none' } }}
      title="Tóm tắt AI - Phân tích Đề cương"
      width={760}
    >
      <Space direction="vertical" style={{ width: '100%' }} size={12}>
        <Alert
          type="info"
          showIcon
          message="Tóm tắt được tạo bởi AI"
          description="Nội dung này được phân tích tự động, chỉ mang tính chất tham khảo."
        />

        <Card size="small" title="Tổng quan">
          <Text>{summary.overview}</Text>
        </Card>

        <Card size="small" title="Điểm nổi bật">
          <ul style={{ margin: 0, paddingLeft: 18 }}>
            {summary.highlights.map((x, i) => (
              <li key={i}>{x}</li>
            ))}
          </ul>
        </Card>

        <Card size="small" title="Khuyến nghị">
          <ul style={{ margin: 0, paddingLeft: 18 }}>
            {summary.recommendations.map((x, i) => (
              <li key={i}>{x}</li>
            ))}
          </ul>
        </Card>
      </Space>
    </Modal>
  );
};
