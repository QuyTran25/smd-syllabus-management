#!/usr/bin/env python3
"""
Test AI Summarization Function
Sends a test message to RabbitMQ ai_summarize_queue
"""
import pika
import json
from datetime import datetime

# Create test message
test_message = {
    "messageId": f"test-{datetime.now().strftime('%Y%m%d-%H%M%S')}",
    "action": "SUMMARIZE_SYLLABUS",
    "priority": "LOW",
    "timestamp": datetime.utcnow().isoformat() + "Z",
    "userId": "test-user-001",
    "payload": {
        "syllabus_id": "syllabus-test-001",
        "syllabus_data": {
            "course_name": "Thiết kế và tối ưu hóa CSDL",
            "description": "Môn học trang bị kiến thức về thiết kế CSDL quan hệ, chuẩn hóa, và tối ưu truy vấn. Sinh viên học cách thiết kế ERD, chuẩn hóa đến 3NF, viết SQL queries phức tạp và tối ưu hiệu năng database.",
            "theory_hours": 30,
            "practice_hours": 30,
            "learning_outcomes": [
                {"description": "Thiết kế ERD và chuẩn hóa CSDL đến 3NF"},
                {"description": "Viết truy vấn SQL phức tạp với JOIN, subquery"},
                {"description": "Tối ưu hiệu năng database với indexes"}
            ],
            "assessment_scheme": [
                {"type": "Thi giữa kỳ", "weight": 30},
                {"type": "Bài tập", "weight": 20},
                {"type": "Dự án", "weight": 20},
                {"type": "Thi cuối kỳ", "weight": 30}
            ],
            "prerequisites": ["Cấu trúc dữ liệu và giải thuật", "OOP"]
        }
    }
}

print("\n🧪 TEST AI SUMMARIZATION")
print("=" * 50)
print(f"\n📝 Test Message ID: {test_message['messageId']}")
print(f"📋 Action: {test_message['action']}")
print(f"👤 User: {test_message['userId']}")
print(f"📚 Course: {test_message['payload']['syllabus_data']['course_name']}")

# Connect to RabbitMQ
try:
    print("\n🔌 Connecting to RabbitMQ...")
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
    
    # Send message
    print("📤 Sending message to ai_summarize_queue...")
    channel.basic_publish(
        exchange='',
        routing_key='ai_summarize_queue',
        body=json.dumps(test_message, ensure_ascii=False),
        properties=pika.BasicProperties(
            delivery_mode=2,  # Make message persistent
            priority=1
        )
    )
    
    print("\n✅ Test message sent successfully!")
    print("=" * 50)
    print("\n📊 Next steps:")
    print("   1. Check the summarize_worker terminal for processing logs")
    print("   2. Look for: [Received] Action: SUMMARIZE_SYLLABUS")
    print("   3. Wait ~8-15 seconds for AI processing")
    print("   4. Check for: ✅ SUMMARIZE_SYLLABUS completed\n")
    
    connection.close()
    
except pika.exceptions.AMQPConnectionError as e:
    print(f"\n❌ Failed to connect to RabbitMQ: {e}")
    print("   Make sure RabbitMQ is running:")
    print("   docker-compose up -d rabbitmq\n")
except Exception as e:
    print(f"\n❌ Error: {e}\n")
