"""
RabbitMQ Consumer Base Class
Lắng nghe messages từ RabbitMQ queue
"""
import pika
import json
import logging
from typing import Callable, Optional

logger = logging.getLogger(__name__)


class RabbitMQConsumer:
    """Base RabbitMQ Consumer với support cho priority queues"""
    
    def __init__(self, queue_name: str, callback: Callable, rabbitmq_config: dict = None):
        """
        Args:
            queue_name: Tên queue cần lắng nghe
            callback: Function xử lý message nhận được
            rabbitmq_config: Dict chứa host, port, username, password
        """
        self.queue_name = queue_name
        self.callback = callback
        self.connection: Optional[pika.BlockingConnection] = None
        self.channel: Optional[pika.channel.Channel] = None
        
        # Default config (sẽ đọc từ env trong production)
        self.config = rabbitmq_config or {
            'host': 'localhost',  # localhost khi chạy local
            'port': 5673,  # Mapped port
            'username': 'guest',
            'password': 'guest'
        }
        
    def connect(self):
        """Kết nối tới RabbitMQ"""
        try:
            credentials = pika.PlainCredentials(
                self.config['username'],
                self.config['password']
            )
            
            parameters = pika.ConnectionParameters(
                host=self.config['host'],
                port=self.config['port'],
                credentials=credentials,
                heartbeat=600,
                blocked_connection_timeout=300
            )
            
            self.connection = pika.BlockingConnection(parameters)
            self.channel = self.connection.channel()
            
            # Declare queue (idempotent - queue đã có sẵn trong definitions.json)
            self.channel.queue_declare(
                queue=self.queue_name,
                durable=True,
                arguments={'x-max-priority': 5}
            )
            
            # QoS - chỉ xử lý 1 message tại 1 thời điểm
            self.channel.basic_qos(prefetch_count=1)
            
            logger.info(f"✅ Connected to RabbitMQ queue: {self.queue_name}")
            
        except Exception as e:
            logger.error(f"❌ Failed to connect to RabbitMQ: {e}")
            raise
    
    def start_consuming(self):
        """Bắt đầu lắng nghe messages"""
        logger.info(f"🎧 Waiting for messages in '{self.queue_name}'...")
        
        self.channel.basic_consume(
            queue=self.queue_name,
            on_message_callback=self._on_message,
            auto_ack=False  # Manual ACK
        )
        
        try:
            self.channel.start_consuming()
        except KeyboardInterrupt:
            logger.info("🛑 Stopping consumer...")
            self.stop()
        except Exception as e:
            logger.error(f"❌ Error in consuming loop: {e}")
            self.stop()
            raise
    
    def _on_message(self, ch, method, properties, body):
        """Callback khi nhận được message"""
        try:
            # Parse JSON message
            message = json.loads(body)
            message_id = message.get('message_id', 'unknown')
            action = message.get('action', 'unknown')
            
            logger.info(f"📨 Received message: {message_id} - Action: {action}")
            
            # Process message với custom callback
            result = self.callback(message)
            
            # ACK message (thành công)
            ch.basic_ack(delivery_tag=method.delivery_tag)
            logger.info(f"✅ Message {message_id} processed successfully")
            
            return result
            
        except json.JSONDecodeError as e:
            logger.error(f"❌ Invalid JSON message: {e}")
            # NACK without requeue (message lỗi format)
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
            
        except Exception as e:
            logger.error(f"❌ Error processing message: {e}", exc_info=True)
            # NACK and requeue (lỗi xử lý, có thể retry)
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
    
    def stop(self):
        """Dừng consumer và đóng kết nối"""
        if self.channel:
            try:
                self.channel.stop_consuming()
            except:
                pass
                
        if self.connection:
            try:
                self.connection.close()
            except:
                pass
                
        logger.info("🔌 RabbitMQ connection closed")
