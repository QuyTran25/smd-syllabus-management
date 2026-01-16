package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.RevisionSession;
import vn.edu.smd.shared.enums.RevisionSessionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RevisionSession entity
 */
@Repository
public interface RevisionSessionRepository extends JpaRepository<RevisionSession, UUID> {

    /**
     * Find all sessions for a syllabus version
     */
    List<RevisionSession> findBySyllabusVersionIdOrderBySessionNumberDesc(UUID syllabusVersionId);

    /**
     * Find active (non-closed) session for a syllabus version
     */
    @Query("SELECT rs FROM RevisionSession rs WHERE rs.syllabusVersion.id = :syllabusVersionId " +
           "AND rs.status IN ('OPEN', 'IN_PROGRESS', 'PENDING_HOD') " +
           "ORDER BY rs.sessionNumber DESC")
    Optional<RevisionSession> findActiveSessionBySyllabusVersionId(@Param("syllabusVersionId") UUID syllabusVersionId);

    /**
     * Find sessions by status
     */
    List<RevisionSession> findByStatusOrderByInitiatedAtDesc(RevisionSessionStatus status);

    /**
     * Find all pending HOD review sessions
     */
    @Query("SELECT rs FROM RevisionSession rs WHERE rs.status = 'PENDING_HOD' ORDER BY rs.updatedAt ASC")
    List<RevisionSession> findPendingHodReview();

    /**
     * Count sessions for a syllabus version
     */
    long countBySyllabusVersionId(UUID syllabusVersionId);

    /**
     * Check if syllabus has active revision session
     */
    @Query("SELECT CASE WHEN COUNT(rs) > 0 THEN true ELSE false END FROM RevisionSession rs " +
           "WHERE rs.syllabusVersion.id = :syllabusVersionId " +
           "AND rs.status IN ('OPEN', 'IN_PROGRESS', 'PENDING_HOD')")
    boolean hasActiveSession(@Param("syllabusVersionId") UUID syllabusVersionId);
}
