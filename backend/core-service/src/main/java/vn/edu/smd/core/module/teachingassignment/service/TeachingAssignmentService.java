package vn.edu.smd.core.module.teachingassignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Get all teaching assignments with pagination
     * Database handles filtering and pagination for better performance
     */
    public Page<TeachingAssignmentResponse> getAllAssignments(Pageable pageable, List<String> statusList) {
        Page<TeachingAssignment> assignmentPage;
        
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
        
        // Notification for main lecturer
        String mainTitle = String.format("[Nhiệm vụ mới] Biên soạn đề cương: %s - %s", 
                assignment.getSubject().getCode(), 
                assignment.getAcademicTerm().getName());
        
        String mainMessage = buildMainLecturerMessage(assignment, formatter);
        
        Notification mainNotification = Notification.builder()
                .user(mainLecturer)
                .title(mainTitle)
                .message(mainMessage)
                .type("ASSIGNMENT")
                .isRead(false)
                .relatedEntityType("TEACHING_ASSIGNMENT")
                .relatedEntityId(assignment.getId())
                .build();
        notificationRepository.save(mainNotification);
        
        // Notifications for collaborators
        if (collaboratorIds != null && !collaboratorIds.isEmpty()) {
            String collabTitle = String.format("[Cộng tác] Đóng góp ý kiến đề cương: %s - %s", 
                    assignment.getSubject().getCode(), 
                    assignment.getAcademicTerm().getName());
            
            String collabMessage = buildCollaboratorMessage(assignment, mainLecturer, formatter);
            
            for (UUID collaboratorId : collaboratorIds) {
                User collaborator = userRepository.findById(collaboratorId).orElse(null);
                if (collaborator != null) {
                    Notification collabNotification = Notification.builder()
                            .user(collaborator)
                            .title(collabTitle)
                            .message(collabMessage)
                            .type("ASSIGNMENT")
                            .isRead(false)
                            .relatedEntityType("TEACHING_ASSIGNMENT")
                            .relatedEntityId(assignment.getId())
                            .build();
                    notificationRepository.save(collabNotification);
                }
            }
        }
        
        log.info("Sent notifications for assignment {}", assignment.getId());
    }

    private String buildMainLecturerMessage(TeachingAssignment assignment, DateTimeFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chào bạn,\n\n");
        sb.append(String.format("Bạn được phân công biên soạn đề cương cho môn học: %s - %s (%s).\n\n",
                assignment.getSubject().getCode(),
                assignment.getSubject().getCurrentNameVi(),
                assignment.getAcademicTerm().getName()));
        sb.append("Thông tin chi tiết:\n");
        sb.append(String.format("• Số tín chỉ: %d\n", assignment.getSubject().getDefaultCredits()));
        sb.append(String.format("• Hạn hoàn thành: %s\n", assignment.getDeadline().format(formatter)));
        if (assignment.getComments() != null && !assignment.getComments().isEmpty()) {
            sb.append(String.format("• Ghi chú: %s\n", assignment.getComments()));
        }
        sb.append("\nVui lòng truy cập hệ thống để bắt đầu biên soạn đề cương.");
        return sb.toString();
    }

    private String buildCollaboratorMessage(TeachingAssignment assignment, User mainLecturer, 
                                           DateTimeFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chào bạn,\n\n");
        sb.append(String.format("Bạn được mời cộng tác đóng góp ý kiến cho đề cương môn học: %s - %s (%s).\n\n",
                assignment.getSubject().getCode(),
                assignment.getSubject().getCurrentNameVi(),
                assignment.getAcademicTerm().getName()));
        sb.append("Thông tin chi tiết:\n");
        sb.append(String.format("• Giảng viên chính: %s\n", mainLecturer.getFullName()));
        sb.append(String.format("• Hạn hoàn thành: %s\n", assignment.getDeadline().format(formatter)));
        sb.append("\nVui lòng theo dõi và đóng góp ý kiến trong quá trình biên soạn đề cương.");
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
