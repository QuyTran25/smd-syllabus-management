import React, { useMemo } from 'react';
import { Card, Progress, Typography, message } from 'antd';
import { StarOutlined, StarFilled, MoreOutlined } from '@ant-design/icons';
import type { SyllabusListItem } from '../types';

const { Text, Title } = Typography;

type Props = {
  item: SyllabusListItem;
  onOpen: (id: string) => void;
  onToggleTrack: (id: string) => void;
};

export const SyllabusCard: React.FC<Props> = ({ item, onOpen, onToggleTrack }) => {
  // Gradient theo majorShort
  const headerBg = useMemo(() => {
    switch (item.majorShort) {
      case 'EE':
        return 'linear-gradient(90deg, rgba(255,153,102,0.95), rgba(255,214,102,0.95))';
      case 'CS':
      default:
        return item.id === '2'
          ? 'linear-gradient(90deg, rgba(102,255,179,0.95), rgba(119,255,204,0.95))'
          : item.id === '3'
          ? 'linear-gradient(90deg, rgba(255,102,204,0.90), rgba(255,153,102,0.90))'
          : item.id === '5'
          ? 'linear-gradient(90deg, rgba(92,110,235,0.90), rgba(114,73,160,0.90))'
          : 'linear-gradient(90deg, rgba(80,155,255,0.92), rgba(102,214,255,0.92))';
    }
  }, [item.id, item.majorShort]);

  return (
    <Card
      hoverable
      onClick={() => onOpen(item.id)}
      style={{
        borderRadius: 10,
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,0.06)',
        height: '100%', // ⭐ QUAN TRỌNG: cho Col stretch
      }}
      bodyStyle={{
        padding: 14,
        display: 'flex',
        flexDirection: 'column',
        minHeight: 360, // ⭐ ép chiều cao card bằng nhau
      }}
    >
      {/* ===== Header màu ===== */}
      <div
        style={{
          height: 86,
          borderRadius: 8,
          padding: 12,
          position: 'relative',
          background: headerBg,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {/* Star trái */}
        <div
          onClick={(e) => {
            e.stopPropagation();
            onToggleTrack(item.id);
            message.success(item.tracked ? 'Đã bỏ theo dõi' : 'Đã theo dõi');
          }}
          style={{
            position: 'absolute',
            left: 10,
            top: 10,
            color: 'rgba(255,255,255,0.95)',
            fontSize: 16,
            cursor: 'pointer',
          }}
        >
          {item.tracked ? <StarFilled /> : <StarOutlined />}
        </div>

        {/* More phải */}
        <div
          style={{
            position: 'absolute',
            right: 10,
            top: 10,
            color: 'rgba(255,255,255,0.95)',
            fontSize: 16,
          }}
        >
          <MoreOutlined />
        </div>

        <Title level={2} style={{ margin: 0, color: 'white', letterSpacing: 1 }}>
          {item.majorShort}
        </Title>
      </div>

      {/* ===== Content ===== */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: 8,
          marginTop: 10,
          flex: 1, // ⭐ cho content chiếm hết chiều cao còn lại
        }}
      >
        <Text type="secondary" style={{ fontSize: 12 }}>
          [{item.code}] · {item.term}
        </Text>

        <Title level={5} style={{ margin: 0 }} ellipsis={{ rows: 2 }}>
          {item.nameVi}
        </Title>

        <div style={{ fontSize: 12, lineHeight: 1.6 }}>
          <div>
            <Text type="secondary">Tín chỉ:</Text> <Text>{item.credits}</Text>
          </div>
          <div>
            <Text type="secondary">Khoa:</Text> <Text>{item.faculty}</Text>
          </div>
          <div>
            <Text type="secondary">Chương trình:</Text> <Text>{item.program}</Text>
          </div>
          <div>
            <Text type="secondary">TS.</Text> <Text>{item.lecturerName}</Text>
          </div>
        </div>

        {/* ===== PHẦN ĐÁY – ÉP THẲNG HÀNG ===== */}
        <div style={{ marginTop: 'auto' }}>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 4,
            }}
          >
            <Text type="secondary" style={{ fontSize: 12 }}>
              Đã xuất bản
            </Text>
            <Text style={{ fontSize: 12, color: '#52c41a' }}>{item.progress}% complete</Text>
          </div>

          <Progress percent={item.progress} showInfo={false} strokeColor="#52c41a" />
        </div>
      </div>
    </Card>
  );
};
