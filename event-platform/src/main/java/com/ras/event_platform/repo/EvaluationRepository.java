package com.ras.event_platform.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ras.event_platform.model.Evaluation;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {
  @Query("SELECT e FROM Evaluation e WHERE e.submissionId = :submissionId")
  List<Evaluation> findBySubId(@Param("submissionId") Integer submissionId);
}
