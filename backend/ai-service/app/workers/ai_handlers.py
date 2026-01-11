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

# AI Model imports
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
        Initialize handler with AI model for SUMMARIZE function
        """
        self.mock_mode = os.getenv('MOCK_MODE', 'false').lower() == 'true'
        self.model = None
        self.tokenizer = None
        self.device = None
        self.rabbitmq_manager = rabbitmq_manager
        
        # Load AI model for SUMMARIZE if not in mock mode
        if not self.mock_mode and AI_AVAILABLE:
            try:
                self._load_summarize_model()
            except Exception as e:
                logger.error(f"❌ Failed to load AI model: {e}")
                logger.warning("⚠️ Falling back to MOCK mode")
                self.mock_mode = True
        
        mode = "MOCK" if self.mock_mode else "AI"
        logger.info(f"🤖 AI Message Handler initialized in {mode} mode")
    
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
        
        Sử dụng AI model thật (VietAI/vit5-base) để tóm tắt
        """
        syllabus_id = payload.get('syllabus_id')
        syllabus_data = payload.get('syllabus_data', {})
        
        logger.info(f"📝 Summarizing syllabus: {syllabus_id}")
        
        # Use real AI if available
        if not self.mock_mode and self.model is not None:
            return self._summarize_with_ai(syllabus_data)
        
        # Fallback to mock
        logger.info("⚠️ Using MOCK data (AI model not available)")
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
        Load VinAI/bartpho-word model cho summarization
        BART Vietnamese - Tốt hơn cho Vietnamese text generation
        """
        model_name = os.getenv('AI_MODEL_NAME', 'vinai/bartpho-word')
        logger.info(f"📦 Loading model: {model_name}")
        
        # Determine device
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        logger.info(f"🔧 Using device: {self.device}")
        
        # Load tokenizer
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        
        # Load model - BART architecture
        self.model = BartForConditionalGeneration.from_pretrained(
            model_name,
            torch_dtype=torch.float16 if self.device == "cuda" else torch.float32
        ).to(self.device)
        
        logger.info(f"✅ Model loaded successfully on {self.device}")
    
    def _summarize_with_ai(self, syllabus_data: Dict) -> Dict:
        """
        Tạo tóm tắt có cấu trúc từ dữ liệu đề cương theo format chuẩn
        """
        try:
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
            
            # 1. Mô tả học phần
            mo_ta = description if description else "Không có thông tin"
            
            # 2. Mục tiêu học phần
            muc_tieu = []
            if objectives:
                # Check if objectives is a list or string
                if isinstance(objectives, list):
                    for obj in objectives:
                        obj_text = obj if isinstance(obj, str) else str(obj)
                        if obj_text:
                            muc_tieu.append(obj_text)
                elif isinstance(objectives, str):
                    # If it's a string, split by common delimiters or add as single item
                    if '\n' in objectives:
                        muc_tieu = [o.strip() for o in objectives.split('\n') if o.strip()]
                    elif '. ' in objectives:
                        muc_tieu = [o.strip() + '.' for o in objectives.split('. ') if o.strip()]
                    else:
                        muc_tieu = [objectives]
            
            # 3. Phương pháp giảng dạy (từ weekly_content nếu có)
            phuong_phap_giang_day = []
            if weekly_content and len(weekly_content) > 0:
                for week in weekly_content[:3]:
                    if isinstance(week, dict):
                        activities = week.get('activities', '')
                        if activities and activities not in phuong_phap_giang_day:
                            phuong_phap_giang_day.append(activities)
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
            if textbooks and len(textbooks) > 0:
                for book in textbooks:
                    if isinstance(book, dict):
                        if book.get('type') == 'required':
                            giao_trinh_chinh.append({
                                "title": book.get('title', ''),
                                "authors": book.get('authors', ''),
                                "year": book.get('year', '')
                            })
            
            # 6. Tài liệu tham khảo
            tai_lieu_tham_khao = []
            if textbooks and len(textbooks) > 0:
                for book in textbooks:
                    if isinstance(book, dict):
                        if book.get('type') == 'reference':
                            tai_lieu_tham_khao.append({
                                "title": book.get('title', ''),
                                "authors": book.get('authors', ''),
                                "year": book.get('year', '')
                            })
            if references and len(references) > 0:
                for ref in references:
                    ref_text = ref if isinstance(ref, str) else str(ref)
                    if ref_text:
                        tai_lieu_tham_khao.append({"title": ref_text})
            
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
            
            # 8. Chuẩn đầu ra học phần (CLO)
            clo_list = []
            if learning_outcomes and len(learning_outcomes) > 0:
                for clo in learning_outcomes:
                    if isinstance(clo, dict):
                        clo_list.append({
                            "code": clo.get('code', ''),
                            "description": clo.get('description', ''),
                            "bloom_level": clo.get('bloom_level', ''),
                            "weight": str(clo.get('weight', ''))
                        })
            
            result = {
                "course_name": course_name,
                "mo_ta_hoc_phan": mo_ta,
                "muc_tieu_hoc_phan": muc_tieu,
                "phuong_phap_giang_day": phuong_phap_giang_day,
                "phuong_phap_danh_gia": phuong_phap_danh_gia,
                "giao_trinh_chinh": giao_trinh_chinh,
                "tai_lieu_tham_khao": tai_lieu_tham_khao,
                "nhiem_vu_sinh_vien": nhiem_vu,
                "clo": clo_list
            }
            
            logger.info("✅ Structured Summary completed")
            logger.info(f"📄 Result:\n{json.dumps(result, ensure_ascii=False, indent=2)}")
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
