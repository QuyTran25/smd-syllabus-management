package vn.edu.smd.core.module.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.module.admin.dto.PublishSyllabusRequest;
import vn.edu.smd.core.module.admin.service.AdminSyllabusService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/syllabi")
@RequiredArgsConstructor
public class AdminSyllabusController {

    private final AdminSyllabusService adminSyllabusService;

    @PatchMapping("/{id}/publish") 
    public ResponseEntity<?> publishSyllabus(
            @PathVariable UUID id,
            @RequestBody PublishSyllabusRequest request) {
        
        adminSyllabusService.publishSyllabus(id, request.getComment());
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã xuất hành đề cương thành công"));
    }

    // 2. Gỡ bỏ đề cương
    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublishSyllabus(
            @PathVariable UUID id,
            @RequestBody PublishSyllabusRequest request) { // ✅ Đã sửa dùng DTO
        
        adminSyllabusService.unpublishSyllabus(id, request.getReason());
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã gỡ bỏ đề cương"));
    }

    // 3. Cập nhật ngày hiệu lực
    @PatchMapping("/{id}/effective-date")
    public ResponseEntity<?> updateEffectiveDate(
            @PathVariable UUID id,
            @RequestBody PublishSyllabusRequest request) { // ✅ Đã sửa dùng DTO
        
        adminSyllabusService.updateEffectiveDate(id, request.getEffectiveDate());
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã cập nhật ngày hiệu lực"));
    }
}