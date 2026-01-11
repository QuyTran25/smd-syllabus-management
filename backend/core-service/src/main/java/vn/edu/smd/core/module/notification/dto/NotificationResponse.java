package vn.edu.smd.core.module.notification.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private String type;
    private String title;
    private String message;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private Map<String, Object> payload;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
