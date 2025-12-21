package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SyllabusCollaborator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SyllabusCollaborator entity
 */
@Repository
public interface SyllabusCollaboratorRepository extends JpaRepository<SyllabusCollaborator, UUID> {
    
    List<SyllabusCollaborator> findBySyllabusVersionId(UUID syllabusVersionId);
    
    List<SyllabusCollaborator> findByUserId(UUID userId);
    
    Optional<SyllabusCollaborator> findBySyllabusVersionIdAndUserId(UUID syllabusVersionId, UUID userId);
    
    void deleteBySyllabusVersionIdAndUserId(UUID syllabusVersionId, UUID userId);
}
