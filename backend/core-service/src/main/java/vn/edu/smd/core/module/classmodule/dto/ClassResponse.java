package vn.edu.smd.core.module.classmodule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponse {

    private UUID id;
    private String code;
    private String name;
    private UUID subjectId;
    private String subjectCode;
    private String subjectName;
    private UUID semesterId;
    private String semesterName;
    private UUID lecturerId;
    private String lecturerName;
    private Integer maxStudents;
    private Integer currentStudents;
    private String schedule;
    private String room;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
