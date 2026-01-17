import React, { useEffect, useMemo, useState } from 'react';
import { Badge, Breadcrumb, Typography, Skeleton, Empty } from 'antd';
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
    sort: 'newest', // Mặc định là mới nhất
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

  // Lấy dữ liệu từ Hook
  const { data, isLoading } = useStudentSyllabi(filters);
  const toggleTrack = useToggleTrack();

  const rows = useMemo(() => data ?? [], [data]);

  const trackedCount = useMemo(() => rows.filter((r) => r.tracked).length, [rows]);

  // Tự động trích xuất danh sách options cho bộ lọc
  const faculties = useMemo(() => Array.from(new Set(rows.map((x) => x.faculty))).sort(), [rows]);
  const programs = useMemo(() => Array.from(new Set(rows.map((x) => x.program))).sort(), [rows]);
  const terms = useMemo(() => Array.from(new Set(rows.map((x) => x.term))).sort(), [rows]);

  // --- LOGIC LỌC VÀ SẮP XẾP ---
  const filteredRows = useMemo(() => {
    // 1. Lọc dữ liệu (Filter)
    const result = rows.filter((item) => {
      // Lọc theo scope (Theo dõi)
      if (filters.scope === 'TRACKED' && !item.tracked) return false;

      // Lọc theo từ khóa (Search)
      if (filters.q) {
        const q = filters.q.toLowerCase();
        // Kiểm tra an toàn
        const code = item.code?.toLowerCase() || '';
        const nameVi = item.nameVi?.toLowerCase() || '';
        const lecturer = item.lecturerName?.toLowerCase() || '';

        if (!code.includes(q) && !nameVi.includes(q) && !lecturer.includes(q)) {
          return false;
        }
      }

      // Lọc theo các dropdown
      if (filters.faculty && item.faculty !== filters.faculty) return false;
      if (filters.program && item.program !== filters.program) return false;
      if (filters.term && item.term !== filters.term) return false;

      return true;
    });

    // 2. Sắp xếp dữ liệu (Sort)
    return result.sort((a, b) => {
      // SỬA LỖI: Dùng 'id' để sắp xếp thay vì 'createdAt'
      // Giả sử ID là số (hoặc chuỗi số), ID lớn = Mới hơn
      const idA = Number(a.id);
      const idB = Number(b.id);

      // Nếu id không phải số (ví dụ UUID), đoạn này sẽ không sort được theo thời gian.
      // Khi đó bạn cần báo Back-end trả về thêm trường 'createdDate'.

      if (filters.sort === 'newest') {
        return idB - idA; // Mới nhất (ID lớn) lên đầu
      }
      if (filters.sort === 'oldest') {
        return idA - idB; // Cũ nhất (ID nhỏ) lên đầu
      }
      return 0;
    });
  }, [rows, filters]);

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
        </div>
      </div>
    </>
  );
};
