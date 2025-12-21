package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.GradingScale;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for GradingScale entity
 */
@Repository
public interface GradingScaleRepository extends JpaRepository<GradingScale, UUID> {
    
    Optional<GradingScale> findByName(String name);
}
