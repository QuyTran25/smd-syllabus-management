package vn.edu.smd.core.module.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.common.dto.PageResponse;
import vn.edu.smd.core.module.user.dto.AssignRolesRequest;
import vn.edu.smd.core.module.user.dto.UpdateStatusRequest;
import vn.edu.smd.core.module.user.dto.UserRequest;
import vn.edu.smd.core.module.user.dto.UserResponse;
import vn.edu.smd.core.module.user.service.UserService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Tag(name = "User Management", description = "User management APIs")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Get list of users with pagination and filtering")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        // Truyền xuống Service nhưng service lúc này sẽ bỏ qua bộ lọc
        Page<UserResponse> users = userService.getAllUsers(role, isActive, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(users)));
    }

    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(summary = "Create user", description = "Create new user")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", user));
    }

    @Operation(summary = "Update user", description = "Update user information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @Operation(summary = "Delete user", description = "Delete user by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @Operation(summary = "Update user status", description = "Update user status (ACTIVE/INACTIVE)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        UserResponse user = userService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", user));
    }

    @Operation(summary = "Toggle user status", description = "Quickly lock/unlock user")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(@PathVariable UUID id) {
        UserResponse user = userService.toggleUserStatus(id);
        String msg = user.getStatus().equals("ACTIVE") ? "User unlocked" : "User locked";
        return ResponseEntity.ok(ApiResponse.success(msg, user));
    }

    @Operation(summary = "Import users", description = "Import users from CSV file")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> importUsers(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = userService.importUsers(file);
        return ResponseEntity.ok(ApiResponse.success("Import processed", result));
    }

    @Operation(summary = "Get user roles", description = "Get roles assigned to user")
    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<Set<String>>> getUserRoles(@PathVariable UUID id) {
        Set<String> roles = userService.getUserRoles(id);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @Operation(summary = "Assign roles to user", description = "Assign roles to user")
    @PostMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<Set<String>>> assignRoles(@PathVariable UUID id, @Valid @RequestBody AssignRolesRequest request) {
        Set<String> roles = userService.assignRoles(id, request);
        return ResponseEntity.ok(ApiResponse.success("Roles assigned successfully", roles));
    }

    @Operation(summary = "Remove role from user", description = "Remove specific role from user")
    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<ApiResponse<Void>> removeRole(@PathVariable UUID id, @PathVariable UUID roleId) {
        userService.removeRole(id, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", null));
    }
}