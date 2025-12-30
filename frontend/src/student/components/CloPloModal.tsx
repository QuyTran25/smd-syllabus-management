import React, { useMemo } from 'react';
import { Modal, Alert, Card, Table, Tag, Space, Typography } from 'antd';
import { CheckOutlined } from '@ant-design/icons';
import type { CloRow } from '../types';

const { Text } = Typography;

type Props = {
  open: boolean;
  onClose: () => void;
  clos: CloRow[];
  ploList: string[];
  cloPloMap: Record<string, string[]>;
};

export const CloPloModal: React.FC<Props> = ({
  open,
  onClose,
  clos = [],
  ploList = [],
  cloPloMap = {},
}) => {
  // Cấu trúc cột cho bảng ma trận
  const columns = useMemo(() => {
    const basePloList = ploList || [];
    return [
      {
        title: 'CLO',
        dataIndex: 'code',
        key: 'code',
        width: 110,
        fixed: 'left' as const,
      },
      ...basePloList.map((plo) => ({
        title: plo,
        dataIndex: plo,
        key: plo,
        align: 'center' as const,
        render: (_: any, row: CloRow) => {
          const mapped = (cloPloMap || {})[row.code] ?? [];
          return mapped.includes(plo) ? (
            <CheckOutlined style={{ color: '#52c41a', fontSize: 16, fontWeight: 'bold' }} />
          ) : null;
        },
      })),
    ];
  }, [ploList, cloPloMap]);

  // Chuẩn bị dữ liệu cho bảng
  const dataSource = useMemo(() => (clos || []).map((c) => ({ ...c, key: c.code })), [clos]);

  return (
    <Modal
      open={open}
      onCancel={onClose}
      onOk={onClose}
      okText="Đóng"
      cancelButtonProps={{ style: { display: 'none' } }}
      title="Bản đồ CLO-PLO - Mối quan hệ Chuẩn đầu ra"
      width={920}
      styles={{ body: { paddingTop: 12 } }}
    >
      <Space direction="vertical" style={{ width: '100%' }} size={12}>
        <Alert
          type="info"
          showIcon
          message="Bản đồ này cho thấy mối quan hệ giữa Chuẩn đầu ra học phần (CLO) và Chuẩn đầu ra chương trình (PLO)."
        />

        <Card size="small" title="Ma trận CLO - PLO" styles={{ body: { padding: 0 } }}>
          <Table
            size="small"
            columns={columns as any}
            dataSource={dataSource}
            pagination={false}
            scroll={{ x: 800 }}
            bordered
          />
        </Card>

        <Card size="small" title="Phân tích chi tiết">
          <Space direction="vertical" style={{ width: '100%' }} size={10}>
            {(clos || []).map((c) => (
              <Card key={c.code} size="small" style={{ borderRadius: 10 }}>
                <Space direction="vertical" style={{ width: '100%' }} size={6}>
                  <Text strong>
                    {c.code}: {c.description}
                  </Text>
                  <Space wrap>
                    <Tag color="blue">Bloom: {c.bloomLevel}</Tag>
                    <Tag color="orange">Trọng số: {c.weight}%</Tag>
                    <Tag color="green">Ánh xạ tới:</Tag>
                    {(c.plo ?? []).map((p) => (
                      <Tag key={p} color="green">
                        {p}
                      </Tag>
                    ))}
                  </Space>
                </Space>
              </Card>
            ))}
          </Space>
        </Card>
      </Space>
    </Modal>
  );
};
