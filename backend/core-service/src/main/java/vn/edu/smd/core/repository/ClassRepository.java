package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.ClassEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, UUID> {
    Optional<ClassEntity> findByCode(String code);
    boolean existsByCode(String code);
}
