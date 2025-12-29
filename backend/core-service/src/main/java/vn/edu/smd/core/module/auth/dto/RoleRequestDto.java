package vn.edu.smd.core.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDto {
    private UUID id;
    private UUID userId;
    private String requestedRole;
    private String status;
    private String comment;
}
