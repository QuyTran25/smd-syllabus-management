package vn.edu.smd.core.module.auditlog.dto;

import lombok.Data;
import vn.edu.smd.shared.enums.AuditStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class AuditLogResponse {
    private UUID id;
    private String entityName;
    private UUID entityId;
    private String action;
    private UUID actorId;
    
    // Thông tin user để hiển thị trên frontend
    private String actorName;
    private String actorEmail;
    private String actorRole;
    
    private String description;
    private AuditStatus status;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
