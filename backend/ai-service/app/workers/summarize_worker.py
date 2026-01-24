#!/usr/bin/env python3
"""
Summarize Worker
Worker riêng để lắng nghe queue 'ai_summarize_queue' và xử lý:
- SUMMARIZE_SYLLABUS (priority LOW, background processing)
"""
import sys
import os
import logging
import json

# Add app directory to Python path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.config.settings import settings
from app.config.rabbitmq import RabbitMQConnectionManager
from app.workers.ai_handlers import AIMessageHandler

# Setup logging
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL),
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)


def main():
    """Main entry point cho summarize worker"""
    logger.info("🚀 Starting Summarize Worker...")
    logger.info(f"📋 Listening to: {settings.QUEUE_AI_SUMMARIZE}")
    logger.info("🎯 Actions: SUMMARIZE_SYLLABUS")
    logger.info(f"🤖 Mock Mode: {settings.MOCK_MODE}")
    
    # Initialize RabbitMQ connection first
    conn_manager = RabbitMQConnectionManager(
        host=settings.RABBITMQ_HOST,
        port=settings.RABBITMQ_PORT,
        username=settings.RABBITMQ_USER,
        password=settings.RABBITMQ_PASSWORD,
        vhost=settings.RABBITMQ_VHOST,
        heartbeat=settings.RABBITMQ_HEARTBEAT,
        blocked_timeout=settings.RABBITMQ_BLOCKED_TIMEOUT
    )
    
    # Initialize handler with RabbitMQ manager
    handler = AIMessageHandler(rabbitmq_manager=conn_manager)
    
    def on_message_callback(ch, method, properties, body):
        """Callback khi nhận được message"""
        try:
            # Parse JSON message
            message = json.loads(body)
            
            # Process message (async function)
            import asyncio
            result = asyncio.run(handler.handle_message(message))
            
            # ACK message (thành công)
            ch.basic_ack(delivery_tag=method.delivery_tag)
            
            return result
            
        except json.JSONDecodeError as e:
            logger.error(f"❌ Invalid JSON message: {e}")
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
            
        except Exception as e:
            logger.error(f"❌ Error processing message: {e}", exc_info=True)
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
    
    try:
        # Connect to RabbitMQ
        if not conn_manager.connect():
            logger.error("❌ Failed to connect to RabbitMQ. Exiting...")
            sys.exit(1)
        
        # Declare queue với priority support
        conn_manager.declare_queue(
            queue_name=settings.QUEUE_AI_SUMMARIZE,
            durable=True,
            max_priority=3
        )
        
        # Set QoS
        conn_manager.set_qos(prefetch_count=settings.RABBITMQ_PREFETCH_COUNT)
        
        # Start consuming (blocking)
        conn_manager.start_consuming(
            queue_name=settings.QUEUE_AI_SUMMARIZE,
            callback=on_message_callback
        )
        
    except KeyboardInterrupt:
        logger.info("🛑 Worker stopped by user")
    except Exception as e:
        logger.error(f"❌ Worker crashed: {e}", exc_info=True)
        sys.exit(1)
    finally:
        conn_manager.close()


if __name__ == '__main__':
    main()
