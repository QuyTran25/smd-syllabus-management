package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Organization;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Organization entity
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    Optional<Organization> findByCode(String code);
    
    boolean existsByCode(String code);
}
