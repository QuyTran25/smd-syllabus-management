package vn.edu.smd.core.module.department.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DepartmentResponse {
    private UUID id;
    private UUID facultyId;
    private String facultyCode;
    private String facultyName;
    private String code;
    private String name;
    private String headOfDepartmentName; // Tên trưởng bộ môn
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
