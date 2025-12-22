package vn.edu.smd.core.module.organization.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OrganizationResponse {
    private UUID id;
    private String name;
    private String code;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
