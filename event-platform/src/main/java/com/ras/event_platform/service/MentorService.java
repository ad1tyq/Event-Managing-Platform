package com.ras.event_platform.service;

import com.ras.event_platform.model.MentorProfile;
import com.ras.event_platform.model.MentorSession;
import com.ras.event_platform.repo.MentorProfileRepository;
import com.ras.event_platform.repo.MentorSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MentorService {

    @Autowired
    private MentorSessionRepository sessionRepository;

    @Autowired
    private MentorProfileRepository profileRepository;

    // 1. Team Manual Withdrawal
    @Transactional
    public void withdrawRequest(UUID teamId, Integer sessionId) {
        MentorSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getRegistrationId().equals(teamId)) {
            throw new IllegalStateException("You cannot withdraw another team's request.");
        }

        if (!"REQUESTED".equals(session.getStatus())) {
            throw new IllegalStateException("Cannot withdraw. The session is already " + session.getStatus());
        }

        session.setStatus("CANCELLED");
        session.setResolvedAt(LocalDateTime.now());
        sessionRepository.save(session);
        
        // Ensure mentor is marked back as AVAILABLE
        MentorProfile profile = profileRepository.findById(session.getMentorId()).orElse(null);
        if (profile != null) {
            profile.setCurrentStatus("AVAILABLE");
            profileRepository.save(profile);
        }
    }

    // 2. The Auto-Cancel Cron Job (Runs every 1 minute)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelStaleRequests() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        
        List<MentorSession> staleSessions = sessionRepository.findStaleRequests("REQUESTED", tenMinutesAgo);

        for (MentorSession session : staleSessions) {
            session.setStatus("CANCELLED");
            session.setResolvedAt(LocalDateTime.now());
            sessionRepository.save(session);

            // Free up the mentor
            MentorProfile profile = profileRepository.findById(session.getMentorId()).orElse(null);
            if (profile != null) {
                profile.setCurrentStatus("AVAILABLE");
                profileRepository.save(profile);
            }
            System.out.println("Auto-cancelled stale mentor request ID: " + session.getId());
        }
    }
}
