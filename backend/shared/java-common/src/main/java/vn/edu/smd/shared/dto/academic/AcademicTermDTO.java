package vn.edu.smd.shared.dto.academic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Academic Term (Học kỳ) DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AcademicTermDTO {
    
    /**
     * Term UUID
     */
    private String id;
    
    /**
     * Term code (e.g., "HK1-2024", "FA24")
     */
    private String code;
    
    /**
     * Term name (e.g., "Học kỳ 1 năm 2024-2025")
     */
    private String name;
    
    /**
     * Start date
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    /**
     * End date
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    /**
     * Is this term currently active
     */
    private Boolean isActive;
    
    /**
     * Number of syllabi in this term
     */
    private Integer syllabusCount;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * Check if term is currently ongoing
     */
    public boolean isOngoing() {
        LocalDate now = LocalDate.now();
        return startDate != null && endDate != null &&
               !now.isBefore(startDate) && !now.isAfter(endDate);
    }
}
