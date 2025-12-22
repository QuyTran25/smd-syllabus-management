package vn.edu.smd.core.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserInfoResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Set<String> roles;
    private String status;
}
