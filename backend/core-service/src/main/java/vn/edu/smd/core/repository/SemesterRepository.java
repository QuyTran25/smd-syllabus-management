package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Semester;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    Optional<Semester> findByCode(String code);
    Optional<Semester> findByIsActiveTrue();
    boolean existsByCode(String code);
    List<Semester> findByAcademicYear(String academicYear);
}
