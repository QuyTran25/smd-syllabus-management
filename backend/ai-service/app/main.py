"""
AI Service - FastAPI Main Application
Handles HTTP API for AI task management
"""
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
import logging
import os
import asyncio

from app.config.settings import settings

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize FastAPI app
app = FastAPI(
    title="AI Service API",
    description="Microservice for AI-powered syllabus analysis",
    version="1.0.0",
    docs_url="/api/ai/docs",
    redoc_url="/api/ai/redoc",
    openapi_url="/api/ai/openapi.json"
)

# CORS Configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:8888",  # Gateway
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================
# Request/Response Models
# ============================================

class AnalysisRequest(BaseModel):
    """Request for AI analysis"""
    syllabus_id: str = Field(..., description="UUID of syllabus version")
    analysis_type: str = Field(..., description="Type: MAP_CLO_PLO, COMPARE_VERSIONS, SUMMARIZE_SYLLABUS")
    priority: Optional[str] = Field(default="MEDIUM", description="Priority: LOW, MEDIUM, HIGH")
    user_id: str = Field(..., description="UUID of requesting user")
    payload: Optional[Dict[str, Any]] = Field(default=None, description="Additional parameters")

class TaskStatusResponse(BaseModel):
    """Response for task status"""
    task_id: str
    action: str
    status: str  # QUEUED, PROCESSING, SUCCESS, ERROR
    progress: int
    message: Optional[str] = None
    result: Optional[Dict[str, Any]] = None
    error: Optional[str] = None
    timestamp: int

class HealthResponse(BaseModel):
    """Health check response"""
    status: str
    service: str
    version: str
    rabbitmq_connected: bool
    redis_connected: bool
    database_connected: bool

# ============================================
# Health Check
# ============================================

@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    """
    Health check endpoint for container orchestration
    """
    # TODO: Implement actual health checks
    return HealthResponse(
        status="healthy",
        service="ai-service",
        version="1.0.0",
        rabbitmq_connected=True,  # TODO: Check actual connection
        redis_connected=True,      # TODO: Check actual connection
        database_connected=True    # TODO: Check actual connection
    )

@app.get("/api/ai/health", response_model=HealthResponse, tags=["Health"])
async def api_health_check():
    """
    API health check (with /api/ai prefix for Gateway routing)
    """
    return await health_check()

# ============================================
# Task Management Endpoints
# ============================================

@app.get("/api/ai/tasks/{task_id}", response_model=TaskStatusResponse, tags=["Tasks"])
async def get_task_status(task_id: str):
    """
    Get status of an AI analysis task
    
    This endpoint allows Core Service or Frontend to poll task status.
    Status is stored in Redis for fast access.
    """
    try:
        # TODO: Query Redis for task status
        # For now, return mock response
        logger.info(f"📊 Fetching task status: {task_id}")
        
        # Mock response - will be replaced with Redis query
        return TaskStatusResponse(
            task_id=task_id,
            action="MOCK",
            status="QUEUED",
            progress=0,
            message="Task queued for processing",
            timestamp=0
        )
        
    except Exception as e:
        logger.error(f"❌ Error fetching task status: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/ai/analyze", response_model=TaskStatusResponse, tags=["Analysis"])
async def create_analysis_task(request: AnalysisRequest, background_tasks: BackgroundTasks):
    """
    Create a new AI analysis task
    
    This is a SYNC endpoint that:
    1. Validates the request
    2. Sends message to RabbitMQ queue
    3. Stores initial status in Redis
    4. Returns task_id for polling
    
    The actual AI processing happens ASYNC via RabbitMQ workers.
    """
    try:
        import uuid
        import time
        
        # Generate task ID
        task_id = str(uuid.uuid4())
        
        logger.info(f"📥 Creating {request.analysis_type} task: {task_id}")
        logger.info(f"   Syllabus: {request.syllabus_id}, User: {request.user_id}, Priority: {request.priority}")
        
        # TODO: Send to RabbitMQ queue
        # For now, log only
        logger.info(f"📤 [TODO] Send to RabbitMQ: {request.analysis_type}")
        
        # TODO: Store in Redis
        # For now, return mock response
        response = TaskStatusResponse(
            task_id=task_id,
            action=request.analysis_type,
            status="QUEUED",
            progress=0,
            message="Task queued for processing",
            timestamp=int(time.time() * 1000)
        )
        
        return response
        
    except Exception as e:
        logger.error(f"❌ Error creating task: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# ============================================
# Database Query Endpoints (for Core Service)
# ============================================

@app.get("/api/ai/analysis/{syllabus_id}", tags=["Analysis"])
async def get_syllabus_analysis_history(syllabus_id: str):
    """
    Get all AI analysis history for a syllabus
    
    This allows Core Service to query AI Service's database
    without direct database access (Database per Service pattern).
    """
    try:
        logger.info(f"📊 Fetching analysis history for syllabus: {syllabus_id}")
        
        # TODO: Query PostgreSQL ai_service.syllabus_ai_analysis
        # For now, return empty list
        return {
            "syllabus_id": syllabus_id,
            "analyses": []
        }
        
    except Exception as e:
        logger.error(f"❌ Error fetching analysis history: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# ============================================
# Startup/Shutdown Events
# ============================================

@app.on_event("startup")
async def startup_event():
    """
    Initialize connections on startup
    """
    logger.info("🚀 AI Service starting up...")
    logger.info(f"   Environment: {settings.ENVIRONMENT}")
    logger.info(f"   AI Provider: {settings.AI_PROVIDER}")
    logger.info(f"   Mock Mode: {settings.MOCK_MODE}")
    
    # TODO: Initialize Redis connection
    # TODO: Initialize Database connection pool
    # TODO: Verify RabbitMQ connection
    
    logger.info("✅ AI Service ready to accept requests")

@app.on_event("shutdown")
async def shutdown_event():
    """
    Cleanup on shutdown
    """
    logger.info("🛑 AI Service shutting down...")
    
    # TODO: Close Redis connection
    # TODO: Close Database connection pool
    # TODO: Close RabbitMQ connection
    
    logger.info("✅ AI Service shutdown complete")

# ============================================
# Root Endpoint
# ============================================

@app.get("/", tags=["Info"])
async def root():
    """
    Root endpoint with service information
    """
    return {
        "service": "AI Service",
        "version": "1.0.0",
        "status": "running",
        "docs": "/api/ai/docs",
        "health": "/api/ai/health"
    }

if __name__ == "__main__":
    import uvicorn
    
    # Run with hot reload in development
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8082,
        reload=True,
        log_level="info"
    )
