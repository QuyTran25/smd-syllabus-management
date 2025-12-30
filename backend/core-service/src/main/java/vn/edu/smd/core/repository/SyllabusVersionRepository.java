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

    // QUAN TRỌNG: Chọn logic của Main để khớp với SyllabusService
    // Service đang gọi hàm này với tham số List<SyllabusStatus>
    @Query("SELECT s FROM SyllabusVersion s WHERE s.status IN :statuses AND s.isDeleted = false")
    List<SyllabusVersion> findByStatusInAndIsDeletedFalse(@Param("statuses") List<SyllabusStatus> statuses);

    List<SyllabusVersion> findByCreatedById(UUID createdById);

    List<SyllabusVersion> findBySubject(Subject subject);

    // Hàm hỗ trợ Dashboard (lấy thống kê)
    // Spring Data JPA tự động generate query dựa trên tên hàm
    List<SyllabusVersion> findByIsDeletedFalse();

    // Giữ lại alias này nếu code cũ có chỗ dùng
    @Query("SELECT s FROM SyllabusVersion s WHERE s.isDeleted = false")
    List<SyllabusVersion> findAllActive();

    @Query("SELECT s FROM SyllabusVersion s WHERE s.status = :status AND s.isDeleted = false")
    List<SyllabusVersion> findByStatusAndNotDeleted(@Param("status") SyllabusStatus status);
}