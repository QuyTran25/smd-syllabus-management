"""
Quick check if Gemini API is working (indicates quota available)
"""
import os
import google.generativeai as genai

# Load API key from .env
api_key = 'AIzaSyAShxKMx7W0-1kTkhH1FqfqaT-Cp6tNbKY'

try:
    genai.configure(api_key=api_key)
    model = genai.GenerativeModel('gemini-2.5-flash')
    
    print("🧪 Testing Gemini API...")
    response = model.generate_content("Say hello in Vietnamese in 5 words")
    print(f"✅ SUCCESS! Response: {response.text}")
    print("\n✅ Gemini API is working - You have quota remaining!")
    print("📊 To see exact quota, visit: https://aistudio.google.com/app/apikey")
    
except Exception as e:
    error_msg = str(e)
    if '429' in error_msg or 'quota' in error_msg.lower():
        print("❌ QUOTA EXCEEDED - You've used all 1500 requests today")
        print("⏰ Quota resets at midnight Pacific Time (PT)")
    elif '403' in error_msg or 'permission' in error_msg.lower():
        print("❌ API KEY ERROR - Key may be invalid or restricted")
    else:
        print(f"❌ ERROR: {error_msg}")
