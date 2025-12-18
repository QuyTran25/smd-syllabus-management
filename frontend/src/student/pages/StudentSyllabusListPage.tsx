import React, { useEffect, useMemo, useState } from 'react';
import { Badge, Breadcrumb, Col, Row, Space, Typography, Skeleton, Empty } from 'antd';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { StudentFilters } from '../components/StudentFilters';
import { SyllabusCard } from '../components/SyllabusCard';
import { useStudentSyllabi, useToggleTrack } from '../hooks/useStudentSyllabus';
import type { StudentSyllabiFilters } from '../types';

const { Title, Text } = Typography;

export const StudentSyllabusListPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [filters, setFilters] = useState<StudentSyllabiFilters>({
    scope: 'ALL',
    q: '',
    faculty: undefined,
    program: undefined,
    term: undefined,
    sort: 'newest',
  });

  // Sync scope from query (?scope=TRACKED)
  useEffect(() => {
    const scope = searchParams.get('scope');
    if (scope === 'TRACKED' && filters.scope !== 'TRACKED')
      setFilters((p) => ({ ...p, scope: 'TRACKED' }));
    if (scope === 'ALL' && filters.scope !== 'ALL') setFilters((p) => ({ ...p, scope: 'ALL' }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  // Persist scope to query
  useEffect(() => {
    const current = searchParams.get('scope');
    if (filters.scope !== current) {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        next.set('scope', filters.scope);
        return next;
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.scope]);

  const { data, isLoading } = useStudentSyllabi(filters);
  const toggleTrack = useToggleTrack();

  const rows = data?.rows ?? [];
  const trackedCount = data?.trackedCount ?? 0;

  const faculties = useMemo(() => Array.from(new Set(rows.map((x) => x.faculty))).sort(), [rows]);
  const programs = useMemo(() => Array.from(new Set(rows.map((x) => x.program))).sort(), [rows]);
  const terms = useMemo(() => Array.from(new Set(rows.map((x) => x.term))).sort(), [rows]);

  return (
    <>
      {/* Banner full width - escape khỏi container maxWidth 1200px */}
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
              'radial-gradient(circle at 10% 30%, rgba(255,255,255,0.18) 0 60px, transparent 62px),' +
              'radial-gradient(circle at 90% 35%, rgba(255,255,255,0.14) 0 70px, transparent 72px),' +
              'radial-gradient(circle at 98% 80%, rgba(255,255,255,0.12) 0 55px, transparent 57px)',
            pointerEvents: 'none',
          }}
        />

        <Space direction="vertical" align="center" size={6} style={{ position: 'relative' }}>
          <Title level={3} style={{ margin: 0, color: 'white' }}>
            Đề cương của tôi
          </Title>

          <Breadcrumb
            items={[
              { title: <Text style={{ color: 'rgba(255,255,255,0.85)' }}>Đề cương của tôi</Text> },
              {
                title: (
                  <Text style={{ color: 'rgba(255,255,255,0.85)' }}>Có trang của trang web</Text>
                ),
              },
              { title: <Text style={{ color: 'rgba(255,255,255,0.85)' }}>Đề cương của tôi</Text> },
            ]}
          />
        </Space>
      </div>

      {/* Content với padding */}
      <div style={{ padding: '0 18px' }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: 12,
          flexWrap: 'wrap',
          marginBottom: 12,
        }}
      >
        <Title level={4} style={{ margin: 0 }}>
          Chào bạn, Sinh viên! 👋
        </Title>

        <Badge count={trackedCount} overflowCount={99} offset={[-4, 4]}>
          <div
            style={{
              border: '1px solid #ffe58f',
              background: '#fff7e6',
              padding: '6px 10px',
              borderRadius: 6,
              fontSize: 12,
              display: 'flex',
              alignItems: 'center',
              gap: 6,
            }}
          >
            ⭐ Đang theo dõi: {trackedCount} đề cương
          </div>
        </Badge>
      </div>

      {/* Khối Tổng quan + filter (y hệt cấu trúc trong ảnh) */}
      <StudentFilters
        value={filters}
        faculties={faculties}
        programs={programs}
        terms={terms}
        onChange={setFilters}
      />

      {/* Cards */}
      <div style={{ marginTop: 14 }}>
        {isLoading ? (
          <Skeleton active />
        ) : rows.length === 0 ? (
          <div style={{ marginTop: 28 }}>
            <Empty description="Không có đề cương phù hợp bộ lọc." />
          </div>
        ) : (
          <div
            style={{
              display: 'grid',
              gap: 14,
              gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
              alignItems: 'stretch',
            }}
          >
            {rows.map((item) => (
              <div key={item.id} style={{ minWidth: 0 }}>
                <SyllabusCard
                  item={item}
                  onOpen={(sid) => navigate(`/syllabi/${sid}`)}
                  onToggleTrack={(sid) => toggleTrack.mutate(sid)}
                />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
    </>
  );
};
