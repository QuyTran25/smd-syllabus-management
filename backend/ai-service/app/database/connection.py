"""
Database Connection Pool for AI Service
Manages PostgreSQL connection for ai_service schema
"""
import asyncpg
import logging
from typing import Optional

from app.config.settings import settings

logger = logging.getLogger(__name__)

# Global connection pool
_db_pool: Optional[asyncpg.Pool] = None


async def init_db_pool() -> asyncpg.Pool:
    """
    Initialize PostgreSQL connection pool
    
    Returns:
        asyncpg.Pool: Database connection pool
    """
    global _db_pool
    
    if _db_pool is not None:
        logger.warning("⚠️ Database pool already initialized")
        return _db_pool
    
    try:
        logger.info(f"🔌 Connecting to PostgreSQL: {settings.POSTGRES_HOST}:{settings.POSTGRES_PORT}")
        
        # Build connection string
        dsn = (
            f"postgresql://{settings.POSTGRES_USER}:{settings.POSTGRES_PASSWORD}"
            f"@{settings.POSTGRES_HOST}:{settings.POSTGRES_PORT}/{settings.POSTGRES_DB}"
        )
        
        _db_pool = await asyncpg.create_pool(
            dsn,
            min_size=2,
            max_size=10,
            command_timeout=60,
            # Set search_path to ai_service schema
            server_settings={'search_path': 'ai_service'}
        )
        
        # Test connection
        async with _db_pool.acquire() as conn:
            version = await conn.fetchval('SELECT version()')
            logger.info(f"✅ Connected to PostgreSQL: {version[:50]}...")
            
            # Verify ai_service schema exists
            schema_exists = await conn.fetchval(
                "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = 'ai_service')"
            )
            if not schema_exists:
                logger.error("❌ Schema 'ai_service' does not exist!")
                raise Exception("ai_service schema not found in database")
            
            logger.info("✅ Schema 'ai_service' verified")
        
        return _db_pool
        
    except Exception as e:
        logger.error(f"❌ Failed to connect to PostgreSQL: {e}")
        raise


async def get_db_pool() -> asyncpg.Pool:
    """
    Get the database connection pool
    
    Returns:
        asyncpg.Pool: The initialized pool
    """
    global _db_pool
    
    if _db_pool is None:
        logger.warning("⚠️ Database pool not initialized, initializing now...")
        await init_db_pool()
    
    return _db_pool


async def close_db_pool():
    """
    Close the database connection pool
    """
    global _db_pool
    
    if _db_pool is not None:
        logger.info("🔌 Closing database connection pool...")
        await _db_pool.close()
        _db_pool = None
        logger.info("✅ Database pool closed")
    else:
        logger.warning("⚠️ Database pool already closed or not initialized")


async def execute_query(query: str, *args):
    """
    Execute a query and return results
    
    Args:
        query: SQL query string
        *args: Query parameters
        
    Returns:
        Query results
    """
    pool = await get_db_pool()
    async with pool.acquire() as conn:
        return await conn.fetch(query, *args)


async def execute_one(query: str, *args):
    """
    Execute a query and return single row
    
    Args:
        query: SQL query string
        *args: Query parameters
        
    Returns:
        Single row or None
    """
    pool = await get_db_pool()
    async with pool.acquire() as conn:
        return await conn.fetchrow(query, *args)


async def execute_insert(query: str, *args):
    """
    Execute an INSERT/UPDATE/DELETE query
    
    Args:
        query: SQL query string
        *args: Query parameters
        
    Returns:
        Status message
    """
    pool = await get_db_pool()
    async with pool.acquire() as conn:
        return await conn.execute(query, *args)
