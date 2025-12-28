package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.FeedbackResponse;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, UUID> {
    List<FeedbackResponse> findByQuestionId(UUID questionId);
    List<FeedbackResponse> findByRespondentId(UUID respondentId);
    
    @Query("SELECT fr FROM FeedbackResponse fr " +
           "JOIN FETCH fr.question fq " +
           "JOIN FETCH fq.syllabusVersion sv " +
           "WHERE sv.id = :syllabusId")
    List<FeedbackResponse> findBySyllabusVersionId(@Param("syllabusId") UUID syllabusId);
}
