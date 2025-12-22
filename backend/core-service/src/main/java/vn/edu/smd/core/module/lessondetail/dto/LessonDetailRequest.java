package vn.edu.smd.core.module.lessondetail.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class LessonDetailRequest {
    
    @NotNull(message = "Lesson plan ID is required")
    private UUID lessonPlanId;
    
    @NotNull(message = "Session number is required")
    @Min(value = 1, message = "Session number must be positive")
    private Integer sessionNumber;
    
    private String content;
    
    private String activity;
    
    @Min(value = 0, message = "Duration must be non-negative")
    private Integer durationMinutes;
    
    private String materials;
}
