package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    
    @EntityGraph(attributePaths = {"subject"})
    @Query("SELECT p FROM PLO p")
    List<PLO> findAll();
    
    @EntityGraph(attributePaths = {"subject"})
    List<PLO> findBySubjectId(UUID subjectId);
    
    Optional<PLO> findBySubjectIdAndCode(UUID subjectId, String code);
    
    boolean existsBySubjectIdAndCode(UUID subjectId, String code);
}
