package vn.edu.smd.shared.dto.assessment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Assessment-CLO Mapping DTO
 * Maps assessment schemes to CLOs with contribution percentage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentCloMappingDTO {
    
    /**
     * Mapping UUID
     */
    private String id;
    
    /**
     * Assessment scheme ID
     */
    private String assessmentSchemeId;
    
    /**
     * Assessment scheme details (optional, for nested response)
     */
    private AssessmentSchemeDTO assessmentScheme;
    
    /**
     * CLO ID
     */
    private String cloId;
    
    /**
     * CLO details (optional, for nested response)
     */
    private CLODTO clo;
    
    /**
     * Contribution percentage (0-100)
     * Indicates how much this assessment contributes to evaluating this CLO
     */
    private BigDecimal contributionPercent;
    
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
