package vn.edu.smd.core.module.role.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.role.dto.RoleRequest;
import vn.edu.smd.core.module.role.dto.RoleResponse;
import vn.edu.smd.core.module.role.service.RoleService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Role Management", description = "Role management APIs")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get all roles", description = "Get list of all roles")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @Operation(summary = "Get role by ID", description = "Get role details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable UUID id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @Operation(summary = "Create role", description = "Create new role")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse role = roleService.createRole(request);
        return ResponseEntity.ok(ApiResponse.success("Role created successfully", role));
    }

    @Operation(summary = "Update role", description = "Update role information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", role));
    }

    @Operation(summary = "Delete role", description = "Delete role by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }
}
