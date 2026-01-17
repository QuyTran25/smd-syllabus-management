# LUỒNG HOẠT ĐỘNG POST-PUBLICATION WORKFLOW
## Quy trình chỉnh sửa đề cương sau khi xuất bản

> **Tài liệu này mô tả chi tiết luồng xử lý khi sinh viên phản hồi lỗi trên đề cương đã xuất bản và quy trình chỉnh sửa - duyệt - xuất bản lại.**

---

## 📋 MỤC LỤC

1. [Tổng quan workflow](#tổng-quan-workflow)
2. [Các trạng thái chính](#các-trạng-thái-chính)
3. [Luồng chi tiết từng bước](#luồng-chi-tiết-từng-bước)
4. [API Endpoints](#api-endpoints)
5. [Database Schema](#database-schema)
6. [Frontend Implementation Guide](#frontend-implementation-guide)
7. [Notification Structure](#notification-structure)

---

## 🎯 TỔNG QUAN WORKFLOW

```
┌─────────────────────────────────────────────────────────────────┐
│                    PUBLISHED SYLLABUS                           │
│                  (Sinh viên đang xem)                           │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
         ┌───────────────────────┐
         │ Sinh viên gửi feedback│
         │   (Báo lỗi/đề xuất)  │
         └───────────┬───────────┘
                     │
                     ▼
         ┌───────────────────────┐
         │   Admin xem feedback  │
         │   Quyết định xử lý    │
         └───────────┬───────────┘
                     │
            ┌────────┴────────┐
            │                 │
         Lỗi do              Lỗi thật
       hiểu nhầm          cần sửa
            │                 │
            ▼                 ▼
    ┌──────────────┐   ┌──────────────────┐
    │ Phản hồi SV  │   │ Mở Revision      │
    │ (REJECTED)   │   │ Session (OPEN)   │
    └──────────────┘   └────────┬─────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │ Lecturer chỉnh sửa   │
                    │ (REVISION_IN_PROGRESS)│
                    └────────┬─────────────┘
                             │
                             ▼
                    ┌──────────────────────┐
                    │ Submit to HoD        │
                    │ (PENDING_HOD_REVISION)│
                    └────────┬─────────────┘
                             │
                    ┌────────┴─────────┐
                    │                  │
                 Reject             Approve
                    │                  │
                    ▼                  ▼
        ┌──────────────────┐   ┌──────────────────────┐
        │ Back to Lecturer │   │ Pending Admin        │
        │ (REVISION_IN_    │   │ Republish            │
        │  PROGRESS)       │   │ (PENDING_ADMIN_      │
        └──────────────────┘   │  REPUBLISH)          │
                               └────────┬─────────────┘
                                        │
                                        ▼
                               ┌──────────────────┐
                               │ Admin Republish  │
                               │ (PUBLISHED)      │
                               └────────┬─────────┘
                                        │
                                        ▼
                               ┌──────────────────┐
                               │ Thông báo        │
                               │ sinh viên        │
                               │ (Đã cập nhật)    │
                               └──────────────────┘
```

---

## 🔄 CÁC TRẠNG THÁI CHÍNH

### **Syllabus Status**

| Status | Tên tiếng Việt | Ý nghĩa |
|--------|----------------|---------|
| `PUBLISHED` | Đã xuất bản | Trạng thái ổn định, sinh viên đang xem |
| `REVISION_IN_PROGRESS` | Đang chỉnh sửa | Lecturer đang sửa theo feedback |
| `PENDING_HOD_REVISION` | Chờ TBM duyệt lại | Đã submit, chờ HoD review |
| `PENDING_ADMIN_REPUBLISH` | Chờ xuất bản lại | HoD đã duyệt, chờ admin publish |

### **Feedback Status**

| Status | Tên tiếng Việt | Ý nghĩa |
|--------|----------------|---------|
| `PENDING` | Chờ xử lý | Feedback mới, chưa ai xem |
| `IN_REVIEW` | Đang xem xét | Admin đang xem xét |
| `AWAITING_REVISION` | Chờ chỉnh sửa | Admin đã approve, chờ lecturer sửa |
| `IN_REVISION` | Đang chỉnh sửa | Đang được xử lý trong revision session |
| `RESOLVED` | Đã giải quyết | Đã fix và xuất bản lại |
| `REJECTED` | Từ chối | Admin từ chối (không phải lỗi) |

### **Revision Session Status**

| Status | Tên tiếng Việt | Ý nghĩa |
|--------|----------------|---------|
| `OPEN` | Đang mở | Admin vừa mở, đang thu thập feedback |
| `IN_PROGRESS` | Đang xử lý | Lecturer đang sửa |
| `PENDING_HOD` | Chờ TBM duyệt | Đã submit cho HoD |
| `COMPLETED` | Hoàn thành | HoD đã duyệt, chờ admin publish |
| `CANCELLED` | Đã hủy | Session bị hủy |

---

## 📝 LUỒNG CHI TIẾT TỪNG BƯỚC

### **BƯỚC 1: Sinh viên gửi feedback**

**Điều kiện:**
- Đề cương ở trạng thái `PUBLISHED`
- Sinh viên đã đăng nhập

**Action:**
```typescript
// Frontend
const feedback = await feedbackService.createFeedback({
  syllabusId: 'uuid',
  type: 'ERROR', // ERROR, SUGGESTION, QUESTION, OTHER
  title: 'Sai chính tả chương 3',
  description: 'Phần CLO 3.2 bị lỗi chính tả...',
  section: 'CLO', // CLO, PLO, ASSESSMENT, etc.
});
```

**API:**
```
POST /api/student-feedbacks
Body: {
  syllabusId: UUID,
  type: FeedbackType,
  title: string,
  description: string,
  section: string
}
```

**Kết quả:**
- Feedback được tạo với status = `PENDING`
- Admin nhận được thông báo (nếu có)

---

### **BƯỚC 2: Admin xem và quyết định**

**Điều kiện:**
- User có role `ADMIN`
- Feedback ở trạng thái `PENDING`

**Option 1: Lỗi do hiểu nhầm → Phản hồi trực tiếp**
```typescript
// Frontend
await feedbackService.respondToFeedback(feedbackId, {
  response: 'Đây không phải lỗi. PLO 3.2 được mapping đúng theo quy định...',
  enableEdit: false
});
```

**API:**
```
POST /api/student-feedbacks/{id}/respond
Body: {
  response: string,
  enableEdit: false
}
```

**Kết quả:**
- Feedback status → `REJECTED`
- Sinh viên nhận thông báo với phản hồi

**Option 2: Lỗi thật → Mở revision session**
```typescript
// Frontend
const session = await revisionService.startRevision({
  syllabusVersionId: 'uuid',
  feedbackIds: ['uuid1', 'uuid2', 'uuid3'], // Gom nhiều feedback vào 1 đợt
  description: 'Sửa lỗi chính tả và PLO mapping'
});
```

**API:**
```
POST /api/revisions/start
Body: {
  syllabusVersionId: UUID,
  feedbackIds: UUID[],
  description?: string
}
```

**Kết quả:**
- Tạo `RevisionSession` với status = `OPEN`
- Feedbacks được gán vào session, status → `AWAITING_REVISION`
- Syllabus status → `REVISION_IN_PROGRESS`
- Lecturer nhận notification với danh sách lỗi cần sửa

---

### **BƯỚC 3: Lecturer chỉnh sửa**

**Điều kiện:**
- User là lecturer được assign
- Revision session ở trạng thái `OPEN` hoặc `IN_PROGRESS`

**Action:**
1. Lecturer vào trang edit syllabus
2. Xem danh sách feedback trong session
3. Chỉnh sửa nội dung đề cương
4. Submit revision

```typescript
// Frontend
await revisionService.submitRevision({
  revisionSessionId: 'uuid',
  summary: 'Đã sửa: 1) Chính tả chương 3, 2) PLO mapping 3.2, 3) Assessment breakdown'
});
```

**API:**
```
POST /api/revisions/submit
Body: {
  revisionSessionId: UUID,
  summary?: string
}
```

**Kết quả:**
- Revision session status → `PENDING_HOD`
- Syllabus status → `PENDING_HOD_REVISION`
- Feedbacks status → `IN_REVISION`
- HoD nhận notification

---

### **BƯỚC 4: HoD duyệt revision**

**Điều kiện:**
- User có role `HOD`
- Revision session ở trạng thái `PENDING_HOD`

**Option 1: Duyệt (Approve)**
```typescript
// Frontend
await revisionService.reviewRevision({
  revisionSessionId: 'uuid',
  decision: 'APPROVED',
  comment: 'Đã kiểm tra, phiên bản chỉnh sửa đạt yêu cầu'
});
```

**API:**
```
POST /api/revisions/review
Body: {
  revisionSessionId: UUID,
  decision: 'APPROVED',
  comment?: string
}
```

**Kết quả:**
- Revision session status → `COMPLETED`
- Syllabus status → `PENDING_ADMIN_REPUBLISH`
- Admin nhận notification

**Option 2: Từ chối (Reject)**
```typescript
// Frontend
await revisionService.reviewRevision({
  revisionSessionId: 'uuid',
  decision: 'REJECTED',
  comment: 'Cần bổ sung thêm chi tiết phần rubric scoring'
});
```

**API:**
```
POST /api/revisions/review
Body: {
  revisionSessionId: UUID,
  decision: 'REJECTED',
  comment: string
}
```

**Kết quả:**
- Revision session status → `IN_PROGRESS`
- Syllabus status → `REVISION_IN_PROGRESS`
- Lecturer nhận notification với lý do từ chối
- Lecturer có thể tiếp tục chỉnh sửa

---

### **BƯỚC 5: Admin xuất bản lại**

**Điều kiện:**
- User có role `ADMIN`
- Revision session ở trạng thái `COMPLETED`
- Syllabus status = `PENDING_ADMIN_REPUBLISH`

**Action:**
```typescript
// Frontend
await revisionService.republishSyllabus(sessionId);
```

**API:**
```
POST /api/revisions/{sessionId}/republish
```

**Kết quả:**
- **Snapshot cũ được lưu vào history:**
  - Nội dung version cũ → `syllabus_version_history`
  - `snapshot_reason` = "BEFORE_REPUBLISH"

- **Cập nhật syllabus version:**
  - Status → `PUBLISHED`
  - Version number: 1 → 2
  - Version no: "V1.0" → "V2.0"

- **Cập nhật feedbacks:**
  - Status → `RESOLVED`
  - `resolved_in_version_id` = current version
  - `resolved_in_version_no` = "V2.0"
  - `resolved_by` = admin
  - `resolved_at` = now

- **Gửi notification:**
  - Tất cả sinh viên có feedback được fix → nhận thông báo
  - Nội dung: "Đề cương đã cập nhật dựa trên phản hồi của bạn"

---

## 🔌 API ENDPOINTS

### **Revision Management**

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/revisions/start` | ADMIN | Mở revision session |
| POST | `/api/revisions/submit` | LECTURER | Submit revision cho HoD |
| POST | `/api/revisions/review` | HOD | Duyệt/từ chối revision |
| POST | `/api/revisions/{id}/republish` | ADMIN | Xuất bản lại |
| GET | `/api/revisions/pending-hod` | HOD | List đang chờ duyệt |
| GET | `/api/revisions/pending-republish` | ADMIN | List chờ publish |

### **Feedback Management**

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/student-feedbacks` | STUDENT | Tạo feedback |
| POST | `/api/student-feedbacks/{id}/respond` | ADMIN | Phản hồi feedback |
| GET | `/api/student-feedbacks` | ADMIN | List tất cả feedback |
| GET | `/api/student-feedbacks/status/{status}` | ADMIN | Filter by status |
| GET | `/api/student-feedbacks/syllabus/{id}` | ALL | Feedback của 1 syllabus |

---

## 💾 DATABASE SCHEMA

### **Table: revision_sessions**

```sql
CREATE TABLE revision_sessions (
    id UUID PRIMARY KEY,
    syllabus_version_id UUID NOT NULL,
    session_number INTEGER NOT NULL,
    status revision_session_status NOT NULL,
    
    initiated_by UUID NOT NULL,
    initiated_at TIMESTAMP NOT NULL,
    description TEXT,
    
    assigned_lecturer_id UUID,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    hod_reviewed_by UUID,
    hod_reviewed_at TIMESTAMP,
    hod_decision VARCHAR(20),
    hod_comment TEXT,
    
    republished_by UUID,
    republished_at TIMESTAMP,
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### **Table: syllabus_error_reports (Updated)**

```sql
ALTER TABLE syllabus_error_reports 
ADD COLUMN revision_session_id UUID,
ADD COLUMN resolved_in_version_id UUID,
ADD COLUMN resolved_in_version_no VARCHAR(20);
```

### **Table: syllabus_version_history**

```sql
CREATE TABLE syllabus_version_history (
    id UUID PRIMARY KEY,
    syllabus_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    version_no VARCHAR(20) NOT NULL,
    status syllabus_status NOT NULL,
    content JSONB,
    snapshot_reason VARCHAR(100),
    created_at TIMESTAMP,
    created_by UUID
);
```

---

## 🎨 FRONTEND IMPLEMENTATION GUIDE

### **1. Admin Dashboard - Feedback Management**

**Component: `AdminFeedbackList.vue`**

```typescript
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { feedbackService, revisionService } from '@/services';

const feedbacks = ref<StudentFeedback[]>([]);
const selectedFeedbacks = ref<string[]>([]);

const loadPendingFeedbacks = async () => {
  feedbacks.value = await feedbackService.getFeedbacks({
    status: [FeedbackStatus.PENDING, FeedbackStatus.IN_REVIEW]
  });
};

const handleStartRevision = async () => {
  if (selectedFeedbacks.value.length === 0) {
    alert('Vui lòng chọn ít nhất 1 feedback');
    return;
  }
  
  const syllabusId = feedbacks.value[0].syllabusId;
  
  await revisionService.startRevision({
    syllabusVersionId: syllabusId,
    feedbackIds: selectedFeedbacks.value,
    description: 'Batch fix'
  });
  
  alert('Đã mở revision session!');
  await loadPendingFeedbacks();
};

onMounted(() => {
  loadPendingFeedbacks();
});
</script>
```

### **2. Lecturer - Revision Editor**

**Component: `LecturerRevisionEditor.vue`**

```typescript
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { revisionService } from '@/services';

const route = useRoute();
const sessionId = ref(route.params.sessionId as string);
const session = ref<RevisionSession | null>(null);
const feedbacks = ref<StudentFeedback[]>([]);

const loadSession = async () => {
  // Load session data
  // Load feedbacks for this session
};

const submitRevision = async () => {
  await revisionService.submitRevision({
    revisionSessionId: sessionId.value
  });
  
  alert('Đã gửi revision cho TBM duyệt!');
};
</script>
```

### **3. HoD - Revision Approval**

**Component: `HodRevisionApproval.vue`**

```typescript
<script setup lang="ts">
import { ref } from 'vue';
import { revisionService } from '@/services';

const pendingSessions = ref<RevisionSession[]>([]);

const loadPendingSessions = async () => {
  pendingSessions.value = await revisionService.getPendingHodReview();
};

const handleApprove = async (sessionId: string) => {
  await revisionService.reviewRevision({
    revisionSessionId: sessionId,
    decision: 'APPROVED',
    comment: 'Đạt yêu cầu'
  });
  
  alert('Đã duyệt!');
  await loadPendingSessions();
};

const handleReject = async (sessionId: string, comment: string) => {
  await revisionService.reviewRevision({
    revisionSessionId: sessionId,
    decision: 'REJECTED',
    comment
  });
  
  alert('Đã từ chối!');
  await loadPendingSessions();
};
</script>
```

### **4. Admin - Republish**

**Component: `AdminRepublish.vue`**

```typescript
<script setup lang="ts">
import { ref } from 'vue';
import { revisionService } from '@/services';

const pendingRepublish = ref<RevisionSession[]>([]);

const loadPendingRepublish = async () => {
  pendingRepublish.value = await revisionService.getPendingRepublish();
};

const handleRepublish = async (sessionId: string) => {
  if (!confirm('Xác nhận xuất bản lại?')) return;
  
  await revisionService.republishSyllabus(sessionId);
  
  alert('Đã xuất bản lại! Sinh viên sẽ nhận được thông báo.');
  await loadPendingRepublish();
};
</script>
```

---

## 📧 NOTIFICATION STRUCTURE

### **1. Lecturer - Revision Requested**

```json
{
  "title": "[Yêu cầu chỉnh sửa] Đề cương IT4501 - Lập trình Web",
  "message": "Admin đã phát hiện 3 lỗi cần chỉnh sửa...",
  "type": "ERROR_REPORT",
  "payload": {
    "syllabusId": "uuid",
    "revisionSessionId": "uuid",
    "feedbackCount": 3,
    "actionUrl": "/lecturer/syllabi/{id}/edit",
    "actionLabel": "Chỉnh sửa ngay",
    "priority": "HIGH"
  }
}
```

### **2. HOD - Revision Submitted**

```json
{
  "title": "[Chờ duyệt] Đề cương đã chỉnh sửa: IT4501",
  "message": "Giảng viên Nguyễn Văn A đã hoàn thành chỉnh sửa...",
  "type": "APPROVAL",
  "payload": {
    "syllabusId": "uuid",
    "revisionSessionId": "uuid",
    "lecturerName": "Nguyễn Văn A",
    "feedbackCount": 3,
    "actionUrl": "/hod/approvals/{id}",
    "actionLabel": "Xem và duyệt",
    "priority": "HIGH"
  }
}
```

### **3. Admin - Revision Approved**

```json
{
  "title": "[Chờ xuất bản lại] Đề cương IT4501 đã được TBM duyệt",
  "message": "Trưởng bộ môn đã phê duyệt phiên bản chỉnh sửa...",
  "type": "PUBLICATION",
  "payload": {
    "syllabusId": "uuid",
    "revisionSessionId": "uuid",
    "actionUrl": "/admin/syllabi/{id}/republish",
    "actionLabel": "Xuất bản ngay",
    "priority": "MEDIUM"
  }
}
```

### **4. Student - Syllabus Updated**

```json
{
  "title": "[Cập nhật] Đề cương IT4501 đã được cập nhật",
  "message": "Đề cương môn học đã được cập nhật dựa trên phản hồi của sinh viên...",
  "type": "PUBLICATION",
  "payload": {
    "syllabusId": "uuid",
    "newVersionNo": "V2.0",
    "changesSummary": "3 Báo lỗi, 1 Đề xuất",
    "yourFeedbackResolved": true,
    "actionUrl": "/student/syllabi/{id}",
    "actionLabel": "Xem ngay",
    "priority": "MEDIUM"
  }
}
```

---

## ✅ CHECKLIST IMPLEMENTATION

### Backend
- [x] Migration V47 - revision_sessions table
- [x] RevisionSession entity và repository
- [x] Update FeedbackStatus enum (AWAITING_REVISION, IN_REVISION)
- [x] Update SyllabusErrorReport entity (revision_session_id, resolved_in_version)
- [x] NotificationService
- [x] RevisionService (start, submit, review, republish)
- [x] RevisionController API endpoints

### Frontend
- [x] Types: RevisionSession, FeedbackStatus updates
- [x] Services: revisionService
- [ ] Components: AdminFeedbackManagement
- [ ] Components: LecturerRevisionEditor
- [ ] Components: HodRevisionApproval
- [ ] Components: AdminRepublish
- [ ] Components: StudentFeedbackForm

### Testing
- [ ] Unit tests cho RevisionService
- [ ] Integration tests cho revision workflow
- [ ] E2E test cho full flow
- [ ] Load test với multiple concurrent revisions

---

## 🚀 DEPLOYMENT CHECKLIST

1. **Database Migration**
   ```bash
   # Run migration V47
   docker exec -it smd-postgres psql -U smd_user -d smd_database
   # Check if migration applied
   SELECT * FROM flyway_schema_history WHERE version = '47';
   ```

2. **Backend Compile**
   ```bash
   cd backend/core-service
   mvn clean install
   ```

3. **Frontend Build**
   ```bash
   cd frontend
   npm run build
   ```

4. **Verify APIs**
   - Test POST /api/revisions/start
   - Test POST /api/revisions/submit
   - Test POST /api/revisions/review
   - Test POST /api/revisions/{id}/republish

5. **Notification Testing**
   - Verify lecturer receives notification
   - Verify HoD receives notification
   - Verify admin receives notification
   - Verify students receive notification

---

## 📞 SUPPORT & CONTACT

Nếu có thắc mắc về implementation, liên hệ:
- **Tech Lead**: [Tên người phụ trách]
- **Backend Team**: [Contact]
- **Frontend Team**: [Contact]

---

**Document Version**: 1.0
**Last Updated**: 2026-01-16
**Author**: AI Assistant + Development Team
