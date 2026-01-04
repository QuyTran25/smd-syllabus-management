# 📋 KẾ HOẠCH TRIỂN KHAI CHI TIẾT - RABBITMQ & AI INTEGRATION

**Ngày tạo:** 04/01/2026  
**Người phụ trách:** Leader - Infrastructure & Connectivity  
**Mục tiêu:** Thiết lập RabbitMQ & Cấu trúc Message chung cho giao tiếp async giữa Core Service (Java) và AI Service (Python)

---

## 🎯 **MỤC TIÊU TỔNG QUAN**

Tích hợp RabbitMQ để Core Service (Java) giao tiếp async với AI Service (Python) cho **3 chức năng AI**:

1. **Tóm tắt đề cương** (SUMMARIZE_SYLLABUS) - Cho sinh viên
2. **So sánh phiên bản** (COMPARE_VERSIONS) - Cho AA/HoD  
3. **Kiểm tra tuân thủ CLO-PLO** (MAP_CLO_PLO) - Cho AA/Principal

### **Nguyên tắc thiết kế:**
- ❌ **KHÔNG** cài đặt HTTP Route cho AI Service (AI chạy ẩn hoàn toàn sau Queue)
- ✅ Core Service đẩy `syllabus_id` vào Queue, AI Service xử lý xong ghi kết quả vào DB
- ✅ Frontend sẽ **fetch kết quả AI từ DB** (polling) để tránh đợi lâu
- ✅ Thông báo Firebase sẽ làm sau

---

## 🏗️ **KIẾN TRÚC TỔNG QUAN**

### **Infrastructure đã có sẵn ✅**

```yaml
RabbitMQ:
  Image: rabbitmq:3.12-management-alpine
  Ports:
    - 5672:5672   # AMQP
    - 15672:15672 # Management UI
  Network: smd-network
  Volume: ./infrastructure/rabbitmq/definitions.json

Database (PostgreSQL):
  Schema: ai_service
  Tables:
    - ai_jobs (job_type enum: SUMMARIZE_SYLLABUS, DIFF_VERSIONS, VALIDATE_PLO_MAPPING)
    - syllabus_ai_analysis (analysis_type enum: SUMMARY, PLO_ALIGNMENT, VERSION_DIFF)
    - syllabus_ai_recommendation

Redis:
  Port: 6379
  Usage: Cache task status cho polling nhanh

Kafka:
  Usage: Audit logs (không dùng cho AI messaging)
```

### **Queues Strategy - 2 QUEUES RIÊNG BIỆT**

```yaml
Queue 1: ai_processing_queue
  Purpose: Cho AA/HoD/Principal (realtime)
  Actions: 
    - COMPARE_VERSIONS (priority: MEDIUM)
    - MAP_CLO_PLO (priority: HIGH)
  Priority: 1-5 (5 = highest)
  Routing Key: ai.process

Queue 2: ai_summarize_queue [CẦN THÊM MỚI]
  Purpose: Cho sinh viên (background, có thể pre-generate)
  Actions:
    - SUMMARIZE_SYLLABUS (priority: LOW)
  Priority: 1-3
  Routing Key: ai.summarize

Queue 3: ai_result_queue
  Purpose: Optional - callback notification
  Routing Key: ai.result

Queue 4: notification_queue
  Purpose: FCM notifications (làm sau)
  Routing Key: notification.*
```

### **Lý do tách 2 queues:**
- ✅ **Analysis queue** cần xử lý ngay (người dùng đang đợi)
- ✅ **Summarize queue** có thể chạy background, không ảnh hưởng tốc độ hệ thống
- ✅ Tách biệt user flow: AA/HoD vs Student

---

## 📨 **CẤU TRÚC MESSAGE CHUẨN**

### **Nguyên tắc quan trọng:**
- ✅ Message structure **CỐ ĐỊNH** (dễ parse)
- ✅ Message content **THAY ĐỔI** theo từng đề cương
- ❌ **KHÔNG** gửi full content trong message (chỉ gửi ID)
- ✅ AI Service sẽ **query DB** để lấy data theo ID

### **Message Request (Java → RabbitMQ)**

```json
{
  "message_id": "uuid-v4",
  "action": "SUMMARIZE_SYLLABUS | COMPARE_VERSIONS | MAP_CLO_PLO",
  "priority": "HIGH | MEDIUM | LOW",
  "timestamp": "2026-01-04T15:00:00Z",
  "user_id": "uuid-of-requester",
  "payload": {
    // Khác nhau theo action, CHỈ chứa IDs, KHÔNG chứa full data
  }
}
```

### **1. SUMMARIZE_SYLLABUS (Cho Sinh viên)**

**Request:**
```json
{
  "message_id": "sum-001",
  "action": "SUMMARIZE_SYLLABUS",
  "priority": "LOW",
  "timestamp": "2026-01-04T15:00:00Z",
  "payload": {
    "syllabus_id": "uuid",
    "language": "vi",
    "include_prerequisites": true
  }
}
```

**Response Structure (Lưu vào DB - `ai_service.syllabus_ai_analysis`):**
```json
{
  "message_id": "sum-001",
  "action": "SUMMARIZE_SYLLABUS",
  "status": "SUCCESS",
  "result": {
    "overview": {
      "title": "Thiết kế và tối ưu hóa CSDL",
      "description": "Môn học trang bị kiến thức về..."
    },
    "highlights": {
      "difficulty": {
        "level": "MEDIUM",
        "description": "Trung bình - Phù hợp sinh viên năm 2-3"
      },
      "duration": {
        "theory_hours": 30,
        "practice_hours": 30,
        "total_hours": 60,
        "description": "30 lý thuyết + 30 tiết thực hành"
      },
      "assessment": {
        "summary": "Cân bằng giữa thi và bài tập/dự án",
        "breakdown": [
          { "type": "Thi giữa kỳ", "weight": 30 },
          { "type": "Bài tập", "weight": 20 },
          { "type": "Dự án", "weight": 20 },
          { "type": "Thi cuối kỳ", "weight": 30 }
        ]
      },
      "skills_acquired": {
        "summary": "Ánh xạ CLO tới PLO rõ ràng",
        "key_skills": [
          "Thiết kế ERD và chuẩn hóa CSDL",
          "Viết truy vấn SQL phức tạp",
          "Tối ưu hiệu năng database"
        ]
      }
    },
    "recommendations": {
      "prerequisites": {
        "required": ["Cấu trúc dữ liệu", "OOP"],
        "description": "Nên có kiến thức cơ bản về các môn tiên quyết"
      },
      "preparation": {
        "tips": [
          "Ôn lại kiến thức nền về cấu trúc dữ liệu",
          "Làm quen với SQL cơ bản"
        ],
        "description": "Chuẩn bị trước: Ôn lại kiến thức nền"
      },
      "study_time": {
        "hours_per_week": 6,
        "breakdown": "4 giờ làm bài tập + 2 giờ đọc tài liệu",
        "description": "Dành ít nhất 6 giờ/tuần"
      }
    }
  },
  "processing_time_ms": 3200
}
```

**UI Frontend cần hiển thị:**
- **Tổng quan:** Mô tả môn học
- **Điểm nổi bật:** Độ khó, thời lượng, đánh giá, kỹ năng đạt được
- **Khuyến nghị:** Tiên quyết, chuẩn bị, thời gian tự học

---

### **2. MAP_CLO_PLO (Kiểm tra tuân thủ PLO)**

**Request:**
```json
{
  "message_id": "map-002",
  "action": "MAP_CLO_PLO",
  "priority": "HIGH",
  "timestamp": "2026-01-04T15:05:00Z",
  "user_id": "aa-user-uuid",
  "payload": {
    "syllabus_id": "uuid",
    "curriculum_id": "uuid",
    "strict_mode": true,
    "check_weights": true
  }
}
```

**Response Structure:**
```json
{
  "message_id": "map-002",
  "action": "MAP_CLO_PLO",
  "status": "SUCCESS",
  "result": {
    "overall_status": "NEEDS_IMPROVEMENT | COMPLIANT",
    "compliance_score": 75.5,
    
    "issues": [
      {
        "severity": "HIGH | MEDIUM | LOW",
        "type": "MISSING_PLO_MAPPING | INSUFFICIENT_WEIGHT",
        "code": "PLO2",
        "title": "PLO2: CLO chưa ánh xạ đủ sang PLO2 (yêu cầu tối thiểu 2 CLO)",
        "description": "Hiện tại chỉ có 1 CLO ánh xạ sang PLO2",
        "current_count": 1,
        "required_count": 2,
        "affected_clos": ["CLO-1"]
      }
    ],
    
    "suggestions": [
      {
        "priority": 1,
        "action": "ADD_CLO | ADJUST_WEIGHT | REVIEW_CONSISTENCY",
        "title": "Thêm CLO về kỹ năng phân tích dữ liệu ứng PLO2",
        "description": "Ví dụ: 'Sinh viên có khả năng phân tích...'"
      }
    ],
    
    "compliant_mappings": [
      {
        "plo_code": "PLO1",
        "mapped_clos": ["CLO-1", "CLO-2", "CLO-3"],
        "total_weight": 45,
        "status": "GOOD"
      }
    ]
  }
}
```

**UI Frontend cần hiển thị:**
- Trạng thái tổng quan (màu đỏ/xanh)
- **Vấn đề phát hiện:** Danh sách issues với severity colors
- **Đề xuất cải thiện:** Prioritized suggestions
- Nếu tuân thủ tốt → Badge "Tuân thủ tốt ✓"

---

### **3. COMPARE_VERSIONS (So sánh phiên bản)**

**Request:**
```json
{
  "message_id": "cmp-003",
  "action": "COMPARE_VERSIONS",
  "priority": "MEDIUM",
  "timestamp": "2026-01-04T15:10:00Z",
  "user_id": "hod-user-uuid",
  "payload": {
    "subject_id": "uuid",
    "old_version_id": "v1-uuid",
    "new_version_id": "v2-uuid",
    "comparison_depth": "DETAILED | SUMMARY"
  }
}
```

**Response Structure:**
```json
{
  "message_id": "cmp-003",
  "action": "COMPARE_VERSIONS",
  "status": "SUCCESS",
  "result": {
    "is_first_version": false,
    
    "version_history": [
      {
        "version_number": "NaN",
        "status": "Hiện tại",
        "created_by": "Trần Thị Lan",
        "created_at": "02/01/2026 08:24",
        "is_current": true
      }
    ],
    
    "changes_summary": {
      "total_changes": 3,
      "major_changes": 2,
      "minor_changes": 1,
      "sections_affected": ["learning_outcomes", "assessment_scheme"]
    },
    
    "detailed_changes": [
      {
        "section": "learning_outcomes",
        "section_title": "Mục tiêu học tập",
        "change_type": "MODIFIED | ADDED | DELETED",
        "changes": [
          {
            "field": "CLO 1",
            "old_value": "Sinh viên hiểu các khái niệm...",
            "new_value": "Sinh viên nắm vững và áp dụng được...",
            "significance": "HIGH | MEDIUM | LOW",
            "impact": "Tăng mức độ yêu cầu từ 'hiểu' lên 'áp dụng'"
          }
        ]
      }
    ],
    
    "ai_analysis": {
      "overall_assessment": "Phiên bản mới có cải thiện đáng kể...",
      "key_improvements": [
        "CLO được nâng cấp từ mức độ 'hiểu' lên 'áp dụng'"
      ],
      "recommendations": [
        "Cân nhắc bổ sung rubric chi tiết..."
      ]
    }
  }
}
```

**UI Frontend cần hiển thị:**
- Nếu `is_first_version: true` → "Đây là phiên bản đầu tiên chưa có sự thay đổi"
- **Lịch sử phiên bản:** Timeline với created_by, created_at
- **Thay đổi chính:** Grouped by sections
- **Phân tích AI:** Overall assessment + recommendations

---

## 🔄 **CƠ CHẾ POLLING (Không dùng WebSocket)**

### **Lý do chọn Polling:**
- ✅ Đơn giản, dễ implement và debug
- ✅ Stateless, dễ scale
- ✅ Đủ tốt cho quy mô trường học (50-100 users đồng thời)
- ✅ AI processing time: 3-10 giây → Chỉ 2-5 requests polling
- ✅ Có thể upgrade lên WebSocket sau nếu cần

### **Smart Polling Strategy - Exponential Backoff:**

```typescript
Polling Intervals:
  - First 5s:  Poll every 1s   (nhanh để user thấy responsive)
  - 5-15s:     Poll every 2s   (cân bằng)
  - 15-30s:    Poll every 5s   (giảm tải)
  - 30s+:      TIMEOUT         (show error, user retry)
```

### **Workflow chi tiết:**

```
1. User click "Kiểm tra CLO-PLO"
   ↓
2. Frontend: POST /api/ai/syllabus/{id}/check-clo-plo
   Response: { "task_id": "abc-123", "status": "QUEUED" }
   ↓
3. Core Service:
   - Tạo message với action="MAP_CLO_PLO"
   - Lưu task status vào Redis: { status: "QUEUED", progress: 0 }
   - Gửi vào RabbitMQ queue (ai_processing_queue)
   - Return task_id cho Frontend
   ↓
4. Frontend:
   - Hiện notification góc dưới phải (KHÔNG block UI)
   - Bắt đầu polling: GET /api/tasks/{task_id}/status mỗi 1-5s
   - User VẪN dùng được UI, edit đề cương, xem tab khác
   ↓
5. AI Service (Python Worker):
   - Lắng nghe queue priority-based
   - Nhận message, bắt đầu xử lý
   - Update progress: 25% → 50% → 75% → 100%
   - Ghi kết quả vào DB: ai_service.syllabus_ai_analysis
   ↓
6. Frontend polling thấy status="SUCCESS":
   - Update notification → "Phân tích hoàn tất! [Xem kết quả →]"
   - User click → Hiển thị modal với results
```

---

## 🎨 **UX STRATEGIES ĐỂ GIẢM CẢM GIÁC CHỜ ĐỢI**

### **Chiến lược 1: ASYNC WORKFLOW (Quan trọng nhất!)**
- ❌ Không dùng Modal block toàn bộ UI
- ✅ Notification góc dưới phải, user vẫn làm việc khác
- ✅ Khi xong → Notification cập nhật, user click xem khi tiện

### **Chiến lược 2: PROGRESSIVE DISCLOSURE**
- Hiển thị từng phần kết quả khi có sẵn:
  - Step 1: Đã đọc CLOs (1s) → Hiển thị ngay
  - Step 2: Đã đọc PLOs (2s) → Hiển thị ngay
  - Step 3: AI analysis (5s) → Hiển thị final results
- User thấy progress liên tục → Cảm giác nhanh hơn nhiều!

### **Chiến lược 3: SKELETON LOADING + TIMELINE**
```tsx
<Timeline>
  <Timeline.Item color="green" dot={<CheckCircleOutlined />}>
    ✓ Đang đọc CLOs và PLOs... (1-2s)
  </Timeline.Item>
  <Timeline.Item color="blue" dot={<LoadingOutlined />}>
    🔄 AI đang phân tích mức độ phù hợp... (~3-5s)
  </Timeline.Item>
  <Timeline.Item color="gray" dot={<ClockCircleOutlined />}>
    ⏳ Tạo đề xuất cải thiện... (1-2s)
  </Timeline.Item>
</Timeline>

<Alert type="info" message="💡 Bạn có biết?">
  CLO nên được viết theo động từ hành động của Bloom's Taxonomy
</Alert>
```

### **Chiến lược 4: PROGRESS BAR + ESTIMATED TIME**
```tsx
<Progress percent={estimatedProgress} status="active" />
<span>Còn khoảng {remaining} giây...</span>
<span>{elapsed}s / ~{estimatedTotal}s</span>
```

### **Chiến lược 5: ENTERTAINING LOADING**
- Brain thinking animation (Lottie)
- Fun facts rotation mỗi 2s
- "🤔 Đang đọc CLO..." → "🧠 Phân tích Bloom..." → "✨ Hoàn thiện..."

### **Chiến lược 6: BACKGROUND TASK LIST**
- FloatButton góc dưới phải
- Badge hiển thị số tasks đang chạy
- User click → Xem tất cả tasks, progress, results

---

## 📁 **CẤU TRÚC CODE DỰ KIẾN**

```
backend/
├── core-service/
│   ├── src/main/java/vn/edu/smd/core/
│   │   ├── config/
│   │   │   └── RabbitMQConfig.java          [MỚI]
│   │   ├── service/
│   │   │   └── AITaskService.java           [MỚI]
│   │   ├── controller/
│   │   │   └── AIAnalysisController.java    [MỚI]
│   │   └── ...
│   └── pom.xml (đã có spring-boot-starter-amqp ✅)
│
├── ai-service/
│   ├── app/
│   │   ├── workers/
│   │   │   ├── rabbitmq_consumer.py         [MỚI]
│   │   │   ├── ai_handlers.py               [MỚI]
│   │   │   ├── analysis_worker.py           [MỚI]
│   │   │   └── summarize_worker.py          [MỚI]
│   │   ├── services/
│   │   │   ├── summarize_service.py         [MỚI]
│   │   │   ├── compare_service.py           [MỚI]
│   │   │   └── clo_plo_service.py           [MỚI]
│   │   └── ...
│   └── requirements.txt (thêm pika)
│
├── shared/
│   └── java-common/
│       └── dto/ai/
│           ├── AIMessageRequest.java        [MỚI]
│           └── AIMessageResponse.java       [MỚI]

frontend/
├── src/
│   ├── services/
│   │   └── aiService.ts                     [MỚI]
│   ├── hooks/
│   │   └── useTaskPolling.ts                [MỚI]
│   └── components/ai/
│       ├── CloPloCheckButton.tsx            [MỚI]
│       ├── CloPloResult.tsx                 [MỚI]
│       ├── CompareVersionsButton.tsx        [MỚI]
│       ├── SummarizeButton.tsx              [MỚI]
│       ├── AIThinkingAnimation.tsx          [MỚI]
│       └── BackgroundTaskPanel.tsx          [MỚI]

infrastructure/
└── rabbitmq/
    └── definitions.json (cập nhật thêm queue)
```

---

## ✅ **CHECKLIST TRIỂN KHAI**

### **Phase 1: Cấu hình RabbitMQ**
- [ ] Cập nhật `definitions.json` với `ai_summarize_queue`
- [ ] Thêm binding cho routing key `ai.summarize`
- [ ] Test RabbitMQ UI: http://localhost:15672
- [ ] Verify 4 queues tồn tại

### **Phase 2: Database**
- [ ] Verify enum types trong `ai_service` schema
- [ ] Đảm bảo có đủ 3 job types
- [ ] Test query tables: ai_jobs, syllabus_ai_analysis

### **Phase 3: Core Service (Java)**
- [ ] Tạo `RabbitMQConfig.java` với exchanges, queues, bindings
- [ ] Tạo DTOs: `AIMessageRequest`, `AIMessageResponse`
- [ ] Implement `AITaskService` với 3 methods
- [ ] Implement `AIAnalysisController` với REST endpoints
- [ ] Test gửi message vào queue (check RabbitMQ UI)
- [ ] Redis cache integration cho task status

### **Phase 4: AI Service (Python)**
- [ ] Add `pika` vào requirements.txt
- [ ] Tạo `RabbitMQConsumer` base class
- [ ] Implement `AIMessageHandler` với routing logic
- [ ] Tạo 2 workers: `analysis_worker.py`, `summarize_worker.py`
- [ ] Implement 3 services:
  - [ ] `SummarizeService` (LLM-based)
  - [ ] `CompareService` (semantic diff)
  - [ ] `CloPloService` (validation logic)
- [ ] Test consume message từ queue
- [ ] Test ghi kết quả vào DB

### **Phase 5: Frontend (React)**
- [ ] Tạo `aiService.ts` API client
- [ ] Implement `useTaskPolling` hook với exponential backoff
- [ ] Component: `CloPloCheckButton` với notification
- [ ] Component: `CloPloResult` modal
- [ ] Component: `CompareVersionsButton`
- [ ] Component: `SummarizeButton` (cho student portal)
- [ ] Component: `AIThinkingAnimation`
- [ ] Component: `BackgroundTaskPanel`
- [ ] Test end-to-end flow

### **Phase 6: Testing**
- [ ] Unit tests cho Java Producer
- [ ] Unit tests cho Python Consumer
- [ ] Integration test: Java → RabbitMQ → Python → DB
- [ ] E2E test: Frontend → Backend → AI → Result
- [ ] Timeout handling test
- [ ] Error scenarios test

### **Phase 7: Monitoring**
- [ ] RabbitMQ metrics (queue length, message rate)
- [ ] AI processing time tracking
- [ ] Success/failure rate
- [ ] Frontend polling metrics

---

## 📊 **OUTPUT KỲ VỌNG**

Sau khi hoàn thành Phase 1-7:

✅ **RabbitMQ Dashboard hoạt động** (localhost:15672)  
✅ **Core Service gửi message thành công** → Log: "Sent message {id} to queue"  
✅ **AI Service nhận và xử lý** → Log: "Processing message {id}, action={action}"  
✅ **❌ KHÔNG có HTTP endpoint cho AI Service** (AI chạy ẩn hoàn toàn)  
✅ **Frontend polling và hiển thị kết quả** trong 3-10 giây  
✅ **User KHÔNG cảm thấy chờ đợi** (async workflow + UX strategies)  
✅ **3 chức năng AI hoạt động:**
   - Tóm tắt đề cương cho sinh viên
   - So sánh phiên bản cho AA/HoD
   - Kiểm tra CLO-PLO cho AA/Principal

---

## 🚨 **LƯU Ý QUAN TRỌNG**

### **Về Message Content:**
- ❌ **KHÔNG BAO GIỜ** gửi full syllabus content trong message (tốn băng thông, duplicate data)
- ✅ Chỉ gửi `syllabus_id`, AI Service tự query DB
- ✅ DB connection: AI Service kết nối tới schema `core_service` để đọc data

### **Về Priority:**
```
MAP_CLO_PLO (5 - HIGH)     → Xử lý trước
COMPARE_VERSIONS (3 - MED) → Xử lý sau
SUMMARIZE (1 - LOW)        → Xử lý cuối (background)
```

### **Về Error Handling:**
- Retry mechanism: Max 3 lần
- Nếu fail sau 3 lần → Lưu error vào DB, notify user
- Timeout: 30s → Frontend show "Quá thời gian chờ, vui lòng thử lại"

### **Về Security:**
- RabbitMQ: Dùng guest/guest cho dev, đổi credentials cho production
- JWT token: Pass trong `user_id` field để audit

### **Về Performance:**
- Cache Redis TTL: 30 phút
- Database cleanup: Xóa old analysis results sau 90 ngày
- Queue cleanup: Auto-delete messages after processed

---

## 📞 **LIÊN HỆ & THẢO LUẬN**

Nếu có thắc mắc hoặc cần điều chỉnh kế hoạch, hãy review lại file này và thảo luận với team trước khi implement.

**Good luck! 🚀**
