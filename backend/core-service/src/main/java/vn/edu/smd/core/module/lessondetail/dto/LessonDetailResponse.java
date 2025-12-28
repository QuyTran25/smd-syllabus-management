package vn.edu.smd.core.module.lessondetail.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LessonDetailResponse {
    private UUID id;
    private UUID lessonPlanId;
    private Integer sessionNumber;
    private String content;
    private String activity;
    private Integer durationMinutes;
    private String materials;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
