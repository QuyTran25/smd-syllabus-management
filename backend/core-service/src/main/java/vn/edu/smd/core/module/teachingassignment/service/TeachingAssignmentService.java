package vn.edu.smd.core.module.teachingassignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.repository.*;
import vn.edu.smd.core.module.teachingassignment.dto.TeachingAssignmentRequest;
import vn.edu.smd.core.module.teachingassignment.dto.TeachingAssignmentResponse;
import vn.edu.smd.core.security.UserPrincipal;
import vn.edu.smd.shared.enums.AssignmentStatus;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Teaching Assignment operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeachingAssignmentService {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingAssignmentCollaboratorRepository collaboratorRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicTermRepository academicTermRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final vn.edu.smd.core.service.FCMService fcmService;

    /**
     * Get all teaching assignments with pagination
     * Database handles filtering and pagination for better performance
     * Sorted by createdAt DESC (newest first)
     */
    public Page<TeachingAssignmentResponse> getAllAssignments(Pageable pageable, List<String> statusList) {
        Page<TeachingAssignment> assignmentPage;
        
        // Add default sort by createdAt descending if no sort provided
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("createdAt").descending()
            );
        }
        
        if (statusList != null && !statusList.isEmpty()) {
            // Convert status strings to enums for filtering using safe fromString method
            List<AssignmentStatus> statuses = statusList.stream()
                .map(AssignmentStatus::fromString)
                .collect(Collectors.toList());
            
            // Let database handle filtering and pagination
            assignmentPage = teachingAssignmentRepository.findByStatusIn(statuses, pageable);
        } else {
            // Get all with pagination
            assignmentPage = teachingAssignmentRepository.findAll(pageable);
        }
        
        // Map to response DTOs
        return assignmentPage.map(this::mapToResponse);
    }

    /**
     * Get teaching assignment by ID
     */
    public TeachingAssignmentResponse getAssignmentById(UUID id) {
        TeachingAssignment assignment = teachingAssignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Teaching assignment not found: " + id));
        return mapToResponse(assignment);
    }

    /**
     * Get assignments by main lecturer ID
     */
    public List<TeachingAssignmentResponse> getAssignmentsByLecturer(UUID lecturerId) {
        return teachingAssignmentRepository.findByMainLecturerId(lecturerId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Create new teaching assignment (for HOD)
     */
    @Transactional
    public TeachingAssignmentResponse createAssignment(TeachingAssignmentRequest request) {
        User currentUser = getCurrentUser();
        
        // Validate subject exists
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));
        
        // Validate academic term exists
        AcademicTerm academicTerm = academicTermRepository.findById(request.getAcademicTermId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "id", request.getAcademicTermId()));
        
        // Validate main lecturer exists and belongs to same faculty
        User mainLecturer = userRepository.findById(request.getMainLecturerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getMainLecturerId()));
        
        if (currentUser.getFaculty() == null) {
            throw new BadRequestException("You do not belong to any faculty");
        }
        
        if (mainLecturer.getFaculty() == null || 
            !mainLecturer.getFaculty().getId().equals(currentUser.getFaculty().getId())) {
            throw new BadRequestException("Main lecturer must belong to your faculty");
        }
        
        // Check if assignment already exists for this subject and term
        Optional<TeachingAssignment> existing = teachingAssignmentRepository
                .findBySubjectIdAndAcademicTermId(request.getSubjectId(), request.getAcademicTermId());
        if (existing.isPresent()) {
            throw new BadRequestException("Assignment already exists for this subject and academic term");
        }
        
        // Create teaching assignment
        TeachingAssignment assignment = TeachingAssignment.builder()
                .subject(subject)
                .academicTerm(academicTerm)
                .mainLecturer(mainLecturer)
                .deadline(request.getDeadline())
                .status(AssignmentStatus.PENDING)
                .assignedBy(currentUser)
                .comments(request.getComments())
                .build();
        
        TeachingAssignment savedAssignment = teachingAssignmentRepository.save(assignment);
        
        // Add collaborators if provided
        if (request.getCollaboratorIds() != null && !request.getCollaboratorIds().isEmpty()) {
            for (UUID collaboratorId : request.getCollaboratorIds()) {
                User collaborator = userRepository.findById(collaboratorId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", collaboratorId));
                
                if (collaborator.getFaculty() == null || 
                    !collaborator.getFaculty().getId().equals(currentUser.getFaculty().getId())) {
                    throw new BadRequestException("All collaborators must belong to your faculty");
                }
                
                TeachingAssignmentCollaborator collab = TeachingAssignmentCollaborator.builder()
                        .assignment(savedAssignment)
                        .lecturer(collaborator)
                        .build();
                collaboratorRepository.save(collab);
            }
        }
        
        // Send notifications to main lecturer and collaborators
        sendNotificationToLecturers(savedAssignment, mainLecturer, request.getCollaboratorIds());
        
        log.info("Created teaching assignment {} by HOD {}", savedAssignment.getId(), currentUser.getEmail());
        
        return mapToResponse(savedAssignment);
    }

    /**
     * Get subjects for current HOD (subjects belonging to HOD's department)
     */
    public List<Map<String, Object>> getSubjectsForHod() {
        User currentUser = getCurrentUser();
        
        if (currentUser.getDepartment() == null) {
            throw new BadRequestException("User does not belong to any department");
        }
        
        List<Subject> subjects = subjectRepository.findByDepartmentId(currentUser.getDepartment().getId());
        
        return subjects.stream()
                .filter(Subject::getIsActive)
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.getId().toString());
                    map.put("code", s.getCode());
                    map.put("nameVi", s.getCurrentNameVi());
                    map.put("nameEn", s.getCurrentNameEn());
                    map.put("credits", s.getDefaultCredits());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get lecturers in HOD's faculty (not just department)
     */
    public List<Map<String, Object>> getLecturersForHod() {
        User currentUser = getCurrentUser();
        
        // Get faculty from department
        if (currentUser.getDepartment() == null || currentUser.getDepartment().getFaculty() == null) {
            throw new BadRequestException("User does not belong to any faculty");
        }
        
        UUID facultyId = currentUser.getDepartment().getFaculty().getId();
        
        // Get all users in the same faculty
        List<User> lecturers = userRepository.findByFacultyId(facultyId);
        
        // Filter lecturers by checking userRoles for LECTURER or HEAD_OF_DEPARTMENT
        return lecturers.stream()
                .filter(u -> u.getUserRoles() != null && !u.getUserRoles().isEmpty())
                .filter(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole() != null && 
                                (ur.getRole().getCode().equals("LECTURER") || 
                                 ur.getRole().getCode().equals("HEAD_OF_DEPARTMENT"))))
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId().toString());
                    map.put("fullName", u.getFullName());
                    map.put("email", u.getEmail());
                    map.put("phone", u.getPhone());
                    // Add department info to help distinguish
                    if (u.getDepartment() != null) {
                        map.put("departmentName", u.getDepartment().getName());
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Send notifications to main lecturer and collaborators
     */
    private void sendNotificationToLecturers(TeachingAssignment assignment, User mainLecturer, 
                                             List<UUID> collaboratorIds) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        User assignedBy = assignment.getAssignedBy();
        String hodName = assignedBy != null ? assignedBy.getFullName() : "Trưởng bộ môn";
        
        // Notification for main lecturer (Giảng viên chính)
        String mainTitle = String.format("[Phân công biên soạn] Đề cương môn học: %s - %s", 
                assignment.getSubject().getCode(), 
                assignment.getSubject().getCurrentNameVi());
        
        String mainMessage = buildMainLecturerMessage(assignment, hodName, formatter);
        
        // Create payload for action URL
        Map<String, Object> mainPayload = new HashMap<>();
        mainPayload.put("assignmentId", assignment.getId().toString());
        mainPayload.put("subjectCode", assignment.getSubject().getCode());
        mainPayload.put("teachingAssignmentId", assignment.getId().toString()); // Để frontend biết assignment ID
        mainPayload.put("actionUrl", "/lecturer/syllabi/create?assignmentId=" + assignment.getId()); // URL để GV soạn đề cương
        mainPayload.put("actionLabel", "Soạn đề cương ngay");
        
        Notification mainNotification = Notification.builder()
                .user(mainLecturer)
                .title(mainTitle)
                .message(mainMessage)
                .type("ASSIGNMENT")
                .payload(mainPayload)
                .isRead(false)
                .relatedEntityType("TEACHING_ASSIGNMENT")
                .relatedEntityId(assignment.getId())
                .build();
        Notification savedMain = notificationRepository.save(mainNotification);
        
        // 🔔 Send FCM push notification to main lecturer
        try {
            String pushBody = mainMessage.length() > 100 
                ? mainMessage.substring(0, 100) + "..." 
                : mainMessage;
            
            Map<String, String> fcmData = new HashMap<>();
            fcmData.put("notificationId", savedMain.getId().toString());
            fcmData.put("type", savedMain.getType());
            fcmData.put("actionUrl", mainPayload.get("actionUrl").toString());
            fcmData.put("assignmentId", assignment.getId().toString());
            fcmData.put("subjectCode", assignment.getSubject().getCode());
            fcmData.put("hodName", hodName);
            
            fcmService.sendNotificationToUser(mainLecturer, mainTitle, pushBody, fcmData);
        } catch (Exception fcmError) {
            log.warn("Failed to send FCM to main lecturer {}: {}", 
                     mainLecturer.getId(), fcmError.getMessage());
        }
        
        log.info("Sent notification to main lecturer: {} ({})", mainLecturer.getFullName(), mainLecturer.getEmail());
        
        // Notifications for collaborators (Giảng viên cộng tác)
        if (collaboratorIds != null && !collaboratorIds.isEmpty()) {
            String collabTitle = String.format("[Mời cộng tác] Tham gia biên soạn đề cương: %s - %s", 
                    assignment.getSubject().getCode(), 
                    assignment.getSubject().getCurrentNameVi());
            
            String collabMessage = buildCollaboratorMessage(assignment, mainLecturer, formatter);
            
            for (UUID collaboratorId : collaboratorIds) {
                User collaborator = userRepository.findById(collaboratorId).orElse(null);
                if (collaborator != null) {
                    // Create payload for collaborator
                    Map<String, Object> collabPayload = new HashMap<>();
                    collabPayload.put("assignmentId", assignment.getId().toString());
                    collabPayload.put("subjectCode", assignment.getSubject().getCode());
                    collabPayload.put("mainLecturerId", mainLecturer.getId().toString());
                    // Collaborator should view their dashboard to see syllabus when it's created
                    collabPayload.put("actionUrl", "/lecturer");
                    collabPayload.put("actionLabel", "Xem nhiệm vụ");
                    
                    Notification collabNotification = Notification.builder()
                            .user(collaborator)
                            .title(collabTitle)
                            .message(collabMessage)
                            .type("ASSIGNMENT")
                            .payload(collabPayload)
                            .isRead(false)
                            .relatedEntityType("TEACHING_ASSIGNMENT")
                            .relatedEntityId(assignment.getId())
                            .build();
                    Notification savedCollab = notificationRepository.save(collabNotification);
                    
                    // 🔔 Send FCM push notification to collaborator
                    try {
                        String pushBody = collabMessage.length() > 100 
                            ? collabMessage.substring(0, 100) + "..." 
                            : collabMessage;
                        
                        Map<String, String> fcmData = new HashMap<>();
                        fcmData.put("notificationId", savedCollab.getId().toString());
                        fcmData.put("type", savedCollab.getType());
                        fcmData.put("actionUrl", collabPayload.get("actionUrl").toString());
                        fcmData.put("assignmentId", assignment.getId().toString());
                        fcmData.put("subjectCode", assignment.getSubject().getCode());
                        fcmData.put("mainLecturerId", mainLecturer.getId().toString());
                        fcmData.put("mainLecturerName", mainLecturer.getFullName());
                        
                        fcmService.sendNotificationToUser(collaborator, collabTitle, pushBody, fcmData);
                    } catch (Exception fcmError) {
                        log.warn("Failed to send FCM to collaborator {}: {}", 
                                 collaborator.getId(), fcmError.getMessage());
                    }
                    
                    log.info("Sent notification to collaborator: {} ({})", collaborator.getFullName(), collaborator.getEmail());
                }
            }
        }
        
        log.info("Successfully sent all notifications for assignment {}", assignment.getId());
    }

    private String buildMainLecturerMessage(TeachingAssignment assignment, String hodName, DateTimeFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Chào Giảng viên %s,\n\n", assignment.getMainLecturer().getFullName()));
        sb.append(String.format("Theo phân công của Trưởng bộ môn %s, bạn được chỉ định là Giảng viên chịu trách nhiệm chính biên soạn đề cương cho:\n\n", hodName));
        sb.append(String.format("Môn học: %s - %s\n\n", 
                assignment.getSubject().getCode(),
                assignment.getSubject().getCurrentNameVi()));
        sb.append(String.format("Học kỳ: %s\n\n", assignment.getAcademicTerm().getName()));
        sb.append(String.format("Hạn hoàn thành: %s\n\n", assignment.getDeadline().format(formatter)));
        
        // Thêm thông tin về số tín chỉ
        sb.append("Thông tin môn học:\n");
        sb.append(String.format("• Số tín chỉ: %d\n", assignment.getSubject().getDefaultCredits()));
        sb.append(String.format("• Lý thuyết: %d giờ\n", assignment.getSubject().getDefaultTheoryHours()));
        sb.append(String.format("• Thực hành: %d giờ\n\n", assignment.getSubject().getDefaultPracticeHours()));
        
        if (assignment.getComments() != null && !assignment.getComments().isEmpty()) {
            sb.append(String.format("Ghi chú từ Trưởng bộ môn: %s\n\n", assignment.getComments()));
        }
        sb.append("Vui lòng phối hợp với các giảng viên cộng tác (nếu có) để hoàn thiện và gửi duyệt trước thời hạn.");
        return sb.toString();
    }

    private String buildCollaboratorMessage(TeachingAssignment assignment, User mainLecturer, 
                                           DateTimeFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Chào Giảng viên,\n\n"));
        sb.append(String.format("Bạn đã được thêm vào nhóm biên soạn đề cương môn %s - %s với vai trò Giảng viên cộng tác.\n\n",
                assignment.getSubject().getCode(),
                assignment.getSubject().getCurrentNameVi()));
        sb.append(String.format("Giảng viên chịu trách nhiệm chính: %s\n\n", mainLecturer.getFullName()));
        sb.append(String.format("Học kỳ: %s\n", assignment.getAcademicTerm().getName()));
        sb.append(String.format("Hạn hoàn thành: %s\n\n", assignment.getDeadline().format(formatter)));
        sb.append("Vui lòng truy cập hệ thống để đóng góp nội dung và rà soát chuyên môn theo phân công.");
        return sb.toString();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }

    /**
     * Map entity to response DTO
     */
    private TeachingAssignmentResponse mapToResponse(TeachingAssignment assignment) {
        // Get collaborators
        List<TeachingAssignmentCollaborator> collaborators = 
            collaboratorRepository.findByAssignmentId(assignment.getId());
        
        List<TeachingAssignmentResponse.CollaboratorInfo> coLecturers = collaborators.stream()
            .map(c -> TeachingAssignmentResponse.CollaboratorInfo.builder()
                .id(c.getLecturer().getId().toString())
                .name(c.getLecturer().getFullName())
                .email(c.getLecturer().getEmail())
                .build())
            .collect(Collectors.toList());

        return TeachingAssignmentResponse.builder()
            .id(assignment.getId().toString())
            // Subject info
            .subjectId(assignment.getSubject().getId().toString())
            .subjectCode(assignment.getSubject().getCode())
            .subjectNameVi(assignment.getSubject().getCurrentNameVi())
            .subjectNameEn(assignment.getSubject().getCurrentNameEn())
            .credits(assignment.getSubject().getDefaultCredits())
            // Academic term info
            .academicTermId(assignment.getAcademicTerm().getId().toString())
            .semester(assignment.getAcademicTerm().getName())
            // Main lecturer info
            .mainLecturerId(assignment.getMainLecturer().getId().toString())
            .mainLecturerName(assignment.getMainLecturer().getFullName())
            .mainLecturerEmail(assignment.getMainLecturer().getEmail())
            // Co-lecturers
            .coLecturers(coLecturers)
            .coLecturerCount(coLecturers.size())
            // Assignment details
            .deadline(assignment.getDeadline())
            .status(assignment.getStatus())
            .syllabusId(assignment.getSyllabusVersion() != null 
                ? assignment.getSyllabusVersion().getId().toString() 
                : null)
            .assignedById(assignment.getAssignedBy().getId().toString())
            .assignedByName(assignment.getAssignedBy().getFullName())
            .comments(assignment.getComments())
            .createdAt(assignment.getCreatedAt())
            .updatedAt(assignment.getUpdatedAt())
            .build();
    }
}
