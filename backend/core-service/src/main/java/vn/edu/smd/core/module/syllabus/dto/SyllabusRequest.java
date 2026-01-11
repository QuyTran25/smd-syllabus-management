package vn.edu.smd.core.module.syllabus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class SyllabusRequest {
    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    private UUID academicTermId;

    @NotBlank(message = "Version number is required")
    private String versionNo;

    private LocalDateTime reviewDeadline;
    private LocalDate effectiveDate;

    private String[] keywords;
    
    // Trường này dùng để chứa cấu trúc JSON linh hoạt nếu cần
    private Map<String, Object> content;

    // FIX: Bổ sung các trường text để khớp với logic trong Service và Entity
    private String description;
    private String objectives;
    private String studentTasks;
    
    // Link to teaching assignment (if created from notification)
    private UUID teachingAssignmentId;
}