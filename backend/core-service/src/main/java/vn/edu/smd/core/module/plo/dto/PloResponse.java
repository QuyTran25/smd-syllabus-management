package vn.edu.smd.core.module.plo.dto;

import lombok.Data;
import vn.edu.smd.shared.enums.PloCategory;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PloResponse {
    private UUID id;
    private UUID curriculumId;
    private String curriculumCode;
    private String curriculumName;
    private String code;
    private String description;
    private PloCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
