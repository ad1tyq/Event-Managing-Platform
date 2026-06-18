package com.ras.event_platform.repo;

import com.ras.event_platform.model.MentorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MentorSessionRepository extends JpaRepository<MentorSession, Integer> {
    
    @Query("SELECT s FROM MentorSession s WHERE s.status = :status AND s.requestedAt < :threshold")
    List<MentorSession> findStaleRequests(@Param("status") String status, @Param("threshold") LocalDateTime threshold);

    List<MentorSession> findByRegistrationIdAndStatusIn(UUID registrationId, List<String> statuses);

    List<MentorSession> findByMentorIdAndStatusIn(Long mentorId, List<String> statuses);
}
