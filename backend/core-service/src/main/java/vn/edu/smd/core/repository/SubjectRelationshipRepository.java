package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SubjectRelationship;
import vn.edu.smd.shared.enums.SubjectRelationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SubjectRelationship entity
 */
@Repository
public interface SubjectRelationshipRepository extends JpaRepository<SubjectRelationship, UUID> {
    
    @Query("SELECT sr FROM SubjectRelationship sr JOIN FETCH sr.relatedSubject WHERE sr.subject.id = :subjectId AND sr.type = :type")
    List<SubjectRelationship> findBySubjectIdAndTypeWithRelatedSubject(@Param("subjectId") UUID subjectId, @Param("type") SubjectRelationType type);
    
    @Query("SELECT sr FROM SubjectRelationship sr JOIN FETCH sr.relatedSubject JOIN FETCH sr.subject WHERE sr.subject.id IN :subjectIds AND sr.type = :type")
    List<SubjectRelationship> findBySubjectIdsAndTypeWithRelatedSubject(@Param("subjectIds") List<UUID> subjectIds, @Param("type") SubjectRelationType type);
    
    List<SubjectRelationship> findBySubjectId(UUID subjectId);
    
    List<SubjectRelationship> findByRelatedSubjectId(UUID relatedSubjectId);
    
    List<SubjectRelationship> findBySubjectIdAndType(UUID subjectId, SubjectRelationType type);
    
    Optional<SubjectRelationship> findBySubjectIdAndRelatedSubjectIdAndType(
        UUID subjectId, UUID relatedSubjectId, SubjectRelationType type);
}
