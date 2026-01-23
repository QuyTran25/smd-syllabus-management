"""
Test Gemini API tóm tắt văn bản
"""
import os
from dotenv import load_dotenv
import google.generativeai as genai

# Load .env
load_dotenv()

GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')
GEMINI_MODEL = 'gemini-2.5-flash'  # Correct model name from API

print(f"🔑 API Key: {GEMINI_API_KEY[:10]}...{GEMINI_API_KEY[-5:]}")
print(f"🤖 Model: {GEMINI_MODEL}")
print()

# Initialize Gemini
genai.configure(api_key=GEMINI_API_KEY)
model = genai.GenerativeModel(GEMINI_MODEL)

print("✅ Gemini initialized successfully!")
print()

# Test text - một đoạn dài
test_text = """
Về kiến thức: * Nắm vững các khái niệm về Logic mệnh đề và vị từ, làm nền tảng cho việc học lập trình và tư duy logic.
Hiểu về Lý thuyết tập hợp, Quan hệ và Hàm số để quản trị cơ sở dữ liệu.
Sử dụng các phương pháp Đếm (Tổ hợp, Chỉnh hợp) và nguyên lý Dirichlet để giải quyết các bài toán đếm phức tạp.
Lý thuyết đồ thị là cốt lõi của tìm đường (Google Maps) và mạng xã hội.
Về kỹ năng: * Rèn luyện khả năng chứng minh toán học (đặc biệt là phương pháp quy nạp toán học) để tư duy logic và xây dựng thuật toán.
"""

print("📝 Văn bản gốc:")
print(test_text)
print(f"Độ dài: {len(test_text)} ký tự")
print()

# Test tóm tắt
max_length = 250
target_words = max_length // 5

prompt = f"""Hãy tóm tắt văn bản sau thành {target_words} từ TỐI ĐA. CHỈ trả về nội dung tóm tắt, không thêm bất kỳ giải thích nào.

Văn bản gốc:
{test_text[:2000]}

Yêu cầu:
- Tóm tắt thành TỐI ĐA {target_words} từ
- Giữ thông tin quan trọng nhất
- Viết ngắn gọn, súc tích
- CHỈ trả về nội dung tóm tắt

Tóm tắt:"""

print("🤖 Đang gọi Gemini API...")
try:
    response = model.generate_content(prompt)
    summary = response.text.strip()
    
    print("✅ Tóm tắt thành công!")
    print()
    print("📊 Kết quả tóm tắt:")
    print(summary)
    print()
    print(f"Độ dài tóm tắt: {len(summary)} ký tự")
    print(f"Tỷ lệ nén: {len(test_text)} → {len(summary)} ({len(summary)*100//len(test_text)}%)")
    
    # Check if need truncate
    if len(summary) > max_length:
        print(f"⚠️ Gemini trả về {len(summary)} ký tự, vượt quá {max_length}")
        truncated = summary[:max_length].rsplit(' ', 1)[0].strip() + '...'
        print("✂️ Sau khi cắt:")
        print(truncated)
        print(f"Độ dài sau cắt: {len(truncated)} ký tự")
    
except Exception as e:
    print(f"❌ LỖI: {e}")
    print()
    import traceback
    traceback.print_exc()
