package vn.edu.smd.core.module.student.dto;

import lombok.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSyllabusDetailDto {
    private UUID id;
    private UUID versionId;
    private String code;
    private String nameVi;
    private String nameEn;
    private String term;
    private Integer credits;
    private String faculty;
    private String program;
    private String lecturerName;
    private String lecturerEmail;
    private String description;
    private String status;
    private String teachingMethods;
    private String publishedAt;
    private String summaryInline; // Nội dung Tóm tắt AI


    private boolean isTracked;
    private TimeAllocationDto timeAllocation;
    private List<String> objectives;
    private List<String> studentTasks;
    private List<String> textbooks;
    private List<String> references;
    
    private List<CloDto> clos;
    private List<AssessmentDto> assessmentMatrix;
    
    private List<String> ploList; // Danh sách mã PLO (PLO1, PLO2...)
    private Map<String, List<String>> cloPloMap; // Ánh xạ để hiện dấu ✓

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeAllocationDto {
        private Integer theory;
        private Integer practice;
        private Integer selfStudy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CloDto {
        private String code;
        private String description;
        private String bloomLevel;
        private Integer weight;
        private List<String> plo; // Danh sách mã PLO ánh xạ
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentDto {
        private String method;
        private String form;
        private List<String> clo; // Danh sách CLO liên quan
        private String criteria;
        private Integer weight;
    }
}