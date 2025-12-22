package vn.edu.smd.core.module.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {
    @NotBlank(message = "Role name is required")
    private String name;

    private String description;
}
