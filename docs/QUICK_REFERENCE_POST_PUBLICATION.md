# QUICK REFERENCE - Post-Publication Workflow

> Tài liệu tra cứu nhanh cho developers

## 🎯 CÁC API CHÍNH

### Admin - Mở Revision Session
```bash
POST /api/revisions/start
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "syllabusVersionId": "uuid-of-syllabus",
  "feedbackIds": ["feedback-uuid-1", "feedback-uuid-2"],
  "description": "Batch fix for spelling errors"
}
```

### Lecturer - Submit Revision
```bash
POST /api/revisions/submit
Authorization: Bearer {lecturer_token}
Content-Type: application/json

{
  "revisionSessionId": "uuid-of-session",
  "summary": "Fixed all issues"
}
```

### HoD - Approve/Reject
```bash
POST /api/revisions/review
Authorization: Bearer {hod_token}
Content-Type: application/json

{
  "revisionSessionId": "uuid-of-session",
  "decision": "APPROVED",  // or "REJECTED"
  "comment": "Looks good"
}
```

### Admin - Republish
```bash
POST /api/revisions/{sessionId}/republish
Authorization: Bearer {admin_token}
```

---

## 📊 TRẠNG THÁI MAPPING

### Từ PUBLISHED đến PUBLISHED (Happy Path)

```
PUBLISHED
  → Admin starts revision → REVISION_IN_PROGRESS
  → Lecturer submits → PENDING_HOD_REVISION
  → HoD approves → PENDING_ADMIN_REPUBLISH
  → Admin republishes → PUBLISHED (version++)
```

### Revision bị Reject

```
PENDING_HOD_REVISION
  → HoD rejects → REVISION_IN_PROGRESS
  → Lecturer fixes again → PENDING_HOD_REVISION
  (loop until approved)
```

---

## 🗄️ DATABASE QUERIES

### Tìm tất cả revision sessions đang mở
```sql
SELECT * FROM core_service.revision_sessions
WHERE status IN ('OPEN', 'IN_PROGRESS', 'PENDING_HOD')
ORDER BY initiated_at DESC;
```

### Tìm feedbacks chưa được xử lý
```sql
SELECT * FROM core_service.syllabus_error_reports
WHERE status = 'PENDING'
  AND revision_session_id IS NULL
ORDER BY created_at ASC;
```

### Kiểm tra history snapshots
```sql
SELECT 
  h.version_no,
  h.snapshot_reason,
  h.created_at,
  u.full_name as created_by
FROM core_service.syllabus_version_history h
JOIN core_service.users u ON h.created_by = u.id
WHERE h.syllabus_id = 'your-syllabus-uuid'
ORDER BY h.version_number DESC;
```

### Đếm feedback theo type
```sql
SELECT 
  type,
  status,
  COUNT(*) as count
FROM core_service.syllabus_error_reports
GROUP BY type, status;
```

---

## 🎨 FRONTEND COMPONENTS CẦN TẠO

### 1. Admin - Feedback Management
**File**: `frontend/src/components/admin/FeedbackManagement.vue`

Chức năng:
- Hiển thị list feedbacks (PENDING, IN_REVIEW)
- Checkbox để chọn nhiều feedbacks
- Button "Start Revision" để gom feedbacks vào 1 session
- Button "Respond" để phản hồi trực tiếp cho sinh viên

### 2. Lecturer - Revision Dashboard
**File**: `frontend/src/components/lecturer/RevisionDashboard.vue`

Chức năng:
- Hiển thị các revision sessions đang được assign
- Xem chi tiết feedbacks trong mỗi session
- Link đến editor để sửa đề cương
- Button "Submit Revision" khi hoàn thành

### 3. HoD - Approval Queue
**File**: `frontend/src/components/hod/RevisionApprovalQueue.vue`

Chức năng:
- List các revisions đang chờ duyệt (PENDING_HOD)
- Xem diff giữa version cũ và mới
- Button "Approve" / "Reject" với comment

### 4. Admin - Republish Queue
**File**: `frontend/src/components/admin/RepublishQueue.vue`

Chức năng:
- List các revisions đã HoD duyệt
- Review thay đổi cuối cùng
- Button "Republish" để publish

### 5. Student - Feedback Form
**File**: `frontend/src/components/student/FeedbackForm.vue`

Chức năng:
- Form để báo lỗi/đề xuất
- Chọn type (ERROR, SUGGESTION, QUESTION, OTHER)
- Chọn section (CLO, PLO, ASSESSMENT, etc.)
- Text area cho description

---

## 🔔 NOTIFICATION PAYLOAD EXAMPLES

### Error Report Notification (for Lecturer)
```typescript
{
  type: 'ERROR_REPORT',
  payload: {
    syllabusId: string,
    syllabusCode: string,
    revisionSessionId: string,
    feedbackCount: number,
    actionUrl: string,
    actionLabel: string,
    priority: 'HIGH' | 'MEDIUM' | 'LOW'
  }
}
```

### Approval Notification (for HoD/Admin)
```typescript
{
  type: 'APPROVAL',
  payload: {
    syllabusId: string,
    syllabusCode: string,
    revisionSessionId: string,
    lecturerName: string,
    feedbackCount: number,
    actionUrl: string,
    actionLabel: string,
    priority: 'HIGH' | 'MEDIUM' | 'LOW'
  }
}
```

### Publication Notification (for Students)
```typescript
{
  type: 'PUBLICATION',
  payload: {
    syllabusId: string,
    syllabusCode: string,
    newVersionNo: string,
    changesSummary: string,
    yourFeedbackResolved: boolean,
    actionUrl: string,
    actionLabel: string,
    priority: 'MEDIUM'
  }
}
```

---

## 🐛 COMMON ISSUES & SOLUTIONS

### Issue 1: "Revision session already exists"
**Lỗi**: Admin cố mở revision session cho syllabus đã có session active

**Giải pháp**: Check trước khi mở
```sql
SELECT * FROM revision_sessions 
WHERE syllabus_version_id = 'uuid' 
  AND status IN ('OPEN', 'IN_PROGRESS', 'PENDING_HOD');
```
Nếu có → thêm feedbacks vào session hiện tại thay vì tạo mới

### Issue 2: "Cannot submit - not assigned lecturer"
**Lỗi**: User cố submit revision nhưng không phải lecturer được assign

**Giải pháp**: Validate `session.assignedLecturerId === currentUserId`

### Issue 3: "Version number conflict"
**Lỗi**: 2 admin cùng lúc republish 2 revisions khác nhau

**Giải pháp**: Use optimistic locking hoặc row-level lock
```java
@Version
private Long version;
```

### Issue 4: "Notification not sent"
**Lỗi**: Notification entity được tạo nhưng user không thấy

**Giải pháp**: 
1. Check `user.id` có đúng không
2. Check frontend có poll `/api/notifications` không
3. Check WebSocket connection (nếu real-time)

---

## 📝 CODE SNIPPETS

### Backend - Get Current User ID
```java
private UUID getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
        throw new UnauthorizedException("User not authenticated");
    }
    
    String username = auth.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    
    return user.getId();
}
```

### Frontend - Polling Notifications
```typescript
// In App.vue or Layout component
const pollNotifications = async () => {
  const unread = await notificationService.getUnread();
  notificationStore.setUnread(unread);
};

// Poll every 30 seconds
setInterval(pollNotifications, 30000);
```

### Frontend - Format Notification Time
```typescript
import { formatDistanceToNow } from 'date-fns';
import { vi } from 'date-fns/locale';

const formatNotificationTime = (createdAt: string) => {
  return formatDistanceToNow(new Date(createdAt), {
    addSuffix: true,
    locale: vi
  });
};

// Output: "3 phút trước", "2 giờ trước", "1 ngày trước"
```

---

## 🧪 TESTING CHECKLIST

### Unit Tests
```bash
# Backend
mvn test -Dtest=RevisionServiceTest
mvn test -Dtest=NotificationServiceTest

# Frontend
npm run test:unit -- revision.service.spec.ts
```

### Integration Tests
```bash
# Test full workflow
mvn test -Dtest=RevisionWorkflowIntegrationTest
```

### Manual Testing Script
1. Login as Admin
2. Navigate to Feedbacks page
3. Select 2-3 feedbacks
4. Click "Start Revision"
5. Logout → Login as Lecturer
6. Navigate to My Revisions
7. Open the session → Edit syllabus
8. Submit revision
9. Logout → Login as HoD
10. Navigate to Pending Approvals
11. Approve the revision
12. Logout → Login as Admin
13. Navigate to Pending Republish
14. Click "Republish"
15. Verify students can see updated syllabus

---

## 📚 RELATED DOCS

- [Full Workflow Documentation](./POST_PUBLICATION_WORKFLOW.md)
- [API Documentation](./api/)
- [Database Schema](./database/)
- [Frontend Architecture](./architecture/)

---

**Last Updated**: 2026-01-16
**Version**: 1.0
