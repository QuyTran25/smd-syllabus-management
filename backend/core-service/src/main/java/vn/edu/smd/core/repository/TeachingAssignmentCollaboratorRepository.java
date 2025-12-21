package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.TeachingAssignmentCollaborator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TeachingAssignmentCollaborator entity
 */
@Repository
public interface TeachingAssignmentCollaboratorRepository extends JpaRepository<TeachingAssignmentCollaborator, UUID> {

    /**
     * Find all collaborators by assignment ID
     */
    List<TeachingAssignmentCollaborator> findByAssignmentId(UUID assignmentId);

    /**
     * Find all assignments where user is collaborator
     */
    List<TeachingAssignmentCollaborator> findByLecturerId(UUID lecturerId);

    /**
     * Find specific collaborator
     */
    Optional<TeachingAssignmentCollaborator> findByAssignmentIdAndLecturerId(UUID assignmentId, UUID lecturerId);

    /**
     * Check if collaborator exists
     */
    boolean existsByAssignmentIdAndLecturerId(UUID assignmentId, UUID lecturerId);

    /**
     * Delete by assignment ID
     */
    void deleteByAssignmentId(UUID assignmentId);

    /**
     * Delete specific collaborator
     */
    void deleteByAssignmentIdAndLecturerId(UUID assignmentId, UUID lecturerId);

    /**
     * Count collaborators for assignment
     */
    long countByAssignmentId(UUID assignmentId);
}
