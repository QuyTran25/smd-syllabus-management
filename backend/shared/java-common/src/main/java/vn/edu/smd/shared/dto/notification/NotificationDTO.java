package vn.edu.smd.shared.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification DTO
 * Used for user notifications (FCM push, in-app, email)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDTO {
    
    /**
     * Notification UUID
     */
    private String id;
    
    /**
     * Recipient user ID
     */
    private String userId;
    
    /**
     * Notification title
     */
    private String title;
    
    /**
     * Notification message/content
     */
    private String message;
    
    /**
     * Notification type
     * (e.g., "INFO", "WARNING", "APPROVAL_REQUEST", "APPROVAL_RESULT", "DEADLINE_REMINDER")
     */
    private String type;
    
    /**
     * Related resource type (e.g., "SYLLABUS", "USER", "FEEDBACK")
     */
    private String resourceType;
    
    /**
     * Related resource ID
     */
    private String resourceId;
    
    /**
     * Action URL (deep link for navigation)
     */
    private String actionUrl;
    
    /**
     * Is notification read
     */
    private Boolean isRead;
    
    /**
     * Read timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readAt;
    
    /**
     * Creation timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * Expiration timestamp (optional)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
}
