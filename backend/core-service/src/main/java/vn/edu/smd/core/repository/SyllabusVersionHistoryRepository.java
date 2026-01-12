package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SyllabusVersionHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SyllabusVersionHistoryRepository extends JpaRepository<SyllabusVersionHistory, UUID> {
    
    /**
     * Find all history snapshots for a syllabus, ordered by version number descending
     */
    List<SyllabusVersionHistory> findBySyllabusVersionIdOrderByVersionNumberDesc(UUID syllabusId);
    
    /**
     * Find specific version snapshot
     */
    Optional<SyllabusVersionHistory> findBySyllabusVersionIdAndVersionNumber(UUID syllabusId, Integer versionNumber);
    
    /**
     * Get latest version number for a syllabus
     */
    @Query("SELECT MAX(h.versionNumber) FROM SyllabusVersionHistory h WHERE h.syllabusVersion.id = :syllabusId")
    Optional<Integer> findMaxVersionNumber(UUID syllabusId);
    
    /**
     * Count history snapshots for a syllabus
     */
    long countBySyllabusVersionId(UUID syllabusId);
}
