package vn.edu.smd.core.module.subjectcomponent.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubjectComponentResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private String componentType;
    private Integer hours;
    private String description;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
