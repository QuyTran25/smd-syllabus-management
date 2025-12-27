import React, { useMemo } from 'react';
import { Card, Progress, Typography } from 'antd';
import { StarOutlined, StarFilled, MoreOutlined } from '@ant-design/icons';
import type { SyllabusListItem } from '../types';

const { Text, Title } = Typography;

type Props = {
  item: SyllabusListItem;
  onOpen: (id: string) => void;
  onToggleTrack: (id: string) => void;
};

export const SyllabusCard: React.FC<Props> = ({ item, onOpen, onToggleTrack }) => {
  // Màu sắc Gradient tự động theo mã ngành (CS: Xanh/Tím, EE: Cam)
  const headerBg = useMemo(() => {
    const colors: Record<string, string> = {
      CS: 'linear-gradient(90deg, #4facfe 0%, #00f2fe 100%)',
      EE: 'linear-gradient(90deg, #f6d365 0%, #fda085 100%)',
      IT: 'linear-gradient(90deg, #667eea 0%, #764ba2 100%)',
    };
    // Nếu là CS và ID cụ thể, có thể đổi sang Tím hoặc Xanh lá như ảnh mẫu
    if (item.majorShort === 'CS' && item.code === 'CS101')
      return 'linear-gradient(90deg, #6a11cb 0%, #2575fc 100%)';
    return colors[item.majorShort] || 'linear-gradient(90deg, #84fab0 0%, #8fd3f4 100%)';
  }, [item.majorShort, item.code]);

  return (
    <Card
      hoverable
      onClick={() => onOpen(item.id)}
      style={{
        borderRadius: 12,
        overflow: 'hidden',
        boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
        height: '100%',
      }}
      // ✅ SỬA LỖI: Sử dụng styles.body thay cho bodyStyle bị khai tử
      styles={{
        body: {
          padding: 16,
          display: 'flex',
          flexDirection: 'column',
          minHeight: 360,
        },
      }}
    >
      {/* ===== Phần đầu Card (Mã ngành) ===== */}
      <div
        style={{
          height: 80,
          borderRadius: 8,
          background: headerBg,
          position: 'relative',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 12,
        }}
      >
        <div
          onClick={(e) => {
            e.stopPropagation();
            onToggleTrack(item.id);
          }}
          style={{
            position: 'absolute',
            left: 12,
            top: 12,
            color: 'white',
            cursor: 'pointer',
            fontSize: 18,
          }}
        >
          {item.tracked ? <StarFilled style={{ color: '#fadb14' }} /> : <StarOutlined />}
        </div>
        <Title level={2} style={{ margin: 0, color: 'white', fontWeight: 700 }}>
          {item.majorShort}
        </Title>
        <MoreOutlined
          style={{ position: 'absolute', right: 12, top: 12, color: 'white', fontSize: 18 }}
        />
      </div>

      {/* ===== Nội dung chi tiết môn học ===== */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 6 }}>
        <Text type="secondary" style={{ fontSize: 12 }}>
          [{item.code}] · {item.term}
        </Text>

        <Title level={5} style={{ margin: '2px 0 10px 0', minHeight: 48 }} ellipsis={{ rows: 2 }}>
          {item.nameVi}
        </Title>

        <div style={{ fontSize: 13, lineHeight: 1.8 }}>
          <div>
            <Text type="secondary">Tín chỉ: </Text> <Text strong>{item.credits}</Text>
          </div>
          <div>
            <Text type="secondary">Khoa: </Text> <Text>{item.faculty}</Text>
          </div>
          <div>
            <Text type="secondary">Chương trình: </Text> <Text>{item.program}</Text>
          </div>
          <div>
            <Text type="secondary">TS. </Text> <Text>{item.lecturerName}</Text>
          </div>
        </div>

        {/* ===== Thanh tiến độ ở đáy Card ===== */}
        <div style={{ marginTop: 'auto', paddingTop: 14 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
            <Text type="secondary" style={{ fontSize: 11 }}>
              Đã xuất bản
            </Text>
            <Text style={{ fontSize: 11, color: '#52c41a' }}>100% complete</Text>
          </div>
          <Progress percent={100} showInfo={false} strokeColor="#52c41a" size="small" />
        </div>
      </div>
    </Card>
  );
};
