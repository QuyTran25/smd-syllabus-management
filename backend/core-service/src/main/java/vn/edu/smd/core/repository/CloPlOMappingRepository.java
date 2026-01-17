package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.CloPlOMapping;

import java.util.List;
import java.util.UUID;

/**
 * Repository for CloPlOMapping entity
 */
@Repository
public interface CloPlOMappingRepository extends JpaRepository<CloPlOMapping, UUID> {
    
    @EntityGraph(attributePaths = {"plo", "plo.subject"})
    List<CloPlOMapping> findByCloId(UUID cloId);
    
    List<CloPlOMapping> findByPloId(UUID ploId);
    
    void deleteByCloId(UUID cloId);
    
    void deleteByPloId(UUID ploId);

    List<CloPlOMapping> findByCloIdIn(List<UUID> cloIds);
}
