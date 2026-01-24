package vn.edu.smd.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Task Status DTO for Redis Serialization
 * 
 * This DTO is used to store AI task status in Redis.
 * It implements Serializable to support Jackson JSON serialization.
 * 
 * Purpose: Replace Map<String, Object> to avoid ClassCastException
 * and ensure type safety when reading from Redis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Task identifier (UUID)
     */
    private String taskId;
    
    /**
     * Action type: MAP_CLO_PLO, COMPARE_VERSIONS, SUMMARIZE_SYLLABUS
     */
    private String action;
    
    /**
     * Current status: QUEUED, PROCESSING, SUCCESS, ERROR
     */
    private String status;
    
    /**
     * Progress percentage (0-100)
     */
    private Integer progress;
    
    /**
     * Status message
     */
    private String message;
    
    /**
     * AI analysis result (JSON-serializable Map)
     * Only populated when status = SUCCESS
     */
    private Map<String, Object> result;
    
    /**
     * Error message (only when status = ERROR)
     */
    private String errorMessage;
    
    /**
     * Timestamp in milliseconds
     */
    private Long timestamp;
    
    /**
     * Processing time in milliseconds (only when completed)
     */
    private Integer processingTimeMs;
    
    /**
     * User ID who requested the task
     */
    private String userId;
    
    /**
     * Priority: LOW, MEDIUM, HIGH
     */
    private String priority;
}
