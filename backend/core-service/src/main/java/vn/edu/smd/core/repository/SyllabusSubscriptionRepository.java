package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SyllabusSubscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SyllabusSubscription entity
 */
@Repository
public interface SyllabusSubscriptionRepository extends JpaRepository<SyllabusSubscription, UUID> {

    /**
     * Find all subscriptions by user ID
     */
    List<SyllabusSubscription> findByUserId(UUID userId);

    /**
     * Find all subscriptions by subject ID
     */
    List<SyllabusSubscription> findBySubjectId(UUID subjectId);

    /**
     * Find all subscriptions by syllabus version ID
     */
    List<SyllabusSubscription> findBySyllabusVersionId(UUID syllabusVersionId);

    /**
     * Find active subscriptions by user ID
     */
    List<SyllabusSubscription> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Find specific subscription
     */
    Optional<SyllabusSubscription> findByUserIdAndSubjectIdAndSyllabusVersionId(
        UUID userId, UUID subjectId, UUID syllabusVersionId
    );

    /**
     * Check if subscription exists
     */
    boolean existsByUserIdAndSubjectId(UUID userId, UUID subjectId);

    /**
     * Count subscriptions for a syllabus version
     */
    long countBySyllabusVersionId(UUID syllabusVersionId);
}
