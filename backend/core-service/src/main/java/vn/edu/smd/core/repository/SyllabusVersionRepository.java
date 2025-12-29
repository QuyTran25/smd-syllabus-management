package vn.edu.smd.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface SyllabusVersionRepository extends JpaRepository<SyllabusVersion, UUID> {

    List<SyllabusVersion> findBySubjectId(UUID subjectId);

    List<SyllabusVersion> findByAcademicTermId(UUID academicTermId);

    // FIX: Đổi kiểu String thành SyllabusStatus để đồng nhất với Entity
    List<SyllabusVersion> findByStatus(SyllabusStatus status);

    // Native query to handle PostgreSQL ENUM type correctly
    @Query(value = "SELECT * FROM core_service.syllabus_versions WHERE CAST(status AS TEXT) IN (:statuses) AND is_deleted = false", 
           countQuery = "SELECT COUNT(*) FROM core_service.syllabus_versions WHERE CAST(status AS TEXT) IN (:statuses) AND is_deleted = false",
           nativeQuery = true)
    Page<SyllabusVersion> findByStatusInAndIsDeletedFalse(@Param("statuses") List<String> statuses, Pageable pageable);

    List<SyllabusVersion> findByCreatedById(UUID createdById);

    List<SyllabusVersion> findBySubject(Subject subject);

    @Query("SELECT s FROM SyllabusVersion s WHERE s.isDeleted = false")
    List<SyllabusVersion> findAllActive();

    @Query("SELECT s FROM SyllabusVersion s WHERE s.status = :status AND s.isDeleted = false")
    List<SyllabusVersion> findByStatusAndNotDeleted(@Param("status") SyllabusStatus status);
}