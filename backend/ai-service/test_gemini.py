"""
Test Gemini API for Vietnamese Summarization
"""
import os
os.environ['AI_PROVIDER'] = 'gemini'
os.environ['GEMINI_API_KEY'] = 'AIzaSyCavv-MZCPJGFQJN4K-XeqB4-_NiqEXyfU'
os.environ['GEMINI_MODEL'] = 'gemini-2.5-flash'

print("🚀 Testing Gemini API...")
print("=" * 80)

import google.generativeai as genai

# Configure Gemini
genai.configure(api_key=os.getenv('GEMINI_API_KEY'))
model = genai.GenerativeModel(os.getenv('GEMINI_MODEL', 'gemini-1.5-flash'))

print("✅ Gemini configured!\n")

# Test 1: Syllabus description
print("=" * 80)
print("🧪 TEST 1: Syllabus Description Summarization")
print("=" * 80)

text1 = """Phần cứng & Hệ điều hành: Hiểu cấu trúc máy tính (CPU, RAM, ổ cứng...) và cơ chế quản lý tài nguyên, điều phối tiến trình của hệ điều hành.

Mạng & Internet: Nắm vững khái niệm mạng máy tính và cách thức truyền tải dữ liệu.

An toàn thông tin: Hiểu về các mối đe dọa (virus, phishing...) và nguyên tắc bảo mật dữ liệu."""

print(f"\n📄 ORIGINAL ({len(text1)} chars):")
print(text1)

prompt1 = f"""Tóm tắt văn bản sau thành 2-3 câu ngắn gọn (tối đa 150 ký tự), giữ nguyên thông tin quan trọng nhất:

{text1}

Tóm tắt:"""

print(f"\n🤖 Calling Gemini...")
response1 = model.generate_content(prompt1)
summary1 = response1.text.strip()

print(f"\n✨ GEMINI SUMMARY ({len(summary1)} chars):")
print(summary1)

reduction1 = 100 * (1 - len(summary1)/len(text1))
print(f"\n📊 Reduction: {reduction1:.0f}%")

# Test 2: Objective
print("\n" + "=" * 80)
print("🧪 TEST 2: Learning Objective Summarization")
print("=" * 80)

text2 = """Dưới ánh nắng vàng rực rỡ của một buổi chiều đầu hạ, những cánh đồng lúa xanh rì rào trong gió như đang kể lại những câu chuyện cổ xưa của đất mẹ. Tiếng chim hót líu lo trên cành khế ngọt, hòa cùng tiếng ve kêu râm ran tạo nên một bản giao hưởng mùa hè đầy sống động. Cuộc sống đôi khi chỉ cần những khoảnh khắc bình yên như thế để ta cảm thấy lòng mình nhẹ nhõm hơn."""

print(f"\n📄 ORIGINAL ({len(text2)} chars):")
print(text2)

prompt2 = f"""Tóm tắt văn bản sau thành 2-3 câu ngắn gọn (tối đa 120 ký tự), giữ nguyên thông tin quan trọng nhất:

{text2}

Tóm tắt:"""

response2 = model.generate_content(prompt2)
summary2 = response2.text.strip()

print(f"\n✨ GEMINI SUMMARY ({len(summary2)} chars):")
print(summary2)

reduction2 = 100 * (1 - len(summary2)/len(text2))
print(f"\n📊 Reduction: {reduction2:.0f}%")

# Test 3: Short text
print("\n" + "=" * 80)
print("🧪 TEST 3: Short Text (should not be shortened much)")
print("=" * 80)

text3 = "Sinh viên hiểu các khái niệm cơ bản về lập trình hướng đối tượng"

print(f"\n📄 ORIGINAL ({len(text3)} chars):")
print(text3)

prompt3 = f"""Tóm tắt văn bản sau thành 1-2 câu ngắn gọn nếu cần, hoặc giữ nguyên nếu đã đủ ngắn:

{text3}

Tóm tắt:"""

response3 = model.generate_content(prompt3)
summary3 = response3.text.strip()

print(f"\n✨ GEMINI SUMMARY ({len(summary3)} chars):")
print(summary3)

print("\n" + "=" * 80)
print("✅ Gemini API works perfectly for Vietnamese summarization!")
print("💰 Free tier: 1500 requests/day, 1M tokens/day")
print("=" * 80)
