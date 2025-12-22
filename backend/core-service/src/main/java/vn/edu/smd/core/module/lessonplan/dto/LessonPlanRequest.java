package vn.edu.smd.core.module.lessonplan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class LessonPlanRequest {
    
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;
    
    @NotNull(message = "Week number is required")
    @Min(value = 1, message = "Week number must be positive")
    private Integer weekNumber;
    
    @NotBlank(message = "Topic is required")
    private String topic;
    
    private String objectives;
    
    private String teachingMethod;
    
    private String assessmentMethod;
}
