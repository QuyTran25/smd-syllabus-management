#!/usr/bin/env python3
"""
Analysis Worker
Worker để lắng nghe queue 'ai_processing_queue' và xử lý:
- MAP_CLO_PLO (priority HIGH)
- COMPARE_VERSIONS (priority MEDIUM)
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
    """Main entry point cho analysis worker"""
    logger.info("🚀 Starting Analysis Worker...")
    logger.info("📋 Listening to: ai_processing_queue")
    logger.info("🎯 Actions: MAP_CLO_PLO, COMPARE_VERSIONS")
    
    # Initialize handler
    handler = AIMessageHandler()
    
    # Initialize consumer
    consumer = RabbitMQConsumer(
        queue_name='ai_processing_queue',
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
