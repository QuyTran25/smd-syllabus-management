package vn.edu.smd.core.module.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class UserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Password có thể null khi Update (giữ nguyên pass cũ)
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phoneNumber;
    private String status;
    
    // Role code (ADMIN, LECTURER,...)
    private String role; 
    
    private UUID facultyId;
    private UUID departmentId;
    
    // Dành cho Lecturer chọn Manager (HOD)
    private UUID managerId; 
}