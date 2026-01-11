"""
RabbitMQ Connection Manager
Quản lý kết nối bền vững (Robust Connection) tới RabbitMQ với auto-reconnect
"""
import pika
import logging
import time
import json
from typing import Optional, Callable
from pika.adapters.blocking_connection import BlockingChannel
from pika.exceptions import AMQPConnectionError, AMQPChannelError

logger = logging.getLogger(__name__)


class RabbitMQConnectionManager:
    """
    Quản lý kết nối RabbitMQ với tính năng:
    - Auto-reconnect khi mất kết nối
    - Heartbeat để duy trì kết nối
    - Retry logic với exponential backoff
    """
    
    def __init__(self, host: str, port: int, username: str, password: str, 
                 vhost: str = "/", heartbeat: int = 600, blocked_timeout: int = 300):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.vhost = vhost
        self.heartbeat = heartbeat
        self.blocked_timeout = blocked_timeout
        
        self.connection: Optional[pika.BlockingConnection] = None
        self.channel: Optional[BlockingChannel] = None
        self.is_connected = False
        self.retry_count = 0
        self.max_retries = 10
        
    def connect(self) -> bool:
        """
        Kết nối tới RabbitMQ với retry logic
        
        Returns:
            bool: True nếu kết nối thành công
        """
        while self.retry_count < self.max_retries:
            try:
                logger.info(f"🔌 Connecting to RabbitMQ at {self.host}:{self.port}...")
                
                # Credentials
                credentials = pika.PlainCredentials(self.username, self.password)
                
                # Connection parameters với heartbeat
                parameters = pika.ConnectionParameters(
                    host=self.host,
                    port=self.port,
                    virtual_host=self.vhost,
                    credentials=credentials,
                    heartbeat=self.heartbeat,
                    blocked_connection_timeout=self.blocked_timeout,
                    connection_attempts=3,
                    retry_delay=2,
                )
                
                # Establish connection
                self.connection = pika.BlockingConnection(parameters)
                self.channel = self.connection.channel()
                
                self.is_connected = True
                self.retry_count = 0
                
                logger.info("✅ [Connected] Successfully connected to RabbitMQ!")
                return True
                
            except AMQPConnectionError as e:
                self.retry_count += 1
                wait_time = min(2 ** self.retry_count, 60)  # Exponential backoff, max 60s
                
                logger.warning(
                    f"⚠️ Connection failed (attempt {self.retry_count}/{self.max_retries}): {e}"
                )
                
                if self.retry_count < self.max_retries:
                    logger.info(f"🔄 Retrying in {wait_time} seconds...")
                    time.sleep(wait_time)
                else:
                    logger.error("❌ Max retries reached. Could not connect to RabbitMQ.")
                    return False
                    
            except Exception as e:
                logger.error(f"❌ Unexpected error during connection: {e}", exc_info=True)
                return False
        
        return False
    
    def declare_queue(self, queue_name: str, durable: bool = True, 
                     max_priority: Optional[int] = None) -> bool:
        """
        Khai báo queue (idempotent)
        """
        if not self.is_connected or not self.channel:
            logger.error("❌ Cannot declare queue: Not connected")
            return False
        
        try:
            arguments = {}
            if max_priority:
                arguments['x-max-priority'] = max_priority
            
            self.channel.queue_declare(
                queue=queue_name,
                durable=durable,
                arguments=arguments if arguments else None
            )
            
            logger.info(f"✅ Queue declared: {queue_name}" + 
                       (f" (priority: {max_priority})" if max_priority else ""))
            return True
            
        except AMQPChannelError as e:
            logger.error(f"❌ Error declaring queue {queue_name}: {e}")
            return False
    
    def set_qos(self, prefetch_count: int = 1) -> bool:
        """Set Quality of Service (QoS)"""
        if not self.channel:
            return False
        
        try:
            self.channel.basic_qos(prefetch_count=prefetch_count)
            logger.info(f"✅ QoS set: prefetch_count={prefetch_count}")
            return True
        except Exception as e:
            logger.error(f"❌ Error setting QoS: {e}")
            return False
    
    def start_consuming(self, queue_name: str, callback: Callable) -> None:
        """
        Bắt đầu lắng nghe messages từ queue
        """
        if not self.is_connected or not self.channel:
            logger.error("❌ Cannot start consuming: Not connected")
            return
        
        logger.info(f"🎧 Listening to queue: {queue_name}")
        logger.info(f"⏱️ Heartbeat: {self.heartbeat}s")
        logger.info("🔔 [Connected] Waiting for messages from RabbitMQ...")
        
        try:
            self.channel.basic_consume(
                queue=queue_name,
                on_message_callback=callback,
                auto_ack=False  # Manual ACK
            )
            
            # Blocking call
            self.channel.start_consuming()
            
        except KeyboardInterrupt:
            logger.info("🛑 Stopping consumer (Keyboard Interrupt)...")
            self.stop_consuming()
            
        except AMQPConnectionError as e:
            logger.error(f"❌ Connection lost: {e}")
            self.is_connected = False
            
        except Exception as e:
            logger.error(f"❌ Error in consuming loop: {e}", exc_info=True)
            self.stop_consuming()
    
    def publish_message(self, queue_name: str, message: dict) -> bool:
        """Publish message to queue"""
        if not self.is_connected or not self.channel:
            logger.error("❌ Cannot publish: Not connected")
            return False
        
        try:
            self.channel.basic_publish(
                exchange='',
                routing_key=queue_name,
                body=json.dumps(message, ensure_ascii=False)
            )
            logger.debug(f"✅ Published to {queue_name}: {message.get('messageId', 'N/A')}")
            return True
        except Exception as e:
            logger.error(f"❌ Error publishing to {queue_name}: {e}")
            return False
    
    def stop_consuming(self) -> None:
        """Dừng consumer"""
        if self.channel:
            try:
                self.channel.stop_consuming()
                logger.info("⏸️ Consumer stopped")
            except Exception as e:
                logger.warning(f"⚠️ Error stopping consumer: {e}")
    
    def close(self) -> None:
        """Đóng kết nối"""
        self.stop_consuming()
        
        if self.channel and self.channel.is_open:
            try:
                self.channel.close()
                logger.info("📪 Channel closed")
            except Exception as e:
                logger.warning(f"⚠️ Error closing channel: {e}")
        
        if self.connection and self.connection.is_open:
            try:
                self.connection.close()
                logger.info("🔌 Connection closed")
            except Exception as e:
                logger.warning(f"⚠️ Error closing connection: {e}")
        
        self.is_connected = False
    
    def __enter__(self):
        """Context manager entry"""
        self.connect()
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit"""
        self.close()
