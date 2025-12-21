package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Faculty;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Faculty entity
 */
@Repository
public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    
    Optional<Faculty> findByCode(String code);
    
    boolean existsByCode(String code);
}
