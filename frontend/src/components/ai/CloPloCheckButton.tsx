import React, { useState } from 'react';
import { Button, Progress, Modal, Tag, Typography, Space, Alert, List } from 'antd';
import { CheckCircleOutlined, ExclamationCircleOutlined, LoadingOutlined } from '@ant-design/icons';
import { aiService, CloPloResult } from '../../services/aiService';
import { useTaskPolling } from '../../hooks/useTaskPolling';

const { Title, Text, Paragraph } = Typography;

interface CloPloCheckButtonProps {
  syllabusId: string;
  curriculumId: string;
  onComplete?: (result: CloPloResult) => void;
}

/**
 * Button để trigger AI check CLO-PLO mapping
 * Hiển thị progress trong modal, sau đó show kết quả
 */
export const CloPloCheckButton: React.FC<CloPloCheckButtonProps> = ({
  syllabusId,
  curriculumId,
  onComplete,
}) => {
  const [taskId, setTaskId] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isRequesting, setIsRequesting] = useState(false);

  const { status, progress, result, error, isPolling } = useTaskPolling({
    taskId,
    enabled: !!taskId,
    onSuccess: (data) => {
      onComplete?.(data as CloPloResult);
    },
    onError: (err) => {
      console.error('Task failed:', err);
    },
  });

  const handleCheckCloPlo = async () => {
    try {
      setIsRequesting(true);
      const newTaskId = await aiService.checkCloPloMapping(syllabusId, curriculumId);
      setTaskId(newTaskId);
      setIsModalOpen(true);
    } catch (err: any) {
      console.error('Failed to start CLO-PLO check:', err);
      Modal.error({
        title: 'Failed to Start Analysis',
        content: err.response?.data?.message || err.message || 'Unknown error',
      });
    } finally {
      setIsRequesting(false);
    }
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setTaskId(null);
  };

  const renderSeverityTag = (severity: 'HIGH' | 'MEDIUM' | 'LOW') => {
    const colors = { HIGH: 'red', MEDIUM: 'orange', LOW: 'blue' };
    return <Tag color={colors[severity]}>{severity}</Tag>;
  };

  const renderStatusTag = (status: CloPloResult['status']) => {
    const config = {
      COMPLIANT: { color: 'green', icon: <CheckCircleOutlined />, text: 'Compliant' },
      NEEDS_IMPROVEMENT: { color: 'orange', icon: <ExclamationCircleOutlined />, text: 'Needs Improvement' },
      NON_COMPLIANT: { color: 'red', icon: <ExclamationCircleOutlined />, text: 'Non-Compliant' },
    };
    const { color, icon, text } = config[status];
    return <Tag color={color} icon={icon}>{text}</Tag>;
  };

  const renderModalContent = () => {
    // Đang chờ AI xử lý
    if (isPolling && !result) {
      return (
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <div style={{ textAlign: 'center' }}>
            <LoadingOutlined style={{ fontSize: 48, color: '#1890ff' }} />
            <Title level={4} style={{ marginTop: 16 }}>
              Analyzing CLO-PLO Mapping...
            </Title>
            <Text type="secondary">
              {status === 'QUEUED' && 'Task queued, waiting for processing...'}
              {status === 'PROCESSING' && 'AI is analyzing your syllabus...'}
            </Text>
          </div>
          
          <Progress 
            percent={progress} 
            status="active"
            strokeColor={{
              '0%': '#108ee9',
              '100%': '#87d068',
            }}
          />
          
          <Alert
            message="This usually takes 5-10 seconds"
            type="info"
            showIcon
          />
        </Space>
      );
    }

    // Có lỗi
    if (error) {
      return (
        <Alert
          message="Analysis Failed"
          description={error}
          type="error"
          showIcon
        />
      );
    }

    // Có kết quả
    if (result && status === 'SUCCESS') {
      const cloPloResult = result as CloPloResult;
      
      return (
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          {/* Overall Status */}
          <div>
            <Title level={4}>Overall Status</Title>
            {renderStatusTag(cloPloResult.status)}
            <Paragraph style={{ marginTop: 8 }}>
              <Text strong>Score:</Text> {cloPloResult.score.toFixed(1)}% 
              <Text type="secondary"> ({cloPloResult.compliant_mappings}/{cloPloResult.total_mappings} mappings compliant)</Text>
            </Paragraph>
          </div>

          {/* Issues */}
          {cloPloResult.issues && cloPloResult.issues.length > 0 && (
            <div>
              <Title level={5}>Issues Found ({cloPloResult.issues.length})</Title>
              <List
                size="small"
                bordered
                dataSource={cloPloResult.issues}
                renderItem={(issue) => (
                  <List.Item>
                    <Space direction="vertical" style={{ width: '100%' }}>
                      <div>
                        {renderSeverityTag(issue.severity)}
                        <Tag>{issue.type}</Tag>
                        <Text strong>{issue.clo_code}</Text> → 
                        <Text type="secondary"> {issue.plo_codes.join(', ')}</Text>
                      </div>
                      <Text>{issue.message}</Text>
                    </Space>
                  </List.Item>
                )}
              />
            </div>
          )}

          {/* Suggestions */}
          {cloPloResult.suggestions && cloPloResult.suggestions.length > 0 && (
            <div>
              <Title level={5}>AI Suggestions ({cloPloResult.suggestions.length})</Title>
              <List
                size="small"
                bordered
                dataSource={cloPloResult.suggestions}
                renderItem={(suggestion) => (
                  <List.Item>
                    <Space direction="vertical" style={{ width: '100%' }}>
                      <Text strong>{suggestion.clo_code}</Text>
                      <div>
                        <Text type="secondary">Current: </Text>
                        {suggestion.current_plos.map(plo => <Tag key={plo}>{plo}</Tag>)}
                        <Text type="secondary"> → Suggested: </Text>
                        {suggestion.suggested_plos.map(plo => <Tag key={plo} color="blue">{plo}</Tag>)}
                      </div>
                      <Text italic>{suggestion.reason}</Text>
                    </Space>
                  </List.Item>
                )}
              />
            </div>
          )}
        </Space>
      );
    }

    return null;
  };

  return (
    <>
      <Button
        type="primary"
        icon={<CheckCircleOutlined />}
        onClick={handleCheckCloPlo}
        loading={isRequesting}
      >
        Check CLO-PLO Compliance
      </Button>

      <Modal
        title="CLO-PLO Mapping Analysis"
        open={isModalOpen}
        onCancel={handleCloseModal}
        footer={status === 'SUCCESS' || error ? [
          <Button key="close" onClick={handleCloseModal}>
            Close
          </Button>
        ] : null}
        width={800}
        destroyOnClose
      >
        {renderModalContent()}
      </Modal>
    </>
  );
};

export default CloPloCheckButton;
