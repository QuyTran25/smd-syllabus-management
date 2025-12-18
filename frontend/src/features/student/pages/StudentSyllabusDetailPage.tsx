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
  message,
  Skeleton,
} from 'antd';
import { useNavigate, useParams } from 'react-router-dom';

import { AISummaryModal } from '../../../student/components/AISummaryModal';
import { CloPloModal } from '../../../student/components/CloPloModal';
import { ReportIssueModal } from '../../../student/components/ReportIssueModal';

import {
  useDownloadPdf,
  useReportIssue,
  useStudentSyllabusDetail,
  useToggleTrack,
} from '../hooks/useStudentSyllabus';

const { Title, Text } = Typography;

export const StudentSyllabusDetailPage: React.FC = () => {
  const { id = '' } = useParams();
  const navigate = useNavigate();

  const { data, isLoading } = useStudentSyllabusDetail(id);

  const toggleTrack = useToggleTrack();
  const downloadPdf = useDownloadPdf();
  const reportIssue = useReportIssue();

  const [openAi, setOpenAi] = useState(false);
  const [openCloPlo, setOpenCloPlo] = useState(false);
  const [openReport, setOpenReport] = useState(false);

  // ===== AI summary object for modal (uses the inline summary + derived bullets) =====
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
        'Nên có kiến thức cơ bản về môn tiên quyết (nếu có)',
        'Chuẩn bị trước: ôn lại kiến thức nền',
        'Thời gian tự học: dành ít nhất 6 giờ/tuần',
      ],
    };
  }, [data]);

  // ===== Tables columns =====
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
      { title: 'Tiêu chí', dataIndex: 'criteria', key: 'criteria', width: 90 },
      {
        title: 'Trọng số',
        dataIndex: 'weight',
        key: 'weight',
        width: 90,
        render: (v: number) => `${v}%`,
      },
    ],
    []
  );

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
              <Tag key={p} color="green">
                {p}
              </Tag>
            ))}
          </Space>
        ),
      },
    ],
    []
  );

  // ===== CLO - PLO Matrix (missing section you requested) =====
  const ploMatrixColumns = useMemo(() => {
    if (!data?.ploList) return [];
    const base: any[] = [{ title: 'CLO', dataIndex: 'clo', key: 'clo', width: 120, fixed: 'left' }];

    const dyn = data.ploList.map((plo: string) => ({
      title: plo,
      dataIndex: plo,
      key: plo,
      align: 'center' as const,
      width: 90,
      render: (v: boolean) =>
        v ? <span style={{ color: '#52c41a', fontWeight: 800 }}>✓</span> : null,
    }));

    return [...base, ...dyn];
  }, [data?.ploList]);

  const ploMatrixRows = useMemo(() => {
    if (!data?.clos || !data?.ploList) return [];

    return data.clos.map((c: any) => {
      const row: Record<string, any> = { key: c.code, clo: c.code };
      const mapped = new Set((data.cloPloMap?.[c.code] ?? []) as string[]);
      data.ploList.forEach((plo: string) => {
        row[plo] = mapped.has(plo);
      });
      return row;
    });
  }, [data?.clos, data?.ploList, data?.cloPloMap]);

  // ===== Render states =====
  if (isLoading) {
    return (
      <div style={{ padding: 18 }}>
        <Skeleton active />
      </div>
    );
  }

  if (!data) {
    return (
      <div style={{ padding: 18 }}>
        <Card
          style={{
            borderRadius: 14,
            border: '1px solid rgba(0,0,0,0.06)',
            boxShadow: '0 10px 30px rgba(0,0,0,0.06)',
          }}
          bodyStyle={{ padding: 18 }}
        >
          <Title level={5} style={{ marginTop: 0 }}>
            Không tìm thấy đề cương
          </Title>
          <Button onClick={() => navigate('/student/syllabi')}>Quay lại danh sách</Button>
        </Card>
      </div>
    );
  }

  // ===== UI tokens =====
  const cardStyle: React.CSSProperties = {
    borderRadius: 14,
    border: '1px solid rgba(0,0,0,0.06)',
    boxShadow: '0 10px 30px rgba(0,0,0,0.06)',
  };

  const sectionTitleStyle: React.CSSProperties = {
    marginTop: 0,
    marginBottom: 10,
  };

  return (
    <div style={{ padding: 18 }}>
      {/* Breadcrumb */}
      <Breadcrumb
        items={[
          { title: <a onClick={() => navigate('/student/syllabi')}>Đề cương của tôi</a> },
          { title: data.code },
        ]}
      />

      {/* Header + actions */}
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
          <Button onClick={() => setOpenAi(true)}>Tóm tắt AI</Button>
          <Button onClick={() => setOpenCloPlo(true)}>Bản đồ CLO-PLO</Button>

          <Button
            type="primary"
            loading={downloadPdf.isPending}
            onClick={async () => {
              try {
                await downloadPdf.mutateAsync(data.id);
                message.success('Mock: Tải PDF thành công');
              } catch {
                message.error('Không thể tải PDF');
              }
            }}
          >
            Tải PDF
          </Button>

          <Button
            onClick={async () => {
              try {
                await toggleTrack.mutateAsync(data.id);
                message.success(data.tracked ? 'Đã bỏ theo dõi' : 'Đã theo dõi');
              } catch {
                message.error('Không thể cập nhật theo dõi');
              }
            }}
          >
            {data.tracked ? 'Bỏ theo dõi' : 'Theo dõi'}
          </Button>

          <Button danger onClick={() => setOpenReport(true)}>
            Báo lỗi
          </Button>
        </Space>
      </div>

      {/* Inline AI summary (subtle, clean) */}
      <div style={{ marginTop: 12 }}>
        <Alert
          type="info"
          showIcon
          message="Tóm tắt AI"
          description={data.summaryInline}
          style={{
            borderRadius: 12,
            border: '1px solid rgba(24,144,255,0.20)',
            background: 'rgba(24,144,255,0.06)',
          }}
        />
      </div>

      {/* Main card */}
      <Card style={{ marginTop: 12, ...cardStyle }} bodyStyle={{ padding: 18 }}>
        {/* Info table */}
        <Descriptions bordered size="small" column={{ xs: 1, sm: 2, md: 3 }}>
          <Descriptions.Item label="Mã học phần">{data.code}</Descriptions.Item>
          <Descriptions.Item label="Số tín chỉ">{data.credits}</Descriptions.Item>
          <Descriptions.Item label="Ngày xuất bản">{data.publishedAt}</Descriptions.Item>

          <Descriptions.Item label="Tên học phần (Tiếng Việt)">{data.nameVi}</Descriptions.Item>
          <Descriptions.Item label="Tên học phần (Tiếng Anh)">{data.nameEn}</Descriptions.Item>
          <Descriptions.Item label="Loại học phần">
            <Tag color="red">Bắt buộc</Tag>
          </Descriptions.Item>

          <Descriptions.Item label="Học kỳ">{data.term}</Descriptions.Item>
          <Descriptions.Item label="Khoa/Bộ môn">{data.faculty}</Descriptions.Item>
          <Descriptions.Item label="Chương trình">{data.program}</Descriptions.Item>

          <Descriptions.Item label="Giảng viên">
            {data.lecturerName} {data.lecturerEmail ? `(${data.lecturerEmail})` : ''}
          </Descriptions.Item>

          <Descriptions.Item label="Học phần tiên quyết">
            <a href="#" onClick={(e) => e.preventDefault()}>
              {data.prerequisite?.text ?? 'Không'}
            </a>
          </Descriptions.Item>

          <Descriptions.Item label="Thang điểm">10</Descriptions.Item>
        </Descriptions>

        <Divider style={{ margin: '18px 0' }} />

        {/* Time allocation */}
        <Title level={5} style={sectionTitleStyle}>
          Phân bổ Thời gian
        </Title>
        <Descriptions bordered size="small" column={3}>
          <Descriptions.Item label="Lý thuyết">{data.timeAllocation.theory} tiết</Descriptions.Item>
          <Descriptions.Item label="Thực hành">
            {data.timeAllocation.practice} tiết
          </Descriptions.Item>
          <Descriptions.Item label="Tự học">{data.timeAllocation.selfStudy} tiết</Descriptions.Item>
        </Descriptions>

        <Divider style={{ margin: '18px 0' }} />

        {/* Description */}
        <Title level={5} style={sectionTitleStyle}>
          Mô tả học phần
        </Title>
        <Text>{data.description}</Text>

        <Divider style={{ margin: '18px 0' }} />

        {/* Objectives */}
        <Title level={5} style={sectionTitleStyle}>
          Mục tiêu học phần
        </Title>
        <ol style={{ paddingLeft: 18, margin: 0 }}>
          {(data.objectives ?? []).map((x: string, i: number) => (
            <li key={i}>{x}</li>
          ))}
        </ol>

        <Divider style={{ margin: '18px 0' }} />

        {/* Teaching methods */}
        <Title level={5} style={sectionTitleStyle}>
          Phương pháp giảng dạy
        </Title>
        <Text>{data.teachingMethods}</Text>

        <Divider style={{ margin: '18px 0' }} />

        {/* Student tasks */}
        <Title level={5} style={sectionTitleStyle}>
          Nhiệm vụ của Sinh viên
        </Title>
        <div
          style={{
            padding: 12,
            borderRadius: 12,
            border: '1px solid rgba(0,0,0,0.06)',
            background: 'rgba(0,0,0,0.02)',
          }}
        >
          <ul style={{ margin: 0, paddingLeft: 18 }}>
            {(data.studentTasks ?? []).map((x: string, i: number) => (
              <li key={i}>{x}</li>
            ))}
          </ul>
        </div>

        <Divider style={{ margin: '18px 0' }} />

        {/* Assessment matrix */}
        <Title level={5} style={sectionTitleStyle}>
          Ma trận Đánh giá
        </Title>
        <Table
          size="small"
          columns={assessmentColumns as any}
          dataSource={(data.assessmentMatrix ?? []).map((x: any, idx: number) => ({
            ...x,
            key: idx,
          }))}
          pagination={false}
          bordered
        />

        <Divider style={{ margin: '18px 0' }} />

        {/* CLO table */}
        <Title level={5} style={sectionTitleStyle}>
          Chuẩn đầu ra học phần (CLO)
        </Title>
        <Table
          size="small"
          columns={cloColumns as any}
          dataSource={(data.clos ?? []).map((x: any) => ({ ...x, key: x.code }))}
          pagination={false}
          bordered
        />

        {/* ===== Missing section added here: CLO-PLO Matrix ===== */}
        <Divider style={{ margin: '18px 0' }} />
        <Title level={5} style={sectionTitleStyle}>
          Ma trận CLO - PLO
        </Title>
        <Table
          size="small"
          columns={ploMatrixColumns as any}
          dataSource={ploMatrixRows as any}
          pagination={false}
          bordered
          scroll={{ x: 'max-content' }}
        />

        <Divider style={{ margin: '18px 0' }} />

        {/* Textbooks & references */}
        <Title level={5} style={sectionTitleStyle}>
          Giáo trình &amp; Tài liệu
        </Title>

        <Title level={5} style={{ marginTop: 0, marginBottom: 8 }}>
          Giáo trình chính:
        </Title>
        <ol style={{ paddingLeft: 18, marginTop: 0 }}>
          {(data.textbooks ?? []).map((x: string, i: number) => (
            <li key={i}>{x}</li>
          ))}
        </ol>

        <Title level={5} style={{ marginTop: 14, marginBottom: 8 }}>
          Tài liệu tham khảo:
        </Title>
        <ol style={{ paddingLeft: 18, marginTop: 0 }}>
          {(data.references ?? []).map((x: string, i: number) => (
            <li key={i}>{x}</li>
          ))}
        </ol>
      </Card>

      {/* Modals (keep existing features, no new feature) */}
      <AISummaryModal open={openAi} onClose={() => setOpenAi(false)} summary={aiSummary} />

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
        submitting={reportIssue.isPending}
        onSubmit={async (v: any) => {
          try {
            await reportIssue.mutateAsync({
              syllabusId: data.id,
              section: v.section,
              description: v.description,
            });
            message.success('Đã gửi báo cáo (mock)');
            setOpenReport(false);
          } catch {
            message.error('Không thể gửi báo cáo');
          }
        }}
      />
    </div>
  );
};
