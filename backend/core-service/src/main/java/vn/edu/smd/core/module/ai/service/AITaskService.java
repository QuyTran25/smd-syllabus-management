package vn.edu.smd.core.module.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import vn.edu.smd.core.config.RabbitMQConfig;
import vn.edu.smd.core.entity.CLO;
import vn.edu.smd.core.entity.AssessmentScheme;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.repository.CLORepository;
import vn.edu.smd.core.repository.AssessmentSchemeRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.shared.dto.ai.AIMessageRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI Task Service
 * Gửi messages vào RabbitMQ queue để AI Service xử lý
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AITaskService {
    
    private final RabbitTemplate rabbitTemplate;
    private final SyllabusVersionRepository syllabusVersionRepository;
    private final CLORepository cloRepository;
    private final AssessmentSchemeRepository assessmentSchemeRepository;
    
    // In-memory cache cho task status (TODO: Replace with Redis in production)
    private final Map<String, Map<String, Object>> taskStatusCache = new ConcurrentHashMap<>();
    
    // TODO: Add RedisTemplate for caching task status
    // private final RedisTemplate<String, Object> redisTemplate;
    
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
        
        // Cache initial task status
        Map<String, Object> initialStatus = new HashMap<>();
        initialStatus.put("taskId", messageId);
        initialStatus.put("action", "MAP_CLO_PLO");
        initialStatus.put("status", "QUEUED");
        initialStatus.put("progress", 0);
        initialStatus.put("message", "Task queued for processing");
        initialStatus.put("timestamp", System.currentTimeMillis());
        taskStatusCache.put(messageId, initialStatus);
        
        // TODO: Lưu task status vào Redis
        // redisTemplate.opsForValue().set(
        //     "task:" + messageId,
        //     Map.of("status", "QUEUED", "progress", 0),
        //     Duration.ofMinutes(30)
        // );
        
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
    public String requestCompareVersions(UUID oldVersionId, UUID newVersionId, 
                                         UUID subjectId, String userId) {
        String messageId = UUID.randomUUID().toString();
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("old_version_id", oldVersionId.toString());
        payload.put("new_version_id", newVersionId.toString());
        payload.put("subject_id", subjectId.toString());
        payload.put("comparison_depth", "DETAILED");
        
        AIMessageRequest message = AIMessageRequest.builder()
                .messageId(messageId)
                .action("COMPARE_VERSIONS")
                .priority("MEDIUM")
                .timestamp(Instant.now())
                .userId(userId)
                .payload(payload)
                .build();
        
        // Cache initial status
        Map<String, Object> initialStatus = new HashMap<>();
        initialStatus.put("taskId", messageId);
        initialStatus.put("action", "COMPARE_VERSIONS");
        initialStatus.put("status", "QUEUED");
        initialStatus.put("progress", 0);
        initialStatus.put("message", "Task queued for processing");
        initialStatus.put("timestamp", System.currentTimeMillis());
        taskStatusCache.put(messageId, initialStatus);
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_KEY_PROCESS,
                message,
                msg -> {
                    msg.getMessageProperties().setPriority(3); // MEDIUM priority
                    return msg;
                }
        );
        
        log.info("Sent COMPARE_VERSIONS request: messageId={}, oldVersion={}, newVersion={}", 
                 messageId, oldVersionId, newVersionId);
        
        return messageId;
    }
    
    // =============================================
    // 3. SUMMARIZE_SYLLABUS - Tóm tắt cho sinh viên
    // =============================================
    
    /**
     * Request AI tóm tắt đề cương
     */
    public String requestSummarize(UUID syllabusId, String userId) {
        String messageId = UUID.randomUUID().toString();
        
        // Query syllabus data from database
        SyllabusVersion syllabus = syllabusVersionRepository.findById(syllabusId)
                .orElseThrow(() -> new RuntimeException("Syllabus not found: " + syllabusId));
        
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
        
        // CLOs from database table (REAL DATA)
        List<Map<String, Object>> cloList = clos.stream().map(clo -> {
            Map<String, Object> cloMap = new HashMap<>();
            cloMap.put("code", clo.getCode());
            cloMap.put("description", clo.getDescription());
            cloMap.put("bloom_level", clo.getBloomLevel());
            cloMap.put("weight", clo.getWeight());
            return cloMap;
        }).collect(Collectors.toList());
        syllabusData.put("learning_outcomes", cloList);
        
        // Assessment from database table (REAL DATA)
        List<Map<String, Object>> assessmentList = assessments.stream().map(as -> {
            Map<String, Object> assessMap = new HashMap<>();
            assessMap.put("method", as.getName());  // Field is 'name' not 'assessmentType'
            assessMap.put("weight", as.getWeightPercent());  // Field is 'weightPercent' not 'weight'
            return assessMap;
        }).collect(Collectors.toList());
        syllabusData.put("assessment_scheme", assessmentList);
        
        if (content != null) {
            // Description & Objectives from JSONB
            syllabusData.put("description", content.get("description"));
            syllabusData.put("objectives", content.get("objectives"));
            
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
        payload.put("syllabus_data", syllabusData);  // NOW WITH FULL DATA!
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
        
        // Cache initial status
        Map<String, Object> initialStatus = new HashMap<>();
        initialStatus.put("taskId", messageId);
        initialStatus.put("action", "SUMMARIZE_SYLLABUS");
        initialStatus.put("status", "QUEUED");
        initialStatus.put("progress", 0);
        initialStatus.put("message", "Task queued for processing");
        initialStatus.put("timestamp", System.currentTimeMillis());
        taskStatusCache.put(messageId, initialStatus);
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_KEY_SUMMARIZE,
                message,
                msg -> {
                    msg.getMessageProperties().setPriority(1); // LOW priority
                    return msg;
                }
        );
        
        log.info("Sent SUMMARIZE_SYLLABUS request: messageId={}, syllabusId={}, course={}", 
                 messageId, syllabusId, syllabus.getSnapSubjectNameVi());
        
        return messageId;
    }
    
    // =============================================
    // POLLING - Get Task Status
    // =============================================
    
    /**
     * Lấy status của task để Frontend polling
     * 
     * @param taskId Message ID
     * @return Task status map
     */
    public Map<String, Object> getTaskStatus(String taskId) {
        // Check in-memory cache
        Map<String, Object> cachedStatus = taskStatusCache.get(taskId);
        
        if (cachedStatus != null) {
            log.debug("Task status found in cache: taskId={}, status={}", 
                     taskId, cachedStatus.get("status"));
            return cachedStatus;
        }
        
        // TODO: Query from Redis if not in cache
        // TODO: Query from DB (ai_service.syllabus_ai_analysis) if not in Redis
        
        // Task not found
        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", "NOT_FOUND");
        response.put("message", "Task not found. It may have expired or never existed.");
        
        log.warn("Task status not found: taskId={}", taskId);
        return response;
    }
    
    /**
     * Update task status (được gọi khi nhận response từ AI service)
     * TODO: Create listener consumer for ai_result_queue
     */
    public void updateTaskStatus(String taskId, String status, int progress, 
                                  Map<String, Object> result, String errorMessage) {
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("taskId", taskId);
        statusUpdate.put("status", status);
        statusUpdate.put("progress", progress);
        statusUpdate.put("timestamp", System.currentTimeMillis());
        
        if (result != null) {
            statusUpdate.put("result", result);
        }
        if (errorMessage != null) {
            statusUpdate.put("error", errorMessage);
        }
        
        taskStatusCache.put(taskId, statusUpdate);
        log.info("Updated task status: taskId={}, status={}, progress={}", 
                 taskId, status, progress);
        
        // TODO: Also update Redis cache
    }
}
