package vn.edu.smd.shared.dto.assessment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.academic.PerformanceIndicatorDTO;
import vn.edu.smd.shared.enums.MappingLevel;

import java.time.LocalDateTime;

/**
 * CLO-PI Mapping DTO
 * Maps Course Learning Outcomes to Performance Indicators with achievement level
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloPiMappingDTO {
    
    /**
     * Mapping UUID
     */
    private String id;
    
    /**
     * CLO ID
     */
    private String cloId;
    
    /**
     * CLO details (optional, for nested response)
     */
    private CLODTO clo;
    
    /**
     * Performance Indicator ID
     */
    private String piId;
    
    /**
     * Performance Indicator details (optional, for nested response)
     */
    private PerformanceIndicatorDTO pi;
    
    /**
     * Mapping level: H (High), M (Medium), L (Low)
     * Indicates the strength of relationship between CLO and PI
     */
    private MappingLevel level;
    
    /**
     * Level description (for display)
     */
    private String levelDescription;
    
    /**
     * Additional notes about the mapping
     */
    private String notes;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
