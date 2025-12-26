package vn.edu.smd.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SyllabusVersion entity
 */
@Repository
public interface SyllabusVersionRepository extends JpaRepository<SyllabusVersion, UUID> {
    
    List<SyllabusVersion> findBySubjectId(UUID subjectId);
    
    List<SyllabusVersion> findByAcademicTermId(UUID academicTermId);
    
    List<SyllabusVersion> findByStatus(SyllabusStatus status);
    
    @Query(value = "SELECT * FROM core_service.syllabus_versions WHERE status = ANY(CAST(:statuses AS core_service.syllabus_status[])) AND is_deleted = false ORDER BY created_at DESC", 
           countQuery = "SELECT COUNT(*) FROM core_service.syllabus_versions WHERE status = ANY(CAST(:statuses AS core_service.syllabus_status[])) AND is_deleted = false",
           nativeQuery = true)
    Page<SyllabusVersion> findByStatusInWithPage(@Param("statuses") String[] statuses, Pageable pageable);
    
    List<SyllabusVersion> findByCreatedById(UUID createdById);
    
    @Query("SELECT s FROM SyllabusVersion s WHERE s.subject.id = :subjectId AND s.academicTerm.id = :termId AND s.status = :status AND s.isDeleted = false")
    Optional<SyllabusVersion> findBySubjectAndTermAndStatus(
        @Param("subjectId") UUID subjectId,
        @Param("termId") UUID termId,
        @Param("status") SyllabusStatus status
    );
    
    @Query("SELECT s FROM SyllabusVersion s WHERE s.isDeleted = false")
    List<SyllabusVersion> findAllActive();
    
    @Query("SELECT s FROM SyllabusVersion s WHERE s.status = :status AND s.isDeleted = false")
    List<SyllabusVersion> findByStatusAndNotDeleted(@Param("status") SyllabusStatus status);
    
    List<SyllabusVersion> findBySubject(Subject subject);
}
