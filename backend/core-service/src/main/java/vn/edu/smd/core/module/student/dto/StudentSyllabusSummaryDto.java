package vn.edu.smd.core.module.student.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentSyllabusSummaryDto {
    private UUID id;
    private String code;
    private String nameVi;
    private String term;
    private Integer credits;
    private String faculty;
    private String program;
    private String lecturerName;
    private String majorShort;
    private Integer progress;
    private boolean tracked;
}