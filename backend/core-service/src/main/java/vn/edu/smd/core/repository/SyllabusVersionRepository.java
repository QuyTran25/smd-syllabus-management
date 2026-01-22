package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SyllabusVersionRepository extends JpaRepository<SyllabusVersion, UUID> {

    List<SyllabusVersion> findBySubjectId(UUID subjectId);

    @Query("SELECT s FROM SyllabusVersion s WHERE s.subject.id = :subjectId AND s.isDeleted = false ORDER BY s.versionNumber DESC")
    List<SyllabusVersion> findBySubjectIdAndNotDeleted(@Param("subjectId") UUID subjectId);

    List<SyllabusVersion> findByAcademicTermId(UUID academicTermId);

    List<SyllabusVersion> findByStatus(SyllabusStatus status);

    // Use custom query to avoid PostgreSQL enum casting issues
    @Query(value = "SELECT * FROM core_service.syllabus_versions WHERE CAST(status AS TEXT) = ANY(CAST(:statuses AS TEXT[])) AND is_deleted = false", nativeQuery = true)
    List<SyllabusVersion> findByStatusInAndIsDeletedFalse(@Param("statuses") String[] statuses);

    List<SyllabusVersion> findByCreatedById(UUID createdById);

    List<SyllabusVersion> findBySubject(Subject subject);

    @Query("SELECT s FROM SyllabusVersion s WHERE s.isDeleted = false")
    List<SyllabusVersion> findAllActive();

    @Query("SELECT s FROM SyllabusVersion s WHERE s.status = :status AND s.isDeleted = false")
    List<SyllabusVersion> findByStatusAndNotDeleted(@Param("status") SyllabusStatus status);

    // ==========================================
    // CÁC HÀM MỚI ĐƯỢC THÊM ĐỂ FIX LỖI BUILD
    // ==========================================

    // 1. Tìm Syllabus theo Subject, Term và Status (Trả về Optional để xử lý null safe)
    Optional<SyllabusVersion> findBySubjectIdAndAcademicTermIdAndStatus(UUID subjectId, UUID academicTermId, SyllabusStatus status);

    // 2. Đếm số lượng Syllabus theo Subject và Term
    long countBySubjectIdAndAcademicTermId(UUID subjectId, UUID academicTermId);

    // 3. Lấy Syllabus mới nhất (theo ngày tạo) của một Subject
    Optional<SyllabusVersion> findFirstBySubjectIdOrderByCreatedAtDesc(UUID subjectId);
    
    // 4. Override findById để chỉ lấy record chưa xóa
    @Query("SELECT s FROM SyllabusVersion s WHERE s.id = :id AND s.isDeleted = false")
    Optional<SyllabusVersion> findByIdAndNotDeleted(@Param("id") UUID id);
    
    // Count syllabi by status
    long countByStatusAndIsDeletedFalse(SyllabusStatus status);
}