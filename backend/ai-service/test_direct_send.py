"""
Simple test script to send message directly to RabbitMQ
No authentication needed - bypasses API layer
"""
import pika
import json
from datetime import datetime
import sys

def send_test_message(syllabus_id):
    """Send test message to AI Worker via RabbitMQ"""
    
    # Create test message
    test_message = {
        'messageId': f'test-{datetime.now().strftime("%Y%m%d-%H%M%S")}',
        'action': 'SUMMARIZE_SYLLABUS',
        'priority': 'LOW',
        'timestamp': datetime.utcnow().isoformat() + 'Z',
        'userId': 'test-user-001',
        'payload': {
            'syllabus_id': syllabus_id,
            'syllabus_data': {
                'course_name': f'Test Course - Syllabus ID {syllabus_id}',
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
        # Connect to RabbitMQ
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(
                host='localhost',
                port=5672,
                credentials=pika.PlainCredentials('guest', 'guest')
            )
        )
        
        channel = connection.channel()
        
        # Declare queue with same settings as worker
        channel.queue_declare(
            queue='ai_summarize_queue',
            durable=True,
            arguments={'x-max-priority': 3}
        )
        
        # Send message
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
        sys.exit(1)

if __name__ == '__main__':
    # Get syllabus ID from command line or use default
    if len(sys.argv) > 1:
        syllabus_id = sys.argv[1]
    else:
        syllabus_id = input('Nhập Syllabus ID (Enter = 124001): ').strip() or '124001'
    
    print(f'\n🧪 Testing AI Summarize with Syllabus ID: {syllabus_id}')
    send_test_message(syllabus_id)
