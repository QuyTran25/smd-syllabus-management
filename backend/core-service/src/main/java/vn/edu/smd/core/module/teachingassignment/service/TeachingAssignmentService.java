package vn.edu.smd.core.module.teachingassignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.entity.TeachingAssignment;
import vn.edu.smd.core.entity.TeachingAssignmentCollaborator;
import vn.edu.smd.core.repository.TeachingAssignmentRepository;
import vn.edu.smd.core.repository.TeachingAssignmentCollaboratorRepository;
import vn.edu.smd.core.module.teachingassignment.dto.TeachingAssignmentResponse;
import vn.edu.smd.shared.enums.AssignmentStatus;

import java.util.List;
import java.util.UUID;
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
