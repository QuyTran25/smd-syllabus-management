package vn.edu.smd.core.module.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.entity.SyllabusErrorReport;
import vn.edu.smd.core.module.admin.dto.AdminResolveIssueDto;
import vn.edu.smd.core.module.admin.service.AdminFeedbackService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;

    // 1. API Lấy danh sách lỗi chờ xử lý
    // GET /api/admin/feedback/pending
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingIssues() {
        List<SyllabusErrorReport> issues = adminFeedbackService.getPendingIssues();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", issues
        ));
    }

    // 2. API Xử lý lỗi (Duyệt/Từ chối)
    // POST /api/admin/feedback/resolve
    @PostMapping("/resolve")
    public ResponseEntity<?> resolveIssue(@RequestBody AdminResolveIssueDto dto) {
        adminFeedbackService.resolveIssue(dto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã xử lý phản hồi thành công"
        ));
    }
}