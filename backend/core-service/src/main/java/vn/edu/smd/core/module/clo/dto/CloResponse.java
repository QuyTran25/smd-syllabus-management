package vn.edu.smd.core.module.clo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CloResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private String code;
    private String description;
    private String bloomLevel;
    private BigDecimal weight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
