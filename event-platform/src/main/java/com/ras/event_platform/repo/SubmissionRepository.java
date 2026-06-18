package com.ras.event_platform.repo;

import com.ras.event_platform.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
  @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Submission s WHERE s.registrationId = :regId AND s.status = :status AND s.roundNumber = :round AND s.taskId = :taskId")
  Boolean hasPendingSubmission(
      @Param("regId") UUID regId,
      @Param("status") String status,
      @Param("round") Integer round,
      @Param("taskId") String taskId);

  List<Submission> findByRegistrationId(UUID registrationId);

  List<Submission> findByStatus(String status);

  @Query("SELECT s FROM Submission s WHERE s.status = 'PENDING' AND NOT EXISTS (SELECT 1 FROM Evaluation e WHERE e.submissionId = s.id AND e.judgeId = :judgeId)")
  List<Submission> findPendingSubmissionsNotEvaluatedBy(@Param("judgeId") Long judgeId);

  @Query("SELECT DISTINCT s FROM Submission s JOIN Evaluation e ON s.id = e.submissionId WHERE s.status = 'PENDING'")
  List<Submission> findPendingSubmissionsWithEvaluations();

  List<Submission> findByTaskIdAndStatusOrderBySubmittedAtAsc(String taskId, String status);
}
