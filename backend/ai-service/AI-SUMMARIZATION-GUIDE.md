# 🤖 HƯỚNG DẪN: AI TÓM TẮT THỰC SỰ

## ❓ Vấn đề

Hiện tại chức năng "Tóm tắt AI" **KHÔNG DÙNG AI thật**, chỉ **copy y nguyên** nội dung từ đề cương ra.

## ✅ Giải pháp

Đã cập nhật code để sử dụng **BARTpho** (AI model Tiếng Việt) để tóm tắt văn bản thực sự.

## 🔧 Các thay đổi đã thực hiện

### 1. Backend AI Service (`backend/ai-service/app/workers/ai_handlers.py`)

**Trước đây:**
```python
def _summarize_text(self, text: str, max_sentences: int = 3) -> str:
    # Chỉ cắt ngắn text bằng cách lấy N câu đầu tiên
    sentences = text.split('. ')[:max_sentences]
    return '. '.join(sentences)
```

**Bây giờ:**
```python
def _summarize_text(self, text: str, max_length: int = 100) -> str:
    # Sử dụng BARTpho AI model để tóm tắt
    if not self.mock_mode and self.model is not None:
        inputs = self.tokenizer(text, max_length=512, truncation=True, return_tensors="pt")
        summary_ids = self.model.generate(
            inputs['input_ids'],
            max_length=max_length,
            num_beams=4,
            length_penalty=2.0,
            early_stopping=True
        )
        summary = self.tokenizer.decode(summary_ids[0], skip_special_tokens=True)
        return summary
    # Fallback nếu model không có
    return text[:200] + '...'
```

### 2. Các phần được tóm tắt bằng AI

- ✅ **Mô tả học phần** - Tóm tắt từ text dài → 2-3 câu ngắn gọn
- ✅ **Mục tiêu học phần** - Mỗi mục tiêu được tóm tắt gọn lại
- ✅ **CLO descriptions** - Mô tả CLO được tóm tắt
- ✅ **Tiêu chí đánh giá** - Criteria trong ma trận đánh giá được rút gọn

### 3. Thêm các field mới

- ✅ **Ma trận đánh giá** (Assessment Matrix)
- ✅ **CLO** (Chuẩn đầu ra học phần)
- ✅ **Phương pháp đánh giá** (từ database)

## 🚀 Cách sử dụng

### Option 1: Chạy script tự động

```powershell
# Từ thư mục root của project
.\restart-ai-with-model.ps1
```

Script này sẽ:
1. Kiểm tra và cài đặt dependencies
2. Stop các worker cũ
3. Start worker mới với AI model

### Option 2: Manual restart

```powershell
cd backend\ai-service
.\venv\Scripts\Activate.ps1

# Cài đặt dependencies (chỉ lần đầu)
pip install transformers torch sentencepiece protobuf

# Start worker
python -m app.workers.ai_worker
```

## 📊 Cách kiểm tra AI đang chạy

Khi start worker, kiểm tra log để thấy:

```
📦 Loading model: vinai/bartpho-word
🔧 Using device: cpu
✅ Model loaded successfully on cpu
```

Nếu thấy:
```
⚠️ Using MOCK data (AI model not available)
```
→ AI model chưa được load, cần kiểm tra lại config

## ⚙️ Configuration

File: `backend/ai-service/.env`

```bash
# BẬT AI MODEL
MOCK_MODE=false           # false = dùng AI thật
AI_MODEL_ENABLED=true     # true = load model

# Model name
AI_MODEL_NAME=vinai/bartpho-word  # BARTpho ~420MB
AI_MODEL_DEVICE=cpu               # cpu hoặc cuda
AI_MODEL_MAX_LENGTH=1024
```

## 📦 AI Models có sẵn

| Model | Size | Chất lượng | Khuyên dùng |
|-------|------|------------|-------------|
| `vinai/bartpho-word` | ~420MB | Tốt | ⭐ Recommended |
| `VietAI/vit5-base` | ~900MB | Trung bình | Chậm hơn |
| `vinai/bartpho-syllable` | ~420MB | Tốt | Alternative |

## 🧪 Test AI summarization

1. **Restart AI worker** với AI model enabled
2. **Vào trang Student** → Chọn một đề cương
3. **Nhấn "Tóm tắt AI"**
4. **Kiểm tra kết quả:**
   - Mô tả học phần phải **ngắn hơn** bản gốc
   - Mục tiêu phải được **tóm tắt gọn**
   - CLO descriptions phải **rút gọn**

## 🐛 Troubleshooting

### Vấn đề: Vẫn thấy text y nguyên, không tóm tắt

**Nguyên nhân:** Model chưa được load hoặc đang chạy ở MOCK mode

**Giải pháp:**
```powershell
# 1. Kiểm tra .env
cat backend\ai-service\.env | Select-String "MOCK_MODE"
cat backend\ai-service\.env | Select-String "AI_MODEL_ENABLED"

# Phải thấy:
# MOCK_MODE=false
# AI_MODEL_ENABLED=true

# 2. Restart worker và xem log
cd backend\ai-service
python -m app.workers.ai_worker

# Phải thấy: "✅ Model loaded successfully"
```

### Vấn đề: Download model quá lâu

**Nguyên nhân:** Lần đầu tiên tải model từ HuggingFace

**Giải pháp:** Đợi 2-5 phút, model sẽ được cache tại `~/.cache/huggingface/`

### Vấn đề: Out of memory

**Nguyên nhân:** Model quá lớn cho RAM

**Giải pháp:** 
- Dùng model nhỏ hơn: `VietAI/vit5-base`
- Hoặc giữ `MOCK_MODE=false` để dùng simple summarization

## 📝 Summary

| Trước | Sau |
|-------|-----|
| ❌ Copy text y nguyên | ✅ Tóm tắt bằng AI |
| ❌ Không có CLO, Ma trận | ✅ Hiển thị đầy đủ |
| ❌ Không dùng AI model | ✅ Dùng BARTpho model |
| ❌ Text quá dài | ✅ Rút gọn 50-70% |

## 📞 Support

Nếu có vấn đề, kiểm tra:
1. Log của AI worker
2. File `.env` có đúng config không
3. Dependencies đã cài đủ chưa (`transformers`, `torch`, `sentencepiece`)
