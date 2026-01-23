import React from 'react';
import { Modal, Alert, Card, Typography, Space, Spin } from 'antd';
import { LoadingOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import type { TaskStatusResponse } from '../../services/aiService';

const { Text, Title } = Typography;

type Props = {
  open: boolean;
  onClose: () => void;
  taskStatus?: TaskStatusResponse;
};

export const AISummaryModal: React.FC<Props> = ({ open, onClose, taskStatus }) => {
  const isLoading = !taskStatus || taskStatus.status === 'QUEUED' || taskStatus.status === 'PROCESSING';
  const isSuccess = taskStatus?.status === 'SUCCESS';
  const isError = taskStatus?.status === 'ERROR' || taskStatus?.status === 'FAILED';
  
  // Extract result from taskStatus
  const result = taskStatus?.result;

  return (
    <Modal
      open={open}
      onCancel={onClose}
      onOk={onClose}
      okText="Đóng"
      cancelButtonProps={{ style: { display: 'none' } }}
      title="Tóm tắt AI - Phân tích Đề cương"
      width={860}
    >
      <Space direction="vertical" style={{ width: '100%' }} size={12}>
        {/* Loading State */}
        {isLoading && (
          <Alert
            type="info"
            showIcon
            icon={<LoadingOutlined spin />}
            message="Đang xử lý với AI..."
            description={`Trạng thái: ${taskStatus?.status || 'QUEUED'}. Vui lòng đợi khoảng 2-3 giây...`}
          />
        )}

        {/* Success State */}
        {isSuccess && (
          <Alert
            type="success"
            showIcon
            icon={<CheckCircleOutlined />}
            message="AI đã phân tích xong!"
            description="Kết quả được trích xuất từ nội dung đề cương trong hệ thống."
          />
        )}

        {/* Error State */}
        {isError && (
          <Alert
            type="error"
            showIcon
            icon={<CloseCircleOutlined />}
            message="Có lỗi xảy ra"
            description={taskStatus?.error || 'Không thể xử lý AI, vui lòng thử lại.'}
          />
        )}

        {/* Show loading spinner while processing */}
        {isLoading && (
          <div style={{ textAlign: 'center', padding: '40px 0' }}>
            <Spin size="large" tip="AI đang phân tích đề cương..." />
          </div>
        )}

        {/* Show results when success */}
        {isSuccess && result && (
          <>
            {/* 1. Mô tả học phần */}
            {result.mo_ta_hoc_phan && (
              <Card size="small" title=" Mô tả học phần">
                <Text>{result.mo_ta_hoc_phan}</Text>
              </Card>
            )}

            {/* 2. Mục tiêu học phần */}
            {result.muc_tieu_hoc_phan && result.muc_tieu_hoc_phan.length > 0 && (
              <Card size="small" title=" Mục tiêu học phần">
                <ul style={{ margin: 0, paddingLeft: 18 }}>
                  {result.muc_tieu_hoc_phan.map((muc_tieu: string, i: number) => (
                    <li key={i}>{muc_tieu}</li>
                  ))}
                </ul>
              </Card>
            )}

            {/* 3. Phương pháp giảng dạy */}
            {result.phuong_phap_giang_day && result.phuong_phap_giang_day.length > 0 && (
              <Card size="small" title=" Phương pháp giảng dạy">
                <ul style={{ margin: 0, paddingLeft: 18 }}>
                  {result.phuong_phap_giang_day.map((pp: string, i: number) => (
                    <li key={i}>{pp}</li>
                  ))}
                </ul>
              </Card>
            )}

            {/* 4. Phương pháp đánh giá */}
            {result.phuong_phap_danh_gia && result.phuong_phap_danh_gia.length > 0 && (
              <Card size="small" title=" Phương pháp đánh giá">
                <ul style={{ margin: 0, paddingLeft: 18 }}>
                  {result.phuong_phap_danh_gia.map((dg: any, i: number) => (
                    <li key={i}>
                      <strong>{dg.method}:</strong> {dg.weight}%
                    </li>
                  ))}
                </ul>
              </Card>
            )}

            {/* 5. Giáo trình chính */}
            {result.giao_trinh_chinh && result.giao_trinh_chinh.length > 0 && (
              <Card size="small" title=" Giáo trình chính">
                <ul style={{ margin: 0, paddingLeft: 18 }}>
                  {result.giao_trinh_chinh.map((gt: any, i: number) => (
                    <li key={i}>
                      {gt.title} - {gt.authors} ({gt.year})
                    </li>
                  ))}
                </ul>
              </Card>
            )}

            {/* 6. Tài liệu tham khảo */}
            {result.tai_lieu_tham_khao && result.tai_lieu_tham_khao.length > 0 && (
              <Card size="small" title=" Tài liệu tham khảo">
                <ul style={{ margin: 0, paddingLeft: 18 }}>
                  {result.tai_lieu_tham_khao.slice(0, 5).map((tl: any, i: number) => (
                    <li key={i}>
                      {tl.title} {tl.authors && `- ${tl.authors}`} {tl.year && `(${tl.year})`}
                    </li>
                  ))}
                </ul>
              </Card>
            )}

            {/* 7. Nhiệm vụ của Sinh viên */}
            {result.nhiem_vu_sinh_vien && result.nhiem_vu_sinh_vien.length > 0 && (
              <Card size="small" title=" Nhiệm vụ của Sinh viên">
                <ul style={{ margin: 0, paddingLeft: 18 }}>
                  {result.nhiem_vu_sinh_vien.map((nv: string, i: number) => (
                    <li key={i}>{nv}</li>
                  ))}
                </ul>
              </Card>
            )}

            {/* 8. Chuẩn đầu ra học phần (CLO) */}
            {result.clo && result.clo.length > 0 && (
              <Card size="small" title=" Chuẩn đầu ra học phần (CLO)">
                <ul style={{ margin: 0, paddingLeft: 18 }}>
                  {result.clo.map((clo: any, i: number) => (
                    <li key={i}>
                      <strong>{clo.code}:</strong> {clo.description}
                      {clo.weight && <Text type="secondary"> (Tỷ trọng: {clo.weight}%)</Text>}
                    </li>
                  ))}
                </ul>
              </Card>
            )}

            {/* 9. Ma trận đánh giá */}
            {result.ma_tran_danh_gia && result.ma_tran_danh_gia.length > 0 && (
              <Card size="small" title=" Ma trận đánh giá">
                <ul style={{ margin: 0, paddingLeft: 18 }}>
                  {result.ma_tran_danh_gia.map((item: any, i: number) => (
                    <li key={i}>
                      <strong>{item.method}</strong>
                      {item.form && <> - Hình thức: {item.form}</>}
                      {item.weight && <> - <Text type="secondary">Tỷ trọng: {item.weight}%</Text></>}
                      {item.criteria && (
                        <div style={{ marginTop: 4, fontSize: '13px', color: '#666' }}>
                          Tiêu chí: {item.criteria}
                        </div>
                      )}
                    </li>
                  ))}
                </ul>
              </Card>
            )}
          </>
        )}
      </Space>
    </Modal>
  );
};
