package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
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
    
    @EntityGraph(attributePaths = {"faculty"})
    Optional<Department> findWithFacultyByCode(String code);
    
    @EntityGraph(attributePaths = {"faculty"})
    List<Department> findByFacultyId(UUID facultyId);
    
    @EntityGraph(attributePaths = {"faculty"})
    Optional<Department> findWithFacultyById(UUID id);
    
    @EntityGraph(attributePaths = {"faculty"})
    @Query("SELECT d FROM Department d")
    List<Department> findAllWithFaculty();
    
    boolean existsByCode(String code);
}
