package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.CLO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CLO entity
 */
@Repository
public interface CLORepository extends JpaRepository<CLO, UUID> {
    
    List<CLO> findBySyllabusVersionId(UUID syllabusVersionId);
    
    Optional<CLO> findBySyllabusVersionIdAndCode(UUID syllabusVersionId, String code);
    
    boolean existsBySyllabusVersionIdAndCode(UUID syllabusVersionId, String code);

    List<CLO> findBySyllabusVersionIdOrderByCodeAsc(UUID syllabusVersionId);
}
