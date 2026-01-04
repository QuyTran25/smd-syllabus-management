package vn.edu.smd.shared.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

/**
 * AI Message Request DTO
 * Được gửi từ Core Service → RabbitMQ → AI Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIMessageRequest {
    
    /**
     * Unique message ID (UUID)
     */
    @NotBlank(message = "Message ID không được để trống")
    private String messageId;
    
    /**
     * Action type: SUMMARIZE_SYLLABUS | COMPARE_VERSIONS | MAP_CLO_PLO
     */
    @NotBlank(message = "Action không được để trống")
    private String action;
    
    /**
     * Priority: HIGH | MEDIUM | LOW
     */
    @NotBlank(message = "Priority không được để trống")
    private String priority;
    
    /**
     * Timestamp khi message được tạo
     */
    @NotNull(message = "Timestamp không được null")
    private Instant timestamp;
    
    /**
     * User ID của người request (optional, cho audit)
     */
    private String userId;
    
    /**
     * Payload chứa data cần thiết cho AI processing
     * - SUMMARIZE: { syllabus_id, language, include_prerequisites }
     * - COMPARE: { old_version_id, new_version_id, comparison_depth }
     * - MAP_CLO_PLO: { syllabus_id, curriculum_id, strict_mode, check_weights }
     */
    @NotNull(message = "Payload không được null")
    private Map<String, Object> payload;
}
