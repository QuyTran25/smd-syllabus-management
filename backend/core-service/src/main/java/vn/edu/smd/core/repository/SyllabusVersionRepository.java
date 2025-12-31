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

    // FIX: Thống nhất kiểu SyllabusStatus để đồng nhất với Entity và Enum Postgres
    List<SyllabusVersion> findByStatus(SyllabusStatus status);

    /**
     * Tìm danh sách Syllabus theo trạng thái và chưa bị xóa.
     * ANTI-CONFLICT: Dùng JPQL Query để tránh lỗi casting Enum với PostgreSQL.
     */
    @Query("SELECT s FROM SyllabusVersion s WHERE s.status IN :statuses AND s.isDeleted = false")
    List<SyllabusVersion> findByStatusInAndIsDeletedFalse(@Param("statuses") List<SyllabusStatus> statuses);

    List<SyllabusVersion> findByCreatedById(UUID createdById);

    List<SyllabusVersion> findBySubject(Subject subject);

    // FIX: Khôi phục lại hàm này từ HEAD để không làm hỏng logic trong SyllabusService.getStatistics()
    List<SyllabusVersion> findByIsDeletedFalse();

    @Query("SELECT s FROM SyllabusVersion s WHERE s.isDeleted = false")
    List<SyllabusVersion> findAllActive();

    @Query("SELECT s FROM SyllabusVersion s WHERE s.status = :status AND s.isDeleted = false")
    List<SyllabusVersion> findByStatusAndNotDeleted(@Param("status") SyllabusStatus status);
}