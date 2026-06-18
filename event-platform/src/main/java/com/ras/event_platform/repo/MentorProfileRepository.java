package com.ras.event_platform.repo;

import com.ras.event_platform.model.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    List<MentorProfile> findByIsActiveTrue();
}
