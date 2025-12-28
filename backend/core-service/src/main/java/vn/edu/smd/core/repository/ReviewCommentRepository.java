package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.ReviewComment;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ReviewComment entity
 */
@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, UUID> {
    
    List<ReviewComment> findBySyllabusVersionId(UUID syllabusVersionId);
    
    List<ReviewComment> findByParentId(UUID parentId);
    
    List<ReviewComment> findBySyllabusVersionIdAndParentIsNull(UUID syllabusVersionId);
    
    List<ReviewComment> findBySyllabusVersionIdAndIsResolved(UUID syllabusVersionId, Boolean isResolved);
    
    List<ReviewComment> findBySyllabusVersionIdOrderByCreatedAtDesc(UUID syllabusVersionId);
}
