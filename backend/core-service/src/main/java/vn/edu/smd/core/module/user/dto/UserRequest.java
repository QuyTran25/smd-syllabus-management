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

    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phoneNumber;
    private String status;
    private UUID facultyId;
    private UUID departmentId;
}
