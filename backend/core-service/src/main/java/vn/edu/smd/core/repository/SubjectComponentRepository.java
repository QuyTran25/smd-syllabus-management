package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SubjectComponent;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectComponentRepository extends JpaRepository<SubjectComponent, UUID> {
    List<SubjectComponent> findBySyllabusVersionId(UUID syllabusVersionId);
}
