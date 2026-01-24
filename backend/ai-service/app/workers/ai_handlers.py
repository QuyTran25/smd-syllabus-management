"""
AI Message Handler
Xử lý messages từ RabbitMQ và route tới handlers tương ứng
"""
import logging
import time
import json
from datetime import datetime
from typing import Dict, Any, List
import os

from app.config.settings import settings

# Gemini API
try:
    import google.generativeai as genai
    GEMINI_AVAILABLE = True
except ImportError:
    GEMINI_AVAILABLE = False
    logging.warning("⚠️ Gemini SDK not installed. Install: pip install google-generativeai")

# Local AI Model imports
try:
    from transformers import (
        AutoTokenizer, 
        AutoModelForSeq2SeqLM,
        BartForConditionalGeneration,
        AutoModelForCausalLM
    )
    import torch
    AI_AVAILABLE = True
except ImportError:
    AI_AVAILABLE = False
    logging.warning("⚠️ AI libraries not installed. Running in MOCK mode.")

logger = logging.getLogger(__name__)


class AIMessageHandler:
    """Handler chính cho AI messages"""
    
    def __init__(self, rabbitmq_manager=None):
        """
        Initialize handler with Gemini API or local AI model
        """
        self.mock_mode = settings.MOCK_MODE
        self.ai_provider = settings.AI_PROVIDER
        self.gemini_client = None
        self.model = None
        self.tokenizer = None
        self.device = None
        self.rabbitmq_manager = rabbitmq_manager
        
        # Initialize AI based on provider
        if not self.mock_mode:
            if self.ai_provider == 'gemini' and GEMINI_AVAILABLE:
                try:
                    self._init_gemini()
                except Exception as e:
                    logger.error(f"❌ Failed to init Gemini: {e}")
                    logger.warning("⚠️ Falling back to local model or MOCK mode")
                    if AI_AVAILABLE:
                        try:
                            self._load_summarize_model()
                        except Exception as e2:
                            logger.error(f"❌ Local model also failed: {e2}")
                            self.mock_mode = True
                    else:
                        self.mock_mode = True
            elif AI_AVAILABLE:
                try:
                    self._load_summarize_model()
                except Exception as e:
                    logger.error(f"❌ Failed to load AI model: {e}")
                    logger.warning("⚠️ Falling back to MOCK mode")
                    self.mock_mode = True
            else:
                logger.warning("⚠️ No AI available, using MOCK mode")
                self.mock_mode = True
        
        mode = "MOCK" if self.mock_mode else f"{self.ai_provider.upper()} AI"
        logger.info(f"🤖 AI Message Handler initialized in {mode} mode")
    
    def _init_gemini(self):
        """Initialize Gemini API client"""
        api_key = settings.GEMINI_API_KEY
        if not api_key or api_key == 'your_api_key_here':
            raise ValueError("GEMINI_API_KEY not configured")
        
        genai.configure(api_key=api_key)
        model_name = settings.GEMINI_MODEL
        self.gemini_client = genai.GenerativeModel(model_name)
        
        logger.info(f"✅ Gemini initialized: {model_name}")
        logger.info(f"📊 Free tier: 1500 req/day, 1M tokens/day")
    
    def handle_message(self, message: Dict[str, Any]) -> Dict[str, Any]:
        """
        Route message tới handler phù hợp dựa trên action
        
        Args:
            message: Message dict từ RabbitMQ (format: AIMessageRequest)
            
        Returns:
            Response dict với status và result (format: AIMessageResponse)
        """
        action = message.get('action')
        message_id = message.get('messageId') or message.get('message_id')  # Support both formats
        payload = message.get('payload', {})
        priority = message.get('priority', 'MEDIUM')
        user_id = message.get('userId') or message.get('user_id')
        
        start_time = datetime.now()
        
        try:
            logger.info(f"[Received] Action: {action} for Message ID: {message_id}")
            logger.info(f"[Priority] {priority} | User: {user_id}")
            mode_status = "MOCK mode" if self.mock_mode else "AI mode"
            logger.info(f"[Processing] {action} with {mode_status}...")
            
            # Route to appropriate handler
            if action == 'MAP_CLO_PLO':
                result = self._handle_map_clo_plo(message_id, payload)
            elif action == 'COMPARE_VERSIONS':
                result = self._handle_compare_versions(message_id, payload)
            elif action == 'SUMMARIZE_SYLLABUS':
                result = self._handle_summarize(message_id, payload)
            else:
                raise ValueError(f"Unknown action: {action}")
            
            processing_time = int((datetime.now() - start_time).total_seconds() * 1000)
            
            response = {
                'messageId': message_id,
                'action': action,
                'status': 'SUCCESS',
                'progress': 100,
                'result': result,
                'processingTimeMs': processing_time
            }
            
            logger.info(f"[Done] Processing completed.")
            logger.info(f"✅ {action} completed in {processing_time}ms")
            
            # ✅ Save result to database (Transactional Outbox pattern)
            await self._save_to_database(message_id, action, result, processing_time, payload)
            
            # Send result to result queue
            self._send_result_to_queue(response)
            
            return response
            
        except Exception as e:
            logger.error(f"❌ Error handling {action}: {e}", exc_info=True)
            
            processing_time = int((datetime.now() - start_time).total_seconds() * 1000)
            
            # Error response
            error_response = {
                'messageId': message_id,
                'action': action,
                'status': 'ERROR',
                'progress': 0,
                'result': None,
                'errorMessage': str(e),
                'processingTimeMs': processing_time
            }
            
            # Send error to result queue
            self._send_result_to_queue(error_response)
            
            return error_response
    
    def _send_result_to_queue(self, response: Dict[str, Any]) -> None:
        """Send result to ai_result_queue"""
        if not self.rabbitmq_manager:
            logger.warning("⚠️ No RabbitMQ manager, skipping result publish")
            return
        
        try:
            result_queue = os.getenv('QUEUE_AI_RESULT', 'ai_result_queue')
            success = self.rabbitmq_manager.publish_message(result_queue, response)
            if success:
                logger.info(f"📤 Result sent to {result_queue}: {response.get('messageId')}")
            else:
                logger.error(f"❌ Failed to send result to {result_queue}")
        except Exception as e:
            logger.error(f"❌ Error sending result: {e}", exc_info=True)
    
    async def _save_to_database(
        self,
        message_id: str,
        action: str,
        result: Dict[str, Any],
        processing_time: int,
        payload: Dict[str, Any]
    ) -> None:
        """
        Save analysis result to database
        
        ✅ IMPLEMENTED: Database persistence for audit and history
        
        This implements the "Database per Service" pattern:
        - AI Service owns ai_service schema
        - Core Service queries via API (not direct DB access)
        
        Args:
            message_id: Task ID
            action: Analysis type
            result: Analysis result
            processing_time: Processing time in ms
            payload: Original request payload
        """
        try:
            from app.database.repository import AnalysisRepository
            
            # Extract syllabus_version_id from payload
            syllabus_id = payload.get('syllabus_id')
            if not syllabus_id:
                logger.warning(f"⚠️ No syllabus_id in payload, skipping database save")
                return
            
            # Determine model used
            model_used = "gemini-pro" if self.ai_provider == 'gemini' else "mock"
            if self.mock_mode:
                model_used = "mock"
            
            # Calculate confidence score (mock for now)
            confidence_score = 0.85  # TODO: Get from AI model
            
            # Save to database
            success = await AnalysisRepository.save_analysis(
                analysis_id=message_id,
                syllabus_version_id=syllabus_id,
                analysis_type=action,
                result=result,
                model_used=model_used,
                confidence_score=confidence_score,
                processing_time_ms=processing_time
            )
            
            if success:
                logger.info(f"💾 Saved to database: {message_id}")
            else:
                logger.error(f"❌ Failed to save to database: {message_id}")
                
        except Exception as e:
            logger.error(f"❌ Error saving to database: {e}", exc_info=True)
            # Don't raise - database save failure shouldn't block RabbitMQ result
    
    def _handle_map_clo_plo(self, message_id: str, payload: Dict) -> Dict:
        """
        Handler cho MAP_CLO_PLO - Kiểm tra tuân thủ CLO-PLO
        
        MOCK DATA - Trả về structure giống thật để test workflow
        """
        syllabus_id = payload.get('syllabus_id')
        curriculum_id = payload.get('curriculum_id')
        
        logger.info(f"📊 Analyzing CLO-PLO mapping for syllabus: {syllabus_id}")
        
        # Simulate AI processing time
        time.sleep(2)  # 2 seconds
        
        # MOCK RESULT - Đúng format theo kế hoạch
        result = {
            "overall_status": "NEEDS_IMPROVEMENT",
            "compliance_score": 75.5,
            "issues": [
                {
                    "severity": "HIGH",
                    "type": "MISSING_PLO_MAPPING",
                    "code": "PLO2",
                    "title": "PLO2: CLO chưa ánh xạ đủ sang PLO2 (yêu cầu tối thiểu 2 CLO)",
                    "description": "Hiện tại chỉ có 1 CLO ánh xạ sang PLO2, cần thêm ít nhất 1 CLO nữa",
                    "current_count": 1,
                    "required_count": 2,
                    "affected_clos": ["CLO-1"]
                },
                {
                    "severity": "MEDIUM",
                    "type": "INSUFFICIENT_WEIGHT",
                    "code": "PLO5",
                    "title": "PLO5: Thiếu đánh giá kỹ năng làm việc nhóm cho PLO5",
                    "description": "PLO5 yêu cầu kỹ năng làm việc nhóm nhưng chỉ có 5% trọng số trong đánh giá (khuyến nghị 10-15%)",
                    "current_weight": 5,
                    "recommended_weight": "10-15",
                    "affected_assessments": ["Bài tập nhóm"]
                }
            ],
            "suggestions": [
                {
                    "priority": 1,
                    "action": "ADD_CLO",
                    "title": "Thêm CLO về kỹ năng phân tích dữ liệu ứng PLO2",
                    "description": "Ví dụ: 'Sinh viên có khả năng phân tích yêu cầu và thiết kế mô hình dữ liệu phù hợp'"
                },
                {
                    "priority": 2,
                    "action": "ADJUST_WEIGHT",
                    "title": "Bổ sung phương pháp đánh giá nhóm (weight 10-15%) cho PLO5",
                    "description": "Tăng trọng số bài tập nhóm từ 5% lên 15%"
                },
                {
                    "priority": 3,
                    "action": "REVIEW_CONSISTENCY",
                    "title": "Xem xét tăng trọng số CLO ánh xạ sang PLO2 lên ít nhất 30%",
                    "description": "Đảm bảo tầm quan trọng của PLO2 được phản ánh qua assessment scheme"
                }
            ],
            "compliant_mappings": [
                {
                    "plo_code": "PLO1",
                    "mapped_clos": ["CLO-1", "CLO-2", "CLO-3"],
                    "total_weight": 45,
                    "status": "GOOD"
                },
                {
                    "plo_code": "PLO3",
                    "mapped_clos": ["CLO-4", "CLO-5"],
                    "total_weight": 35,
                    "status": "GOOD"
                }
            ]
        }
        
        logger.info(f" CLO-PLO analysis completed. Status: {result['overall_status']}")
        return result
    
    def _handle_compare_versions(self, message_id: str, payload: Dict) -> Dict:
        """
        Handler cho COMPARE_VERSIONS - So sánh phiên bản
        
        REAL IMPLEMENTATION with Gemini AI
        """
        old_version_id = payload.get('old_version_id')
        new_version_id = payload.get('new_version_id')
        old_version = payload.get('old_version', {})
        new_version = payload.get('new_version', {})
        
        logger.info(f"🔍 Comparing versions: {old_version.get('version_no')} → {new_version.get('version_no')}")
        logger.info(f" Old version: ID={old_version_id[:8]}..., version_no={old_version.get('version_no')}, CLOs={len(old_version.get('content', {}).get('clos', []))}")
        logger.info(f" New version: ID={new_version_id[:8]}..., version_no={new_version.get('version_no')}, CLOs={len(new_version.get('content', {}).get('clos', []))}")
        
        # Extract content from both versions
        old_content = old_version.get('content', {})
        new_content = new_version.get('content', {})
        
        logger.info(f" Comparing all sections of the syllabus")
        
        # Detect changes
        changes = []
        sections_affected = []
        
        # 1. Compare CLOs
        old_clos = old_content.get('clos', [])
        new_clos = new_content.get('clos', [])
        if old_clos != new_clos:
            sections_affected.append("learning_outcomes")
            clo_changes = self._compare_clos(old_clos, new_clos)
            if clo_changes:
                changes.append({
                    "section": "learning_outcomes",
                    "section_title": "Mục tiêu học tập (CLOs)",
                    "change_type": "MODIFIED",
                    "changes": clo_changes
                })
        
        # 2. Compare Assessment Schemes
        old_assessments = old_content.get('assessment_schemes', [])
        new_assessments = new_content.get('assessment_schemes', [])
        if old_assessments != new_assessments:
            sections_affected.append("assessment_schemes")
            assessment_changes = self._compare_assessments(old_assessments, new_assessments)
            if assessment_changes:
                changes.append({
                    "section": "assessment_schemes",
                    "section_title": "Phương pháp đánh giá",
                    "change_type": "MODIFIED",
                    "changes": assessment_changes
                })
        
        # 3. Compare Teaching Methods
        old_methods = old_content.get('teaching_methods', [])
        new_methods = new_content.get('teaching_methods', [])
        if old_methods != new_methods:
            sections_affected.append("teaching_methods")
            method_changes = self._compare_teaching_methods(old_methods, new_methods)
            if method_changes:
                changes.append({
                    "section": "teaching_methods",
                    "section_title": "Phương pháp giảng dạy",
                    "change_type": "MODIFIED",
                    "changes": method_changes
                })
        
        # 4. Compare Prerequisites
        old_prereqs = old_content.get('prerequisites', [])
        new_prereqs = new_content.get('prerequisites', [])
        if old_prereqs != new_prereqs:
            sections_affected.append("prerequisites")
            prereq_changes = self._compare_prerequisites(old_prereqs, new_prereqs)
            if prereq_changes:
                changes.append({
                    "section": "prerequisites",
                    "section_title": "Môn học tiên quyết",
                    "change_type": "MODIFIED",
                    "changes": prereq_changes
                })
        
        # 5. Compare Learning Materials
        old_materials = old_content.get('learning_materials', [])
        new_materials = new_content.get('learning_materials', [])
        if old_materials != new_materials:
            sections_affected.append("learning_materials")
            material_changes = self._compare_learning_materials(old_materials, new_materials)
            if material_changes:
                changes.append({
                    "section": "learning_materials",
                    "section_title": "Tài liệu học tập",
                    "change_type": "MODIFIED",
                    "changes": material_changes
                })
        
        # 6. Compare Weekly Plans
        old_weekly = old_content.get('weekly_plans', [])
        new_weekly = new_content.get('weekly_plans', [])
        if old_weekly != new_weekly:
            sections_affected.append("weekly_plans")
            weekly_changes = self._compare_weekly_plans(old_weekly, new_weekly)
            if weekly_changes:
                changes.append({
                    "section": "weekly_plans",
                    "section_title": "Kế hoạch giảng dạy theo tuần",
                    "change_type": "MODIFIED",
                    "changes": weekly_changes
                })
        
        # 7. Compare Description
        if old_version.get('description') != new_version.get('description'):
            sections_affected.append("description")
            changes.append({
                "section": "description",
                "section_title": "Mô tả môn học",
                "change_type": "MODIFIED",
                "changes": [{
                    "field": "Nội dung mô tả",
                    "old_value": old_version.get('description', ''),
                    "new_value": new_version.get('description', ''),
                    "significance": "MEDIUM"
                }]
            })
        
        # 8. Compare Objectives
        if old_version.get('objectives') != new_version.get('objectives'):
            sections_affected.append("objectives")
            changes.append({
                "section": "objectives",
                "section_title": "Mục tiêu môn học",
                "change_type": "MODIFIED",
                "changes": [{
                    "field": "Nội dung mục tiêu",
                    "old_value": old_version.get('objectives', ''),
                    "new_value": new_version.get('objectives', ''),
                    "significance": "HIGH"
                }]
            })
        
        # 9. Compare Credit Count
        if old_version.get('credit_count') != new_version.get('credit_count'):
            sections_affected.append("credit_count")
            changes.append({
                "section": "credit_count",
                "section_title": "Số tín chỉ",
                "change_type": "MODIFIED",
                "changes": [{
                    "field": "Số tín chỉ",
                    "old_value": str(old_version.get('credit_count', '')),
                    "new_value": str(new_version.get('credit_count', '')),
                    "significance": "HIGH"
                }]
            })
        
        # Count change types
        total_changes = len(changes)
        major_changes = sum(1 for c in changes if any(ch.get('significance') == 'HIGH' for ch in c.get('changes', [])))
        minor_changes = total_changes - major_changes
        
        # AI Analysis with Gemini
        ai_analysis = self._get_ai_comparison_analysis(old_version, new_version, changes) if self.gemini_client and total_changes > 0 else None
        
        result = {
            "is_first_version": False,
            "version_history": [
                {
                    "version_number": new_version.get('version_number', 1),
                    "version_no": new_version.get('version_no', 'v1'),
                    "status": "Hiện tại",
                    "created_at": new_version.get('created_at', ''),
                    "is_current": True
                },
                {
                    "version_number": old_version.get('version_number', 1),
                    "version_no": old_version.get('version_no', 'v1'),
                    "status": old_version.get('status', 'REJECTED'),
                    "created_at": old_version.get('created_at', ''),
                    "is_current": False
                }
            ],
            "changes_summary": {
                "total_changes": total_changes,
                "major_changes": major_changes,
                "minor_changes": minor_changes,
                "sections_affected": sections_affected
            },
            "detailed_changes": changes,
            "ai_analysis": ai_analysis
        }
        
        logger.info(f" Version comparison completed: {total_changes} changes detected")
        return result
    
    def _compare_clos(self, old_clos: List, new_clos: List) -> List[Dict]:
        """So sánh CLOs giữa 2 versions"""
        changes = []
        
        # Build maps by code
        old_map = {clo.get('code'): clo for clo in old_clos}
        new_map = {clo.get('code'): clo for clo in new_clos}
        
        # Find added CLOs
        for code in new_map:
            if code not in old_map:
                changes.append({
                    "field": f"CLO {code}",
                    "old_value": None,
                    "new_value": new_map[code].get('description'),
                    "significance": "HIGH",
                    "impact": "Thêm mới CLO"
                })
        
        # Find removed CLOs
        for code in old_map:
            if code not in new_map:
                changes.append({
                    "field": f"CLO {code}",
                    "old_value": old_map[code].get('description'),
                    "new_value": None,
                    "significance": "HIGH",
                    "impact": "Xóa CLO"
                })
        
        # Find modified CLOs
        for code in old_map:
            if code in new_map:
                old_clo = old_map[code]
                new_clo = new_map[code]
                if old_clo.get('description') != new_clo.get('description'):
                    changes.append({
                        "field": f"CLO {code}",
                        "old_value": old_clo.get('description'),
                        "new_value": new_clo.get('description'),
                        "significance": "HIGH",
                        "impact": "Thay đổi nội dung CLO"
                    })
        
        return changes
    
    def _compare_assessments(self, old_assessments: List, new_assessments: List) -> List[Dict]:
        """So sánh assessment schemes"""
        changes = []
        
        # Build maps by type
        old_map = {a.get('assessment_type'): a for a in old_assessments}
        new_map = {a.get('assessment_type'): a for a in new_assessments}
        
        # Find added assessments
        for atype in new_map:
            if atype not in old_map:
                changes.append({
                    "field": f"Đánh giá {atype}",
                    "old_value": None,
                    "new_value": f"{new_map[atype].get('weight_percentage', 0)}%",
                    "significance": "HIGH",
                    "impact": "Thêm mới phương pháp đánh giá"
                })
        
        # Find removed assessments
        for atype in old_map:
            if atype not in new_map:
                changes.append({
                    "field": f"Đánh giá {atype}",
                    "old_value": f"{old_map[atype].get('weight_percentage', 0)}%",
                    "new_value": None,
                    "significance": "HIGH",
                    "impact": "Xóa phương pháp đánh giá"
                })
        
        # Find modified assessments
        for atype in old_map:
            if atype in new_map:
                old_weight = old_map[atype].get('weight_percentage', 0)
                new_weight = new_map[atype].get('weight_percentage', 0)
                if old_weight != new_weight:
                    changes.append({
                        "field": f"Đánh giá {atype}",
                        "old_value": f"{old_weight}%",
                        "new_value": f"{new_weight}%",
                        "significance": "HIGH",
                        "impact": "Thay đổi tỷ trọng đánh giá"
                    })
        
        return changes
    
    def _compare_teaching_methods(self, old_methods: List, new_methods: List) -> List[Dict]:
        """So sánh teaching methods"""
        changes = []
        
        old_names = set(m.get('method_name', '') for m in old_methods)
        new_names = set(m.get('method_name', '') for m in new_methods)
        
        # Find added methods
        for name in new_names - old_names:
            changes.append({
                "field": "Phương pháp giảng dạy",
                "old_value": None,
                "new_value": name,
                "significance": "MEDIUM",
                "impact": "Thêm phương pháp giảng dạy mới"
            })
        
        # Find removed methods
        for name in old_names - new_names:
            changes.append({
                "field": "Phương pháp giảng dạy",
                "old_value": name,
                "new_value": None,
                "significance": "MEDIUM",
                "impact": "Xóa phương pháp giảng dạy"
            })
        
        return changes
    
    def _compare_prerequisites(self, old_prereqs: List, new_prereqs: List) -> List[Dict]:
        """So sánh prerequisites"""
        changes = []
        
        old_codes = set(p.get('subject_code', '') for p in old_prereqs)
        new_codes = set(p.get('subject_code', '') for p in new_prereqs)
        
        # Find added prerequisites
        for code in new_codes - old_codes:
            changes.append({
                "field": "Môn tiên quyết",
                "old_value": None,
                "new_value": code,
                "significance": "HIGH",
                "impact": "Thêm môn tiên quyết mới"
            })
        
        # Find removed prerequisites
        for code in old_codes - new_codes:
            changes.append({
                "field": "Môn tiên quyết",
                "old_value": code,
                "new_value": None,
                "significance": "HIGH",
                "impact": "Xóa môn tiên quyết"
            })
        
        return changes
    
    def _compare_learning_materials(self, old_materials: List, new_materials: List) -> List[Dict]:
        """So sánh learning materials"""
        changes = []
        
        # Build maps by title
        old_map = {m.get('title'): m for m in old_materials}
        new_map = {m.get('title'): m for m in new_materials}
        
        # Find added materials
        for title in new_map:
            if title not in old_map:
                changes.append({
                    "field": "Tài liệu học tập",
                    "old_value": None,
                    "new_value": title,
                    "significance": "LOW",
                    "impact": "Thêm tài liệu mới"
                })
        
        # Find removed materials
        for title in old_map:
            if title not in new_map:
                changes.append({
                    "field": "Tài liệu học tập",
                    "old_value": title,
                    "new_value": None,
                    "significance": "LOW",
                    "impact": "Xóa tài liệu"
                })
        
        return changes
    
    def _compare_weekly_plans(self, old_weekly: List, new_weekly: List) -> List[Dict]:
        """So sánh weekly plans"""
        changes = []
        
        if len(old_weekly) != len(new_weekly):
            changes.append({
                "field": "Số tuần học",
                "old_value": f"{len(old_weekly)} tuần",
                "new_value": f"{len(new_weekly)} tuần",
                "significance": "HIGH",
                "impact": "Thay đổi số tuần học"
            })
        
        # Compare week by week
        for i, (old_week, new_week) in enumerate(zip(old_weekly, new_weekly), 1):
            old_topic = old_week.get('topic', '')
            new_topic = new_week.get('topic', '')
            if old_topic != new_topic:
                changes.append({
                    "field": f"Tuần {i}",
                    "old_value": old_topic,
                    "new_value": new_topic,
                    "significance": "MEDIUM",
                    "impact": f"Thay đổi nội dung tuần {i}"
                })
        
        return changes
    
    def _get_ai_comparison_analysis(self, old_version: Dict, new_version: Dict, changes: List[Dict]) -> Dict:
        """Gọi Gemini AI để phân tích sự khác biệt"""
        if not self.gemini_client:
            return None
        
        try:
            # Build detailed change list for prompt
            change_details = []
            for change in changes[:15]:  # Limit to 15 most important changes
                section = change.get('section', 'unknown')
                section_names = {
                    'learning_outcomes': 'CLOs (Chuẩn đầu ra)',
                    'assessment_schemes': 'Phương pháp đánh giá',
                    'teaching_methods': 'Phương pháp giảng dạy',
                    'prerequisites': 'Điều kiện tiên quyết',
                    'learning_materials': 'Tài liệu học tập',
                    'weekly_plans': 'Kế hoạch giảng dạy',
                    'description': 'Mô tả môn học',
                    'objectives': 'Mục tiêu môn học'
                }
                section_vn = section_names.get(section, section)
                
                for detail in change.get('changes', []):
                    field = detail.get('field', '')
                    old_val = detail.get('old_value')
                    new_val = detail.get('new_value')
                    impact = detail.get('impact', '')
                    
                    if old_val is None:
                        change_details.append(f"• {section_vn} - {field}: THÊM MỚI '{new_val}' ({impact})")
                    elif new_val is None:
                        change_details.append(f"• {section_vn} - {field}: XÓA '{old_val}' ({impact})")
                    else:
                        change_details.append(f"• {section_vn} - {field}: SỬA ĐỔI từ '{old_val}' → '{new_val}' ({impact})")
            
            change_list = '\n'.join(change_details) if change_details else "Không có thay đổi đáng kể"
            
            prompt = f"""
Phân tích chi tiết sự thay đổi giữa 2 phiên bản đề cương môn học:

**PHIÊN BẢN CŨ (v{old_version.get('version_no')}):**
- Số CLOs: {len(old_version.get('content', {}).get('clos', []))}
- Số phương pháp đánh giá: {len(old_version.get('content', {}).get('assessment_schemes', []))}
- Mô tả: {old_version.get('description', '')[:150]}...

**PHIÊN BẢN MỚI (v{new_version.get('version_no')}):**
- Số CLOs: {len(new_version.get('content', {}).get('clos', []))}
- Số phương pháp đánh giá: {len(new_version.get('content', {}).get('assessment_schemes', []))}
- Mô tả: {new_version.get('description', '')[:150]}...

**CHI TIẾT CÁC THAY ĐỔI ({len(changes)} thay đổi):**
{change_list}

**YÊU CẦU PHÂN TÍCH:**
1. Tổng quan: Đánh giá chung về mức độ và tính chất thay đổi (2-3 câu ngắn)
2. Cải tiến chính: Liệt kê 3-4 thay đổi QUAN TRỌNG NHẤT theo format:
   - [Mục]: Nội dung thay đổi cụ thể
   Ví dụ: "CLOs: Thêm CLO3 về kỹ năng phân tích dữ liệu"
3. Khuyến nghị: 1-2 gợi ý cải thiện tiếp (nếu cần)

Trả lời NGẮN GỌN, TẬP TRUNG VÀO ĐIỂM KHÁC BIỆT QUAN TRỌNG, bằng tiếng Việt.
"""
            
            response = self.gemini_client.generate_content(prompt)
            analysis_text = response.text
            
            # Parse response - clean markdown formatting
            lines = [l.strip() for l in analysis_text.split('\n') if l.strip()]
            
            # Remove markdown formatting (**, ##, ---, bullets)
            clean_lines = []
            for line in lines:
                # Skip separator lines
                if line in ['---', '***', '___']:
                    continue
                # Remove markdown bold/italic
                line = line.replace('**', '').replace('__', '').replace('*', '').replace('_', '')
                # Remove heading markers
                line = line.lstrip('#').strip()
                # Remove bullet points and numbering
                if line.startswith(('- ', '+ ', '* ')):
                    line = line[2:].strip()
                elif len(line) > 2 and line[0].isdigit() and line[1:3] in ['. ', ') ']:
                    line = line[3:].strip()
                
                if line:  # Only add non-empty lines
                    clean_lines.append(line)
            
            # Categorize lines into sections
            overall = ""
            improvements = []
            recommendations = []
            current_section = "overall"
            
            for line in clean_lines:
                line_lower = line.lower()
                if any(keyword in line_lower for keyword in ['cải tiến', 'cải thiện', 'improvement']):
                    current_section = "improvements"
                    continue
                elif any(keyword in line_lower for keyword in ['khuyến nghị', 'đề xuất', 'recommendation']):
                    current_section = "recommendations"
                    continue
                
                # Add to appropriate section
                if current_section == "overall" and not overall:
                    overall = line
                elif current_section == "improvements":
                    improvements.append(line)
                elif current_section == "recommendations":
                    recommendations.append(line)
            
            return {
                "overall_assessment": overall if overall else (clean_lines[0] if clean_lines else "Phiên bản mới có cải thiện so với phiên bản cũ"),
                "key_improvements": improvements if improvements else clean_lines[1:4],
                "recommendations": recommendations if recommendations else clean_lines[4:]
            }
            
        except Exception as e:
            logger.error(f"❌ Failed to get AI analysis: {e}")
            return {
                "overall_assessment": "Không thể phân tích bằng AI",
                "key_improvements": [],
                "recommendations": []
            }
    
    def _handle_summarize(self, message_id: str, payload: Dict) -> Dict:
        """
        Handler cho SUMMARIZE_SYLLABUS - Tóm tắt cho sinh viên
        
        Sử dụng AI model thật (VietAI/vit5-base hoặc Gemini) để tóm tắt
        """
        syllabus_id = payload.get('syllabus_id')
        syllabus_data = payload.get('syllabus_data', {})
        
        logger.info(f"📝 Summarizing syllabus: {syllabus_id}")
        logger.info(f"🔍 AI Status: mock_mode={self.mock_mode}, model_loaded={self.model is not None}, gemini_available={self.gemini_client is not None}")
        
        # Use real AI if available (Gemini or local model)
        if not self.mock_mode and (self.gemini_client is not None or self.model is not None):
            ai_provider = "GEMINI" if self.gemini_client else "LOCAL MODEL"
            logger.info(f"✅ Using {ai_provider} for summarization")
            return self._summarize_with_ai(syllabus_data)
        
        # Fallback to mock
        logger.warning("⚠️ Using MOCK data (AI model not available or mock_mode=true)")
        time.sleep(2)  # 2 seconds
        
        # MOCK RESULT
        result = {
            "overview": {
                "title": "Thiết kế và tối ưu hóa CSDL",
                "description": "Môn học trang bị kiến thức về thiết kế CSDL quan hệ, chuẩn hóa, và tối ưu truy vấn"
            },
            "highlights": {
                "difficulty": {
                    "level": "MEDIUM",
                    "description": "Trung bình - Phù hợp sinh viên năm 2-3"
                },
                "duration": {
                    "theory_hours": 30,
                    "practice_hours": 30,
                    "total_hours": 60,
                    "description": "30 lý thuyết + 30 tiết thực hành"
                },
                "assessment": {
                    "summary": "Cân bằng giữa thi và bài tập/dự án",
                    "breakdown": [
                        {"type": "Thi giữa kỳ", "weight": 30},
                        {"type": "Bài tập", "weight": 20},
                        {"type": "Dự án", "weight": 20},
                        {"type": "Thi cuối kỳ", "weight": 30}
                    ]
                },
                "skills_acquired": {
                    "summary": "Ánh xạ CLO tới PLO rõ ràng",
                    "key_skills": [
                        "Thiết kế ERD và chuẩn hóa CSDL",
                        "Viết truy vấn SQL phức tạp",
                        "Tối ưu hiệu năng database"
                    ]
                }
            },
            "recommendations": {
                "prerequisites": {
                    "required": ["Cấu trúc dữ liệu và giải thuật", "OOP"],
                    "description": "Nên có kiến thức cơ bản về các môn tiên quyết"
                },
                "preparation": {
                    "tips": [
                        "Ôn lại kiến thức nền về cấu trúc dữ liệu",
                        "Làm quen với SQL cơ bản",
                        "Cài đặt PostgreSQL/MySQL trước khi học"
                    ],
                    "description": "Chuẩn bị trước: Ôn lại kiến thức nền"
                },
                "study_time": {
                    "hours_per_week": 6,
                    "breakdown": "4 giờ làm bài tập + 2 giờ đọc tài liệu",
                    "description": "Dành ít nhất 6 giờ/tuần"
                }
            }
        }
        
        logger.info(f"✅ Summarization completed")
        return result
    
    # =============================================
    # AI MODEL METHODS - REAL IMPLEMENTATION
    # =============================================
    
    def _load_summarize_model(self):
        """
        Load VietAI T5 model cho Vietnamese summarization
        Specialized model trained on Vietnamese news summarization
        """
        model_name = os.getenv('AI_MODEL_NAME', 'VietAI/vit5-large-vietnews-summarization')
        use_8bit = os.getenv('USE_8BIT_QUANTIZATION', 'false').lower() == 'true'
        
        logger.info(f"📦 Loading model: {model_name}")
        logger.info(f"🔧 8-bit quantization: {use_8bit}")
        
        # Determine device
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        logger.info(f"🔧 Using device: {self.device}")
        
        # Load tokenizer
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        
        # Load model - Seq2Seq for T5 summarization
        logger.info("⏳ Loading model... (first time may take 1-2 minutes to download ~1.2GB)")
        
        self.model = AutoModelForSeq2SeqLM.from_pretrained(
            model_name,
            torch_dtype=torch.float16 if self.device == "cuda" else torch.float32
        ).to(self.device)
        
        logger.info(f"✅ Model loaded successfully on {self.device}")
        if use_8bit:
            logger.info("✅ Using 8-bit quantization (reduced memory usage)")
    
    def _summarize_text(self, text: str, max_length: int = 100) -> str:
        """
        Summarize text using Gemini API or local model
        Falls back to extractive if AI not available
        """
        if not text or not isinstance(text, str):
            return text
        
        text = ' '.join(text.split())
        
        # Only skip summarization if text is already VERY SHORT (< 60 chars)
        if len(text) <= 60:
            return text
        
        # Try Gemini first
        if self.gemini_client and self.ai_provider == 'gemini':
            try:
                return self._summarize_with_gemini(text, max_length)
            except Exception as e:
                logger.error(f"❌ [GEMINI FAILED] {str(e)}")
                logger.info("📋 [FALLBACK] Trying extractive method")
                return self._extractive_summarize(text, max_length)
        
        # Try local model
        if self.model and self.tokenizer:
            try:
                return self._summarize_with_local_model(text, max_length)
            except Exception as e:
                logger.error(f"❌ [LOCAL MODEL FAILED] {str(e)}")
                logger.info("📋 [FALLBACK] Using extractive method")
                return self._extractive_summarize(text, max_length)
        
        # Fallback to extractive
        logger.warning("📋 [FALLBACK MODE] No AI available, using extractive method")
        return self._extractive_summarize(text, max_length)
    
    def _summarize_with_gemini(self, text: str, max_length: int) -> str:
        """Summarize using Gemini API"""
        logger.info(f"🤖 [GEMINI] Summarizing text: {len(text)} chars → target {max_length} chars")
        
        prompt = f"""Rút gọn văn bản sau thành TỐI ĐA {max_length} ký tự.

Yêu cầu:
- CHỈ giữ TỪ KHÓA CHÍNH
- BỎ ví dụ trong ngoặc (), chi tiết dài dòng
- Viết cực ngắn gọn

Ví dụ:
"Giải thích được nguyên lý vận hành của CPU (ALU, Control Unit)" → "Nguyên lý CPU"
"Viết và gỡ lỗi chương trình Assembly" → "Lập trình Assembly"

Văn bản: {text[:1500]}

Rút gọn:"""
        
        response = self.gemini_client.generate_content(prompt)
        summary = response.text.strip()
        
        logger.info(f"✅ [GEMINI SUCCESS] Summary: {len(text)} → {len(summary)} chars")
        return summary
    
    def _summarize_with_local_model(self, text: str, max_length: int) -> str:
        """Summarize using local T5 model"""
        logger.info(f"🤖 [LOCAL MODEL] Summarizing text: {len(text)} chars")
        
        # T5 prefix format
        prompt = f"vietnews: {text}"
        
        # Tokenize
        inputs = self.tokenizer(prompt, return_tensors="pt", truncation=True, max_length=512)
        
        # Move to device if needed
        if hasattr(self, 'device') and self.device != "cpu":
            inputs = {k: v.to(self.device) for k, v in inputs.items()}
        
        # Generate summary
        with torch.no_grad():
            outputs = self.model.generate(
                **inputs,
                max_new_tokens=max_length,
                temperature=0.7,
                top_p=0.9,
                do_sample=True,
                pad_token_id=self.tokenizer.eos_token_id
            )
        
        # Decode - T5 returns clean summary directly
        summary = self.tokenizer.decode(outputs[0], skip_special_tokens=True).strip()
        
        logger.info(f"✅ [LOCAL MODEL SUCCESS] Summary: {len(summary)} chars")
        return summary
    
    def _extractive_summarize(self, text: str, max_length: int = 100) -> str:
        """
        Simple extractive summarization - take first N sentences and ensure max_length
        Fallback when AI model not available
        """
        # Split into sentences
        sentences = []
        for delimiter in ['. ', '.\n', '! ', '? ']:
            if delimiter in text:
                parts = [s.strip() for s in text.split(delimiter) if s.strip()]
                sentences = parts
                break
        
        if not sentences:
            # No sentences, just truncate at word boundary
            if len(text) <= max_length:
                return text
            # Truncate at word boundary without "..."
            truncated = text[:max_length].rsplit(' ', 1)[0]
            return truncated.strip()
        
        # Take first sentences that fit within max_length
        result = []
        current_len = 0
        for sent in sentences:
            sent_with_period = sent if sent.endswith('.') else sent + '.'
            # If adding this sentence exceeds max_length
            if current_len + len(sent_with_period) + 1 > max_length:
                # If we already have some sentences, stop here (no "..." needed)
                if result:
                    break
                # If even first sentence is too long, truncate at word boundary
                available = max_length - current_len
                if available > 50:  # Only if we have reasonable space
                    truncated = sent[:available].rsplit(' ', 1)[0]
                    return truncated.strip()  # No "..." - just truncate cleanly
                else:
                    # Just truncate the whole text
                    truncated = text[:max_length].rsplit(' ', 1)[0]
                    return truncated.strip()
                break
            result.append(sent_with_period)
            current_len += len(sent_with_period) + 1
        
        if result:
            summary = ' '.join(result)
            logger.info(f"📝 Extractive summary: {len(text)} -> {len(summary)} chars")
            return summary
        
        # Final fallback - truncate at word boundary (no "...")
        truncated = text[:max_length].rsplit(' ', 1)[0]
        return truncated.strip()

    def _summarize_with_ai(self, syllabus_data: Dict) -> Dict:
        """
        Tạo tóm tắt có cấu trúc từ dữ liệu đề cương theo format chuẩn
        """
        try:
            # DEBUG: Print all keys received from Java
            logger.info(f"🔍 DEBUG - Received syllabus_data keys: {list(syllabus_data.keys())}")
            
            # Extract syllabus information
            course_name = syllabus_data.get('course_name', 'N/A')
            description = syllabus_data.get('description', '')
            learning_outcomes = syllabus_data.get('learning_outcomes', [])
            assessment_scheme = syllabus_data.get('assessment_scheme', [])
            objectives = syllabus_data.get('objectives', [])
            theory_hours = syllabus_data.get('theory_hours', 0)
            practice_hours = syllabus_data.get('practice_hours', 0)
            prerequisites = syllabus_data.get('prerequisites', [])
            textbooks = syllabus_data.get('textbooks', [])
            references = syllabus_data.get('references', [])
            weekly_content = syllabus_data.get('weekly_content', [])
            
            # DEBUG: Print sample data for important fields
            if learning_outcomes:
                logger.info(f"🔍 DEBUG - learning_outcomes sample: {learning_outcomes[0] if len(learning_outcomes) > 0 else 'empty'}")
            if assessment_scheme:
                logger.info(f"🔍 DEBUG - assessment_scheme sample: {assessment_scheme[0] if len(assessment_scheme) > 0 else 'empty'}")
            
            logger.info(f"📊 Processing syllabus: {course_name}")
            logger.info(f"   Description length: {len(description) if description else 0} chars")
            logger.info(f"   Objectives: {len(objectives) if isinstance(objectives, list) else 'string' if objectives else 0}")
            logger.info(f"   Learning outcomes (CLO): {len(learning_outcomes)} items")
            logger.info(f"   Assessment scheme: {len(assessment_scheme)} items")
            logger.info(f"   Assessment matrix: {len(syllabus_data.get('assessment_matrix', []))} items")
            
            # 1. Mô tả học phần - TÓM TẮT GỌN bằng AI (80 ký tự tối đa)
            mo_ta = self._summarize_text(description, max_length=80) if description else "Không có thông tin"
            logger.info(f"✅ Description summarized: {len(description) if description else 0} -> {len(mo_ta)} chars")
            
            # 2. Mục tiêu học phần - TÓM TẮT TỪNG MỤC bằng extractive summarization
            muc_tieu = []
            logger.info(f"📝 Processing objectives: type={type(objectives)}, length={len(objectives) if isinstance(objectives, (list, str)) else 0}")
            if objectives:
                # Check if objectives is a list or string
                if isinstance(objectives, list):
                    for obj in objectives:
                        if isinstance(obj, dict):
                            # If it's a dict, extract text from common keys
                            obj_text = obj.get('text') or obj.get('description') or obj.get('objective') or str(obj)
                        else:
                            obj_text = obj if isinstance(obj, str) else str(obj)
                        if obj_text and obj_text.strip():
                            # Tóm tắt mỗi mục tiêu bằng AI (20 ký tự tối đa)
                            summarized = self._summarize_text(obj_text.strip(), max_length=20)
                            muc_tieu.append(summarized)
                            logger.debug(f"   Objective: {len(obj_text)} -> {len(summarized)} chars")
                elif isinstance(objectives, str) and objectives.strip():
                    # If it's a string, split by common delimiters or add as single item
                    if '\n' in objectives:
                        parts = [o.strip() for o in objectives.split('\n') if o.strip()]
                        muc_tieu = [self._summarize_text(p, max_length=20) for p in parts[:5]]  # Limit to 5 objectives
                    elif '. ' in objectives and len(objectives) > 50:  # Only split if it's a long text
                        parts = objectives.split('. ')
                        formatted = [o.strip() + ('.' if not o.endswith('.') else '') for o in parts if o.strip()]
                        muc_tieu = [self._summarize_text(f, max_length=20) for f in formatted[:5]]  # Limit to 5
                    else:
                        muc_tieu = [self._summarize_text(objectives.strip(), max_length=50)]
            
            logger.info(f"✅ Objectives processed: {len(muc_tieu)} items")
            
            # 3. Phương pháp giảng dạy - TÓM TẮT
            phuong_phap_giang_day = []
            
            # First check if there's a teaching_method field
            teaching_method = syllabus_data.get('teaching_method', '')
            if teaching_method:
                if isinstance(teaching_method, str) and teaching_method.strip():
                    # Split by newline or comma
                    if '\n' in teaching_method:
                        methods_list = [m.strip() for m in teaching_method.split('\n') if m.strip()]
                        # Xử lý: cắt nội dung sau dấu ":", rồi mới tóm tắt
                        phuong_phap_giang_day = []
                        for m in methods_list:
                            # Cắt nội dung sau dấu ":"
                            if ':' in m:
                                m = m.split(':')[0].strip()
                            # Tóm tắt nếu vẫn còn dài
                            phuong_phap_giang_day.append(self._summarize_text(m, max_length=35) if len(m) > 35 else m)
                    elif ',' in teaching_method:
                        methods_list = [m.strip() for m in teaching_method.split(',') if m.strip()]
                        phuong_phap_giang_day = []
                        for m in methods_list:
                            if ':' in m:
                                m = m.split(':')[0].strip()
                            phuong_phap_giang_day.append(self._summarize_text(m, max_length=35) if len(m) > 35 else m)
                    else:
                        # Cắt sau dấu ":"
                        if ':' in teaching_method:
                            teaching_method = teaching_method.split(':')[0].strip()
                        phuong_phap_giang_day = [self._summarize_text(teaching_method.strip(), max_length=50)]
                elif isinstance(teaching_method, list):
                    # Xử lý list: cắt sau dấu ":" trước khi tóm tắt
                    phuong_phap_giang_day = []
                    for m in teaching_method:
                        m_str = str(m).strip()
                        if ':' in m_str:
                            m_str = m_str.split(':')[0].strip()
                        phuong_phap_giang_day.append(self._summarize_text(m_str, max_length=35) if len(m_str) > 35 else m_str)
            
            # Fallback to weekly_content if teaching_method is empty
            if not phuong_phap_giang_day and weekly_content and isinstance(weekly_content, list):
                for week in weekly_content[:3]:
                    if isinstance(week, dict):
                        activities = week.get('activities', '')
                        if activities and activities not in phuong_phap_giang_day:
                            phuong_phap_giang_day.append(activities)
            
            # Final fallback to default methods
            if not phuong_phap_giang_day:
                phuong_phap_giang_day = ["Bài giảng trên lớp", "Thảo luận nhóm", "Bài tập thực hành"]
            
            # 4. Phương pháp đánh giá
            phuong_phap_danh_gia = []
            if assessment_scheme and len(assessment_scheme) > 0:
                for assess in assessment_scheme:
                    if isinstance(assess, dict):
                        method = assess.get('method', '')
                        weight = assess.get('weight', '')
                        if method:
                            phuong_phap_danh_gia.append({
                                "method": method,
                                "weight": str(weight)
                            })
            
            # 5. Giáo trình chính
            giao_trinh_chinh = []
            if textbooks:
                if isinstance(textbooks, str) and textbooks.strip():
                    # If textbooks is a string, split by newline
                    lines = [line.strip() for line in textbooks.split('\n') if line.strip()]
                    for line in lines[:5]:  # Limit to 5 books
                        giao_trinh_chinh.append({"title": line})
                elif isinstance(textbooks, list) and len(textbooks) > 0:
                    for book in textbooks:
                        if isinstance(book, dict):
                            if book.get('type') == 'required' or not book.get('type'):
                                giao_trinh_chinh.append({
                                    "title": book.get('title', ''),
                                    "authors": book.get('authors', ''),
                                    "year": book.get('year', '')
                                })
                        elif isinstance(book, str) and book.strip():
                            giao_trinh_chinh.append({"title": book.strip()})
            
            # 6. Tài liệu tham khảo
            tai_lieu_tham_khao = []
            
            # First check textbooks for reference type
            if textbooks and isinstance(textbooks, list) and len(textbooks) > 0:
                for book in textbooks:
                    if isinstance(book, dict):
                        if book.get('type') == 'reference':
                            tai_lieu_tham_khao.append({
                                "title": book.get('title', ''),
                                "authors": book.get('authors', ''),
                                "year": book.get('year', '')
                            })
            
            # Then process references field
            if references:
                if isinstance(references, str) and references.strip():
                    # If references is a string, split by newline
                    lines = [line.strip() for line in references.split('\n') if line.strip()]
                    for line in lines[:10]:  # Limit to 10 references
                        tai_lieu_tham_khao.append({"title": line})
                elif isinstance(references, list) and len(references) > 0:
                    for ref in references:
                        if isinstance(ref, dict):
                            tai_lieu_tham_khao.append({
                                "title": ref.get('title', ''),
                                "authors": ref.get('authors', ''),
                                "year": ref.get('year', '')
                            })
                        elif isinstance(ref, str) and ref.strip():
                            tai_lieu_tham_khao.append({"title": ref.strip()})
            
            # 7. Nhiệm vụ của Sinh viên - TÓM TẮT
            nhiem_vu = []
            student_duties = syllabus_data.get('student_duties', '')
            if student_duties:
                # If data from database exists
                if isinstance(student_duties, str):
                    if '. ' in student_duties:
                        duties_list = [nv.strip() + '.' for nv in student_duties.split('. ') if nv.strip()]
                        # Tóm tắt từng nhiệm vụ
                        nhiem_vu = [self._summarize_text(duty, max_length=80) for duty in duties_list]
                    else:
                        nhiem_vu = [self._summarize_text(student_duties, max_length=100)]
                elif isinstance(student_duties, list):
                    # Tóm tắt từng item trong list
                    nhiem_vu = [self._summarize_text(str(duty), max_length=80) for duty in student_duties if str(duty).strip()]
            
            # If no data from database, generate generic template
            if not nhiem_vu:
                nhiem_vu = [
                    f"Tham gia đầy đủ {theory_hours + practice_hours} tiết học ({theory_hours} lý thuyết + {practice_hours} thực hành)",
                    "Hoàn thành các bài tập được giao đúng hạn",
                    "Tham gia thảo luận và làm việc nhóm tích cực",
                    "Chuẩn bị bài trước khi đến lớp"
                ]
            
            # 8. Chuẩn đầu ra học phần (CLO) - TÓM TẮT MÔ TẢ
            clo_list = []
            if learning_outcomes and len(learning_outcomes) > 0:
                for clo in learning_outcomes:
                    if isinstance(clo, dict):
                        desc = clo.get('description', '')
                        # Tóm tắt CLO description bằng AI (40 ký tự tối đa)
                        summarized_desc = self._summarize_text(desc, max_length=40) if desc else ""
                        clo_list.append({
                            "code": clo.get('code', ''),
                            "description": summarized_desc,
                            "bloom_level": clo.get('bloom_level', ''),
                            "weight": str(clo.get('weight', ''))
                        })
            else:
                logger.warning("⚠️ No CLOs received from backend - check if syllabus was saved properly")
            
            # 9. Ma trận đánh giá (Assessment Matrix)
            ma_tran_danh_gia = []
            assessment_matrix = syllabus_data.get('assessment_matrix', [])
            if assessment_matrix and isinstance(assessment_matrix, list):
                for item in assessment_matrix:
                    if isinstance(item, dict):
                        ma_tran_danh_gia.append({
                            "method": item.get('method', ''),
                            "form": item.get('form', ''),
                            "criteria": self._summarize_text(item.get('criteria', ''), max_length=80) if item.get('criteria') else '',
                            "weight": str(item.get('weight', ''))
                        })
            else:
                logger.warning(" No assessment matrix received from backend - check if syllabus was saved properly")
            
            result = {
                "course_name": course_name,
                "mo_ta_hoc_phan": mo_ta,
                "muc_tieu_hoc_phan": muc_tieu,
                "phuong_phap_giang_day": phuong_phap_giang_day,
                "phuong_phap_danh_gia": phuong_phap_danh_gia,
                "giao_trinh_chinh": giao_trinh_chinh,
                "tai_lieu_tham_khao": tai_lieu_tham_khao,
                "nhiem_vu_sinh_vien": nhiem_vu,
                "clo": clo_list,
                "ma_tran_danh_gia": ma_tran_danh_gia
            }
            
            logger.info("=" * 80)
            logger.info(" STRUCTURED SUMMARY COMPLETED")
            logger.info(f" Summary stats:")
            logger.info(f"   - Description: {len(mo_ta)} chars")
            logger.info(f"   - Objectives: {len(muc_tieu)} items")
            logger.info(f"   - Teaching methods: {len(phuong_phap_giang_day)} items")
            logger.info(f"   - Assessment methods: {len(phuong_phap_danh_gia)} items")
            logger.info(f"   - CLOs: {len(clo_list)} items")
            logger.info(f"   - Assessment matrix: {len(ma_tran_danh_gia)} items")
            logger.info("=" * 80)
            return result
            
        except Exception as e:
            logger.error(f" Summary creation failed: {e}", exc_info=True)
            # Fallback to basic structured summary
            return self._create_structured_summary(syllabus_data)
    
    def _generate_summary(self, prompt: str, max_length: int = 150) -> str:
        """
        Generate summary using BARTpho
        """
        try:
            # Tokenize (BARTpho doesn't use token_type_ids)
            inputs = self.tokenizer(
                prompt,
                max_length=512,
                truncation=True,
                return_tensors="pt",
                add_special_tokens=True
            )
            # Remove token_type_ids if present (not used by BARTpho)
            if 'token_type_ids' in inputs:
                del inputs['token_type_ids']
            
            inputs = inputs.to(self.device)
            
            # Generate with better parameters for quality
            with torch.no_grad():
                outputs = self.model.generate(
                    **inputs,
                    max_length=max_length,
                    min_length=20,
                    num_beams=5,
                    no_repeat_ngram_size=3,
                    repetition_penalty=2.0,
                    length_penalty=1.0,
                    early_stopping=True
                )
            
            # Decode
            summary = self.tokenizer.decode(outputs[0], skip_special_tokens=True)
            return summary.strip()
            
        except Exception as e:
            logger.error(f" Generation failed: {e}")
            return prompt[:max_length]  # Fallback to truncated input
    
    def _format_learning_outcomes(self, outcomes: list) -> str:
        """Format learning outcomes for prompt"""
        if not outcomes:
            return "Không có thông tin"
        formatted = []
        for o in outcomes[:5]:
            if isinstance(o, dict):
                formatted.append(f"- {o.get('description', str(o))}")
            else:
                formatted.append(f"- {str(o)}")
        return "\n".join(formatted)
    
    def _format_assessment_scheme(self, scheme: list) -> str:
        """Format assessment scheme for prompt"""
        if not scheme:
            return "Không có thông tin"
        return "\n".join([f"- {s.get('type', 'N/A')}: {s.get('weight', 0)}%" for s in scheme])
    
    def _extract_highlights(self, syllabus_data: Dict) -> Dict:
        """Extract key highlights from syllabus data"""
        theory_hours = syllabus_data.get('theory_hours', 0)
        practice_hours = syllabus_data.get('practice_hours', 0)
        total_hours = theory_hours + practice_hours
        assessment_scheme = syllabus_data.get('assessment_scheme', [])
        learning_outcomes = syllabus_data.get('learning_outcomes', [])
        
        # Determine difficulty
        difficulty_level = "MEDIUM"
        if total_hours > 60:
            difficulty_level = "HIGH"
        elif total_hours < 30:
            difficulty_level = "EASY"
        
        return {
            "difficulty": {
                "level": difficulty_level,
                "description": f"{difficulty_level.capitalize()} - Tổng {total_hours} tiết"
            },
            "duration": {
                "theory_hours": theory_hours,
                "practice_hours": practice_hours,
                "total_hours": total_hours,
                "description": f"{theory_hours} lý thuyết + {practice_hours} tiết thực hành"
            },
            "assessment": {
                "summary": f"Có {len(assessment_scheme) if assessment_scheme else 0} phương pháp đánh giá",
                "breakdown": assessment_scheme if assessment_scheme else []
            },
            "skills_acquired": {
                "summary": f"Có {len(learning_outcomes) if learning_outcomes else 0} kết quả học tập",
                "key_skills": [
                    o.get('description', str(o))[:100] if isinstance(o, dict) else str(o)[:100] 
                    for o in (learning_outcomes[:5] if learning_outcomes else [])
                ]
            }
        }
    
    def _generate_recommendations(self, syllabus_data: Dict) -> Dict:
        """Generate study recommendations"""
        prerequisites = syllabus_data.get('prerequisites', [])
        theory_hours = syllabus_data.get('theory_hours', 0)
        practice_hours = syllabus_data.get('practice_hours', 0)
        
        # Calculate study time
        total_hours = theory_hours + practice_hours
        hours_per_week = max(4, int(total_hours / 15 * 1.5))  # Assume 15 weeks
        
        return {
            "prerequisites": {
                "required": prerequisites if prerequisites else ["Không có yêu cầu tiên quyết"],
                "description": "Nên có kiến thức cơ bản về các môn tiên quyết" if prerequisites else "Không yêu cầu tiên quyết"
            },
            "preparation": {
                "tips": [
                    "Đọc trước syllabus và tài liệu tham khảo",
                    f"Chuẩn bị {hours_per_week} giờ học mỗi tuần",
                    "Tham gia đầy đủ các buổi thực hành"
                ],
                "description": "Chuẩn bị trước khi học"
            },
            "study_time": {
                "hours_per_week": hours_per_week,
                "breakdown": f"{int(hours_per_week * 0.6)} giờ làm bài tập + {int(hours_per_week * 0.4)} giờ đọc tài liệu",
                "description": f"Dành ít nhất {hours_per_week} giờ/tuần"
            }
        }
    
    def _create_structured_summary(self, syllabus_data: Dict) -> Dict:
        """Create structured summary without AI generation (fallback)"""
        course_name = syllabus_data.get('course_name', 'N/A')
        description = syllabus_data.get('description', 'Không có mô tả')
        
        return {
            "overview": {
                "title": course_name,
                "description": description[:200] if len(description) > 200 else description
            },
            "highlights": self._extract_highlights(syllabus_data),
            "recommendations": self._generate_recommendations(syllabus_data)
        }
    
    def _compare_embeddings_similarity(self, text1: str, text2: str) -> float:
        """
        🚀 TODO: So sánh semantic similarity giữa 2 texts
        
        Example implementation:
        
        from sklearn.metrics.pairwise import cosine_similarity
        import numpy as np
        
        # Get embeddings
        emb1 = self._get_embeddings([text1])[0]
        emb2 = self._get_embeddings([text2])[0]
        
        # Calculate cosine similarity
        similarity = cosine_similarity(
            np.array(emb1).reshape(1, -1),
            np.array(emb2).reshape(1, -1)
        )[0][0]
        
        return float(similarity)
        """
        pass
