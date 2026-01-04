package vn.edu.smd.shared.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI Message Response DTO
 * Được lưu trong DB (ai_service.syllabus_ai_analysis) và trả về cho Frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIMessageResponse {
    
    /**
     * Message ID tương ứng với request
     */
    private String messageId;
    
    /**
     * Action type
     */
    private String action;
    
    /**
     * Status: SUCCESS | FAILED | PROCESSING | QUEUED
     */
    private String status;
    
    /**
     * Progress (0-100)
     */
    private Integer progress;
    
    /**
     * Current step being processed
     */
    private String currentStep;
    
    /**
     * Result data (structure khác nhau theo action)
     */
    private Map<String, Object> result;
    
    /**
     * Error message nếu failed
     */
    private String errorMessage;
    
    /**
     * Processing time in milliseconds
     */
    private Integer processingTimeMs;
}
