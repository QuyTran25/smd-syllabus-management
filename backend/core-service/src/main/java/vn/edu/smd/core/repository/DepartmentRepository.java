package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Department entity
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    
    Optional<Department> findByCode(String code);
    
    List<Department> findByFacultyId(UUID facultyId);
    
    boolean existsByCode(String code);
}
