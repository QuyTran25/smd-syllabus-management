package vn.edu.smd.core.module.performanceindicator.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PerformanceIndicatorResponse {
    private UUID id;
    private UUID ploId;
    private String ploCode;
    private String code;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
