package vn.edu.smd.shared.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.user.UserDTO;

import java.time.LocalDateTime;

/**
 * Login response DTO
 * Contains JWT tokens and user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * JWT access token for API authentication
     * Short-lived (15 minutes - 1 hour)
     */
    private String accessToken;
    
    /**
     * JWT refresh token for obtaining new access tokens
     * Long-lived (7-30 days)
     */
    private String refreshToken;
    
    /**
     * Token type (always "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * Access token expiration time in seconds
     */
    private Long expiresIn;
    
    /**
     * User information
     */
    private UserDTO user;
    
    /**
     * Login timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime loginAt = LocalDateTime.now();
}
