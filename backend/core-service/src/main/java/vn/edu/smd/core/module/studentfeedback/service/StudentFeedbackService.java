package vn.edu.smd.core.module.studentfeedback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Notification;
import vn.edu.smd.core.entity.SyllabusErrorReport;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.studentfeedback.dto.AdminResponseRequest;
import vn.edu.smd.core.module.studentfeedback.dto.StudentFeedbackRequest;
import vn.edu.smd.core.module.studentfeedback.dto.StudentFeedbackResponse;
import vn.edu.smd.core.repository.NotificationRepository;
import vn.edu.smd.core.repository.SyllabusErrorReportRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.shared.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentFeedbackService {

    private final SyllabusErrorReportRepository feedbackRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

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
        SyllabusErrorReport feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentFeedback", "id", id));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));
        
        feedback.setAdminResponse(request.getResponse());
        feedback.setRespondedBy(admin);
        feedback.setRespondedAt(LocalDateTime.now());
        
        if (Boolean.TRUE.equals(request.getEnableEdit())) {
            feedback.setEditEnabled(true);
            feedback.setStatus("AWAITING_REVISION");
        } else {
            // If not enabling edit, mark as REJECTED (no fix needed)
            feedback.setStatus("REJECTED");
        }
        
        feedback = feedbackRepository.save(feedback);
        
        // Send notification to student
        sendResponseNotificationToStudent(feedback, admin);
        
        return mapToResponse(feedback);
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
}
