package vn.edu.smd.core.module.teachingassignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
     */
    public Page<TeachingAssignmentResponse> getAllAssignments(Pageable pageable, List<String> statusList) {
        // Use findAllWithDetails() with EntityGraph to prevent N+1 query
        List<TeachingAssignment> allAssignments = teachingAssignmentRepository.findAllWithDetails();
        
        List<TeachingAssignment> assignments;
        if (statusList != null && !statusList.isEmpty()) {
            // Filter by status
            List<AssignmentStatus> statuses = statusList.stream()
                .map(s -> AssignmentStatus.valueOf(s.toUpperCase().replace("-", "_")))
                .collect(Collectors.toList());
            
            assignments = allAssignments.stream()
                .filter(a -> statuses.contains(a.getStatus()))
                .collect(Collectors.toList());
        } else {
            assignments = allAssignments;
        }
        
        List<TeachingAssignmentResponse> responses = assignments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        // Simple pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());
        
        if (start > responses.size()) {
            return new PageImpl<>(List.of(), pageable, responses.size());
        }
        
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
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
