package vn.edu.smd.core.module.collaborationchange.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CollaborationChangeResponse {
    private UUID id;
    private UUID collaborationSessionId;
    private UUID userId;
    private String userName;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changeType;
    private LocalDateTime createdAt;
}
