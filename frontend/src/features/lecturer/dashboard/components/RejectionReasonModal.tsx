import React from 'react';
import { Modal, Button, Typography, Space } from 'antd';
import { ExclamationCircleOutlined, EditOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { Title, Text, Paragraph } = Typography;

interface RejectionReasonModalProps {
  open: boolean;
  onClose: () => void;
  syllabusId: number;
  syllabusCode: string;
  syllabusName: string;
  rejectionReason: string;
  rejectionType: 'HOD' | 'AA' | 'PRINCIPAL';
}

const RejectionReasonModal: React.FC<RejectionReasonModalProps> = ({
  open,
  onClose,
  syllabusId,
  syllabusCode,
  syllabusName,
  rejectionReason,
  rejectionType,
}) => {
  const navigate = useNavigate();

  const rejectionTypeLabel: Record<string, string> = {
    HOD: 'Trưởng Bộ môn',
    AA: 'Phòng Đào tạo',
    PRINCIPAL: 'Hiệu trưởng',
  };

  const handleEdit = () => {
    onClose();
    navigate(`/syllabi/edit/${syllabusId}`);
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={[
        <Button key="close" onClick={onClose}>
          Đóng
        </Button>,
        <Button key="edit" type="primary" icon={<EditOutlined />} onClick={handleEdit}>
          Đi tới sửa ngay
        </Button>,
      ]}
      width={600}
    >
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Space align="start">
          <ExclamationCircleOutlined style={{ fontSize: 24, color: '#ff4d4f' }} />
          <div>
            <Title level={4} style={{ margin: 0 }}>
              Đề cương bị từ chối
            </Title>
            <Text type="secondary">
              {syllabusCode} - {syllabusName}
            </Text>
          </div>
        </Space>

        <div>
          <Text strong>Người từ chối: </Text>
          <Text>{rejectionTypeLabel[rejectionType]}</Text>
        </div>

        <div>
          <Text strong>Lý do từ chối:</Text>
          <Paragraph
            style={{
              marginTop: 8,
              padding: 12,
              background: '#fff2e8',
              border: '1px solid #ffbb96',
              borderRadius: 4,
              whiteSpace: 'pre-wrap',
            }}
          >
            {rejectionReason}
          </Paragraph>
        </div>
      </Space>
    </Modal>
  );
};

export default RejectionReasonModal;
