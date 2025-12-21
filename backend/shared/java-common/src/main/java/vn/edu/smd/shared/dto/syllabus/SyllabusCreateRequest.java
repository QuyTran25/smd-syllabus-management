package vn.edu.smd.shared.dto.syllabus;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Create/Update syllabus request DTO
 * Used when creating or updating a syllabus
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusCreateRequest {
    
    /**
     * Subject ID (required)
     */
    @NotBlank(message = "Mã môn học không được để trống")
    private String subjectId;
    
    /**
     * Academic term ID (required)
     */
    @NotBlank(message = "Học kỳ không được để trống")
    private String academicTermId;
    
    /**
     * Version number (auto-generated if not provided)
     */
    private String versionNo;
    
    /**
     * Previous version ID (for version tracking)
     */
    private String previousVersionId;
    
    /**
     * Review deadline
     */
    private LocalDateTime reviewDeadline;
    
    /**
     * Keywords for search
     */
    @Size(max = 20, message = "Không được vượt quá 20 từ khóa")
    private List<String> keywords;
    
    /**
     * Syllabus content (flexible JSONB structure)
     */
    @NotNull(message = "Nội dung đề cương không được để trống")
    private SyllabusContentRequest content;
    
    /**
     * Content structure matching SyllabusDetailDTO.SyllabusContent
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyllabusContentRequest {
        @NotBlank(message = "Mô tả môn học không được để trống")
        @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
        private String description;
        
        @Size(max = 3000, message = "Mục tiêu không được vượt quá 3000 ký tự")
        private String objectives;
        
        private List<String> prerequisiteIds;
        
        @NotNull(message = "Số giờ lý thuyết không được để trống")
        private Integer theoryHours;
        
        @NotNull(message = "Số giờ thực hành không được để trống")
        private Integer practiceHours;
        
        @NotNull(message = "Số giờ tự học không được để trống")
        private Integer selfStudyHours;
        
        private List<String> cloIds;
        private List<String> ploIds;
        private List<SyllabusDetailDTO.WeeklyPlan> weeklyPlans;
        private List<String> assessmentSchemeIds;
        private List<SyllabusDetailDTO.Material> textbooks;
        private List<SyllabusDetailDTO.Material> references;
        private JsonNode additionalInfo;
    }
}
