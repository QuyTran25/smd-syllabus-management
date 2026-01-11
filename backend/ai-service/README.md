# 🤖 AI SERVICE - SMD SYLLABUS MANAGEMENT

## 📋 Mô tả

AI Service chịu trách nhiệm xử lý 3 chức năng AI cho hệ thống SMD:

1. **MAP_CLO_PLO** - Kiểm tra tuân thủ ánh xạ CLO-PLO (priority HIGH)
2. **COMPARE_VERSIONS** - So sánh phiên bản đề cương (priority MEDIUM)
3. **SUMMARIZE_SYLLABUS** - Tóm tắt đề cương cho sinh viên (priority LOW)

## 🏗️ Kiến trúc

```
Core Service (Java) → RabbitMQ → AI Service (Python)
                                      ↓
                                  Database
```

### Nguyên tắc thiết kế:
- ❌ **KHÔNG** gọi ngược lại Java (tính độc lập hoàn toàn của Microservices)
- ❌ **KHÔNG** trực tiếp lưu DB (AI chỉ xử lý và trả về kết quả)
- ✅ **Chỉ** lắng nghe RabbitMQ, xử lý message, và trả về result
- ✅ **Mock Mode** mặc định để ưu tiên tốc độ phát triển

## 🚀 Cài đặt

### 1. Cài đặt Python 3.11+

```powershell
python --version  # Should be 3.11 or higher
```

### 2. Tạo Virtual Environment

```powershell
cd backend/ai-service
python -m venv venv
.\venv\Scripts\Activate.ps1
```

### 3. Cài đặt Dependencies

```powershell
pip install -r requirements.txt
```

### 4. Cấu hình Environment

```powershell
# Copy file .env.example thành .env
Copy-Item .env.example .env

# Chỉnh sửa .env nếu cần (mặc định đã OK cho local development)
```

## ▶️ Chạy Workers

### Analysis Worker (MAP_CLO_PLO, COMPARE_VERSIONS)

```powershell
python -m app.workers.analysis_worker
```

### Summarize Worker (SUMMARIZE_SYLLABUS)

```powershell
python -m app.workers.summarize_worker
```

## 📊 Cấu trúc Message

### Request từ Java (AIMessageRequest)

```json
{
  "messageId": "uuid-v4",
  "action": "MAP_CLO_PLO | COMPARE_VERSIONS | SUMMARIZE_SYLLABUS",
  "priority": "HIGH | MEDIUM | LOW",
  "timestamp": "2026-01-09T10:00:00Z",
  "userId": "user-uuid",
  "payload": {
    // Khác nhau theo action, CHỈ chứa IDs
  }
}
```

### Response từ Python (AIMessageResponse)

```json
{
  "messageId": "uuid-v4",
  "action": "MAP_CLO_PLO",
  "status": "SUCCESS | FAILED | PROCESSING",
  "progress": 100,
  "result": {
    // Dữ liệu chi tiết theo action
  },
  "processingTimeMs": 2340
}
```

## 🧪 Testing

### Test với script có sẵn:

```powershell
# Gửi test message vào RabbitMQ
python test_send_message.py
```

### Log mong đợi:

```
🚀 Starting Analysis Worker...
📋 Listening to: ai_processing_queue
🎯 Actions: MAP_CLO_PLO, COMPARE_VERSIONS
🔌 Connecting to RabbitMQ at localhost:5672...
✅ [Connected] Successfully connected to RabbitMQ!
🔔 [Connected] Waiting for messages from RabbitMQ...

[Received] Action: MAP_CLO_PLO for Message ID: test-123
[Priority] HIGH | User: user-456
[Processing] Simulating AI analysis...
[Done] Mock result generated.
✅ MAP_CLO_PLO completed in 2034ms
```

## 🤖 Hugging Face Integration (Giai đoạn sau)

File `ai_handlers.py` đã có sẵn skeleton code để tích hợp:

### Models được hỗ trợ:

1. **vinai/phogpt-4b-v1-instruct** - Vietnamese GPT model
2. **VietAI/vit5-base** - Vietnamese T5 model
3. **bkai-foundation-models/vietnamese-bi-encoder** - Embeddings

### Để enable AI models thật:

```bash
# 1. Uncomment các dòng trong requirements.txt:
# - transformers
# - torch
# - sentence-transformers

# 2. Install lại dependencies
pip install -r requirements.txt

# 3. Đổi MOCK_MODE=false trong .env
MOCK_MODE=false
AI_MODEL_ENABLED=true
AI_MODEL_NAME=vinai/phogpt-4b-v1-instruct

# 4. Uncomment code trong ai_handlers.py:
# - _load_huggingface_model()
# - _generate_with_model()
# - _get_embeddings()
```

## 📁 Cấu trúc thư mục

```
ai-service/
├── app/
│   ├── config/
│   │   ├── settings.py          # Environment config
│   │   └── rabbitmq.py          # RabbitMQ connection manager
│   ├── workers/
│   │   ├── ai_handlers.py       # Main AI logic + HF skeleton
│   │   ├── analysis_worker.py   # Worker cho analysis queue
│   │   └── summarize_worker.py  # Worker cho summarize queue
│   └── __init__.py
├── requirements.txt              # Python dependencies
├── .env.example                 # Environment template
└── README.md                    # This file
```

## 🔧 Troubleshooting

### 1. Không kết nối được RabbitMQ

```powershell
# Kiểm tra RabbitMQ đang chạy
docker ps | Select-String rabbitmq

# Kiểm tra port mapping
# RabbitMQ phải expose 5672 (AMQP) và 15672 (Management UI)
```

### 2. Module not found error

```powershell
# Đảm bảo đang ở trong virtual environment
.\venv\Scripts\Activate.ps1

# Reinstall dependencies
pip install -r requirements.txt
```

### 3. JSON decode error

- Kiểm tra format message từ Java phải đúng chuẩn AIMessageRequest
- Xem log RabbitMQ Management UI: http://localhost:15672

## 📚 Tài liệu tham khảo

- [RabbitMQ AI Integration Plan](../../docs/RABBITMQ-AI-INTEGRATION-PLAN.md)
- [Hugging Face Transformers](https://huggingface.co/docs/transformers)
- [Vietnamese NLP Models](https://huggingface.co/models?language=vi)

## ✅ Checklist hoàn thành

- [x] Thiết lập môi trường Python
- [x] Cài đặt pika (RabbitMQ client)
- [x] Viết robust RabbitMQ connection
- [x] Xử lý đồng thời 2 queues (ai_processing_queue, ai_summarize_queue)
- [x] Implement process_task với Mock Data đúng 100% cấu trúc JSON
- [x] Tích hợp sẵn Hugging Face skeleton (commented)
- [x] Log terminal chi tiết: [Connected] → [Received] → [Processing] → [Done]
- [x] Độc lập hoàn toàn với Java (không gọi ngược lại)
- [x] Không trực tiếp lưu DB

## 🎯 Mục tiêu đạt được

✅ **Đảm bảo Python "nhặt" được đúng task từ Java gửi sang**  
✅ **Bóc tách được dữ liệu để chuẩn bị cho việc nạp Model thật ở giai đoạn sau**  
✅ **Cấu trúc message chuẩn, dễ mở rộng**
