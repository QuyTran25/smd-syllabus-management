"""
Repository for AI Analysis Results
Handles database operations for syllabus_ai_analysis table
"""
import json
import logging
from typing import Optional, Dict, Any, List
from uuid import UUID
import uuid

from app.database.connection import get_db_pool

logger = logging.getLogger(__name__)


class AnalysisRepository:
    """Repository for AI analysis results"""
    
    @staticmethod
    async def save_analysis(
        analysis_id: str,
        syllabus_version_id: str,
        analysis_type: str,
        result: Dict[str, Any],
        model_used: str,
        confidence_score: Optional[float] = None,
        processing_time_ms: Optional[int] = None
    ) -> bool:
        """
        Save AI analysis result to database
        
        Args:
            analysis_id: UUID of the analysis (task_id)
            syllabus_version_id: UUID of syllabus version
            analysis_type: Type of analysis (MAP_CLO_PLO, COMPARE_VERSIONS, SUMMARIZE_SYLLABUS)
            result: Analysis result as JSON
            model_used: AI model used (gemini-pro, gpt-4, etc.)
            confidence_score: Confidence score (0.0 - 1.0)
            processing_time_ms: Processing time in milliseconds
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            pool = await get_db_pool()
            
            # Convert analysis_type to match ENUM in database
            # MAP_CLO_PLO -> PLO_ALIGNMENT
            # COMPARE_VERSIONS -> VERSION_DIFF
            # SUMMARIZE_SYLLABUS -> SUMMARY
            type_mapping = {
                'MAP_CLO_PLO': 'PLO_ALIGNMENT',
                'COMPARE_VERSIONS': 'VERSION_DIFF',
                'SUMMARIZE_SYLLABUS': 'SUMMARY'
            }
            db_analysis_type = type_mapping.get(analysis_type, analysis_type)
            
            query = """
                INSERT INTO syllabus_ai_analysis (
                    id,
                    syllabus_version_id,
                    analysis_type,
                    result,
                    model_used,
                    confidence_score,
                    processing_time_ms,
                    created_at,
                    updated_at
                ) VALUES ($1, $2, $3::analysis_type, $4, $5, $6, $7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO UPDATE SET
                    result = EXCLUDED.result,
                    confidence_score = EXCLUDED.confidence_score,
                    processing_time_ms = EXCLUDED.processing_time_ms,
                    updated_at = CURRENT_TIMESTAMP
            """
            
            async with pool.acquire() as conn:
                await conn.execute(
                    query,
                    uuid.UUID(analysis_id),
                    uuid.UUID(syllabus_version_id),
                    db_analysis_type,
                    json.dumps(result),
                    model_used,
                    confidence_score,
                    processing_time_ms
                )
            
            logger.info(f"✅ Saved analysis to database: {analysis_id} ({db_analysis_type})")
            return True
            
        except Exception as e:
            logger.error(f"❌ Failed to save analysis to database: {e}", exc_info=True)
            return False
    
    @staticmethod
    async def get_analysis_by_id(analysis_id: str) -> Optional[Dict[str, Any]]:
        """
        Get analysis result by ID
        
        Args:
            analysis_id: UUID of the analysis
            
        Returns:
            Dict with analysis data or None
        """
        try:
            pool = await get_db_pool()
            
            query = """
                SELECT 
                    id,
                    syllabus_version_id,
                    analysis_type,
                    result,
                    model_used,
                    confidence_score,
                    processing_time_ms,
                    created_at,
                    updated_at
                FROM syllabus_ai_analysis
                WHERE id = $1
            """
            
            async with pool.acquire() as conn:
                row = await conn.fetchrow(query, uuid.UUID(analysis_id))
            
            if row:
                return {
                    'id': str(row['id']),
                    'syllabus_version_id': str(row['syllabus_version_id']),
                    'analysis_type': row['analysis_type'],
                    'result': json.loads(row['result']) if isinstance(row['result'], str) else row['result'],
                    'model_used': row['model_used'],
                    'confidence_score': float(row['confidence_score']) if row['confidence_score'] else None,
                    'processing_time_ms': row['processing_time_ms'],
                    'created_at': row['created_at'].isoformat() if row['created_at'] else None,
                    'updated_at': row['updated_at'].isoformat() if row['updated_at'] else None
                }
            
            return None
            
        except Exception as e:
            logger.error(f"❌ Failed to get analysis: {e}")
            return None
    
    @staticmethod
    async def get_analyses_by_syllabus(syllabus_version_id: str) -> List[Dict[str, Any]]:
        """
        Get all analyses for a syllabus version
        
        Args:
            syllabus_version_id: UUID of syllabus version
            
        Returns:
            List of analysis records
        """
        try:
            pool = await get_db_pool()
            
            query = """
                SELECT 
                    id,
                    syllabus_version_id,
                    analysis_type,
                    result,
                    model_used,
                    confidence_score,
                    processing_time_ms,
                    created_at,
                    updated_at
                FROM syllabus_ai_analysis
                WHERE syllabus_version_id = $1
                ORDER BY created_at DESC
            """
            
            async with pool.acquire() as conn:
                rows = await conn.fetch(query, uuid.UUID(syllabus_version_id))
            
            return [
                {
                    'id': str(row['id']),
                    'syllabus_version_id': str(row['syllabus_version_id']),
                    'analysis_type': row['analysis_type'],
                    'result': json.loads(row['result']) if isinstance(row['result'], str) else row['result'],
                    'model_used': row['model_used'],
                    'confidence_score': float(row['confidence_score']) if row['confidence_score'] else None,
                    'processing_time_ms': row['processing_time_ms'],
                    'created_at': row['created_at'].isoformat() if row['created_at'] else None,
                    'updated_at': row['updated_at'].isoformat() if row['updated_at'] else None
                }
                for row in rows
            ]
            
        except Exception as e:
            logger.error(f"❌ Failed to get analyses for syllabus: {e}")
            return []
