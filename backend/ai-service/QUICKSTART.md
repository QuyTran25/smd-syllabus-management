# 🚀 QUICK START - AI SERVICE

## 1️⃣ Setup (chỉ cần chạy 1 lần)

```powershell
cd backend/ai-service
.\setup-dev.ps1
```

**Script này sẽ:**
- ✅ Kiểm tra Python 3.11+
- ✅ Tạo virtual environment
- ✅ Install dependencies
- ✅ Tạo .env file
- ✅ Test RabbitMQ connection

---

## 2️⃣ Start Workers

```powershell
.\start-workers.ps1
```

**Chọn option:**
- `1` - Analysis Worker only (MAP_CLO_PLO, COMPARE_VERSIONS)
- `2` - Summarize Worker only (SUMMARIZE_SYLLABUS)
- `3` - Both Workers (recommended) ⭐

---

## 3️⃣ Test

### Option A: Từ Python test script
```powershell
python test_send_message.py
```

### Option B: Từ Java Core Service
```powershell
# 1. Khởi động Core Service
cd backend/core-service
mvn spring-boot:run

# 2. Submit một syllabus qua API
# → Java sẽ tự động gửi message vào RabbitMQ
```

---

## ✅ Expected Output

```
🚀 Starting Analysis Worker...
📋 Listening to: ai_processing_queue
🎯 Actions: MAP_CLO_PLO, COMPARE_VERSIONS
🤖 Mock Mode: True
🔌 Connecting to RabbitMQ at localhost:5672...
✅ [Connected] Successfully connected to RabbitMQ!
✅ Queue declared: ai_processing_queue (priority: 5)
✅ QoS set: prefetch_count=1
🎧 Listening to queue: ai_processing_queue
⏱️ Heartbeat: 600s
🔔 [Connected] Waiting for messages from RabbitMQ...

[Received] Action: MAP_CLO_PLO for Message ID: 550e8400-e29b-41d4-a716-446655440000
[Priority] HIGH | User: user-123
[Processing] Simulating AI analysis...
[Done] Mock result generated.
✅ MAP_CLO_PLO completed in 2034ms
```

---

## 🔍 Verify

### 1. Check RabbitMQ UI
```
http://localhost:15672
User: guest / Pass: guest
```

**Kiểm tra:**
- Queues tab → Should see:
  - `ai_processing_queue` (1 consumer)
  - `ai_summarize_queue` (1 consumer)

### 2. Check Worker Logs
- Phải thấy `[Connected] Waiting for messages...`
- Khi có message: `[Received] → [Processing] → [Done]`

---

## 🆘 Troubleshooting

### "Cannot connect to RabbitMQ"
```powershell
# Start RabbitMQ
docker-compose up -d rabbitmq

# Check status
docker ps | Select-String rabbitmq
```

### "Module not found"
```powershell
# Activate venv
.\venv\Scripts\Activate.ps1

# Reinstall
pip install -r requirements.txt
```

### "Python not found"
- Install Python 3.11+ from https://www.python.org/
- Restart PowerShell

---

## 📚 More Info

- **Full documentation:** [README.md](./README.md)
- **Integration guide:** [INTEGRATION-GUIDE.md](./INTEGRATION-GUIDE.md)
- **Implementation summary:** [IMPLEMENTATION-SUMMARY.md](./IMPLEMENTATION-SUMMARY.md)

---

**That's it! AI Service is ready!** 🎉
