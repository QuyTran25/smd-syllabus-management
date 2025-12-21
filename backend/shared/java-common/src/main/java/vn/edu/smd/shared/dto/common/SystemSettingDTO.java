package vn.edu.smd.shared.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * System Setting DTO
 * Represents dynamic system configuration settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemSettingDTO {
    
    /**
     * Setting key (unique identifier)
     * Examples: "max_clo_count", "min_clo_count", "grading_scale_default"
     */
    private String key;
    
    /**
     * Setting display name
     */
    private String name;
    
    /**
     * Setting value in JSONB format
     * Can be any valid JSON structure:
     * - Simple value: {"value": 10}
     * - Complex object: {"scale": "10", "pass_threshold": 5}
     * - Array: {"allowed_formats": ["PDF", "DOCX"]}
     */
    private Map<String, Object> value;
    
    /**
     * Setting description
     */
    private String description;
    
    /**
     * Setting category (for grouping in UI)
     */
    private String category;
    
    /**
     * Data type hint for UI (string, number, boolean, object, array)
     */
    private String dataType;
    
    /**
     * Is this setting editable by admin?
     */
    private Boolean isEditable;
    
    /**
     * Is this a system-critical setting?
     */
    private Boolean isSystem;
    
    /**
     * Default value (if any)
     */
    private Map<String, Object> defaultValue;
    
    /**
     * Validation rules (optional)
     */
    private Map<String, Object> validation;
    
    /**
     * Last updated by user ID
     */
    private String updatedBy;
    
    /**
     * Update metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
