package vn.edu.smd.core.module.auditlog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequest {
    // Frontend gửi: "Syllabus", "User", "System"
    private String entityType;
    
    // Frontend gửi ID (có thể là UUID hoặc null)
    private String entityId;
    
    // Frontend gửi: "CREATE", "UPDATE", "PUBLISH"...
    private String action;
    
    // Frontend gửi mô tả chi tiết
    private String description;
    
    // Frontend gửi: "SUCCESS" hoặc "FAILED"
    private String status;
}