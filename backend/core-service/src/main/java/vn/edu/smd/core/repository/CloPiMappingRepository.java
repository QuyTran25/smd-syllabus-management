package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.CloPiMapping;
import vn.edu.smd.shared.enums.MappingLevel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for CloPiMapping entity
 */
@Repository
public interface CloPiMappingRepository extends JpaRepository<CloPiMapping, UUID> {

    /**
     * Find all mappings by CLO ID
     */
    List<CloPiMapping> findByCloId(UUID cloId);

    /**
     * Find all mappings by PI ID
     */
    List<CloPiMapping> findByPiId(UUID piId);

    /**
     * Find specific mapping
     */
    Optional<CloPiMapping> findByCloIdAndPiId(UUID cloId, UUID piId);

    /**
     * Find mappings by level
     */
    List<CloPiMapping> findByCloIdAndLevel(UUID cloId, MappingLevel level);

    /**
     * Check if mapping exists
     */
    boolean existsByCloIdAndPiId(UUID cloId, UUID piId);

    /**
     * Delete by CLO ID
     */
    void deleteByCloId(UUID cloId);

    /**
     * Delete by PI ID
     */
    void deleteByPiId(UUID piId);

    /**
     * Count mappings for CLO
     */
    long countByCloId(UUID cloId);
}
