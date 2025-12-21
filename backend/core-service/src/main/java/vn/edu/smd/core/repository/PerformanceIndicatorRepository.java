package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.PerformanceIndicator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PerformanceIndicator entity
 */
@Repository
public interface PerformanceIndicatorRepository extends JpaRepository<PerformanceIndicator, UUID> {

    /**
     * Find all performance indicators by PLO ID
     */
    List<PerformanceIndicator> findByPloId(UUID ploId);

    /**
     * Find performance indicator by PLO ID and code
     */
    Optional<PerformanceIndicator> findByPloIdAndCode(UUID ploId, String code);

    /**
     * Check if performance indicator exists
     */
    boolean existsByPloIdAndCode(UUID ploId, String code);

    /**
     * Delete by PLO ID
     */
    void deleteByPloId(UUID ploId);

    /**
     * Count performance indicators for a PLO
     */
    long countByPloId(UUID ploId);
}
