package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.PublicationRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PublicationRecord entity
 */
@Repository
public interface PublicationRecordRepository extends JpaRepository<PublicationRecord, UUID> {

    /**
     * Find all publications for a syllabus version, ordered by publication time DESC
     */
    List<PublicationRecord> findBySyllabusVersionIdOrderByPublishedAtDesc(UUID syllabusVersionId);

    /**
     * Find all publications by a specific user
     */
    List<PublicationRecord> findByPublishedByIdOrderByPublishedAtDesc(UUID publishedById);

    /**
     * Find latest publication for a syllabus version
     */
    Optional<PublicationRecord> findFirstBySyllabusVersionIdOrderByPublishedAtDesc(UUID syllabusVersionId);

    /**
     * Find all republications (is_republish = true)
     */
    List<PublicationRecord> findByIsRepublishTrue();

    /**
     * Find publications in date range
     */
    List<PublicationRecord> findByPublishedAtBetweenOrderByPublishedAtDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate
    );

    /**
     * Count publications for a syllabus version
     */
    long countBySyllabusVersionId(UUID syllabusVersionId);

    /**
     * Check if syllabus version has been published
     */
    boolean existsBySyllabusVersionId(UUID syllabusVersionId);

    /**
     * Find all publications referencing a previous publication
     */
    List<PublicationRecord> findByPreviousPublicationId(UUID previousPublicationId);
}
