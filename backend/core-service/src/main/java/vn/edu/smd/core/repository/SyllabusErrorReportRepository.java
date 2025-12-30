package vn.edu.smd.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SyllabusErrorReport;

import java.util.List;
import java.util.UUID;

/**
 * Repository for SyllabusErrorReport entity
 */
@Repository
public interface SyllabusErrorReportRepository extends JpaRepository<SyllabusErrorReport, UUID> {
    
    @EntityGraph(attributePaths = {"syllabusVersion", "user", "respondedBy", "resolvedBy"})
    Page<SyllabusErrorReport> findAll(Pageable pageable);
    
    @EntityGraph(attributePaths = {"syllabusVersion", "user", "respondedBy", "resolvedBy"})
    Page<SyllabusErrorReport> findByStatus(String status, Pageable pageable);
    
    List<SyllabusErrorReport> findBySyllabusVersionId(UUID syllabusVersionId);
    
    List<SyllabusErrorReport> findByUserId(UUID userId);
    
    List<SyllabusErrorReport> findByStatus(String status);
    
    List<SyllabusErrorReport> findBySyllabusVersionIdAndStatus(UUID syllabusVersionId, String status);
}
