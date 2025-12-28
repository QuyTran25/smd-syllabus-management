package vn.edu.smd.core.module.syllabus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class SyllabusRequest {
    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    private UUID academicTermId;

    @NotBlank(message = "Version number is required")
    private String versionNo;

    /** Optional status string (e.g. DRAFT or PENDING_APPROVAL) */
    private String status;

    private LocalDateTime reviewDeadline;
    private LocalDate effectiveDate;

    private String[] keywords;
    private Map<String, Object> content;
}
