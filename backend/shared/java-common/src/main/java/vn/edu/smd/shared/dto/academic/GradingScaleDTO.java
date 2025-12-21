package vn.edu.smd.shared.dto.academic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Grading Scale DTO
 * Defines grading scale configuration (e.g., 10-point scale, 4-point GPA, letter grades)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GradingScaleDTO {
    
    /**
     * Grading scale UUID
     */
    private String id;
    
    /**
     * Scale name (e.g., "10-point", "4-point GPA", "Letter Grade")
     */
    private String name;
    
    /**
     * Scale definition in JSONB format
     * Example for 10-point scale:
     * {
     *   "scale_type": "numeric",
     *   "max_score": 10,
     *   "min_score": 0,
     *   "pass_threshold": 5,
     *   "ranges": [
     *     {"min": 8.5, "max": 10, "grade": "A", "description": "Excellent"},
     *     {"min": 7.0, "max": 8.4, "grade": "B", "description": "Good"},
     *     {"min": 5.5, "max": 6.9, "grade": "C", "description": "Average"},
     *     {"min": 4.0, "max": 5.4, "grade": "D", "description": "Below Average"},
     *     {"min": 0, "max": 3.9, "grade": "F", "description": "Fail"}
     *   ]
     * }
     */
    private Map<String, Object> definition;
    
    /**
     * Is this the default grading scale?
     */
    private Boolean isDefault;
    
    /**
     * Number of syllabi using this grading scale
     */
    private Integer usageCount;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
