package vn.edu.smd.core.module.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class AssignRolesRequest {
    @NotEmpty(message = "Role IDs are required")
    private Set<UUID> roleIds;
}
