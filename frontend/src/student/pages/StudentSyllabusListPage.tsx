import React, { useEffect, useMemo, useState } from 'react';
import { Badge, Breadcrumb, Typography, Skeleton, Empty } from 'antd';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { StudentFilters } from '../components/StudentFilters';
import { SyllabusCard } from '../components/SyllabusCard';
import { useStudentSyllabi, useToggleTrack } from '../hooks/useStudentSyllabus';
import type { StudentSyllabiFilters } from '../types';

const { Title, Text } = Typography;

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
  }, [searchParams]);

  // Đồng bộ Filters -> URL
  useEffect(() => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set('scope', filters.scope);
      return next;
    });
  }, [filters.scope, setSearchParams]);

  // Lấy dữ liệu từ Hook (Dữ liệu trả về là 1 Array)
  const { data, isLoading } = useStudentSyllabi(filters);
  const toggleTrack = useToggleTrack();

  // ⭐ SỬA LỖI: data chính là mảng các rows
  const rows = useMemo(() => data ?? [], [data]);

  // ⭐ SỬA LỖI: Tính toán số lượng theo dõi trực tiếp từ mảng
  const trackedCount = useMemo(() => rows.filter((r) => r.tracked).length, [rows]);

  // Tự động trích xuất danh sách Khoa/Chương trình/Học kỳ từ dữ liệu thật
  const faculties = useMemo(() => Array.from(new Set(rows.map((x) => x.faculty))).sort(), [rows]);
  const programs = useMemo(() => Array.from(new Set(rows.map((x) => x.program))).sort(), [rows]);
  const terms = useMemo(() => Array.from(new Set(rows.map((x) => x.term))).sort(), [rows]);

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
          <Badge count={trackedCount}>
            <div
              style={{
                border: '1px solid #ffe58f',
                background: '#fff7e6',
                padding: '6px 12px',
                borderRadius: 6,
                fontSize: 13,
              }}
            >
              ⭐ Đang theo dõi: {trackedCount} đề cương
            </div>
          </Badge>
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
              {rows.map((item) => (
                <SyllabusCard
                  key={item.id}
                  item={item}
                  onOpen={(sid) => navigate(`/syllabi/${sid}`)}
                  onToggleTrack={(sid) => toggleTrack.mutate(sid)}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
};
