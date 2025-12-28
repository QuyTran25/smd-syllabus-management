package vn.edu.smd.core.module.role.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class RoleResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Boolean isSystem;
}
