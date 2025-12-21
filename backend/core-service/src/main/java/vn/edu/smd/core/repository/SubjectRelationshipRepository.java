package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SubjectRelationship;
import vn.edu.smd.shared.enums.SubjectRelationType;

import java.util.List;
import java.util.UUID;

/**
 * Repository for SubjectRelationship entity
 */
@Repository
public interface SubjectRelationshipRepository extends JpaRepository<SubjectRelationship, UUID> {
    
    List<SubjectRelationship> findBySubjectId(UUID subjectId);
    
    List<SubjectRelationship> findByRelatedSubjectId(UUID relatedSubjectId);
    
    List<SubjectRelationship> findBySubjectIdAndType(UUID subjectId, SubjectRelationType type);
}
