package vn.edu.smd.core.module.lessonplan.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LessonPlanResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private Integer weekNumber;
    private String topic;
    private String objectives;
    private String teachingMethod;
    private String assessmentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
