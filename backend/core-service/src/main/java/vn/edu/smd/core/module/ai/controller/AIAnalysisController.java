package vn.edu.smd.core.module.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.module.ai.service.AITaskService;
import vn.edu.smd.core.security.UserPrincipal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI Analysis Controller
 * REST endpoints cho các chức năng AI
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisController {
    
    private final AITaskService aiTaskService;
    
    // =============================================
    // 1. MAP_CLO_PLO - Kiểm tra tuân thủ CLO-PLO
    // =============================================
    
    /**
     * POST /api/ai/syllabus/{id}/check-clo-plo?curriculumId={curriculumId}
     * 
     * @param id Syllabus version ID
     * @param curriculumId Curriculum ID
     * @param user Authenticated user
     * @return Task ID cho polling
     */
    @PostMapping("/syllabus/{id}/check-clo-plo")
    public ResponseEntity<Map<String, Object>> checkCloPlo(
            @PathVariable UUID id,
            @RequestParam UUID curriculumId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        String userId = user != null ? user.getId().toString() : "anonymous";
        
        log.info("Received CLO-PLO check request: syllabusId={}, curriculumId={}, userId={}", 
                 id, curriculumId, userId);
        
        String taskId = aiTaskService.requestCloPloMapping(id, curriculumId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("task_id", taskId);
        response.put("status", "QUEUED");
        response.put("message", "Yêu cầu kiểm tra CLO-PLO đã được gửi");
        response.put("estimated_time_seconds", 7);
        response.put("poll_url", "/api/ai/tasks/" + taskId + "/status");
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    // =============================================
    // 2. COMPARE_VERSIONS - So sánh phiên bản
    // =============================================
    
    /**
     * POST /api/ai/syllabus/compare?oldVersionId={oldVersionId}&newVersionId={newVersionId}
     * 
     * @param oldVersionId Old version ID
     * @param newVersionId New version ID
     * @param subjectId Subject ID
     * @param user Authenticated user
     * @return Task ID cho polling
     */
    @PostMapping("/syllabus/compare")
    public ResponseEntity<Map<String, Object>> compareVersions(
            @RequestParam UUID oldVersionId,
            @RequestParam UUID newVersionId,
            @RequestParam UUID subjectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        String userId = user != null ? user.getId().toString() : "anonymous";
        
        log.info("Received compare versions request: old={}, new={}, subject={}, userId={}", 
                 oldVersionId, newVersionId, subjectId, userId);
        
        String taskId = aiTaskService.requestCompareVersions(
                oldVersionId, newVersionId, subjectId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("task_id", taskId);
        response.put("status", "QUEUED");
        response.put("message", "Yêu cầu so sánh phiên bản đã được gửi");
        response.put("estimated_time_seconds", 10);
        response.put("poll_url", "/api/ai/tasks/" + taskId + "/status");
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    // =============================================
    // 3. SUMMARIZE_SYLLABUS - Tóm tắt cho sinh viên
    // =============================================
    
    /**
     * POST /api/ai/syllabus/{id}/summarize
     * 
     * @param id Syllabus version ID
     * @param user Authenticated user (optional cho student)
     * @return Task ID cho polling
     */
    @PostMapping("/syllabus/{id}/summarize")
    public ResponseEntity<Map<String, Object>> summarizeSyllabus(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {
        
        String userId = user != null ? user.getId().toString() : "anonymous";
        
        log.info("Received summarize request: syllabusId={}, userId={}", id, userId);
        
        String taskId = aiTaskService.requestSummarize(id, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("task_id", taskId);
        response.put("status", "QUEUED");
        response.put("message", "Yêu cầu tóm tắt đề cương đã được gửi");
        response.put("estimated_time_seconds", 5);
        response.put("poll_url", "/api/ai/tasks/" + taskId + "/status");
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    // =============================================
    // POLLING ENDPOINT - Get Task Status
    // =============================================
    
    /**
     * GET /api/ai/tasks/{taskId}/status
     * Frontend sẽ poll endpoint này để lấy kết quả
     * 
     * @param taskId Task ID (message ID)
     * @return Task status và result nếu có
     */
    @GetMapping("/tasks/{taskId}/status")
    public ResponseEntity<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        
        log.debug("Polling task status: taskId={}", taskId);
        
        Map<String, Object> status = aiTaskService.getTaskStatus(taskId);
        
        return ResponseEntity.ok(status);
    }
}
