package com.ras.event_platform.repo;

import com.ras.event_platform.model.EvaluationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationAuditRepository extends JpaRepository<EvaluationAudit, Long> {
}
