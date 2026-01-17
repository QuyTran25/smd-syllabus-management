package vn.edu.smd.core.module.revision.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.module.revision.dto.*;
import vn.edu.smd.core.repository.*;
import vn.edu.smd.core.service.RevisionNotificationService;
import vn.edu.smd.shared.enums.FeedbackStatus;
import vn.edu.smd.shared.enums.RevisionSessionStatus;
import vn.edu.smd.shared.enums.SyllabusStatus;
import vn.edu.smd.shared.enums.UserRole;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Revision Service
 * Handles post-publication revision workflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevisionService {

    private final RevisionSessionRepository revisionSessionRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;
    private final SyllabusVersionHistoryRepository historyRepository;
    private final SyllabusErrorReportRepository feedbackRepository;
    private final UserRepository userRepository;
    private final RevisionNotificationService notificationService;

    /**
     * Admin starts a revision session
     */
    @Transactional
    public RevisionSessionResponse startRevisionSession(StartRevisionRequest request, UUID adminId) {
        log.info("Admin {} starting revision session for syllabus {}", adminId, request.getSyllabusVersionId());
        
        // Validate syllabus
        SyllabusVersion syllabus = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", request.getSyllabusVersionId()));
        
        if (syllabus.getStatus() != SyllabusStatus.PUBLISHED) {
            throw new BadRequestException("Only published syllabi can be revised");
        }
        
        // Check if already has active session
        if (revisionSessionRepository.hasActiveSession(syllabus.getId())) {
            throw new BadRequestException("This syllabus already has an active revision session");
        }
        
        // Validate feedbacks
        List<SyllabusErrorReport> feedbacks = feedbackRepository.findAllById(request.getFeedbackIds());
        if (feedbacks.size() != request.getFeedbackIds().size()) {
            throw new BadRequestException("Some feedback IDs are invalid");
        }
        
        // Get admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));
        
        // Get lecturer (from syllabus)
        User lecturer = syllabus.getCreatedBy();
        if (lecturer == null) {
            throw new BadRequestException("Syllabus has no assigned lecturer");
        }
        
        // Create snapshot in history before starting revision
        createHistorySnapshot(syllabus, admin, "BEFORE_REVISION");
        
        // Create revision session
        long sessionCount = revisionSessionRepository.countBySyllabusVersionId(syllabus.getId());
        RevisionSession sessionEntity = RevisionSession.builder()
                .syllabusVersion(syllabus)
                .sessionNumber((int) (sessionCount + 1))
                .status(RevisionSessionStatus.OPEN)
                .initiatedBy(admin)
                .initiatedAt(LocalDateTime.now())
                .description(request.getDescription())
                .assignedLecturer(lecturer)
                .startedAt(LocalDateTime.now())
                .build();
        
        final RevisionSession session = revisionSessionRepository.save(sessionEntity);
        
        // Update feedbacks status and link to session
        for (SyllabusErrorReport feedback : feedbacks) {
            feedback.setRevisionSession(session);
            feedback.setStatus(FeedbackStatus.AWAITING_REVISION.name());
            feedback.setEditEnabled(true);
        }
        feedbackRepository.saveAll(feedbacks);
        
        // Update syllabus status to REVISION_IN_PROGRESS
        syllabus.setStatus(SyllabusStatus.REVISION_IN_PROGRESS);
        syllabusVersionRepository.save(syllabus);
        
        // Send notification to lecturer
        notificationService.notifyLecturerRevisionRequested(session, lecturer, feedbacks);
        
        // Send notification to HoD (from lecturer's department)
        if (lecturer.getDepartment() != null) {
            userRepository.findHodByDepartmentId(lecturer.getDepartment().getId())
                .ifPresent(hod -> {
                    notificationService.notifyHodRevisionRequested(session, hod, lecturer, feedbacks.size());
                    log.info("Sent revision request notification to HoD: {}", hod.getFullName());
                });
        } else {
            log.warn("Lecturer {} has no department, cannot notify HoD", lecturer.getId());
        }
        
        log.info("Revision session {} started successfully with {} feedbacks", session.getId(), feedbacks.size());
        
        return mapToResponse(session, feedbacks);
    }

    /**
     * Lecturer submits revision to HOD
     */
    @Transactional
    public RevisionSessionResponse submitRevision(SubmitRevisionRequest request, UUID lecturerId) {
        log.info("Lecturer {} submitting revision session {}", lecturerId, request.getRevisionSessionId());
        
        RevisionSession session = revisionSessionRepository.findById(request.getRevisionSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("RevisionSession", "id", request.getRevisionSessionId()));
        
        // Validate lecturer
        if (!session.getAssignedLecturer().getId().equals(lecturerId)) {
            throw new BadRequestException("You are not assigned to this revision session");
        }
        
        if (session.getStatus() != RevisionSessionStatus.OPEN && session.getStatus() != RevisionSessionStatus.IN_PROGRESS) {
            throw new BadRequestException("This revision session cannot be submitted");
        }
        
        // Update session status
        session.setStatus(RevisionSessionStatus.PENDING_HOD);
        session = revisionSessionRepository.save(session);
        
        // Update syllabus status
        SyllabusVersion syllabus = session.getSyllabusVersion();
        syllabus.setStatus(SyllabusStatus.PENDING_HOD_REVISION);
        syllabusVersionRepository.save(syllabus);
        
        // Update feedbacks status
        List<SyllabusErrorReport> feedbacks = feedbackRepository.findByRevisionSessionId(session.getId());
        for (SyllabusErrorReport feedback : feedbacks) {
            feedback.setStatus(FeedbackStatus.IN_REVISION.name());
        }
        feedbackRepository.saveAll(feedbacks);
        
        // Find HOD and send notification
        User hod = findHodForSyllabus(syllabus);
        if (hod != null) {
            notificationService.notifyHodRevisionSubmitted(session, hod, session.getAssignedLecturer(), feedbacks.size());
        }
        
        log.info("Revision session {} submitted to HOD successfully", session.getId());
        
        return mapToResponse(session, feedbacks);
    }

    /**
     * HOD reviews revision (approve/reject)
     */
    @Transactional
    public RevisionSessionResponse reviewRevision(ReviewRevisionRequest request, UUID hodId) {
        log.info("HOD {} reviewing revision session {}: {}", hodId, request.getRevisionSessionId(), request.getDecision());
        
        RevisionSession session = revisionSessionRepository.findById(request.getRevisionSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("RevisionSession", "id", request.getRevisionSessionId()));
        
        if (session.getStatus() != RevisionSessionStatus.PENDING_HOD) {
            throw new BadRequestException("This revision session is not pending HOD review");
        }
        
        User hod = userRepository.findById(hodId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", hodId));
        
        SyllabusVersion syllabus = session.getSyllabusVersion();
        List<SyllabusErrorReport> feedbacks = feedbackRepository.findByRevisionSessionId(session.getId());
        
        // Update session with HOD decision
        session.setHodReviewedBy(hod);
        session.setHodReviewedAt(LocalDateTime.now());
        session.setHodDecision(request.getDecision());
        session.setHodComment(request.getComment());
        
        if ("APPROVED".equalsIgnoreCase(request.getDecision())) {
            // HOD approved
            session.setStatus(RevisionSessionStatus.COMPLETED);
            syllabus.setStatus(SyllabusStatus.PENDING_ADMIN_REPUBLISH);
            
            // Find admin and notify
            // TODO: Implement proper admin lookup
            List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getCode().equals("ADMIN")))
                .collect(Collectors.toList());
            for (User admin : admins) {
                notificationService.notifyAdminRevisionApproved(session, admin, hod, feedbacks.size());
            }
            
            log.info("Revision session {} approved by HOD", session.getId());
            
        } else {
            // HOD rejected
            session.setStatus(RevisionSessionStatus.IN_PROGRESS);
            syllabus.setStatus(SyllabusStatus.REVISION_IN_PROGRESS);
            
            // Notify lecturer
            notificationService.notifyLecturerRevisionRejected(
                    session, 
                    session.getAssignedLecturer(), 
                    hod, 
                    request.getComment()
            );
            
            log.info("Revision session {} rejected by HOD", session.getId());
        }
        
        revisionSessionRepository.save(session);
        syllabusVersionRepository.save(syllabus);
        
        return mapToResponse(session, feedbacks);
    }

    /**
     * Admin republishes syllabus after revision approved
     */
    @Transactional
    public RevisionSessionResponse republishSyllabus(UUID revisionSessionId, UUID adminId) {
        log.info("Admin {} republishing syllabus for revision session {}", adminId, revisionSessionId);
        
        RevisionSession session = revisionSessionRepository.findById(revisionSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("RevisionSession", "id", revisionSessionId));
        
        if (session.getStatus() != RevisionSessionStatus.COMPLETED) {
            throw new BadRequestException("This revision session is not ready for republishing");
        }
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));
        
        SyllabusVersion syllabus = session.getSyllabusVersion();
        
        if (syllabus.getStatus() != SyllabusStatus.PENDING_ADMIN_REPUBLISH) {
            throw new BadRequestException("Syllabus is not in PENDING_ADMIN_REPUBLISH status");
        }
        
        // Create history snapshot of current version
        createHistorySnapshot(syllabus, admin, "BEFORE_REPUBLISH");
        
        // Update syllabus back to PUBLISHED
        syllabus.setStatus(SyllabusStatus.PUBLISHED);
        Integer currentVersion = syllabus.getVersionNumber() != null ? syllabus.getVersionNumber() : 1;
        syllabus.setVersionNumber(currentVersion + 1);
        syllabus.setVersionNo("V" + (currentVersion + 1) + ".0");
        syllabusVersionRepository.save(syllabus);
        
        // Mark session as completed
        session.setRepublishedBy(admin);
        session.setRepublishedAt(LocalDateTime.now());
        session.setCompletedAt(LocalDateTime.now());
        revisionSessionRepository.save(session);
        
        // Mark all feedbacks as RESOLVED
        List<SyllabusErrorReport> feedbacks = feedbackRepository.findByRevisionSessionId(session.getId());
        for (SyllabusErrorReport feedback : feedbacks) {
            feedback.setStatus(FeedbackStatus.RESOLVED.name());
            feedback.setResolvedInVersion(syllabus);
            feedback.setResolvedInVersionNo(syllabus.getVersionNo());
            feedback.setResolvedBy(admin);
            feedback.setResolvedAt(LocalDateTime.now());
        }
        feedbackRepository.saveAll(feedbacks);
        
        // Get students who provided feedback
        List<User> studentsToNotify = feedbacks.stream()
                .map(SyllabusErrorReport::getUser)
                .distinct()
                .collect(Collectors.toList());
        
        // Notify students
        String changesSummary = buildChangesSummary(feedbacks);
        notificationService.notifyStudentsSyllabusUpdated(syllabus, studentsToNotify, changesSummary, feedbacks.size());
        
        log.info("Syllabus {} republished successfully as version {}", syllabus.getId(), syllabus.getVersionNo());
        
        return mapToResponse(session, feedbacks);
    }

    /**
     * Get pending revision sessions for HOD
     */
    @Transactional(readOnly = true)
    public List<RevisionSessionResponse> getPendingHodReview() {
        List<RevisionSession> sessions = revisionSessionRepository.findPendingHodReview();
        return sessions.stream()
                .map(session -> {
                    List<SyllabusErrorReport> feedbacks = feedbackRepository.findByRevisionSessionId(session.getId());
                    return mapToResponse(session, feedbacks);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get pending republish sessions for Admin
     */
    @Transactional(readOnly = true)
    public List<RevisionSessionResponse> getPendingRepublish() {
        List<RevisionSession> sessions = revisionSessionRepository.findByStatusOrderByInitiatedAtDesc(
                RevisionSessionStatus.COMPLETED
        );
        return sessions.stream()
                .filter(s -> s.getRepublishedAt() == null)
                .map(session -> {
                    List<SyllabusErrorReport> feedbacks = feedbackRepository.findByRevisionSessionId(session.getId());
                    return mapToResponse(session, feedbacks);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get active revision session for a syllabus version
     * Returns null if no active session found (this is a valid case)
     */
    @Transactional(readOnly = true)
    public RevisionSessionResponse getActiveRevisionSession(UUID syllabusVersionId) {
        log.info("Getting active revision session for syllabus {}", syllabusVersionId);
        
        Optional<RevisionSession> sessionOpt = revisionSessionRepository.findActiveSessionBySyllabusVersionId(syllabusVersionId);
        
        if (sessionOpt.isEmpty()) {
            log.info("No active revision session found for syllabus {}", syllabusVersionId);
            return null;
        }
        
        RevisionSession session = sessionOpt.get();
        List<SyllabusErrorReport> feedbacks = feedbackRepository.findByRevisionSessionId(session.getId());
        
        log.info("Found active revision session {} with {} feedbacks", session.getId(), feedbacks.size());
        
        return mapToResponse(session, feedbacks);
    }

    /**
     * Get completed revision session for a syllabus version (for republishing)
     */
    @Transactional(readOnly = true)
    public RevisionSessionResponse getCompletedRevisionSession(UUID syllabusVersionId) {
        log.info("Getting completed revision session for syllabus {}", syllabusVersionId);
        
        // Find the most recent completed session that hasn't been republished yet
        List<RevisionSession> completedSessions = revisionSessionRepository
                .findBySyllabusVersionIdAndStatusOrderByInitiatedAtDesc(
                        syllabusVersionId, 
                        RevisionSessionStatus.COMPLETED
                );
        
        if (completedSessions.isEmpty()) {
            log.info("No completed revision session found for syllabus {}", syllabusVersionId);
            return null;
        }
        
        // Get the most recent one that hasn't been republished
        RevisionSession session = completedSessions.stream()
                .filter(s -> s.getRepublishedAt() == null)
                .findFirst()
                .orElse(null);
        
        if (session == null) {
            log.info("All completed sessions have been republished for syllabus {}", syllabusVersionId);
            return null;
        }
        
        List<SyllabusErrorReport> feedbacks = feedbackRepository.findByRevisionSessionId(session.getId());
        
        log.info("Found completed revision session {} with {} feedbacks", session.getId(), feedbacks.size());
        
        return mapToResponse(session, feedbacks);
    }

    // Helper methods
    
    private void createHistorySnapshot(SyllabusVersion syllabus, User user, String reason) {
        SyllabusVersionHistory history = SyllabusVersionHistory.builder()
                .syllabusVersion(syllabus)
                .versionNumber(syllabus.getVersionNumber())
                .versionNo(syllabus.getVersionNo())
                .status(syllabus.getStatus())
                .snapSubjectCode(syllabus.getSnapSubjectCode())
                .snapSubjectNameVi(syllabus.getSnapSubjectNameVi())
                .snapSubjectNameEn(syllabus.getSnapSubjectNameEn())
                .snapCreditCount(syllabus.getSnapCreditCount())
                .courseType(syllabus.getCourseType())
                .componentType(syllabus.getComponentType())
                .theoryHours(syllabus.getTheoryHours())
                .practiceHours(syllabus.getPracticeHours())
                .selfStudyHours(syllabus.getSelfStudyHours())
                .description(syllabus.getDescription())
                .objectives(syllabus.getObjectives())
                .studentTasks(syllabus.getStudentTasks())
                .studentDuties(syllabus.getStudentDuties())
                .createdBy(user)
                .snapshotReason(reason)
                .build();
        
        historyRepository.save(history);
        log.info("Created history snapshot for syllabus {} with reason: {}", syllabus.getId(), reason);
    }
    
    private User findHodForSyllabus(SyllabusVersion syllabus) {
        // Try to find HOD from syllabus creator's department
        User lecturer = syllabus.getCreatedBy();
        if (lecturer != null && lecturer.getDepartment() != null) {
            Optional<User> hodOpt = userRepository.findHodByDepartmentId(lecturer.getDepartment().getId());
            if (hodOpt.isPresent()) {
                log.info("Found HOD {} for department {}", hodOpt.get().getFullName(), lecturer.getDepartment().getName());
                return hodOpt.get();
            } else {
                log.warn("No HOD found for department {}", lecturer.getDepartment().getName());
            }
        } else {
            log.warn("Syllabus creator has no department, cannot find specific HOD");
        }
        
        // Fallback: find any HOD
        List<User> hods = userRepository.findAll().stream()
            .filter(u -> u.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getCode().equals("HOD")))
            .findFirst()
            .map(List::of)
            .orElse(List.of());
        
        if (hods.isEmpty()) {
            log.error("No HOD found in the system!");
            return null;
        }
        
        log.info("Using fallback HOD: {}", hods.get(0).getFullName());
        return hods.get(0);
    }
    
    private String buildChangesSummary(List<SyllabusErrorReport> feedbacks) {
        Map<String, Long> typeCount = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getType() != null ? f.getType().getDisplayName() : "Khác",
                        Collectors.counting()
                ));
        
        return typeCount.entrySet().stream()
                .map(e -> e.getValue() + " " + e.getKey())
                .collect(Collectors.joining(", "));
    }
    
    private RevisionSessionResponse mapToResponse(RevisionSession session, List<SyllabusErrorReport> feedbacks) {
        return RevisionSessionResponse.builder()
                .id(session.getId())
                .syllabusVersionId(session.getSyllabusVersion().getId())
                .syllabusCode(session.getSyllabusVersion().getSnapSubjectCode())
                .syllabusName(session.getSyllabusVersion().getSnapSubjectNameVi())
                .sessionNumber(session.getSessionNumber())
                .status(session.getStatus().name())
                .initiatedById(session.getInitiatedBy().getId())
                .initiatedByName(session.getInitiatedBy().getFullName())
                .initiatedAt(session.getInitiatedAt())
                .description(session.getDescription())
                .assignedLecturerId(session.getAssignedLecturer() != null ? session.getAssignedLecturer().getId() : null)
                .assignedLecturerName(session.getAssignedLecturer() != null ? session.getAssignedLecturer().getFullName() : null)
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .hodReviewedById(session.getHodReviewedBy() != null ? session.getHodReviewedBy().getId() : null)
                .hodReviewedByName(session.getHodReviewedBy() != null ? session.getHodReviewedBy().getFullName() : null)
                .hodReviewedAt(session.getHodReviewedAt())
                .hodDecision(session.getHodDecision())
                .hodComment(session.getHodComment())
                .republishedById(session.getRepublishedBy() != null ? session.getRepublishedBy().getId() : null)
                .republishedByName(session.getRepublishedBy() != null ? session.getRepublishedBy().getFullName() : null)
                .republishedAt(session.getRepublishedAt())
                .feedbackCount(feedbacks.size())
                .feedbackIds(feedbacks.stream().map(SyllabusErrorReport::getId).collect(Collectors.toList()))
                .feedbacks(feedbacks.stream()
                        .map(fb -> RevisionSessionResponse.FeedbackSimpleDto.builder()
                                .id(fb.getId())
                                .type(fb.getType() != null ? fb.getType().name() : "ERROR")
                                .section(fb.getSection() != null ? fb.getSection().name() : "OTHER")
                                .title(fb.getTitle())
                                .description(fb.getDescription())
                                .status(fb.getStatus())
                                .studentName(fb.getUser() != null ? fb.getUser().getFullName() : "Unknown")
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
