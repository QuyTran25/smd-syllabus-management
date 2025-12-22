package vn.edu.smd.core.module.organization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizationRequest {
    @NotBlank(message = "Organization name is required")
    private String name;

    private String code;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
}
