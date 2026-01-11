# 🤖 HƯỚNG DẪN TÍCH HỢP AI SERVICE - SMD PROJECT

> **Tác giả:** Backend Developer 2 (B2)  
> **Ngày:** 09/01/2026  
> **Mục tiêu:** Xây dựng AI Consumer & Tích hợp Hugging Face Transformer Skeleton

---

## 📋 TỔNG QUAN

AI Service là microservice Python độc lập, chịu trách nhiệm xử lý các tác vụ AI thông qua RabbitMQ message queue.

### Đặc điểm chính:
- ✅ **Độc lập hoàn toàn** với Core Service (Java)
- ✅ **Không gọi ngược lại Java** (tuân thủ nguyên tắc microservices)
- ✅ **Không trực tiếp lưu DB** (chỉ xử lý và trả về kết quả)
- ✅ **Mock Mode mặc định** để ưu tiên tốc độ phát triển
- ✅ **Sẵn sàng tích hợp Hugging Face** models

---

## 🏗️ KIẾN TRÚC

```
┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐
│  Core Service    │  JSON   │    RabbitMQ      │  JSON   │   AI Service     │
│     (Java)       │ ──────> │    (Broker)      │ ──────> │    (Python)      │
│  Spring Boot     │         │  2 Queues:       │         │  2 Workers:      │
│                  │         │  - ai_processing │         │  - analysis      │
│                  │         │  - ai_summarize  │         │  - summarize     │
└──────────────────┘         └──────────────────┘         └──────────────────┘
        │                                                          │
        │                                                          │
        └──────────────────────┬───────────────────────────────────┘
                               │
                        ┌──────▼──────┐
                        │  PostgreSQL │
                        │  Database   │
                        └─────────────┘
```

---

## 📨 CẤU TRÚC MESSAGE

### 1. AIMessageRequest (Java → Python)

```java
// Java DTO (backend/shared/java-common)
public class AIMessageRequest {
    private String messageId;      // UUID
    private String action;         // SUMMARIZE | COMPARE_VERSIONS | MAP_CLO_PLO
    private String priority;       // HIGH | MEDIUM | LOW
    private Instant timestamp;
    private String userId;
    private Map<String, Object> payload;
}
```

**JSON Example:**
```json
{
  "messageId": "550e8400-e29b-41d4-a716-446655440000",
  "action": "MAP_CLO_PLO",
  "priority": "HIGH",
  "timestamp": "2026-01-09T10:30:00Z",
  "userId": "user-123",
  "payload": {
    "syllabus_id": "syllabus-uuid",
    "curriculum_id": "curriculum-uuid",
    "strict_mode": true,
    "check_weights": true
  }
}
```

### 2. AIMessageResponse (Python → Java/DB)

```python
# Python response structure
{
    "messageId": "550e8400-e29b-41d4-a716-446655440000",
    "action": "MAP_CLO_PLO",
    "status": "SUCCESS",  # SUCCESS | FAILED | PROCESSING
    "progress": 100,
    "result": {
        # Dữ liệu chi tiết theo từng action
    },
    "processingTimeMs": 2340,
    "errorMessage": null  # Chỉ có khi status = FAILED
}
```

---

## 🎯 3 CHỨC NĂNG AI

### 1️⃣ MAP_CLO_PLO (Priority: HIGH)

**Mục đích:** Kiểm tra tuân thủ ánh xạ CLO-PLO  
**Queue:** `ai_processing_queue`  
**User:** AA, Principal

**Request Payload:**
```json
{
  "syllabus_id": "uuid",
  "curriculum_id": "uuid",
  "strict_mode": true,
  "check_weights": true
}
```

**Response Result:**
```json
{
  "overall_status": "NEEDS_IMPROVEMENT | GOOD | COMPLIANT",
  "compliance_score": 75.5,
  "issues": [...],
  "suggestions": [...],
  "compliant_mappings": [...]
}
```

---

### 2️⃣ COMPARE_VERSIONS (Priority: MEDIUM)

**Mục đích:** So sánh 2 phiên bản đề cương  
**Queue:** `ai_processing_queue`  
**User:** AA, HoD

**Request Payload:**
```json
{
  "old_version_id": "uuid",
  "new_version_id": "uuid",
  "subject_id": "uuid",
  "comparison_depth": "DETAILED"
}
```

**Response Result:**
```json
{
  "is_first_version": false,
  "version_history": [...],
  "changes_summary": {...},
  "detailed_changes": [...],
  "ai_analysis": {...}
}
```

---

### 3️⃣ SUMMARIZE_SYLLABUS (Priority: LOW)

**Mục đích:** Tóm tắt đề cương cho sinh viên  
**Queue:** `ai_summarize_queue`  
**User:** Student

**Request Payload:**
```json
{
  "syllabus_id": "uuid",
  "language": "vi",
  "include_prerequisites": true
}
```

**Response Result:**
```json
{
  "overview": {...},
  "highlights": {
    "difficulty": {...},
    "duration": {...},
    "assessment": {...},
    "skills_acquired": {...}
  },
  "recommendations": {...}
}
```

---

## 🚀 CÀI ĐẶT VÀ CHẠY

### Bước 1: Setup môi trường

```powershell
cd backend/ai-service

# Chạy script setup tự động
.\setup-dev.ps1
```

### Bước 2: Khởi động workers

```powershell
# Chạy script start workers
.\start-workers.ps1

# Hoặc chạy thủ công:
python -m app.workers.analysis_worker   # Terminal 1
python -m app.workers.summarize_worker  # Terminal 2
```

### Bước 3: Test với message mẫu

```powershell
# Gửi test message vào RabbitMQ
python test_send_message.py
```

**Expected Log:**
```
🚀 Starting Analysis Worker...
📋 Listening to: ai_processing_queue
🔌 Connecting to RabbitMQ at localhost:5672...
✅ [Connected] Successfully connected to RabbitMQ!
🔔 [Connected] Waiting for messages from RabbitMQ...

[Received] Action: MAP_CLO_PLO for Message ID: test-123
[Priority] HIGH | User: user-456
[Processing] Simulating AI analysis...
[Done] Mock result generated.
✅ MAP_CLO_PLO completed in 2034ms
```

---

## 🤖 TÍCH HỢP HUGGING FACE MODELS

### Models hỗ trợ:

| Model | Mục đích | Size |
|-------|----------|------|
| `vinai/phogpt-4b-v1-instruct` | Vietnamese GPT generation | ~8GB |
| `VietAI/vit5-base` | Vietnamese T5 generation | ~892MB |
| `bkai-foundation-models/vietnamese-bi-encoder` | Semantic embeddings | ~400MB |

### Skeleton code đã có sẵn:

File `app/workers/ai_handlers.py` chứa các method:

```python
def _load_huggingface_model(self):
    """Load GPT/T5 model cho text generation"""
    # Code example đã có sẵn, tạm comment

def _load_embedding_model(self):
    """Load embedding model cho semantic search"""
    # Code example đã có sẵn, tạm comment

def _generate_with_model(self, prompt: str) -> str:
    """Generate text với model"""
    # Code example đã có sẵn, tạm comment

def _get_embeddings(self, texts: list) -> list:
    """Generate embeddings"""
    # Code example đã có sẵn, tạm comment
```

### Để enable models thật:

**1. Uncomment dependencies trong `requirements.txt`:**
```python
transformers==4.36.2
torch==2.1.2
sentence-transformers==2.3.1
```

**2. Install lại:**
```powershell
pip install -r requirements.txt
```

**3. Đổi config trong `.env`:**
```bash
MOCK_MODE=false
AI_MODEL_ENABLED=true
AI_MODEL_NAME=vinai/phogpt-4b-v1-instruct
AI_MODEL_DEVICE=cpu  # hoặc cuda nếu có GPU
```

**4. Uncomment code trong `ai_handlers.py`:**
- Tìm các method có comment `# TODO: ...`
- Uncomment code example
- Test lại với message thật

---

## 📊 MONITORING

### RabbitMQ Management UI

```
URL: http://localhost:15672
User: guest
Pass: guest
```

**Kiểm tra:**
- Queues → `ai_processing_queue`, `ai_summarize_queue`
- Messages rate
- Consumer count (phải có 2 workers)

### Logs

```powershell
# Worker logs hiển thị realtime trong terminal
[Connected] Waiting for messages...
[Received] Action: MAP_CLO_PLO for Message ID: xxx
[Processing] Simulating AI analysis...
[Done] Mock result generated.
✅ MAP_CLO_PLO completed in 2034ms
```

---

## ✅ CHECKLIST HOÀN THÀNH

- [x] Thiết lập môi trường Python với FastAPI, pika
- [x] Cấu hình kết nối Robust Connection tới RabbitMQ (smd-network)
- [x] Lắng nghe đồng thời 2 queues: `ai_processing_queue`, `ai_summarize_queue`
- [x] Implement `process_task(payload)` với Mock Data đúng 100% cấu trúc JSON
- [x] Tích hợp sẵn Hugging Face skeleton (commented)
- [x] Log terminal: `[Connected]` → `[Received]` → `[Processing]` → `[Done]`
- [x] ❌ Không gọi ngược lại Java
- [x] ❌ Không trực tiếp lưu DB
- [x] ✅ Độc lập hoàn toàn của Microservices

---

## 🎯 MỤC TIÊU ĐẠT ĐƯỢC

✅ **Đảm bảo Python "nhặt" được đúng task từ Java gửi sang**  
✅ **Bóc tách được dữ liệu để chuẩn bị cho việc nạp Model thật ở giai đoạn sau**  
✅ **Cấu trúc message chuẩn, dễ mở rộng**

---

## 📚 TÀI LIỆU THAM KHẢO

- [RabbitMQ AI Integration Plan](../../docs/RABBITMQ-AI-INTEGRATION-PLAN.md)
- [AI Service README](./README.md)
- [Hugging Face Transformers Docs](https://huggingface.co/docs/transformers)
- [Vietnamese NLP Models](https://huggingface.co/models?language=vi&sort=downloads)

---

## 🆘 TROUBLESHOOTING

### Lỗi: "Cannot connect to RabbitMQ"

**Giải pháp:**
```powershell
# 1. Kiểm tra RabbitMQ đang chạy
docker ps | Select-String rabbitmq

# 2. Kiểm tra port
netstat -an | Select-String "5672"

# 3. Restart RabbitMQ
docker-compose restart rabbitmq
```

### Lỗi: "Module not found"

**Giải pháp:**
```powershell
# Activate virtual environment
.\venv\Scripts\Activate.ps1

# Reinstall dependencies
pip install -r requirements.txt
```

### Lỗi: "JSON decode error"

**Nguyên nhân:** Message format không đúng chuẩn AIMessageRequest

**Giải pháp:**
- Kiểm tra Java code gửi message
- Xem message thật trong RabbitMQ Management UI
- So sánh với example trong document

---

**🎉 AI Service đã sẵn sàng!**
