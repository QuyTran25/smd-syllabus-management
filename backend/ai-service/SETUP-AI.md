# 🚀 SETUP AI MODEL - HƯỚNG DẪN NHANH

## ⏱️ Timeline: 15-30 phút

---

## Bước 1: Cài đặt Dependencies (5 phút)

```powershell
cd backend\ai-service

# Activate virtual environment
.\venv\Scripts\Activate.ps1

# Install AI libraries
pip install transformers==4.36.2 torch==2.1.2 sentencepiece==0.1.99 accelerate==0.25.0
```

**⚠️ Lưu ý:** 
- Torch (~2GB download)
- Mất 3-5 phút tùy tốc độ mạng

---

## Bước 2: Cấu hình Environment (1 phút)

```powershell
# Copy .env.example to .env (nếu chưa có)
Copy-Item .env.example .env

# Edit .env - Đổi MOCK_MODE thành false
# MOCK_MODE=false
```

**File `.env` cần có:**
```env
MOCK_MODE=false
AI_MODEL_NAME=VietAI/vit5-base
AI_MODEL_DEVICE=cpu
```

---

## Bước 3: Download Model (10-20 phút)

Model sẽ tự động download khi chạy worker lần đầu:

```powershell
python app\workers\summarize_worker.py
```

**Lần đầu chạy:**
```
📦 Loading model: VietAI/vit5-base
Downloading (…)lve/main/config.json: 100%|████| 1.2k/1.2k
Downloading model.safetensors: 100%|████| 892MB/892MB
🔧 Using device: cpu
✅ Model loaded successfully on cpu
```

**⏱️ Thời gian:**
- Download: 10-15 phút (892MB)
- Load vào RAM: 2-3 phút

---

## Bước 4: Test chức năng (2 phút)

### Test 1: Gửi message test

```powershell
# Terminal 1: Chạy worker
python app\workers\summarize_worker.py

# Terminal 2: Gửi test message
python test_send_message.py
```

**Kết quả mong đợi:**
```
[Received] Action: SUMMARIZE_SYLLABUS for Message ID: xxx
📦 Loading model: VietAI/vit5-base
✅ Model loaded successfully on cpu
📝 Summarizing syllabus: syllabus-001
✅ AI Summarization completed
✅ SUMMARIZE_SYLLABUS completed in 8500ms
```

### Test 2: Kiểm tra kết quả

Worker sẽ trả về JSON với:
- `overview.description` - Generated bởi AI
- `highlights` - Extracted từ data
- `recommendations` - Generated dựa trên data

---

## ✅ Checklist Hoàn Thành

- [ ] Cài đặt `transformers`, `torch`, `sentencepiece`, `accelerate`
- [ ] Đổi `MOCK_MODE=false` trong `.env`
- [ ] Download model thành công (892MB)
- [ ] Worker chạy không lỗi
- [ ] Test message trả về kết quả AI

---

## 🐛 Troubleshooting

### Lỗi: "No module named 'transformers'"
```powershell
pip install transformers torch sentencepiece accelerate
```

### Lỗi: "Connection timeout" khi download model
- Kiểm tra kết nối mạng
- Thử lại sau vài phút
- Hoặc download thủ công từ: https://huggingface.co/VietAI/vit5-base

### Lỗi: "Out of memory"
- Model yêu cầu ~2GB RAM
- Đóng các ứng dụng khác
- Hoặc dùng MOCK_MODE=true tạm thời

### Worker chạy chậm (>30s/request)
- Bình thường cho CPU mode
- Nếu có GPU: Đổi `AI_MODEL_DEVICE=cuda` trong `.env`
- Xem xét caching kết quả trong Redis

---

## 📊 Performance

| Chế độ | Thời gian xử lý | RAM sử dụng |
|--------|----------------|-------------|
| MOCK | 2s | ~50MB |
| AI (CPU) | 8-15s | ~2GB |
| AI (GPU) | 3-5s | ~2GB |

---

## 🔄 Quay lại Mock Mode

Nếu cần disable AI:

```env
# .env
MOCK_MODE=true
```

Restart worker - sẽ quay về Mock mode (nhanh, không load model).

---

## 🎯 Next Steps

Sau khi SUMMARIZE hoàn thành:
1. Test với Frontend (gọi API thật)
2. Implement AI cho 2 chức năng còn lại:
   - MAP_CLO_PLO
   - COMPARE_VERSIONS
3. Optimize performance (caching, batch processing)

---

**📝 Ghi chú:** Model VietAI/vit5-base phù hợp cho summarization. Nếu cần chất lượng cao hơn, có thể đổi sang `vinai/phogpt-4b-v1-instruct` (8GB).
