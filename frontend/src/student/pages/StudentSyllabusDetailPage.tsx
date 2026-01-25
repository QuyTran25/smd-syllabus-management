import React, { useMemo, useState } from 'react';
import {
  Alert,
  Breadcrumb,
  Button,
  Card,
  Descriptions,
  Divider,
  Space,
  Table,
  Tag,
  Typography,
  Skeleton,
  App,
} from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import {
  RobotOutlined,
  TableOutlined,
  DownloadOutlined,
  StarFilled,
  StarOutlined,
} from '@ant-design/icons';

// Import Modals
import { AISummaryModal } from '../components/AISummaryModal';
import { CloPloModal } from '../components/CloPloModal';
import { ReportIssueModal } from '../components/ReportIssueModal';

import {
  useDownloadPdf,
  useReportIssue,
  useStudentSyllabusDetail,
  useToggleTrack,
  useSummarizeSyllabus,
  useAITaskStatus,
} from '../hooks/useStudentSyllabus';

const { Title, Text } = Typography;

export const StudentSyllabusDetailPage: React.FC = () => {
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const { message } = App.useApp();

  // Lấy dữ liệu chi tiết từ API
  const { data, isLoading, isError } = useStudentSyllabusDetail(id);

  const toggleTrack = useToggleTrack();
  const downloadPdf = useDownloadPdf();
  const reportIssue = useReportIssue();
  const summarizeAI = useSummarizeSyllabus();

  const [openAi, setOpenAi] = useState(false);
  const [openCloPlo, setOpenCloPlo] = useState(false);
  const [openReport, setOpenReport] = useState(false);
  const [currentTaskId, setCurrentTaskId] = useState<string | null>(null);
  
  // Poll AI task status
  const taskStatus = useAITaskStatus(currentTaskId);

  // ===== 1. AI summary object for modal =====
  const aiSummary = useMemo(() => {
    if (!data) return { overview: '', highlights: [], recommendations: [] };

    return {
      overview: data.summaryInline ?? '',
      highlights: [
        'Độ khó: Trung bình - Phù hợp sinh viên năm 2-3',
        `Thời lượng: ${data.timeAllocation?.theory ?? 0} tiết lý thuyết + ${
          data.timeAllocation?.practice ?? 0
        } tiết thực hành`,
        'Đánh giá: cân bằng giữa thi và bài tập/dự án',
        'Kỹ năng đạt được: ánh xạ CLO tới PLO rõ ràng',
      ],
      recommendations: [
        'Nên có kiến thức cơ bản về môn tiên quyết',
        'Chuẩn bị trước: ôn lại kiến thức nền',
        'Thời gian tự học: dành ít nhất 6 giờ/tuần',
      ],
    };
  }, [data]);

  // ===== 2. Table columns for Assessment Matrix =====
  const assessmentColumns = useMemo(
    () => [
      { title: 'Phương pháp', dataIndex: 'method', key: 'method' },
      { title: 'Hình thức', dataIndex: 'form', key: 'form', width: 120 },
      {
        title: 'CLO',
        dataIndex: 'clo',
        key: 'clo',
        width: 230,
        render: (clo: string[]) => (
          <Space wrap>
            {(clo ?? []).map((c) => (
              <Tag key={c} color="blue">
                {c}
              </Tag>
            ))}
          </Space>
        ),
      },
      { title: 'Tiêu chí', dataIndex: 'criteria', key: 'criteria', width: 120 },
      {
        title: 'Trọng số',
        dataIndex: 'weight',
        key: 'weight',
        width: 100,
        render: (v: number) => <Text strong>{v}%</Text>,
      },
    ],
    []
  );

  // ===== 3. Table columns for CLO =====
  const cloColumns = useMemo(
    () => [
      { title: 'Mã CLO', dataIndex: 'code', key: 'code', width: 100 },
      { title: 'Mô tả', dataIndex: 'description', key: 'description' },
      { title: 'Bloom Level', dataIndex: 'bloomLevel', key: 'bloomLevel', width: 140 },
      {
        title: 'Trọng số',
        dataIndex: 'weight',
        key: 'weight',
        width: 100,
        render: (v: number) => `${v}%`,
      },
      {
        title: 'Ánh xạ PLO',
        dataIndex: 'plo',
        key: 'plo',
        width: 260,
        render: (plo: string[]) => (
          <Space wrap>
            {(plo ?? []).map((p) => (
              <Tag
                key={p}
                style={{ color: '#1677ff', borderColor: '#1677ff', background: '#e6f4ff' }}
              >
                {p}
              </Tag>
            ))}
          </Space>
        ),
      },
    ],
    []
  );

  // ===== 4. CLO - PLO Matrix Logic (✓ marks) =====
  const ploMatrixColumns = useMemo(() => {
    if (!data?.ploList) return [];
    const base: any[] = [{ title: 'CLO', dataIndex: 'clo', key: 'clo', width: 150, fixed: 'left' }];

    const dyn = data.ploList.map((plo: string) => ({
      title: plo,
      dataIndex: plo,
      key: plo,
      align: 'center' as const,
      width: 100,
      render: (v: boolean) =>
        v ? <span style={{ color: '#52c41a', fontWeight: 800, fontSize: 20 }}>✓</span> : null,
    }));

    return [...base, ...dyn];
  }, [data?.ploList]);

  const ploMatrixRows = useMemo(() => {
    if (!data?.clos || !data?.ploList) return [];

    return data.clos.map((c: any) => {
      const row: Record<string, any> = { key: c.code, clo: c.code };
      const mapped = new Set((data.cloPloMap?.[c.code] ?? []) as string[]);
      data!.ploList.forEach((plo: string) => {
        row[plo] = mapped.has(plo);
      });
      return row;
    });
  }, [data?.clos, data?.ploList, data?.cloPloMap]);

  // Loading state
  if (isLoading)
    return (
      <div style={{ padding: 18 }}>
        <Skeleton active paragraph={{ rows: 15 }} />
      </div>
    );

  // Error state
  if (isError || !data)
    return (
      <div style={{ padding: 40, textAlign: 'center' }}>
        <Card style={{ maxWidth: 500, margin: '0 auto', borderRadius: 12 }}>
          <Title level={4}>Dữ liệu không khả dụng</Title>
          <Text type="secondary">Đã có lỗi xảy ra hoặc dữ liệu đề cương bị trống.</Text>
          <Divider />
          <Button type="primary" onClick={() => navigate('/syllabi')}>
            Quay lại danh sách
          </Button>
        </Card>
      </div>
    );

  // --- 🔥 UX: Cấu hình nút theo dõi dựa trên trạng thái ---
  const isTracked = data.tracked;
  const trackButtonConfig = {
    icon: isTracked ? <StarFilled style={{ color: '#faad14', fontSize: 18 }} /> : <StarOutlined />,
    text: isTracked ? 'Đang theo dõi' : 'Theo dõi',
    style: isTracked
      ? {
          borderColor: '#faad14',
          color: '#d48806',
          background: '#fffbe6',
          fontWeight: 600,
        }
      : {}, // Style mặc định khi chưa theo dõi
  };

  return (
    <div style={{ padding: 18, background: '#f5f7f9', minHeight: '100vh' }}>
      <Breadcrumb
        items={[
          { title: <a onClick={() => navigate('/syllabi')}>Đề cương của tôi</a> },
          { title: data.code },
        ]}
      />

      <div
        style={{
          marginTop: 10,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          gap: 12,
          flexWrap: 'wrap',
        }}
      >
        <Space direction="vertical" size={2}>
          <Title level={4} style={{ margin: 0 }}>
            {data.code} - {data.nameVi}{' '}
            <Tag color="green" style={{ marginLeft: 8 }}>
              ĐÃ XUẤT BẢN
            </Tag>
          </Title>
          <Text type="secondary">
            {data.term} · {data.faculty} · {data.program}
          </Text>
        </Space>

        <Space wrap>
          <Button 
            icon={<RobotOutlined />} 
            loading={summarizeAI.isPending || taskStatus.isFetching}
            onClick={async () => {
              try {
                message.loading({ content: 'Đang gửi yêu cầu AI...', key: 'ai-loading', duration: 0 });
                // ✅ FIX: Dùng id từ URL thay vì data.id
                const taskId = await summarizeAI.mutateAsync(id);
                
                // Lưu taskId và bắt đầu polling
                setCurrentTaskId(taskId);
                message.destroy('ai-loading');
                message.success('Đang xử lý với AI... (khoảng 15 giây)');
                
                // Mở modal ngay (sẽ hiển thị loading)
                setOpenAi(true);
              } catch (error: any) {
                console.error('🔴 Error:', error);
                message.destroy('ai-loading');
                message.error(error?.response?.data?.message || 'Không thể gọi AI');
              }
            }}
          >
            🤖 Tóm tắt AI
          </Button>
          <Button icon={<TableOutlined />} onClick={() => setOpenCloPlo(true)}>
            📊 Bản đồ CLO-PLO
          </Button>

          <Button
            type="primary"
            icon={<DownloadOutlined />}
            loading={downloadPdf.isPending}
            onClick={async () => {
              try {
                await downloadPdf.mutateAsync(data.id);
                message.success('Tải PDF thành công');
              } catch {
                message.error('Không thể tải PDF');
              }
            }}
          >
            Tải PDF
          </Button>

          <Button
            onClick={() => toggleTrack.mutate(id)}
            loading={toggleTrack.isPending}
            icon={trackButtonConfig.icon}
            style={trackButtonConfig.style}
          >
            {trackButtonConfig.text}
          </Button>

          <Button danger onClick={() => setOpenReport(true)}>
            Báo lỗi
          </Button>
        </Space>
      </div>

      <div style={{ marginTop: 12 }}>
        <Alert
          type="info"
          showIcon
          icon={<RobotOutlined />}
          message="Tóm tắt AI"
          description={data.summaryInline}
          style={{
            borderRadius: 12,
            border: '1px solid rgba(24,144,255,0.20)',
            background: 'rgba(24,144,255,0.06)',
          }}
        />
      </div>

      <Card
        style={{ marginTop: 12, borderRadius: 14, boxShadow: '0 10px 30px rgba(0,0,0,0.06)' }}
        styles={{ body: { padding: 18 } }}
      >
        <Descriptions
          bordered
          size="small"
          column={{ xs: 1, sm: 2, md: 3 }}
          styles={{ label: { fontWeight: 600, background: '#fafafa' } }}
        >
          <Descriptions.Item label="Mã học phần">{data.code}</Descriptions.Item>
          <Descriptions.Item label="Số tín chỉ">{data.credits}</Descriptions.Item>
          <Descriptions.Item label="Ngày xuất bản">{data.publishedAt}</Descriptions.Item>
          <Descriptions.Item label="Tên tiếng Việt">{data.nameVi}</Descriptions.Item>
          <Descriptions.Item label="Tên tiếng Anh">{data.nameEn}</Descriptions.Item>
          <Descriptions.Item label="Loại học phần">
            <Tag color="red">Bắt buộc</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Học kỳ">{data.term}</Descriptions.Item>
          <Descriptions.Item label="Khoa/Bộ môn">{data.faculty}</Descriptions.Item>
          <Descriptions.Item label="Chương trình">{data.program}</Descriptions.Item>
          <Descriptions.Item label="Giảng viên">
            {data.lecturerName} ({data.lecturerEmail || 'N/A'})
          </Descriptions.Item>
          <Descriptions.Item label="Thang điểm">10</Descriptions.Item>
        </Descriptions>

        <Divider orientation="left" style={{ margin: '18px 0' }}>
          <Text strong>Phân bổ Thời gian</Text>
        </Divider>
        <div
          style={{
            display: 'flex',
            border: '1px solid #f0f0f0',
            textAlign: 'center',
            borderRadius: 8,
            overflow: 'hidden',
          }}
        >
          <div style={{ flex: 1, padding: 12, background: '#fafafa' }}>
            <Text type="secondary">Lý thuyết</Text>
            <br />
            <Text strong>{data.timeAllocation?.theory} tiết</Text>
          </div>
          <div
            style={{
              flex: 1,
              padding: 12,
              background: '#fafafa',
              borderLeft: '1px solid #f0f0f0',
              borderRight: '1px solid #f0f0f0',
            }}
          >
            <Text type="secondary">Thực hành</Text>
            <br />
            <Text strong>{data.timeAllocation?.practice} tiết</Text>
          </div>
          <div style={{ flex: 1, padding: 12, background: '#fafafa' }}>
            <Text type="secondary">Tự học</Text>
            <br />
            <Text strong>{data.timeAllocation?.selfStudy} tiết</Text>
          </div>
        </div>

        <Divider orientation="left" style={{ margin: '18px 0' }}>
          <Text strong>Ma trận Đánh giá</Text>
        </Divider>
        <Table
          size="small"
          bordered
          pagination={false}
          columns={assessmentColumns as any}
          dataSource={(data.assessmentMatrix ?? []).map((x: any, idx: number) => ({
            ...x,
            key: idx,
          }))}
          summary={() => (
            <Table.Summary.Row style={{ background: '#fafafa' }}>
              <Table.Summary.Cell index={0} colSpan={4}>
                <Text strong>Tổng</Text>
              </Table.Summary.Cell>
              <Table.Summary.Cell index={1}>
                <Text strong>100%</Text>
              </Table.Summary.Cell>
            </Table.Summary.Row>
          )}
        />

        <Divider orientation="left" style={{ margin: '18px 0' }}>
          <Text strong>Chuẩn đầu ra học phần (CLO)</Text>
        </Divider>
        <Table
          size="small"
          bordered
          pagination={false}
          columns={cloColumns as any}
          dataSource={(data.clos ?? []).map((x: any) => ({ ...x, key: x.code }))}
        />

        <Divider orientation="left" style={{ margin: '18px 0' }}>
          <Text strong>Ma trận CLO - PLO</Text>
        </Divider>
        <Table
          size="small"
          bordered
          pagination={false}
          scroll={{ x: 'max-content' }}
          columns={ploMatrixColumns as any}
          dataSource={ploMatrixRows as any}
        />

        <Divider orientation="left" style={{ margin: '18px 0' }}>
          <Text strong>Giáo trình & Tài liệu</Text>
        </Divider>
        <Text strong>Giáo trình chính:</Text>
        <ol style={{ paddingLeft: 18 }}>
          {(data.textbooks ?? []).map((x: string, i: number) => (
            <li key={i}>{x}</li>
          ))}
        </ol>
        <Text strong style={{ marginTop: 8, display: 'block' }}>
          Tài liệu tham khảo:
        </Text>
        <ol style={{ paddingLeft: 18 }}>
          {(data.references ?? []).map((x: string, i: number) => (
            <li key={i}>{x}</li>
          ))}
        </ol>
      </Card>

      <div style={{ textAlign: 'center', padding: '20px 0', color: '#888' }}>
        Bản quyền thuộc về © Trung tâm Thông tin - Thư viện
      </div>

      <AISummaryModal 
        open={openAi} 
        onClose={() => {
          setOpenAi(false);
          setCurrentTaskId(null); // Reset taskId when closing
        }} 
        taskStatus={taskStatus.data} 
      />
      <CloPloModal
        open={openCloPlo}
        onClose={() => setOpenCloPlo(false)}
        clos={data.clos}
        ploList={data.ploList}
        cloPloMap={data.cloPloMap}
      />
      <ReportIssueModal
        open={openReport}
        onClose={() => setOpenReport(false)}
        onSubmit={async (v: any) => {
          await reportIssue.mutateAsync({
            syllabusId: data!.id,
            section: v.section,
            description: v.description,
          });
          message.success('Đã gửi báo cáo thành công');
          setOpenReport(false);
        }}
      />
    </div>
  );
};
