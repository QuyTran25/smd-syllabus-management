package vn.edu.smd.core.module.role.dto;

import lombok.Data;
<<<<<<< HEAD
import java.time.LocalDateTime;
=======
>>>>>>> origin/main
import java.util.UUID;

@Data
public class RoleResponse {
    private UUID id;
<<<<<<< HEAD
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
=======
    private String code;
    private String name;
    private String description;
    private Boolean isSystem;
>>>>>>> origin/main
}
