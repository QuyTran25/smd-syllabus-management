package vn.edu.smd.shared.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh token request DTO
 * Used to obtain new access token using refresh token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    
    /**
     * Refresh token obtained from login response
     */
    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;
}
