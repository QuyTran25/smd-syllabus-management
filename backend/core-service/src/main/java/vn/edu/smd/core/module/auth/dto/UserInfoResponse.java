package vn.edu.smd.core.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String primaryRole; // Giữ lại để đồng bộ với Entity User và AuthService
    private Set<String> roles;
    private String status;
}