package vn.edu.smd.core.module.prerequisite.dto;

import lombok.Data;
import vn.edu.smd.shared.enums.SubjectRelationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PrerequisiteResponse {
    private UUID id;
    private UUID subjectId;
    private String subjectCode;
    private String subjectName;
    private UUID relatedSubjectId;
    private String relatedSubjectCode;
    private String relatedSubjectName;
    private SubjectRelationType type;
    private LocalDateTime createdAt;
}
