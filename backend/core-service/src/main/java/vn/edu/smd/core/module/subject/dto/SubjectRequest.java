package vn.edu.smd.core.module.subject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.edu.smd.shared.enums.SubjectComponent;
import vn.edu.smd.shared.enums.SubjectType;

import java.util.UUID;

@Data
public class SubjectRequest {
    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z]{2,6}\\d{2}$", message = "Code must be 2-6 uppercase letters followed by 2 digits (e.g., CSDL26)")
    private String code;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;
    
    /**
     * Học kỳ được chọn khi tạo môn học
     * Dùng để hiển thị thông tin và gửi thông báo
     */
    @NotNull(message = "Academic Term ID is required")
    private UUID academicTermId;

    private UUID curriculumId;

    @NotBlank(message = "Name (Vietnamese) is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String currentNameVi;

    @Size(max = 255, message = "Name (English) must not exceed 255 characters")
    private String currentNameEn;

    @NotNull(message = "Credits is required")
    private Integer defaultCredits;

    private Boolean isActive;

    private SubjectType subjectType;

    private SubjectComponent component;

    private Integer defaultTheoryHours;

    private Integer defaultPracticeHours;

    private Integer defaultSelfStudyHours;

    private String description;

    private Integer recommendedTerm;
}
