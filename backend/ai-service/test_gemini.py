"""
Test Gemini API for Vietnamese Summarization
"""
import os
os.environ['AI_PROVIDER'] = 'gemini'
os.environ['GEMINI_API_KEY'] = 'AIzaSyBRCmOcTeQkrgEXKPznbFfu10ptPtNZYqs'
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

text1 = """Thuyết giảng (Lectures): Giảng giải lý thuyết, phân tích cơ chế hoạt động của cấu trúc dữ liệu và từng bước của giải thuật.
Thảo luận nhóm: Phân tích các tình huống (Case studies) để chọn giải pháp tối ưu.
Thực hành tại phòng máy (Lab): Cài đặt trực tiếp các giải thuật bằng ngôn ngữ lập trình (thường là C/C++, Java hoặc Python).
Học tập dựa trên vấn đề (PBL): Giao bài tập lớn (Project) để sinh viên tự giải quyết một bài toán hoàn chỉnh."""

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

