package vn.edu.smd.core.module.teachingmethod.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TeachingMethodResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private String methodName;
    private String description;
    private Integer percentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
