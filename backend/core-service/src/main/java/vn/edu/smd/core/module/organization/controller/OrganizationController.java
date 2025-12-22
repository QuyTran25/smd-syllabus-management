package vn.edu.smd.core.module.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.organization.dto.OrganizationRequest;
import vn.edu.smd.core.module.organization.dto.OrganizationResponse;
import vn.edu.smd.core.module.organization.service.OrganizationService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Organization Management", description = "Organization management APIs")
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Get all organizations", description = "Get list of all organizations")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {
        List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(ApiResponse.success(organizations));
    }

    @Operation(summary = "Get organization by ID", description = "Get organization details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable UUID id) {
        OrganizationResponse organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(ApiResponse.success(organization));
    }

    @Operation(summary = "Create organization", description = "Create new organization")
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(@Valid @RequestBody OrganizationRequest request) {
        OrganizationResponse organization = organizationService.createOrganization(request);
        return ResponseEntity.ok(ApiResponse.success("Organization created successfully", organization));
    }

    @Operation(summary = "Update organization", description = "Update organization information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(@PathVariable UUID id, @Valid @RequestBody OrganizationRequest request) {
        OrganizationResponse organization = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", organization));
    }

    @Operation(summary = "Delete organization", description = "Delete organization by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully", null));
    }
}
