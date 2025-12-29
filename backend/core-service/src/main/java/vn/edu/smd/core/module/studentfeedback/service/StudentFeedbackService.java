package vn.edu.smd.core.module.studentfeedback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.SyllabusErrorReport;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.studentfeedback.dto.AdminResponseRequest;
import vn.edu.smd.core.module.studentfeedback.dto.StudentFeedbackRequest;
import vn.edu.smd.core.module.studentfeedback.dto.StudentFeedbackResponse;
import vn.edu.smd.core.repository.SyllabusErrorReportRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.core.repository.UserRepository;

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

    @Transactional(readOnly = true)
    public Page<StudentFeedbackResponse> getAllFeedbacks(Pageable pageable) {
        Page<SyllabusErrorReport> feedbacks = feedbackRepository.findAll(pageable);
        return feedbacks.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<StudentFeedbackResponse> getFeedbacksByStatus(String status, Pageable pageable) {
        List<SyllabusErrorReport> allFeedbacks = feedbackRepository.findByStatus(status);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allFeedbacks.size());
        
        List<StudentFeedbackResponse> pageContent = allFeedbacks.subList(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pageContent, pageable, allFeedbacks.size());
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
        feedback.setStatus("IN_REVIEW");
        
        if (Boolean.TRUE.equals(request.getEnableEdit())) {
            feedback.setEditEnabled(true);
            // Note: editEnabledBy and editEnabledAt might need separate fields
        }
        
        feedback = feedbackRepository.save(feedback);
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
        
        // Syllabus info
        if (feedback.getSyllabusVersion() != null) {
            response.setSyllabusId(feedback.getSyllabusVersion().getId());
            response.setSyllabusCode(feedback.getSyllabusVersion().getSnapSubjectCode());
            response.setSyllabusName(feedback.getSyllabusVersion().getSnapSubjectNameVi());
        }
        
        // Student info
        if (feedback.getUser() != null) {
            response.setStudentId(feedback.getUser().getId());
            response.setStudentName(feedback.getUser().getFullName());
            response.setStudentEmail(feedback.getUser().getEmail());
        }
        
        // Feedback details
        response.setType(feedback.getType());
        response.setSection(feedback.getSection());
        response.setTitle(feedback.getTitle());
        response.setDescription(feedback.getDescription());
        response.setStatus(feedback.getStatus());
        
        // Admin response
        response.setAdminResponse(feedback.getAdminResponse());
        if (feedback.getRespondedBy() != null) {
            response.setRespondedById(feedback.getRespondedBy().getId());
            response.setRespondedByName(feedback.getRespondedBy().getFullName());
        }
        response.setRespondedAt(feedback.getRespondedAt());
        
        // Edit enabled
        response.setEditEnabled(feedback.getEditEnabled());
        
        // Resolution
        if (feedback.getResolvedBy() != null) {
            response.setResolvedById(feedback.getResolvedBy().getId());
            response.setResolvedByName(feedback.getResolvedBy().getFullName());
        }
        response.setResolvedAt(feedback.getResolvedAt());
        
        // Timestamps
        response.setCreatedAt(feedback.getCreatedAt());
        response.setUpdatedAt(feedback.getUpdatedAt());
        
        return response;
    }
}
