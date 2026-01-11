# ============================================
# TEST AI SUMMARIZE - SIMPLE VERSION
# Test AI worker trực tiếp qua RabbitMQ
# ============================================

Write-Host "`n🧪 TEST AI SUMMARIZE (DIRECT)" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Không cần token, gửi trực tiếp message qua RabbitMQ
Write-Host "📝 Nhập Syllabus ID để test (ví dụ: 1, 124001):" -ForegroundColor Yellow
$syllabusId = Read-Host "Syllabus ID"

if ([string]::IsNullOrWhiteSpace($syllabusId)) {
    $syllabusId = "124001"  # Default
    Write-Host "Sử dụng default: $syllabusId" -ForegroundColor Gray
}

Write-Host "`n🚀 Sending test message to AI Worker..." -ForegroundColor Cyan

# Create temporary Python test script (using single quotes to prevent PowerShell interpretation)
$tempPyScript = @'
import pika
import json
from datetime import datetime

# Create test message for real syllabus
test_message = {
    'messageId': f'test-{datetime.now().strftime("%Y%m%d-%H%M%S")}',
    'action': 'SUMMARIZE_SYLLABUS',
    'priority': 'LOW',
    'timestamp': datetime.utcnow().isoformat() + 'Z',
    'userId': 'test-user-001',
    'payload': {
        'syllabus_id': 'SYLLABUS_ID_PLACEHOLDER',
        'syllabus_data': {
            'course_name': 'Test Course - Syllabus ID SYLLABUS_ID_PLACEHOLDER',
            'description': 'This is a test to verify AI summarization is working with real data from database.',
            'theory_hours': 30,
            'practice_hours': 30,
            'learning_outcomes': [
                {'description': 'Test learning outcome 1'},
                {'description': 'Test learning outcome 2'}
            ],
            'assessment_scheme': [
                {'type': 'Thi giua ky', 'weight': 30},
                {'type': 'Bai tap', 'weight': 20},
                {'type': 'Du an', 'weight': 20},
                {'type': 'Thi cuoi ky', 'weight': 30}
            ],
            'prerequisites': ['Tien quyet 1', 'Tien quyet 2']
        }
    }
}

print('\n📤 Sending message to RabbitMQ...')
print(f'   Syllabus ID: {test_message["payload"]["syllabus_id"]}')
print(f'   Message ID: {test_message["messageId"]}\n')

try:
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(
            host='localhost',
            port=5672,
            credentials=pika.PlainCredentials('guest', 'guest')
        )
    )
    
    channel = connection.channel()
    channel.queue_declare(
        queue='ai_summarize_queue',
        durable=True,
        arguments={'x-max-priority': 3}
    )
    
    channel.basic_publish(
        exchange='',
        routing_key='ai_summarize_queue',
        body=json.dumps(test_message, ensure_ascii=False),
        properties=pika.BasicProperties(
            delivery_mode=2,
            priority=1
        )
    )
    
    print('✅ Message sent successfully!')
    print('\n📊 Next steps:')
    print('   1. Check AI Worker terminal for processing logs')
    print('   2. Look for: [Received] Action: SUMMARIZE_SYLLABUS')
    print('   3. Wait ~14 seconds for AI processing')
    print('   4. Check for: ✅ AI Summarization completed\n')
    
    connection.close()
    
except Exception as e:
    print(f'\n❌ Error: {e}\n')
'@

# Replace placeholder with actual syllabus ID
$tempPyScript = $tempPyScript -replace 'SYLLABUS_ID_PLACEHOLDER', $syllabusId

# Write temporary script
$tempPyScript | Out-File -FilePath "temp_test_summarize.py" -Encoding UTF8

# Activate venv và chạy script
.\venv\Scripts\Activate.ps1
python temp_test_summarize.py

# Clean up
Remove-Item "temp_test_summarize.py" -ErrorAction SilentlyContinue

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "Kiểm tra terminal AI Worker để xem kết quả!`n" -ForegroundColor Yellow
