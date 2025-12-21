package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.PLO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PLO entity
 */
@Repository
public interface PLORepository extends JpaRepository<PLO, UUID> {
    
    List<PLO> findByCurriculumId(UUID curriculumId);
    
    Optional<PLO> findByCurriculumIdAndCode(UUID curriculumId, String code);
    
    boolean existsByCurriculumIdAndCode(UUID curriculumId, String code);
}
