"""
AI Message Handler
Xử lý messages từ RabbitMQ và route tới handlers tương ứng
"""
import logging
import time
import json
from datetime import datetime
from typing import Dict, Any
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
            
            # TODO: Lưu result vào DB (ai_service.syllabus_ai_analysis)
            # self._save_to_database(message_id, action, result, processing_time)
            
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
        
        logger.info(f"✅ CLO-PLO analysis completed. Status: {result['overall_status']}")
        return result
    
    def _handle_compare_versions(self, message_id: str, payload: Dict) -> Dict:
        """
        Handler cho COMPARE_VERSIONS - So sánh phiên bản
        
        MOCK DATA
        """
        old_version_id = payload.get('old_version_id')
        new_version_id = payload.get('new_version_id')
        
        logger.info(f"🔍 Comparing versions: {old_version_id} → {new_version_id}")
        
        time.sleep(3)  # 3 seconds
        
        # MOCK RESULT
        result = {
            "is_first_version": False,
            "version_history": [
                {
                    "version_number": "NaN",
                    "status": "Hiện tại",
                    "created_by": "Trần Thị Lan",
                    "created_at": "02/01/2026 08:24",
                    "is_current": True
                },
                {
                    "version_number": "NaN",
                    "status": "Phiên bản NaN",
                    "created_by": "Trần Thị Lan",
                    "created_at": "30/12/2025 16:20",
                    "is_current": False
                }
            ],
            "changes_summary": {
                "total_changes": 3,
                "major_changes": 2,
                "minor_changes": 1,
                "sections_affected": ["learning_outcomes", "assessment_scheme", "references"]
            },
            "detailed_changes": [
                {
                    "section": "learning_outcomes",
                    "section_title": "Mục tiêu học tập",
                    "change_type": "MODIFIED",
                    "changes": [
                        {
                            "field": "CLO 1",
                            "old_value": "Sinh viên hiểu các khái niệm cơ bản về CSDL",
                            "new_value": "Sinh viên nắm vững và áp dụng được các khái niệm cơ bản về CSDL",
                            "significance": "HIGH",
                            "impact": "Tăng mức độ yêu cầu từ 'hiểu' lên 'áp dụng'"
                        }
                    ]
                }
            ],
            "ai_analysis": {
                "overall_assessment": "Phiên bản mới có cải thiện đáng kể về CLO và phương pháp đánh giá",
                "key_improvements": [
                    "CLO được nâng cấp từ mức độ 'hiểu' lên 'áp dụng', phù hợp với PLO",
                    "Thêm bài tập nhóm giúp phát triển kỹ năng làm việc nhóm"
                ],
                "recommendations": [
                    "Cân nhắc bổ sung rubric chi tiết cho bài tập nhóm"
                ]
            }
        }
        
        logger.info(f"✅ Version comparison completed")
        return result
    
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
        
        if len(text) <= max_length:
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
        logger.info(f"🤖 [GEMINI] Summarizing text: {len(text)} chars")
        
        prompt = f"""Tóm tắt văn bản sau thành 2-3 câu ngắn gọn (tối đa {max_length} ký tự), giữ nguyên thông tin quan trọng nhất:

{text}

Tóm tắt:"""
        
        response = self.gemini_client.generate_content(prompt)
        summary = response.text.strip()
        
        logger.info(f"✅ [GEMINI SUCCESS] Summary: {len(summary)} chars")
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
        Simple extractive summarization - take first N sentences
        Fallback when AI model not available
        """
        # Simple: Take first N sentences
        sentences = []
        for delimiter in ['. ', '.\n', '! ', '? ']:
            if delimiter in text:
                parts = [s.strip() for s in text.split(delimiter) if s.strip()]
                sentences = parts
                break
        
        if not sentences:
            # No sentences, just truncate
            return text[:max_length] + '...'
        
        # Take first 2-3 sentences
        result = []
        current_len = 0
        for sent in sentences[:3]:
            if current_len + len(sent) <= max_length:
                result.append(sent)
                current_len += len(sent) + 2
            else:
                break
        
        if result:
            summary = '. '.join(result)
            if not summary.endswith('.'):
                summary += '.'
            logger.info(f"📝 Simple truncation: {len(text)} -> {len(summary)} chars")
            return summary
        
        # Fallback
        return text[:max_length] + '...'

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
            
            # 1. Mô tả học phần - TÓM TẮT GỌN bằng extractive summarization
            mo_ta = self._summarize_text(description, max_length=200) if description else "Không có thông tin"
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
                            # Tóm tắt mỗi mục tiêu bằng extractive summarization
                            summarized = self._summarize_text(obj_text.strip(), max_length=120)
                            muc_tieu.append(summarized)
                            logger.debug(f"   Objective: {len(obj_text)} -> {len(summarized)} chars")
                elif isinstance(objectives, str) and objectives.strip():
                    # If it's a string, split by common delimiters or add as single item
                    if '\n' in objectives:
                        parts = [o.strip() for o in objectives.split('\n') if o.strip()]
                        muc_tieu = [self._summarize_text(p, max_length=80) for p in parts[:5]]  # Limit to 5 objectives
                    elif '. ' in objectives and len(objectives) > 50:  # Only split if it's a long text
                        parts = objectives.split('. ')
                        formatted = [o.strip() + ('.' if not o.endswith('.') else '') for o in parts if o.strip()]
                        muc_tieu = [self._summarize_text(f, max_length=80) for f in formatted[:5]]  # Limit to 5
                    else:
                        muc_tieu = [self._summarize_text(objectives.strip(), max_length=120)]
            
            logger.info(f"✅ Objectives processed: {len(muc_tieu)} items")
            
            # 3. Phương pháp giảng dạy
            phuong_phap_giang_day = []
            
            # First check if there's a teaching_method field
            teaching_method = syllabus_data.get('teaching_method', '')
            if teaching_method:
                if isinstance(teaching_method, str) and teaching_method.strip():
                    # Split by newline or comma
                    if '\n' in teaching_method:
                        phuong_phap_giang_day = [m.strip() for m in teaching_method.split('\n') if m.strip()]
                    elif ',' in teaching_method:
                        phuong_phap_giang_day = [m.strip() for m in teaching_method.split(',') if m.strip()]
                    else:
                        phuong_phap_giang_day = [teaching_method.strip()]
                elif isinstance(teaching_method, list):
                    phuong_phap_giang_day = [str(m).strip() for m in teaching_method if str(m).strip()]
            
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
            
            # 7. Nhiệm vụ của Sinh viên
            nhiem_vu = []
            student_duties = syllabus_data.get('student_duties', '')
            if student_duties:
                # If data from database exists
                if isinstance(student_duties, str):
                    if '. ' in student_duties:
                        nhiem_vu = [nv.strip() + '.' for nv in student_duties.split('. ') if nv.strip()]
                    else:
                        nhiem_vu = [student_duties]
                elif isinstance(student_duties, list):
                    nhiem_vu = student_duties
            
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
                        summarized_desc = self._summarize_text(desc, max_length=100) if desc else desc
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
                logger.warning("⚠️ No assessment matrix received from backend - check if syllabus was saved properly")
            
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
            logger.info("✅ STRUCTURED SUMMARY COMPLETED")
            logger.info(f"📊 Summary stats:")
            logger.info(f"   - Description: {len(mo_ta)} chars")
            logger.info(f"   - Objectives: {len(muc_tieu)} items")
            logger.info(f"   - Teaching methods: {len(phuong_phap_giang_day)} items")
            logger.info(f"   - Assessment methods: {len(phuong_phap_danh_gia)} items")
            logger.info(f"   - CLOs: {len(clo_list)} items")
            logger.info(f"   - Assessment matrix: {len(ma_tran_danh_gia)} items")
            logger.info("=" * 80)
            return result
            
        except Exception as e:
            logger.error(f"❌ Summary creation failed: {e}", exc_info=True)
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
            logger.error(f"❌ Generation failed: {e}")
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
