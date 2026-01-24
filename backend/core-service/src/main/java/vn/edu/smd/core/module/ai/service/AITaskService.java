package vn.edu.smd.core.module.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.config.RabbitMQConfig;
import vn.edu.smd.core.entity.CLO;
import vn.edu.smd.core.entity.AssessmentScheme;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.repository.CLORepository;
import vn.edu.smd.core.repository.AssessmentSchemeRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.shared.dto.ai.AIMessageRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import vn.edu.smd.core.dto.TaskStatusDTO;

/**
 * AI Task Service
 * Gửi messages vào RabbitMQ queue để AI Service xử lý
 * 
 * ✅ FIXED: Replaced ConcurrentHashMap with Redis for stateless architecture
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AITaskService {
    
    private final RabbitTemplate rabbitTemplate;
    private final SyllabusVersionRepository syllabusVersionRepository;
    private final CLORepository cloRepository;
    private final AssessmentSchemeRepository assessmentSchemeRepository;
    
    // ✅ Redis template for task status caching (stateless, scalable)
    private final RedisTemplate<String, TaskStatusDTO> taskStatusRedisTemplate;
    
    // =============================================
    // 1. MAP_CLO_PLO - Kiểm tra tuân thủ CLO-PLO
    // =============================================
    
    /**
     * Request AI kiểm tra ánh xạ CLO-PLO
     * 
     * @param syllabusId UUID của syllabus version
     * @param curriculumId UUID của curriculum
     * @param userId UUID của user request
     * @return taskId để polling
     */
    public String requestCloPloMapping(UUID syllabusId, UUID curriculumId, String userId) {
        String messageId = UUID.randomUUID().toString();
        
        // Prepare payload - handle null curriculumId
        Map<String, Object> payload = new HashMap<>();
        payload.put("syllabus_id", syllabusId.toString());
        payload.put("curriculum_id", curriculumId != null ? curriculumId.toString() : null);
        payload.put("strict_mode", true);
        payload.put("check_weights", true);
        
        // Build message
        AIMessageRequest message = AIMessageRequest.builder()
                .messageId(messageId)
                .action("MAP_CLO_PLO")
                .priority("HIGH")
                .timestamp(Instant.now())
                .userId(userId)
                .payload(payload)
                .build();
        
        // ✅ Cache initial task status in Redis (stateless)
        TaskStatusDTO initialStatus = TaskStatusDTO.builder()
                .taskId(messageId)
                .action("MAP_CLO_PLO")
                .status("QUEUED")
                .progress(0)
                .message("Task queued for processing")
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .priority("HIGH")
                .build();
        
        // Save to Redis with 30-minute TTL
        taskStatusRedisTemplate.opsForValue().set(
                "task:" + messageId,
                initialStatus,
                Duration.ofMinutes(30)
        );
        
        // Gửi vào queue với priority
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_KEY_PROCESS,
                message,
                msg -> {
                    msg.getMessageProperties().setPriority(5); // HIGH priority
                    return msg;
                }
        );
        
        log.info("[Sent] Message to AI Queue: Syllabus ID #{}", syllabusId);
        
        return messageId;
    }
    
    // =============================================
    // 2. COMPARE_VERSIONS - So sánh phiên bản
    // =============================================
    
    /**
     * Request AI so sánh 2 phiên bản đề cương
     */
    @Transactional(readOnly = true)
    public String requestCompareVersions(UUID oldVersionId, UUID newVersionId, 
                                         UUID subjectId, String userId) {
        String messageId = UUID.randomUUID().toString();
        
        // Query cả 2 versions từ database (bao gồm cả deleted versions để so sánh)
        SyllabusVersion oldVersion = syllabusVersionRepository.findById(oldVersionId)
                .orElseThrow(() -> new RuntimeException("Old version not found: " + oldVersionId));
        SyllabusVersion newVersion = syllabusVersionRepository.findById(newVersionId)
                .orElseThrow(() -> new RuntimeException("New version not found: " + newVersionId));
        
        // Build full content cho mỗi version
        Map<String, Object> oldVersionData = buildVersionData(oldVersion);
        Map<String, Object> newVersionData = buildVersionData(newVersion);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("old_version_id", oldVersionId.toString());
        payload.put("new_version_id", newVersionId.toString());
        payload.put("subject_id", subjectId.toString());
        payload.put("old_version", oldVersionData);
        payload.put("new_version", newVersionData);
        payload.put("comparison_depth", "DETAILED");
        
        AIMessageRequest message = AIMessageRequest.builder()
                .messageId(messageId)
                .action("COMPARE_VERSIONS")
                .priority("MEDIUM")
                .timestamp(Instant.now())
                .userId(userId)
                .payload(payload)
                .build();
        
        // ✅ Cache initial status in Redis
        TaskStatusDTO initialStatus = TaskStatusDTO.builder()
                .taskId(messageId)
                .action("COMPARE_VERSIONS")
                .status("QUEUED")
                .progress(0)
                .message("Task queued for processing")
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .priority("MEDIUM")
                .build();
        
        taskStatusRedisTemplate.opsForValue().set(
                "task:" + messageId,
                initialStatus,
                Duration.ofMinutes(30)
        );
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_KEY_PROCESS,
                message,
                msg -> {
                    msg.getMessageProperties().setPriority(3); // MEDIUM priority
                    return msg;
                }
        );
        
        log.info("Sent COMPARE_VERSIONS request: messageId={}, oldVersion={} (v{}), newVersion={} (v{})", 
                 messageId, oldVersionId, oldVersion.getVersionNo(), newVersionId, newVersion.getVersionNo());
        
        return messageId;
    }
    
    /**
     * Helper method để build version data
     */
    private Map<String, Object> buildVersionData(SyllabusVersion version) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> content = version.getContent() != null ? version.getContent() : new HashMap<>();
        
        data.put("version_no", version.getVersionNo());
        data.put("version_number", version.getVersionNumber());
        data.put("status", version.getStatus().toString());
        data.put("subject_code", version.getSnapSubjectCode());
        data.put("subject_name", version.getSnapSubjectNameVi());
        data.put("credit_count", version.getSnapCreditCount());
        data.put("description", version.getDescription());
        data.put("objectives", version.getObjectives());
        data.put("content", content); // Full JSONB content (clos, assessments, etc.)
        data.put("created_at", version.getCreatedAt());
        data.put("updated_at", version.getUpdatedAt());
        
        return data;
    }
    
    // =============================================
    // 3. SUMMARIZE_SYLLABUS - Tóm tắt cho sinh viên
    // =============================================
    
    /**
     * Request AI tóm tắt đề cương
     */
    public String requestSummarize(UUID syllabusId, String userId) {
        String messageId = UUID.randomUUID().toString();
        
        log.info("🔍 [SUMMARIZE] Searching for syllabusVersion with ID: {}", syllabusId);
        
        // Query syllabus data from database (chỉ lấy chưa xóa)
        SyllabusVersion syllabus = syllabusVersionRepository.findByIdAndNotDeleted(syllabusId)
                .orElseThrow(() -> new RuntimeException("Syllabus not found or deleted: " + syllabusId));
        
        // Query CLOs from database (separate table) - REAL DATA!
        List<CLO> clos = cloRepository.findBySyllabusVersionId(syllabusId);
        
        // Query Assessment Schemes from database (separate table) - REAL DATA!
        List<AssessmentScheme> assessments = assessmentSchemeRepository.findBySyllabusVersionId(syllabusId);
        
        // Build FULL syllabus_data payload
        Map<String, Object> content = syllabus.getContent();
        Map<String, Object> syllabusData = new HashMap<>();
        
        // Basic info
        syllabusData.put("course_name", syllabus.getSnapSubjectNameVi());
        syllabusData.put("course_code", content != null ? content.get("subject_code") : "");
        syllabusData.put("credit_count", syllabus.getSnapCreditCount());
        syllabusData.put("theory_hours", syllabus.getTheoryHours() != null ? syllabus.getTheoryHours() : 0);
        syllabusData.put("practice_hours", syllabus.getPracticeHours() != null ? syllabus.getPracticeHours() : 0);
        
        // CLOs from database table or JSONB content
        List<Map<String, Object>> cloList = new ArrayList<>();
        log.info("🔍 DEBUG - DB CLOs count: {}, content keys: {}", clos.size(), content != null ? content.keySet() : "null");
        if (!clos.isEmpty()) {
            // From database table (REAL DATA)
            log.info("✅ Using CLOs from database table");
            cloList = clos.stream().map(clo -> {
                Map<String, Object> cloMap = new HashMap<>();
                cloMap.put("code", clo.getCode());
                cloMap.put("description", clo.getDescription());
                cloMap.put("bloom_level", clo.getBloomLevel());
                cloMap.put("weight", clo.getWeight());
                return cloMap;
            }).collect(Collectors.toList());
        } else if (content != null && content.get("clos") != null) {
            // Fallback to JSONB content
            log.info("✅ Using CLOs from JSONB content");
            Object closFromJson = content.get("clos");
            log.info("🔍 DEBUG - clos type: {}, value: {}", closFromJson.getClass().getName(), closFromJson);
            if (closFromJson instanceof List) {
                cloList = (List<Map<String, Object>>) closFromJson;
                log.info("✅ Converted to list, size: {}", cloList.size());
            }
        } else {
            log.warn("⚠️ No CLOs found in DB or JSONB content");
        }
        syllabusData.put("learning_outcomes", cloList);
        
        // Assessment from database table or JSONB content
        List<Map<String, Object>> assessmentList = new ArrayList<>();
        if (!assessments.isEmpty()) {
            // From database table (REAL DATA)
            assessmentList = assessments.stream().map(as -> {
                Map<String, Object> assessMap = new HashMap<>();
                assessMap.put("method", as.getName());  // Field is 'name' not 'assessmentType'
                assessMap.put("weight", as.getWeightPercent());  // Field is 'weightPercent' not 'weight'
                return assessMap;
            }).collect(Collectors.toList());
        } else if (content != null && content.get("assessmentMethods") != null) {
            // Fallback to JSONB content
            Object assessFromJson = content.get("assessmentMethods");
            if (assessFromJson instanceof List) {
                assessmentList = (List<Map<String, Object>>) assessFromJson;
            }
        }
        syllabusData.put("assessment_scheme", assessmentList);
        
        if (content != null) {
            // Description & Objectives from JSONB
            syllabusData.put("description", content.get("description"));
            syllabusData.put("objectives", content.get("objectives"));
            
            // Teaching Methods (Phương pháp giảng dạy)
            syllabusData.put("teaching_method", content.get("teachingMethods"));
            
            // Weekly Content/Schedule
            syllabusData.put("weekly_content", content.get("courseOutline"));
            
            // Prerequisites
            syllabusData.put("prerequisites", content.get("prerequisites"));
            
            // References & Materials
            syllabusData.put("references", content.get("references"));
            syllabusData.put("textbooks", content.get("textbooks"));
            
            // Student Duties (Nhiệm vụ sinh viên)
            syllabusData.put("student_duties", content.get("studentDuties"));
        }
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("syllabus_id", syllabusId.toString());
        payload.put("syllabus_data", syllabusData);
        payload.put("language", "vi");
        payload.put("include_prerequisites", true);
        
        AIMessageRequest message = AIMessageRequest.builder()
                .messageId(messageId)
                .action("SUMMARIZE_SYLLABUS")
                .priority("LOW")
                .timestamp(Instant.now())
                .userId(userId)
                .payload(payload)
                .build();
        
        // Cache initial status in Redis
        TaskStatusDTO initialStatus = TaskStatusDTO.builder()
                .taskId(messageId)
                .action("SUMMARIZE_SYLLABUS")
                .status("QUEUED")
                .progress(0)
                .message("Task queued for processing")
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .priority("LOW")
                .build();
        
        taskStatusRedisTemplate.opsForValue().set(
                "task:" + messageId,
                initialStatus,
                Duration.ofMinutes(30)
        );
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_KEY_SUMMARIZE,
                message,
                msg -> {
                    msg.getMessageProperties().setPriority(1); // LOW priority
                    return msg;
                }
        );
        
        String courseName = syllabusData.getOrDefault("course_name", "Unknown").toString();
        log.info("Sent SUMMARIZE_SYLLABUS request: messageId={}, syllabusId={}, course={}", 
                 messageId, syllabusId, courseName);
        
        return messageId;
    }
    
    // =============================================
    // POLLING - Get Task Status
    // =============================================
    
    /**
     * Lấy status của task để Frontend polling
     * 
     * ✅ FIXED: Query from Redis instead of in-memory HashMap
     * 
     * @param taskId Message ID
     * @return Task status DTO
     */
    public TaskStatusDTO getTaskStatus(String taskId) {
        // Query from Redis
        TaskStatusDTO cachedStatus = taskStatusRedisTemplate.opsForValue()
                .get("task:" + taskId);
        
        if (cachedStatus != null) {
            log.debug("✅ Task status found in Redis: taskId={}, status={}", 
                     taskId, cachedStatus.getStatus());
            return cachedStatus;
        }
        
        // Task not found in Redis - may have expired or never existed
        log.warn("⚠️ Task status not found in Redis: taskId={}", taskId);
        
        // Return NOT_FOUND status
        return TaskStatusDTO.builder()
                .taskId(taskId)
                .status("NOT_FOUND")
                .progress(0)
                .message("Task not found. It may have expired (>30min) or never existed.")
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Update task status (được gọi khi nhận response từ AI service)
     * 
     * ✅ FIXED: Update Redis instead of in-memory HashMap
     * 
     * Called by AIResultListener when receiving results from ai_result_queue
     */
    public void updateTaskStatus(String taskId, String status, int progress, 
                                  Map<String, Object> result, String errorMessage) {
        
        // Build status update
        TaskStatusDTO.TaskStatusDTOBuilder statusBuilder = TaskStatusDTO.builder()
                .taskId(taskId)
                .status(status)
                .progress(progress)
                .timestamp(System.currentTimeMillis());
        
        if (result != null) {
            statusBuilder.result(result);
        }
        if (errorMessage != null) {
            statusBuilder.errorMessage(errorMessage);
        }
        
        TaskStatusDTO statusUpdate = statusBuilder.build();
        
        // Update Redis with extended TTL (2 hours for completed tasks)
        Duration ttl = "SUCCESS".equals(status) || "ERROR".equals(status) 
                ? Duration.ofHours(2)  // Keep completed tasks longer
                : Duration.ofMinutes(30); // In-progress tasks expire faster
        
        taskStatusRedisTemplate.opsForValue().set(
                "task:" + taskId,
                statusUpdate,
                ttl
        );
        
        log.info("✅ Updated task status in Redis: taskId={}, status={}, progress={}", 
                 taskId, status, progress);
    }
}
