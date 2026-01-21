import React, { useEffect, useMemo, useState } from 'react';
import { Badge, Breadcrumb, Typography, Skeleton, Empty, Tooltip } from 'antd'; // Thêm Tooltip cho xịn
import { useNavigate, useSearchParams } from 'react-router-dom';
import { StudentFilters } from '../components/StudentFilters';
import { SyllabusCard } from '../components/SyllabusCard';
import { useStudentSyllabi, useToggleTrack } from '../hooks/useStudentSyllabus';
import type { StudentSyllabiFilters } from '../types';

const { Title } = Typography;

export const StudentSyllabusListPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // Khởi tạo bộ lọc
  const [filters, setFilters] = useState<StudentSyllabiFilters>({
    scope: (searchParams.get('scope') as any) || 'ALL',
    q: '',
    faculty: undefined,
    program: undefined,
    term: undefined,
    sort: 'newest',
  });

  // Đồng bộ URL -> Filters
  useEffect(() => {
    const scope = searchParams.get('scope');
    if (scope && (scope === 'ALL' || scope === 'TRACKED')) {
      if (filters.scope !== scope) {
        setFilters((p) => ({ ...p, scope: scope as any }));
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  // Đồng bộ Filters -> URL
  useEffect(() => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (filters.scope) next.set('scope', filters.scope);
      return next;
    });
  }, [filters.scope, setSearchParams]);

  const { data, isLoading } = useStudentSyllabi(filters);
  const toggleTrack = useToggleTrack();

  const rows = useMemo(() => data ?? [], [data]);
  const trackedCount = useMemo(() => rows.filter((r) => r.tracked).length, [rows]);

  const faculties = useMemo(
    () => Array.from(new Set(rows.map((x) => x.faculty).filter(Boolean))).sort(),
    [rows]
  );
  const programs = useMemo(
    () => Array.from(new Set(rows.map((x) => x.program).filter(Boolean))).sort(),
    [rows]
  );
  const terms = useMemo(
    () => Array.from(new Set(rows.map((x) => x.term).filter(Boolean))).sort(),
    [rows]
  );

  // --- LOGIC LỌC VÀ SẮP XẾP ---
  const filteredRows = useMemo(() => {
    let result = rows.filter((item) => {
      if (filters.scope === 'TRACKED' && !item.tracked) return false;

      if (filters.q) {
        const q = filters.q.toLowerCase();
        const code = item.code?.toLowerCase() || '';
        const nameVi = item.nameVi?.toLowerCase() || '';
        const lecturer = item.lecturerName?.toLowerCase() || '';
        if (!code.includes(q) && !nameVi.includes(q) && !lecturer.includes(q)) {
          return false;
        }
      }

      if (filters.faculty && item.faculty !== filters.faculty) return false;
      if (filters.program && item.program !== filters.program) return false;
      if (filters.term && item.term !== filters.term) return false;

      return true;
    });

    return result.sort((a, b) => {
      const dateA = a.publishedAt ? new Date(a.publishedAt).getTime() : 0;
      const dateB = b.publishedAt ? new Date(b.publishedAt).getTime() : 0;
      if (filters.sort === 'newest') return dateB - dateA;
      if (filters.sort === 'oldest') return dateA - dateB;
      return 0;
    });
  }, [rows, filters]);

  // 🔥 UX MỚI: Hàm xử lý khi bấm vào khung "Đang theo dõi"
  const handleToggleScope = () => {
    setFilters((prev) => ({
      ...prev,
      scope: prev.scope === 'TRACKED' ? 'ALL' : 'TRACKED',
    }));
  };

  const isTrackedMode = filters.scope === 'TRACKED';

  return (
    <>
      {/* Banner Header */}
      <div
        style={{
          height: 120,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 18,
          marginLeft: 'calc(-50vw + 50%)',
          marginRight: 'calc(-50vw + 50%)',
          position: 'relative',
          overflow: 'hidden',
          background: 'linear-gradient(90deg, #018486 0%, #1EA69A 100%)',
        }}
      >
        <div
          style={{
            position: 'absolute',
            inset: 0,
            background:
              'radial-gradient(circle at 10% 30%, rgba(255,255,255,0.18) 0 60px, transparent 62px), radial-gradient(circle at 90% 35%, rgba(255,255,255,0.14) 0 70px, transparent 72px)',
            pointerEvents: 'none',
          }}
        />
        <div style={{ textAlign: 'center', position: 'relative' }}>
          <Title level={3} style={{ margin: 0, color: 'white' }}>
            Đề cương của tôi
          </Title>
          <Breadcrumb
            items={[
              { title: <span style={{ color: 'white' }}>Trang chủ</span> },
              { title: <span style={{ color: 'white' }}>Học tập</span> },
              { title: <span style={{ color: 'white' }}>Đề cương</span> },
            ]}
          />
        </div>
      </div>

      <div style={{ padding: '0 18px' }}>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 16,
          }}
        >
          <Title level={4} style={{ margin: 0 }}>
            Chào bạn, Sinh viên! 👋
          </Title>

          {/* 🔥 UX MỚI: Biến khung badge thành nút bấm */}
          <Tooltip title={isTrackedMode ? 'Bấm để xem tất cả' : 'Bấm để xem danh sách yêu thích'}>
            <Badge count={trackedCount} overflowCount={99}>
              <div
                onClick={handleToggleScope}
                style={{
                  border: isTrackedMode ? '1px solid #faad14' : '1px solid #ffe58f',
                  background: isTrackedMode ? '#fffbe6' : '#fff7e6', // Màu nền thay đổi khi active
                  padding: '6px 12px',
                  borderRadius: 6,
                  fontSize: 13,
                  cursor: 'pointer', // Con trỏ chuột thành hình bàn tay
                  transition: 'all 0.2s',
                  userSelect: 'none',
                  fontWeight: isTrackedMode ? 600 : 400,
                  boxShadow: isTrackedMode ? '0 0 0 2px rgba(250, 173, 20, 0.2)' : 'none', // Hiệu ứng focus
                  display: 'flex',
                  alignItems: 'center',
                  gap: 6,
                }}
              >
                <span>⭐</span>
                {isTrackedMode ? (
                  <span style={{ color: '#d48806' }}>
                    Đang xem: {filteredRows.length} yêu thích
                  </span>
                ) : (
                  <span>Đang theo dõi: {trackedCount} đề cương</span>
                )}
              </div>
            </Badge>
          </Tooltip>
        </div>

        {/* Thanh lọc dữ liệu */}
        <StudentFilters
          value={filters}
          faculties={faculties}
          programs={programs}
          terms={terms}
          onChange={setFilters}
        />

        {/* Danh sách Card hiển thị */}
        <div style={{ marginTop: 20 }}>
          {isLoading ? (
            <Skeleton active paragraph={{ rows: 6 }} />
          ) : rows.length === 0 ? (
            <Empty
              description="Không tìm thấy đề cương nào trong hệ thống."
              style={{ marginTop: 40 }}
            />
          ) : (
            <div
              style={{
                display: 'grid',
                gap: 20,
                gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
              }}
            >
              {filteredRows.map((item) => (
                <SyllabusCard
                  key={item.id}
                  item={item}
                  onOpen={(sid) => navigate(`/syllabi/${sid}`)}
                  onToggleTrack={(sid) => toggleTrack.mutate(sid)}
                />
              ))}
            </div>
          )}

          {/* Thông báo khi không có kết quả lọc */}
          {!isLoading && rows.length > 0 && filteredRows.length === 0 && (
            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description={
                isTrackedMode
                  ? 'Bạn chưa theo dõi đề cương nào.'
                  : 'Không tìm thấy kết quả phù hợp.'
              }
            />
          )}
        </div>
      </div>
    </>
  );
};
