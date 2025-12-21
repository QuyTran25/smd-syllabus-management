package vn.edu.smd.shared.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.enums.AuthProvider;
import vn.edu.smd.shared.enums.Gender;
import vn.edu.smd.shared.enums.UserRole;
import vn.edu.smd.shared.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User DTO for API responses
 * Excludes sensitive data like password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    
    /**
     * User UUID
     */
    private String id;
    
    /**
     * Email address
     */
    private String email;
    
    /**
     * Username (optional, can be same as email)
     */
    private String username;
    
    /**
     * Full name
     */
    private String fullName;
    
    /**
     * Phone number
     */
    private String phone;
    
    /**
     * Gender
     */
    private Gender gender;
    
    /**
     * Avatar URL
     */
    private String avatarUrl;
    
    /**
     * User roles (can have multiple)
     */
    private List<UserRole> roles;
    
    /**
     * Primary role for UI display
     */
    private UserRole primaryRole;
    
    /**
     * Account status
     */
    private UserStatus status;
    
    /**
     * Authentication provider
     */
    private AuthProvider authProvider;
    
    /**
     * Faculty ID (if applicable)
     */
    private String facultyId;
    
    /**
     * Faculty name
     */
    private String facultyName;
    
    /**
     * Department ID (if applicable)
     */
    private String departmentId;
    
    /**
     * Department name
     */
    private String departmentName;
    
    /**
     * Account creation date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * Last update date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(UserRole role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Check if user is active
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
