package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SyllabusFullView;

import java.util.List;
import java.util.UUID;

@Repository
public interface SyllabusFullRepository extends JpaRepository<SyllabusFullView, UUID> {
    List<SyllabusFullView> findByCreatedByOrderByUpdatedAtDesc(UUID userId);
}
