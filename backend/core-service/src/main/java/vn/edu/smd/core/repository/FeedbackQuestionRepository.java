package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.FeedbackQuestion;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackQuestionRepository extends JpaRepository<FeedbackQuestion, UUID> {
    List<FeedbackQuestion> findBySyllabusVersionId(UUID syllabusVersionId);
}
