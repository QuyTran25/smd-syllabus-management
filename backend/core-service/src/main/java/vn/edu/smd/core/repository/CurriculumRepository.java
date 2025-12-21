package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Curriculum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Curriculum entity
 */
@Repository
public interface CurriculumRepository extends JpaRepository<Curriculum, UUID> {
    
    Optional<Curriculum> findByCode(String code);
    
    List<Curriculum> findByFacultyId(UUID facultyId);
    
    boolean existsByCode(String code);
}
