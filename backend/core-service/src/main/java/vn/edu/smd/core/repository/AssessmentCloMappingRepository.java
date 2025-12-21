package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.AssessmentCloMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for AssessmentCloMapping entity
 */
@Repository
public interface AssessmentCloMappingRepository extends JpaRepository<AssessmentCloMapping, UUID> {

    /**
     * Find all mappings by assessment scheme ID
     */
    List<AssessmentCloMapping> findByAssessmentSchemeId(UUID assessmentSchemeId);

    /**
     * Find all mappings by CLO ID
     */
    List<AssessmentCloMapping> findByCloId(UUID cloId);

    /**
     * Find specific mapping
     */
    Optional<AssessmentCloMapping> findByAssessmentSchemeIdAndCloId(UUID assessmentSchemeId, UUID cloId);

    /**
     * Check if mapping exists
     */
    boolean existsByAssessmentSchemeIdAndCloId(UUID assessmentSchemeId, UUID cloId);

    /**
     * Delete by assessment scheme ID
     */
    void deleteByAssessmentSchemeId(UUID assessmentSchemeId);

    /**
     * Count mappings for assessment scheme
     */
    long countByAssessmentSchemeId(UUID assessmentSchemeId);
}
