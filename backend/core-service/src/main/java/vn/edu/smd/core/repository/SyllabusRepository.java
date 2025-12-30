package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Syllabus;
import java.util.UUID;

/**
 * Repository cho bảng Syllabi.
 * JpaRepository cung cấp sẵn các hàm: findAll(), findById(), save(), delete()...
 */
@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, UUID> {
    // Spring Data JPA sẽ tự động xử lý mọi thứ, bạn không cần viết code ở đây.
}