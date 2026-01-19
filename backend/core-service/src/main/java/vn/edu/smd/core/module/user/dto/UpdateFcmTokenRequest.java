package vn.edu.smd.core.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for updating Firebase Cloud Messaging token
 */
@Data
public class UpdateFcmTokenRequest {
    
    @NotBlank(message = "FCM token is required")
    private String token;
}
