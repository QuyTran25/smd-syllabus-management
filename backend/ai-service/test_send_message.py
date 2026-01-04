#!/usr/bin/env python3
"""
Simple test to send a message to RabbitMQ and verify Python worker receives it
"""
import pika
import json
import time

def send_test_message():
    # Connect to RabbitMQ
    credentials = pika.PlainCredentials('guest', 'guest')
    parameters = pika.ConnectionParameters(
        host='localhost',
        port=5673,
        credentials=credentials
    )
    
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()
    
    # Create test message
    message = {
        "messageId": "test-message-789",
        "action": "MAP_CLO_PLO",
        "priority": "HIGH",
        "timestamp": int(time.time() * 1000),
        "userId": "test-user-123",
        "payload": {
            "syllabusId": "test-syllabus-abc",
            "curriculumId": "test-curriculum-xyz"
        }
    }
    
    # Send message DIRECTLY to queue (bypassing exchange for testing)
    print("📨 Sending test message DIRECTLY to ai_processing_queue...")
    print(f"📦 Message: {json.dumps(message, indent=2)}")
    
    channel.basic_publish(
        exchange='',  # Default exchange -> direct to queue name
        routing_key='ai_processing_queue',  # Queue name as routing key
        body=json.dumps(message),
        properties=pika.BasicProperties(
            priority=5,
            content_type='application/json',
            delivery_mode=2  # persistent
        )
    )
    
    print("✅ Message sent successfully!")
    
    connection.close()
    print("\n🎧 Now start the worker and it should process this message:")
    print("   python -m app.workers.analysis_worker")

if __name__ == "__main__":
    send_test_message()
