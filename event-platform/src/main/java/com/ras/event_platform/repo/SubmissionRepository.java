package com.ras.event_platform.repo;

import com.ras.event_platform.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
  @Query("SELECT COUNT(s) > 0 FROM Submission s WHERE s.registrationId = :registrationId AND s.status = :status AND s.roundNumber = :roundNumber AND s.taskId = :taskId")
  boolean hasPendingSubmission(UUID registrationId, String status, Integer roundNumber,
      String taskId);

  List<Submission> findByRegistrationId(UUID registrationId);
}
