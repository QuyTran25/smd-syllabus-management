package vn.edu.smd.core.module.curriculum.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CurriculumResponse {
    private UUID id;
    private String code;
    private String name;
    private UUID facultyId;
    private String facultyCode;
    private String facultyName;
    private Integer totalCredits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
