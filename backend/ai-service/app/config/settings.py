"""
Settings Configuration
Quản lý cấu hình từ environment variables
"""
from pydantic_settings import BaseSettings
from pydantic import Field
from typing import Optional


class Settings(BaseSettings):
    """Application configuration"""
    
    # =============================================
    # APPLICATION
    # =============================================
    APP_NAME: str = "SMD AI Service"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = False
    LOG_LEVEL: str = "INFO"
    
    # =============================================
    # RABBITMQ
    # =============================================
    RABBITMQ_HOST: str = Field(default="localhost", env="RABBITMQ_HOST")
    RABBITMQ_PORT: int = Field(default=5672, env="RABBITMQ_PORT")
    RABBITMQ_USER: str = Field(default="guest", env="RABBITMQ_USER")
    RABBITMQ_PASSWORD: str = Field(default="guest", env="RABBITMQ_PASSWORD")
    RABBITMQ_VHOST: str = Field(default="/", env="RABBITMQ_VHOST")
    
    # Queue names
    QUEUE_AI_PROCESSING: str = "ai_processing_queue"
    QUEUE_AI_SUMMARIZE: str = "ai_summarize_queue"
    QUEUE_AI_RESULT: str = "ai_result_queue"
    
    # Connection settings
    RABBITMQ_HEARTBEAT: int = 600
    RABBITMQ_BLOCKED_TIMEOUT: int = 300
    RABBITMQ_PREFETCH_COUNT: int = 1
    
    # =============================================
    # DATABASE (PostgreSQL)
    # =============================================
    DB_HOST: str = Field(default="localhost", env="DB_HOST")
    DB_PORT: int = Field(default=5432, env="DB_PORT")
    DB_NAME: str = Field(default="smd_database", env="DB_NAME")
    DB_USER: str = Field(default="smd_user", env="DB_USER")
    DB_PASSWORD: str = Field(default="smd_password", env="DB_PASSWORD")
    
    @property
    def database_url(self) -> str:
        return f"postgresql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
    
    # =============================================
    # REDIS
    # =============================================
    REDIS_HOST: str = Field(default="localhost", env="REDIS_HOST")
    REDIS_PORT: int = Field(default=6379, env="REDIS_PORT")
    REDIS_DB: int = Field(default=0, env="REDIS_DB")
    REDIS_PASSWORD: Optional[str] = Field(default=None, env="REDIS_PASSWORD")
    
    @property
    def redis_url(self) -> str:
        if self.REDIS_PASSWORD:
            return f"redis://:{self.REDIS_PASSWORD}@{self.REDIS_HOST}:{self.REDIS_PORT}/{self.REDIS_DB}"
        return f"redis://{self.REDIS_HOST}:{self.REDIS_PORT}/{self.REDIS_DB}"
    
    # =============================================
    # AI MODEL
    # =============================================
    # AI Provider
    AI_PROVIDER: str = Field(default="gemini", env="AI_PROVIDER")  # "gemini" or "local"
    
    # Gemini API
    GEMINI_API_KEY: str = Field(default="", env="GEMINI_API_KEY")
    GEMINI_MODEL: str = Field(default="gemini-1.5-flash", env="GEMINI_MODEL")
    
    # Local models
    AI_MODEL_ENABLED: bool = Field(default=False, env="AI_MODEL_ENABLED")
    AI_MODEL_NAME: str = Field(default="vinai/phogpt-4b-v1-instruct", env="AI_MODEL_NAME")
    AI_MODEL_DEVICE: str = Field(default="cpu", env="AI_MODEL_DEVICE")
    AI_MODEL_MAX_LENGTH: int = Field(default=2048, env="AI_MODEL_MAX_LENGTH")
    EMBEDDING_MODEL: str = Field(default="bkai-foundation-models/vietnamese-bi-encoder", env="EMBEDDING_MODEL")
    USE_8BIT_QUANTIZATION: bool = Field(default=True, env="USE_8BIT_QUANTIZATION")
    
    # =============================================
    # WORKER
    # =============================================
    WORKER_PROCESSING_TIMEOUT: int = Field(default=300, env="WORKER_PROCESSING_TIMEOUT")
    WORKER_MAX_RETRIES: int = Field(default=3, env="WORKER_MAX_RETRIES")
    WORKER_RETRY_DELAY: int = Field(default=5, env="WORKER_RETRY_DELAY")
    
    # Mock mode
    MOCK_MODE: bool = Field(default=True, env="MOCK_MODE")
    MOCK_PROCESSING_DELAY: int = Field(default=2, env="MOCK_PROCESSING_DELAY")
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = True


# Singleton instance
settings = Settings()
