package vn.edu.smd.core.module.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.StudentSyllabusTracker;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentSyllabusTrackerRepository extends JpaRepository<StudentSyllabusTracker, UUID> {
    // Tìm xem sinh viên này đã theo dõi môn này chưa
    Optional<StudentSyllabusTracker> findByStudentIdAndSyllabusId(UUID studentId, UUID syllabusId);

    // Lấy danh sách tất cả các môn sinh viên đang theo dõi
    List<StudentSyllabusTracker> findByStudentId(UUID studentId);
}