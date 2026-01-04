"""
AI Message Handler
Xử lý messages từ RabbitMQ và route tới handlers tương ứng
"""
import logging
import time
from datetime import datetime
from typing import Dict, Any

logger = logging.getLogger(__name__)


class AIMessageHandler:
    """Handler chính cho AI messages"""
    
    def __init__(self):
        """
        Initialize handler
        TODO: Inject các services (DB, Redis, AI models) khi implement thật
        """
        logger.info("🤖 AI Message Handler initialized")
    
    def handle_message(self, message: Dict[str, Any]) -> Dict[str, Any]:
        """
        Route message tới handler phù hợp dựa trên action
        
        Args:
            message: Message dict từ RabbitMQ
            
        Returns:
            Response dict với status và result
        """
        action = message.get('action')
        message_id = message.get('message_id')
        payload = message.get('payload', {})
        
        start_time = datetime.now()
        
        try:
            logger.info(f"🔄 Processing {action} - Message ID: {message_id}")
            
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
                'message_id': message_id,
                'action': action,
                'status': 'SUCCESS',
                'result': result,
                'processing_time_ms': processing_time
            }
            
            logger.info(f"✅ {action} completed in {processing_time}ms")
            
            # TODO: Lưu result vào DB (ai_service.syllabus_ai_analysis)
            # self._save_to_database(message_id, action, result, processing_time)
            
            return response
            
        except Exception as e:
            logger.error(f"❌ Error handling {action}: {e}", exc_info=True)
            return {
                'message_id': message_id,
                'action': action,
                'status': 'FAILED',
                'error_message': str(e)
            }
    
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
        
        MOCK DATA
        """
        syllabus_id = payload.get('syllabus_id')
        
        logger.info(f"📝 Summarizing syllabus: {syllabus_id}")
        
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
