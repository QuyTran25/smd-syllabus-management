package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.TeachingAssignment;
import vn.edu.smd.shared.enums.AssignmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TeachingAssignment entity
 */
@Repository
public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, UUID> {

    /**
     * Find assignment by subject and academic term
     */
    Optional<TeachingAssignment> findBySubjectIdAndAcademicTermId(UUID subjectId, UUID academicTermId);

    /**
     * Find all assignments by main lecturer ID
     */
    List<TeachingAssignment> findByMainLecturerId(UUID mainLecturerId);

    /**
     * Find all assignments by academic term ID
     */
    List<TeachingAssignment> findByAcademicTermId(UUID academicTermId);

    /**
     * Find all assignments by status
     */
    List<TeachingAssignment> findByStatus(AssignmentStatus status);

    /**
     * Find all assignments by main lecturer and status
     */
    List<TeachingAssignment> findByMainLecturerIdAndStatus(UUID mainLecturerId, AssignmentStatus status);

    /**
     * Find assignments with deadline before date
     */
    List<TeachingAssignment> findByDeadlineBefore(LocalDate deadline);

    /**
     * Find pending assignments with deadline before date
     */
    List<TeachingAssignment> findByStatusAndDeadlineBefore(AssignmentStatus status, LocalDate deadline);

    /**
     * Check if assignment exists for subject and term
     */
    boolean existsBySubjectIdAndAcademicTermId(UUID subjectId, UUID academicTermId);

    /**
     * Count assignments by lecturer
     */
    long countByMainLecturerId(UUID mainLecturerId);

    /**
     * Count assignments by status
     */
    long countByStatus(AssignmentStatus status);
}
