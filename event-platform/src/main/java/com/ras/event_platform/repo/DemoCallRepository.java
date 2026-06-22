package com.ras.event_platform.repo;

import com.ras.event_platform.model.DemoCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DemoCallRepository extends JpaRepository<DemoCall, Integer> {
  List<DemoCall> findByStatus(String status);
  
  @Query("SELECT d FROM DemoCall d WHERE d.submissionId = :submissionId")
  Optional<DemoCall> findBySubmissionId(@Param("submissionId") Integer submissionId);
}
