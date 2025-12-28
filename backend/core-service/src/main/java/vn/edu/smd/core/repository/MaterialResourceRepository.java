package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.MaterialResource;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaterialResourceRepository extends JpaRepository<MaterialResource, UUID> {
    List<MaterialResource> findBySyllabusVersionId(UUID syllabusVersionId);
}
