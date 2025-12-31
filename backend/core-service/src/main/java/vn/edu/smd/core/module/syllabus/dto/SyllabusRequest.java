package vn.edu.smd.core.module.syllabus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO nhận dữ liệu yêu cầu tạo hoặc cập nhật đề cương (Syllabus).
 * Đã hợp nhất các trường text bổ sung để đồng bộ với Database Schema.
 */
@Data
public class SyllabusRequest {
    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    private UUID academicTermId;

    @NotBlank(message = "Version number is required")
    private String versionNo;

    private String status;

    private LocalDateTime reviewDeadline;
    private LocalDate effectiveDate;

    private String[] keywords;
    
    // Trường này dùng để chứa cấu trúc JSON linh hoạt nếu cần (Cấu trúc động)
    private Map<String, Object> content;

    // FIX: Bổ sung các trường text để khớp với logic trong Service và Entity (Chuẩn Main)
    private String description;
    private String objectives;
    private String studentTasks;
}