package vn.edu.smd.core.module.syllabus.dto;

import lombok.Data;
import java.util.UUID;

/**
 * Request để tạo syllabus draft từ teaching assignment
 */
@Data
public class CreateSyllabusFromAssignmentRequest {
    private UUID teachingAssignmentId;
}
