package vn.edu.smd.core.module.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class NotificationRequest {
    @NotNull
    private UUID userId;
    
    @NotBlank
    private String type;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String message;
    
    private String relatedEntityType;
    private UUID relatedEntityId;
}
