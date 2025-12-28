package vn.edu.smd.core.module.collaboration.dto;

import lombok.Data;
import vn.edu.smd.shared.enums.CollaboratorRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CollaborationResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private CollaboratorRole role;
    private LocalDateTime assignedAt;
}
