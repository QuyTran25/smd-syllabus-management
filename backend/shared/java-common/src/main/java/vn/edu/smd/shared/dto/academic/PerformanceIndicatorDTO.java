package vn.edu.smd.shared.dto.academic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.assessment.PLODTO;

import java.time.LocalDateTime;

/**
 * Performance Indicator (PI) DTO
 * Performance Indicators are sub-outcomes of PLOs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerformanceIndicatorDTO {
    
    /**
     * PI UUID
     */
    private String id;
    
    /**
     * PLO ID that this PI belongs to
     */
    private String ploId;
    
    /**
     * PLO details (optional, for nested response)
     */
    private PLODTO plo;
    
    /**
     * PI code (e.g., "PI1.1", "PI1.2")
     */
    private String code;
    
    /**
     * PI description
     */
    private String description;
    
    /**
     * Weight or importance (if applicable)
     */
    private Double weight;
    
    /**
     * Number of CLOs mapped to this PI
     */
    private Integer cloCount;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
}
