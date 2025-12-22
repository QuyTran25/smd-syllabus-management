package vn.edu.smd.core.module.faculty.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FacultyResponse {
    private UUID id;
    private String code;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
