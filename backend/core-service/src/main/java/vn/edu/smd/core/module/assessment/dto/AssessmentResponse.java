package vn.edu.smd.core.module.assessment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AssessmentResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private UUID parentId;
    private String parentName;
    private String name;
    private BigDecimal weightPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
