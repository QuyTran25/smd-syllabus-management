# ============================================
# TEST SUMMARIZE FUNCTION
# Script to test AI summarization
# ============================================

Write-Host "`n🧪 TEST AI SUMMARIZATION" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Check if .env exists
if (-not (Test-Path ".env")) {
    Write-Host "⚠️  .env not found, copying from .env.example..." -ForegroundColor Yellow
    Copy-Item .env.example .env
}

# Check MOCK_MODE
$envContent = Get-Content .env -Raw
if ($envContent -match "MOCK_MODE=true") {
    Write-Host "⚠️  MOCK_MODE is enabled in .env" -ForegroundColor Yellow
    Write-Host "   To use real AI, change to: MOCK_MODE=false`n" -ForegroundColor White
}

# Activate venv
.\venv\Scripts\Activate.ps1

# Create test message
$testMessage = @{
    messageId = "test-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    action = "SUMMARIZE_SYLLABUS"
    priority = "LOW"
    timestamp = (Get-Date).ToUniversalTime().ToString("o")
    userId = "test-user-001"
    payload = @{
        syllabus_id = "syllabus-test-001"
        syllabus_data = @{
            course_name = "Thiết kế và tối ưu hóa CSDL"
            description = "Môn học trang bị kiến thức về thiết kế CSDL quan hệ, chuẩn hóa, và tối ưu truy vấn. Sinh viên học cách thiết kế ERD, chuẩn hóa đến 3NF, viết SQL queries phức tạp và tối ưu hiệu năng database."
            theory_hours = 30
            practice_hours = 30
            learning_outcomes = @(
                @{ description = "Thiết kế ERD và chuẩn hóa CSDL đến 3NF" },
                @{ description = "Viết truy vấn SQL phức tạp với JOIN, subquery" },
                @{ description = "Tối ưu hiệu năng database với indexes" }
            )
            assessment_scheme = @(
                @{ type = "Thi giữa kỳ"; weight = 30 },
                @{ type = "Bài tập"; weight = 20 },
                @{ type = "Dự án"; weight = 20 },
                @{ type = "Thi cuối kỳ"; weight = 30 }
            )
            prerequisites = @("Cấu trúc dữ liệu và giải thuật", "OOP")
        }
    }
} | ConvertTo-Json -Depth 10

# Save to file
$testMessage | Out-File -FilePath "test_message_summarize.json" -Encoding UTF8

Write-Host "📝 Test message saved to: test_message_summarize.json" -ForegroundColor Green
Write-Host "`n🚀 Starting test..." -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Send message via Python
python -c @"
import pika
import json

with open('test_message_summarize.json', 'r', encoding='utf-8') as f:
    message = json.load(f)

connection = pika.BlockingConnection(
    pika.ConnectionParameters(
        host='localhost',
        port=5672,
        credentials=pika.PlainCredentials('guest', 'guest')
    )
)

channel = connection.channel()
channel.queue_declare(queue='ai_summarize_queue', durable=True)

channel.basic_publish(
    exchange='',
    routing_key='ai_summarize_queue',
    body=json.dumps(message),
    properties=pika.BasicProperties(
        delivery_mode=2,
        priority=1
    )
)

print('✅ Test message sent to ai_summarize_queue')
print(f'   Message ID: {message[\"messageId\"]}')
print(f'   Action: {message[\"action\"]}')
print('')
print('📊 Check worker terminal for processing logs...')

connection.close()
"@

Write-Host "`n✅ Test completed!" -ForegroundColor Green
Write-Host "Check the summarize_worker terminal to see results.`n" -ForegroundColor White
