package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.AcademicTerm;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AcademicTerm entity
 */
@Repository
public interface AcademicTermRepository extends JpaRepository<AcademicTerm, UUID> {
    
    Optional<AcademicTerm> findByCode(String code);
    
    List<AcademicTerm> findByIsActive(Boolean isActive);
    
    Optional<AcademicTerm> findFirstByIsActiveTrue();
    
    boolean existsByCode(String code);
}
