package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Subject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Subject entity
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    
    Optional<Subject> findByCode(String code);
    
    List<Subject> findByDepartmentId(UUID departmentId);
    
    List<Subject> findByCurriculumId(UUID curriculumId);
    
    List<Subject> findByIsActive(Boolean isActive);
    
    boolean existsByCode(String code);
    
    List<Subject> findByCodeContainingIgnoreCaseOrCurrentNameViContainingIgnoreCaseOrCurrentNameEnContainingIgnoreCase(
        String code, String nameVi, String nameEn);
}
