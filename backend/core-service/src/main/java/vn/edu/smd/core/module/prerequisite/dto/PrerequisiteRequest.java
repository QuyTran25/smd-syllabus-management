package vn.edu.smd.core.module.prerequisite.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.smd.shared.enums.SubjectRelationType;

import java.util.UUID;

@Data
public class PrerequisiteRequest {
    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @NotNull(message = "Related Subject ID is required")
    private UUID relatedSubjectId;

    @NotNull(message = "Relationship type is required")
    private SubjectRelationType type;
}
