#!/usr/bin/env python3
"""
Summarize Worker
Worker riêng để lắng nghe queue 'ai_summarize_queue' và xử lý:
- SUMMARIZE_SYLLABUS (priority LOW, background processing)
"""
import sys
import os
import logging

# Add app directory to Python path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.workers.rabbitmq_consumer import RabbitMQConsumer
from app.workers.ai_handlers import AIMessageHandler

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)


def main():
    """Main entry point cho summarize worker"""
    logger.info("🚀 Starting Summarize Worker...")
    logger.info("📋 Listening to: ai_summarize_queue")
    logger.info("🎯 Actions: SUMMARIZE_SYLLABUS")
    
    # Initialize handler
    handler = AIMessageHandler()
    
    # Initialize consumer
    consumer = RabbitMQConsumer(
        queue_name='ai_summarize_queue',
        callback=handler.handle_message
    )
    
    try:
        # Connect to RabbitMQ
        consumer.connect()
        
        # Start consuming (blocking)
        consumer.start_consuming()
        
    except KeyboardInterrupt:
        logger.info("🛑 Worker stopped by user")
    except Exception as e:
        logger.error(f"❌ Worker crashed: {e}", exc_info=True)
        sys.exit(1)
    finally:
        consumer.stop()


if __name__ == '__main__':
    main()
