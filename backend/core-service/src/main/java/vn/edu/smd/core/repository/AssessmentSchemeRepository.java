package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.AssessmentScheme;

import java.util.List;
import java.util.UUID;

/**
 * Repository for AssessmentScheme entity
 */
@Repository
public interface AssessmentSchemeRepository extends JpaRepository<AssessmentScheme, UUID> {
    
    List<AssessmentScheme> findBySyllabusVersionId(UUID syllabusVersionId);
    
    List<AssessmentScheme> findByParentId(UUID parentId);
    
    List<AssessmentScheme> findBySyllabusVersionIdAndParentIsNull(UUID syllabusVersionId);
}
