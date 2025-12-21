package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Role;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByCode(String code);
    
    boolean existsByCode(String code);
}
