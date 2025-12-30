# Hướng dẫn sử dụng Role Permissions

## 📝 Cách sử dụng trong component

### 1. Import hook
```typescript
import { useRolePermissions } from '@/hooks/useRolePermissions';
```

### 2. Sử dụng trong component

```typescript
function SyllabusListPage() {
  const { statusTabs, allowedStatuses, userRole, canView } = useRolePermissions();
  
  // Render tabs chỉ với trạng thái được phép
  return (
    <Tabs items={statusTabs} />
  );
}
```

### 3. Filter API calls

```typescript
function SyllabusListPage() {
  const { allowedStatuses } = useRolePermissions();
  
  // Gọi API chỉ với các status được phép
  const { data } = useQuery({
    queryKey: ['syllabi', allowedStatuses],
    queryFn: () => syllabusApi.getSyllabi({ 
      statuses: allowedStatuses // Gửi array ['APPROVED', 'PUBLISHED', ...]
    })
  });
}
```

### 4. Conditional rendering

```typescript
function SyllabusActions({ syllabus }) {
  const { isAdmin, isPrincipal, canView } = useRolePermissions();
  
  return (
    <>
      {isAdmin && <Button>Xuất bản</Button>}
      {isPrincipal && canView('PENDING_PRINCIPAL') && <Button>Phê duyệt</Button>}
    </>
  );
}
```

## 🎯 Ví dụ cụ thể cho từng role

### Admin (7 trạng thái)
```typescript
const tabs = [
  { key: 'APPROVED', label: 'Đã phê duyệt' },
  { key: 'PUBLISHED', label: 'Đã xuất bản' },
  { key: 'REJECTED', label: 'Bị từ chối' },
  { key: 'REVISION_IN_PROGRESS', label: 'Đang chỉnh sửa' },
  { key: 'PENDING_ADMIN_REPUBLISH', label: 'Chờ xuất bản lại' },
  { key: 'INACTIVE', label: 'Không hoạt động' },
  { key: 'ARCHIVED', label: 'Đã lưu trữ' }
];
```

### Hiệu trưởng (2 trạng thái)
```typescript
const tabs = [
  { key: 'PENDING_PRINCIPAL', label: 'Chờ Hiệu trưởng duyệt' },
  { key: 'APPROVED', label: 'Đã phê duyệt' }
];
```

### Phòng đào tạo (3 trạng thái)
```typescript
const tabs = [
  { key: 'PENDING_AA', label: 'Chờ Phòng ĐT' },
  { key: 'PENDING_PRINCIPAL', label: 'Chờ Hiệu trưởng duyệt' },
  { key: 'REJECTED', label: 'Bị từ chối' }
];
```

### Trưởng bộ môn (5 trạng thái)
```typescript
const tabs = [
  { key: 'PENDING_HOD', label: 'Chờ Trưởng BM' },
  { key: 'PENDING_AA', label: 'Chờ Phòng ĐT' },
  { key: 'REJECTED', label: 'Bị từ chối' },
  { key: 'PENDING_HOD_REVISION', label: 'Chờ TBM duyệt lại' },
  { key: 'PENDING_ADMIN_REPUBLISH', label: 'Chờ xuất bản lại' }
];
```

## ⚠️ Lưu ý quan trọng

1. **Backend API phải hỗ trợ filter theo array statuses:**
```typescript
// ❌ SAI - Gửi từng status riêng
/api/syllabi?status=APPROVED

// ✅ ĐÚNG - Gửi array statuses
/api/syllabi?status=APPROVED&status=PUBLISHED&status=REJECTED
```

2. **Frontend service cần update:**
```typescript
// services/syllabus.service.ts
export const getSyllabi = (filters: {
  statuses?: string[];  // Array thay vì string
  page?: number;
  size?: number;
}) => {
  const params = new URLSearchParams();
  
  // Thêm từng status vào query params
  filters.statuses?.forEach(status => {
    params.append('status', status);
  });
  
  params.append('page', String(filters.page || 0));
  params.append('size', String(filters.size || 10));
  
  return api.get(`/syllabi?${params.toString()}`);
};
```

## 🔧 Cần fix ở backend

Nếu backend chưa hỗ trợ multiple status filter, cần update SyllabusController:

```java
@GetMapping
public ResponseEntity<ApiResponse<Page<SyllabusResponse>>> getAllSyllabi(
    @RequestParam(required = false) List<String> status,  // List thay vì String
    Pageable pageable
) {
    Page<SyllabusResponse> syllabi = syllabusService.getAllSyllabi(status, pageable);
    return ResponseEntity.ok(ApiResponse.success(syllabi));
}
```
