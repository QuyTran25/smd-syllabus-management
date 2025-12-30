package vn.edu.smd.core.module.subject.dto;

import lombok.Data;
import vn.edu.smd.shared.enums.SubjectComponent;
import vn.edu.smd.shared.enums.SubjectType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubjectResponse {
    private UUID id;
    private String code;
    private UUID departmentId;
    private String departmentCode;
    private String departmentName;
    private String facultyName;
    private String semester;
    private String prerequisites;

    private UUID curriculumId;
    private String curriculumCode;
    private String curriculumName;
    private String currentNameVi;
    private String currentNameEn;
    private Integer defaultCredits;
    private Boolean isActive;
    private SubjectType subjectType;
    private SubjectComponent component;
    private Integer defaultTheoryHours;
    private Integer defaultPracticeHours;
    private Integer defaultSelfStudyHours;
    private String description;
    private Integer recommendedTerm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
