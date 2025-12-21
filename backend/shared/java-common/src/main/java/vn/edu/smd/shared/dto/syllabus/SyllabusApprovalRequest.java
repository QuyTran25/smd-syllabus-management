package vn.edu.smd.shared.dto.syllabus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.enums.ApprovalAction;

/**
 * Syllabus approval/rejection request DTO
 * Used by approvers (HOD, AA, Principal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusApprovalRequest {
    
    /**
     * Approval action
     */
    @NotBlank(message = "Hành động không được để trống")
    private ApprovalAction action;
    
    /**
     * Comments from reviewer
     * Required for REJECT and REQUEST_CHANGES
     */
    @Size(max = 2000, message = "Nhận xét không được vượt quá 2000 ký tự")
    private String comments;
    
    /**
     * Optional: Specific issues to address
     */
    private java.util.List<String> issues;
}
