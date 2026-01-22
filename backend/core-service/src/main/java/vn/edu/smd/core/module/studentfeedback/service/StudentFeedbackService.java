package vn.edu.smd.core.module.studentfeedback.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Notification;
import vn.edu.smd.core.entity.SyllabusErrorReport;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.revision.dto.StartRevisionRequest;
import vn.edu.smd.core.module.revision.service.RevisionService;
import vn.edu.smd.core.module.studentfeedback.dto.AdminResponseRequest;
import vn.edu.smd.core.module.studentfeedback.dto.StudentFeedbackRequest;
import vn.edu.smd.core.module.studentfeedback.dto.StudentFeedbackResponse;
import vn.edu.smd.core.repository.NotificationRepository;
import vn.edu.smd.core.repository.SyllabusErrorReportRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.shared.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentFeedbackService {

    private final SyllabusErrorReportRepository feedbackRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final RevisionService revisionService;
    private final vn.edu.smd.core.service.FCMService fcmService;

    @Transactional(readOnly = true)
    public Page<StudentFeedbackResponse> getAllFeedbacks(Pageable pageable) {
        Page<SyllabusErrorReport> feedbacks = feedbackRepository.findAll(pageable);
        return feedbacks.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<StudentFeedbackResponse> getFeedbacksByStatus(String status, Pageable pageable) {
        Page<SyllabusErrorReport> feedbacks = feedbackRepository.findByStatus(status, pageable);
        return feedbacks.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public StudentFeedbackResponse getFeedbackById(UUID id) {
        SyllabusErrorReport feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentFeedback", "id", id));
        return mapToResponse(feedback);
    }

    @Transactional(readOnly = true)
    public List<StudentFeedbackResponse> getFeedbacksBySyllabus(UUID syllabusId) {
        return feedbackRepository.findBySyllabusVersionId(syllabusId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentFeedbackResponse createFeedback(StudentFeedbackRequest request, UUID studentId) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(request.getSyllabusId())
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", request.getSyllabusId()));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        SyllabusErrorReport feedback = SyllabusErrorReport.builder()
                .syllabusVersion(syllabus)
                .user(student)
                .type(request.getType())
                .section(request.getSection())
                .title(request.getTitle())
                .description(request.getDescription())
                .status("PENDING")
                .build();
        
        feedback = feedbackRepository.save(feedback);
        return mapToResponse(feedback);
    }

    @Transactional
    public StudentFeedbackResponse respondToFeedback(UUID id, AdminResponseRequest request, UUID adminId) {
        log.info("Admin {} responding to feedback {}", adminId, id);
        
        SyllabusErrorReport feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentFeedback", "id", id));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));
        
        feedback.setAdminResponse(request.getResponse());
        feedback.setRespondedBy(admin);
        feedback.setRespondedAt(LocalDateTime.now());
        
        if (Boolean.TRUE.equals(request.getEnableEdit())) {
            log.info("Admin enabled edit for feedback {}", id);
            
            // Just mark for revision, actual session will be started separately
            feedback.setEditEnabled(true);
            feedback.setStatus("AWAITING_REVISION");
        } else {
            // If not enabling edit, mark as REJECTED (no fix needed)
            feedback.setStatus("REJECTED");
        }
        
        feedback = feedbackRepository.save(feedback);
        
        // Send notification to student
        sendResponseNotificationToStudent(feedback, admin);
        
        // If enableEdit is true, start revision session in a separate transaction
        if (Boolean.TRUE.equals(request.getEnableEdit())) {
            try {
                startRevisionSessionForFeedback(id, feedback.getSyllabusVersion().getId(), adminId);
            } catch (Exception e) {
                log.error("Failed to start revision session for feedback {}: {}", id, e.getMessage());
                // Continue anyway, feedback is already saved
            }
        }
        
        return mapToResponse(feedback);
    }
    
    /**
     * Start revision session for a single feedback (separate transaction to avoid circular dependency)
     */
    private void startRevisionSessionForFeedback(UUID feedbackId, UUID syllabusId, UUID adminId) {
        try {
            StartRevisionRequest revisionRequest = new StartRevisionRequest();
            revisionRequest.setSyllabusVersionId(syllabusId);
            revisionRequest.setFeedbackIds(Collections.singletonList(feedbackId));
            revisionRequest.setDescription("Chỉnh sửa đề cương dựa trên phản hồi sinh viên");
            
            revisionService.startRevisionSession(revisionRequest, adminId);
            log.info("Revision session started successfully for feedback {}", feedbackId);
        } catch (Exception e) {
            log.error("Error starting revision session: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public StudentFeedbackResponse enableEditForLecturer(UUID id, UUID adminId) {
        SyllabusErrorReport feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentFeedback", "id", id));
        
        feedback.setEditEnabled(true);
        
        feedback = feedbackRepository.save(feedback);
        return mapToResponse(feedback);
    }

    @Transactional
    public StudentFeedbackResponse updateStatus(UUID id, String status) {
        SyllabusErrorReport feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentFeedback", "id", id));
        
        feedback.setStatus(status);
        
        if ("RESOLVED".equals(status)) {
            feedback.setResolvedAt(LocalDateTime.now());
        }
        
        feedback = feedbackRepository.save(feedback);
        return mapToResponse(feedback);
    }

    private StudentFeedbackResponse mapToResponse(SyllabusErrorReport feedback) {
        StudentFeedbackResponse response = new StudentFeedbackResponse();
        response.setId(feedback.getId());
        
        // Syllabus info - safe access
        try {
            if (feedback.getSyllabusVersion() != null) {
                response.setSyllabusId(feedback.getSyllabusVersion().getId());
                response.setSyllabusCode(feedback.getSyllabusVersion().getSnapSubjectCode());
                response.setSyllabusName(feedback.getSyllabusVersion().getSnapSubjectNameVi());
                
                // Lecturer info
                if (feedback.getSyllabusVersion().getCreatedBy() != null) {
                    User lecturer = feedback.getSyllabusVersion().getCreatedBy();
                    response.setLecturerId(lecturer.getId());
                    response.setLecturerName(lecturer.getFullName());
                    response.setLecturerEmail(lecturer.getEmail());
                }
            }
        } catch (Exception e) {
            // Handle lazy loading exception
        }
        
        // Student info - safe access
        try {
            if (feedback.getUser() != null) {
                response.setStudentId(feedback.getUser().getId());
                response.setStudentName(feedback.getUser().getFullName());
                response.setStudentEmail(feedback.getUser().getEmail());
            }
        } catch (Exception e) {
            // Handle lazy loading exception
        }
        
        // Feedback details
        response.setType(feedback.getType());
        response.setSection(feedback.getSection());
        response.setSectionDisplay(feedback.getSection() != null ? feedback.getSection().getDisplayName() : null);
        response.setTitle(feedback.getTitle());
        response.setDescription(feedback.getDescription());
        response.setStatus(feedback.getStatus());
        
        // Admin response
        response.setAdminResponse(feedback.getAdminResponse());
        try {
            if (feedback.getRespondedBy() != null) {
                response.setRespondedById(feedback.getRespondedBy().getId());
                response.setRespondedByName(feedback.getRespondedBy().getFullName());
            }
        } catch (Exception e) {
            // Handle lazy loading exception
        }
        response.setRespondedAt(feedback.getRespondedAt());
        
        // Edit enabled
        response.setEditEnabled(feedback.getEditEnabled());
        
        // Resolution
        try {
            if (feedback.getResolvedBy() != null) {
                response.setResolvedById(feedback.getResolvedBy().getId());
                response.setResolvedByName(feedback.getResolvedBy().getFullName());
            }
        } catch (Exception e) {
            // Handle lazy loading exception
        }
        response.setResolvedAt(feedback.getResolvedAt());
        
        // Timestamps
        response.setCreatedAt(feedback.getCreatedAt());
        response.setUpdatedAt(feedback.getUpdatedAt());
        
        return response;
    }
    
    /**
     * Send notification to student when admin responds to their feedback
     */
    private void sendResponseNotificationToStudent(SyllabusErrorReport feedback, User admin) {
        User student = feedback.getUser();
        SyllabusVersion syllabus = feedback.getSyllabusVersion();
        
        String title = String.format("[Phản hồi] Báo lỗi của bạn về đề cương %s",
                syllabus.getSnapSubjectCode());
        
        // Include admin response in message
        String message = String.format(
                "Admin %s đã phản hồi báo lỗi '%s' của bạn:\n\n📝 %s",
                admin.getFullName(),
                feedback.getTitle(),
                feedback.getAdminResponse());
        
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("feedbackId", feedback.getId().toString());
        payload.put("syllabusId", syllabus.getId().toString());
        payload.put("syllabusCode", syllabus.getSnapSubjectCode());
        payload.put("adminResponse", feedback.getAdminResponse());
        payload.put("status", feedback.getStatus());
        payload.put("actionUrl", "/syllabi");
        payload.put("actionLabel", "Xem đề cương");
        payload.put("priority", "MEDIUM");
        
        Notification notification = Notification.builder()
                .user(student)
                .title(title)
                .message(message)
                .type(NotificationType.COMMENT.name())
                .payload(payload)
                .isRead(false)
                .relatedEntityType("FEEDBACK")
                .relatedEntityId(feedback.getId())
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * Notify all admins when a student reports an issue with a syllabus
     * Called from StudentSyllabusServiceImpl.reportIssue()
     */
    public void notifyAdminsStudentReportedIssue(SyllabusErrorReport report) {
        // Find all users with ADMIN role
        List<User> admins = userRepository.findByRoleName("ADMIN");
        
        if (admins.isEmpty()) {
            log.warn("No admins found to notify about student error report {}", report.getId());
            return;
        }
        
        User student = report.getUser();
        SyllabusVersion syllabus = report.getSyllabusVersion();
        
        String title = String.format("[Báo lỗi mới] Sinh viên báo lỗi đề cương %s", 
                syllabus.getSnapSubjectCode());
        
        String message = String.format(
                "Sinh viên %s đã báo lỗi:\n📍 Phần: %s\n📝 %s",
                student.getFullName(),
                report.getSection() != null ? report.getSection().getDisplayName() : "Khác",
                report.getDescription());
        
        // Prepare payload with FCM data
        java.util.Map<String, String> fcmData = new java.util.HashMap<>();
        fcmData.put("notificationId", "");  // Will be set per notification
        fcmData.put("type", "STUDENT_FEEDBACK");
        fcmData.put("feedbackId", report.getId().toString());
        fcmData.put("studentId", student.getId().toString());
        fcmData.put("studentName", student.getFullName());
        fcmData.put("syllabusId", syllabus.getId().toString());
        fcmData.put("syllabusCode", syllabus.getSnapSubjectCode());
        fcmData.put("section", report.getSection() != null ? report.getSection().name() : "OTHER");
        fcmData.put("actionUrl", "/admin/student-feedback");
        fcmData.put("actionLabel", "Xem báo lỗi");
        
        // Create notification payload for database
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("feedbackId", report.getId().toString());
        payload.put("studentId", student.getId().toString());
        payload.put("studentName", student.getFullName());
        payload.put("syllabusId", syllabus.getId().toString());
        payload.put("syllabusCode", syllabus.getSnapSubjectCode());
        payload.put("section", report.getSection() != null ? report.getSection().name() : "OTHER");
        payload.put("description", report.getDescription());
        payload.put("actionUrl", "/admin/student-feedback");
        payload.put("actionLabel", "Xem báo lỗi");
        payload.put("priority", "HIGH");
        
        // Send notification to each admin
        for (User admin : admins) {
            try {
                // Save to database
                Notification notification = Notification.builder()
                        .user(admin)
                        .title(title)
                        .message(message)
                        .type(NotificationType.ERROR_REPORT.name())
                        .payload(payload)
                        .isRead(false)
                        .relatedEntityType("FEEDBACK")
                        .relatedEntityId(report.getId())
                        .build();
                
                notification = notificationRepository.save(notification);
                
                // Send push notification via FCM
                fcmData.put("notificationId", notification.getId().toString());
                
                // Truncate message for push notification (max 100 chars)
                String pushBody = message.length() > 100 ? message.substring(0, 97) + "..." : message;
                
                fcmService.sendNotificationToUser(admin, title, pushBody, fcmData);
                
                log.info("✅ Notified admin {} about student error report {}", 
                        admin.getEmail(), report.getId());
            } catch (Exception e) {
                log.error("❌ Failed to notify admin {} about error report {}: {}", 
                        admin.getEmail(), report.getId(), e.getMessage());
            }
        }
    }
}
