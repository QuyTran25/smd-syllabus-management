# 🧪 TEST TÍCH HỢP AI SERVICE VỚI CORE SERVICE

## ✅ TRẠNG THÁI HIỆN TẠI

- ✅ **AI Worker đang chạy** (1 consumer trên ai_processing_queue)
- ✅ **RabbitMQ đang hoạt động**
- ⏳ **Core Service đang khởi động** (hoặc cần verify port)

---

## 🎯 CÁCH TEST FLOW HOÀN CHỈNH

### **Option 1: Test từ API (Postman/cURL)**

#### 1. Kiểm tra Core Service đã sẵn sàng:

```powershell
# Test health endpoint
curl http://localhost:8081/actuator/health

# Hoặc test API syllabi
curl http://localhost:8081/api/syllabi
```

#### 2. Login để lấy JWT token:

```powershell
# POST login
curl -X POST http://localhost:8081/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "admin@ut.edu.vn",
    "password": "Admin@123"
  }'
```

#### 3. Submit syllabus (sẽ tự động gửi message sang AI):

```powershell
# PUT submit syllabus
curl -X PUT http://localhost:8081/api/syllabi/{syllabusId}/submit `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -H "Content-Type: application/json"
```

**KẾT QUẢ MONG ĐỢI:**
- Core Service log: `[Sent] Message to AI Queue: Syllabus ID #xxx`
- AI Worker log: `[Received] Action: MAP_CLO_PLO for Message ID: xxx`

---

### **Option 2: Test từ Frontend**

1. Mở Frontend: http://localhost:5173
2. Login với tài khoản Lecturer
3. Tạo/Chỉnh sửa một Syllabus
4. Click **Submit for Approval**
5. → Backend tự động gửi message vào RabbitMQ
6. → AI Worker nhận và xử lý

**Xem log AI Worker trong terminal:**
```
[Received] Action: MAP_CLO_PLO for Message ID: xxx
[Priority] HIGH | User: user-id
[Processing] Simulating AI analysis...
📊 Analyzing CLO-PLO mapping for syllabus: syllabus-id
✅ CLO-PLO analysis completed. Status: NEEDS_IMPROVEMENT
[Done] Mock result generated.
✅ MAP_CLO_PLO completed in 2034ms
```

---

### **Option 3: Test bằng RabbitMQ Management UI**

1. Mở: http://localhost:15672 (guest/guest)
2. Vào tab **Queues**
3. Click vào **ai_processing_queue**
4. Scroll xuống **Publish message**
5. Paste message sau vào **Payload**:

```json
{
  "messageId": "manual-test-001",
  "action": "MAP_CLO_PLO",
  "priority": "HIGH",
  "timestamp": "2026-01-09T18:50:00Z",
  "userId": "manual-tester",
  "payload": {
    "syllabus_id": "test-syllabus-123",
    "curriculum_id": "test-curriculum-456",
    "strict_mode": true,
    "check_weights": true
  }
}
```

6. Click **Publish message**
7. → Xem terminal AI Worker nhận message ngay lập tức!

---

### **Option 4: Test với Java RabbitMQTestSender**

Core Service có sẵn test class:

```powershell
cd backend/core-service
mvn exec:java -Dexec.mainClass="vn.edu.smd.core.RabbitMQTestSender"
```

→ Java sẽ gửi test message vào RabbitMQ
→ Python worker sẽ nhận và xử lý

---

## 📊 CÁCH XEM KẾT QUẢ

### 1. **Log AI Worker** (Terminal đang chạy worker)
```
[Received] Action: MAP_CLO_PLO for Message ID: xxx
[Processing] Simulating AI analysis...
✅ MAP_CLO_PLO completed in 2034ms
```

### 2. **RabbitMQ Management UI**
- Tab Queues → ai_processing_queue
- Xem **Message rates** (messages/sec)
- Xem **Deliver rates** (processed/sec)

### 3. **Database** (Giai đoạn sau)
```sql
SELECT * FROM ai_service.syllabus_ai_analysis
WHERE syllabus_id = 'xxx'
ORDER BY created_at DESC;
```

---

## 🐛 TROUBLESHOOTING

### "Worker không nhận message từ Java"

**Kiểm tra:**
```powershell
# 1. Worker có đang chạy?
docker exec smd-rabbitmq rabbitmqctl list_queues name consumers
# → ai_processing_queue phải có consumer >= 1

# 2. Message có vào queue không?
docker exec smd-rabbitmq rabbitmqctl list_queues name messages
# → Xem số messages trong queue

# 3. Log Core Service có gửi message không?
# Tìm log: "[Sent] Message to AI Queue"
```

### "Core Service không gửi message"

**Kiểm tra file:**
```
backend/core-service/src/main/java/vn/edu/smd/core/module/syllabus/service/SyllabusService.java
```

Phải có đoạn code:
```java
// 🚀 Send message to RabbitMQ AI Queue for processing
aiTaskService.requestCloPloMapping(
    savedSyllabus.getId(),
    curriculumId,
    currentUser.getId().toString()
);
```

### "Message format không đúng"

Python worker chỉ accept format:
```json
{
  "messageId": "uuid",        // HOẶC "message_id"
  "action": "MAP_CLO_PLO",
  "priority": "HIGH",
  "timestamp": "ISO-8601",
  "userId": "uuid",           // HOẶC "user_id"
  "payload": {...}
}
```

---

## ✅ VERIFY THÀNH CÔNG

Khi test thành công, bạn sẽ thấy:

**1. Core Service log:**
```
[Sent] Message to AI Queue: Syllabus ID #123
messageId: 550e8400-e29b-41d4-a716-446655440000
```

**2. AI Worker log:**
```
[Received] Action: MAP_CLO_PLO for Message ID: 550e8400-e29b-41d4-a716-446655440000
[Priority] HIGH | User: user-123
[Processing] Simulating AI analysis...
[Done] Mock result generated.
✅ MAP_CLO_PLO completed in 2034ms
```

**3. RabbitMQ UI:**
- Message delivered: +1
- Consumer acknowledged: +1
- Queue empty (0 messages)

---

## 🎉 KẾT LUẬN

Flow hoàn chỉnh:
```
Frontend/API → Core Service (Java) → RabbitMQ → AI Worker (Python) → [Xử lý] → Done
```

**Hiện tại:**
- ✅ Python Worker sẵn sàng (đang listen)
- ✅ RabbitMQ hoạt động
- ⏳ Core Service cần verify

**Next step:** Chọn một trong 4 options test ở trên để verify flow hoàn chỉnh!
