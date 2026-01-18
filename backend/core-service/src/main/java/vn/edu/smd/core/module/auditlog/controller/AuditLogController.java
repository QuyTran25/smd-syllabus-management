package vn.edu.smd.core.module.auditlog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // 🔥 Import Sort
import org.springframework.data.web.SortDefault; // 🔥 Import SortDefault
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.common.dto.PageResponse;
import vn.edu.smd.core.module.auditlog.dto.AuditLogResponse;
import vn.edu.smd.core.module.auditlog.service.AuditLogService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Audit Log Management", description = "Audit log management APIs")
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Get all audit logs", description = "Get list of audit logs with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAllAuditLogs(
            // 🔥 FIX: Thêm SortDefault để mặc định lấy bản ghi mới nhất (giảm dần theo thời gian)
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        Page<AuditLogResponse> auditLogs = auditLogService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(auditLogs)));
    }

    @Operation(summary = "Get audit log by ID", description = "Get audit log details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(@PathVariable UUID id) {
        AuditLogResponse auditLog = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success(auditLog));
    }

    @Operation(summary = "Get audit logs by entity", description = "Get audit logs for a specific entity")
    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByEntity(
            @PathVariable String entityName, 
            @PathVariable UUID entityId) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByEntity(entityName, entityId);
        return ResponseEntity.ok(ApiResponse.success(auditLogs));
    }

    @Operation(summary = "Get audit logs by actor", description = "Get audit logs for a specific user")
    @GetMapping("/actor/{actorId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByActor(@PathVariable UUID actorId) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByActor(actorId);
        return ResponseEntity.ok(ApiResponse.success(auditLogs));
    }

    @Operation(summary = "Get audit logs of user", description = "Get audit logs for a specific user with pagination")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogsByUser(
            @PathVariable UUID userId, 
            Pageable pageable) {
        Page<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(auditLogs)));
    }

    @Operation(summary = "Search audit logs", description = "Search audit logs with filters (entityName, action, startDate, endDate)")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> searchAuditLogs(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable) {
        Page<AuditLogResponse> auditLogs = auditLogService.searchAuditLogs(entityName, action, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(auditLogs)));
    }
}