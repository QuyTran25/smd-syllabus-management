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
    
    // Trường này dùng để chứa cấu trúc JSON linh hoạt nếu cần
    private Map<String, Object> content;

    // =========================================================================
    // PHẦN MERGE: CHỌN CODE CỦA TEAM (origin/main) VÌ ĐẦY ĐỦ HƠN
    // =========================================================================
    // Team đã bổ sung thêm description và objectives, đồng thời cũng có cả 
    // trường studentTasks mà bạn cần.
    
    private String description;
    private String objectives;
    private String studentTasks;
}