package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.ClassEntity;

import java.util.Optional;
import java.util.UUID;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======
>>>>>>> origin/main

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, UUID> {
    Optional<ClassEntity> findByCode(String code);
    boolean existsByCode(String code);
<<<<<<< HEAD
    Page<ClassEntity> findAllBySemesterId(UUID semesterId, Pageable pageable);
=======
>>>>>>> origin/main
}
