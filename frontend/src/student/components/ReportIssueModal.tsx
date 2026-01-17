import React from 'react';
import { Modal, Form, Input, Select, Space, Button } from 'antd';

type Props = {
  open: boolean;
  onClose: () => void;
  onSubmit: (payload: { section: string; description: string }) => void;
  submitting?: boolean;
};

export const ReportIssueModal: React.FC<Props> = ({ open, onClose, onSubmit, submitting }) => {
  const [form] = Form.useForm();

  return (
    <Modal open={open} onCancel={onClose} footer={null} title="Báo cáo lỗi" width={520}>
      <Form
        form={form}
        layout="vertical"
        onFinish={(values) => {
          onSubmit(values);
          form.resetFields();
        }}
      >
        <Form.Item
          name="section"
          label="Phần nội dung có lỗi"
          rules={[{ required: true, message: 'Vui lòng chọn phần nội dung' }]}
        >
          <Select
            placeholder="VD: CLO3, Mục tiêu, Tài liệu tham khảo..."
            options={[
              { value: 'SUBJECT_INFO', label: 'Thông tin môn học' },
              { value: 'OBJECTIVES', label: 'Mục tiêu học phần' },
              { value: 'ASSESSMENT_MATRIX', label: 'Ma trận đánh giá' },
              { value: 'CLO', label: 'Chuẩn đầu ra học phần (CLO)' },
              { value: 'CLO_PLO_MATRIX', label: 'Ma trận CLO-PLO' },
              { value: 'TEXTBOOK', label: 'Giáo trình' },
              { value: 'REFERENCE', label: 'Tài liệu tham khảo' },
              { value: 'OTHER', label: 'Khác' },
            ]}
          />
        </Form.Item>

        <Form.Item
          name="description"
          label="Mô tả lỗi"
          rules={[{ required: true, message: 'Vui lòng mô tả lỗi' }]}
        >
          <Input.TextArea placeholder="Mô tả chi tiết lỗi bạn phát hiện..." rows={4} />
        </Form.Item>

        <Space style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button onClick={onClose}>Hủy</Button>
          <Button type="primary" htmlType="submit" loading={submitting}>
            Gửi báo cáo
          </Button>
        </Space>
      </Form>
    </Modal>
  );
};
