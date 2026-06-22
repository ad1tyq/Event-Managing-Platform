package com.ras.event_platform.controller;

import com.ras.event_platform.model.MentorProfile;
import com.ras.event_platform.model.MentorSession;
// import com.ras.event_platform.model.Registration;
// import com.ras.event_platform.model.User;
import com.ras.event_platform.repo.MentorProfileRepository;
import com.ras.event_platform.repo.MentorSessionRepository;
import com.ras.event_platform.repo.RegistrationRepository;
import com.ras.event_platform.repo.UserRepository;
import com.ras.event_platform.service.MentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
// import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mentors")
@CrossOrigin(origins = "http://localhost:3000")
public class MentorController {

    @Autowired
    private MentorService mentorService;
    @Autowired
    private MentorProfileRepository profileRepository;
    @Autowired
    private MentorSessionRepository sessionRepository;
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private UserRepository userRepository;

    // --- MENTOR ACTIONS (Requires Judge/Admin Token) ---

    @GetMapping("/me/status")
    public ResponseEntity<?> getMyStatus(@RequestAttribute("userId") Long mentorId) {
        MentorProfile profile = profileRepository.findById(mentorId).orElse(null);
        if (profile == null) {
            profile = new MentorProfile();
            profile.setUserId(mentorId);
            profile.setIsActive(false);
            profile.setCurrentStatus("AVAILABLE");
            profile = profileRepository.save(profile);
        }
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me/status")
    public ResponseEntity<?> updateStatus(@RequestAttribute("userId") Long mentorId,
            @RequestBody Map<String, Object> payload) {
        MentorProfile profile = profileRepository.findById(mentorId).orElse(new MentorProfile());
        profile.setUserId(mentorId);

        if (payload.containsKey("isActive")) {
            profile.setIsActive((Boolean) payload.get("isActive"));
        }
        if (payload.containsKey("currentStatus")) {
            profile.setCurrentStatus((String) payload.get("currentStatus"));
        }
        if (payload.containsKey("skills")) {
            profile.setSkills((String) payload.get("skills"));
        }

        return ResponseEntity.ok(profileRepository.save(profile));
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getMySessions(@RequestAttribute("userId") Long mentorId) {
        List<MentorSession> sessions = sessionRepository.findByMentorIdAndStatusIn(mentorId,
                List.of("REQUESTED", "ACTIVE"));
        
        // Batch Hydration
        if (!sessions.isEmpty()) {
            List<java.util.UUID> regIds = sessions.stream().map(MentorSession::getRegistrationId).toList();
            java.util.Map<java.util.UUID, String> teamNames = registrationRepository.findAllById(regIds).stream()
                    .collect(java.util.stream.Collectors.toMap(com.ras.event_platform.model.Registration::getId, com.ras.event_platform.model.Registration::getTeamName));
            for (MentorSession s : sessions) {
                s.setTeamName(teamNames.get(s.getRegistrationId()));
            }
        }
        return ResponseEntity.ok(sessions);
    }

    @PutMapping("/sessions/{id}/accept")
    public ResponseEntity<?> acceptSession(@RequestAttribute("userId") Long mentorId, @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        MentorSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getMentorId().equals(mentorId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your session"));
        }

        if (!"REQUESTED".equals(session.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("error", "Session is no longer REQUESTED"));
        }

        session.setStatus("ACTIVE");
        session.setMeetingLink(payload.get("meetingLink"));
        sessionRepository.save(session);

        // Lock the mentor
        MentorProfile profile = profileRepository.findById(mentorId).get();
        profile.setCurrentStatus("BUSY");
        profileRepository.save(profile);

        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{id}/resolve")
    public ResponseEntity<?> resolveSession(@RequestAttribute("userId") Long mentorId, @PathVariable Integer id) {
        MentorSession session = sessionRepository.findById(id).orElseThrow();
        if (!session.getMentorId().equals(mentorId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your session"));
        }

        session.setStatus("RESOLVED");
        session.setResolvedAt(LocalDateTime.now());
        sessionRepository.save(session);

        // Free the mentor
        MentorProfile profile = profileRepository.findById(mentorId).get();
        profile.setCurrentStatus("AVAILABLE");
        profileRepository.save(profile);

        return ResponseEntity.ok(session);
    }

    // --- TEAM ACTIONS (Requires Team Token) ---

    @GetMapping("/available")
    public ResponseEntity<List<MentorProfile>> getAvailableMentors() {
        List<MentorProfile> profiles = profileRepository.findByIsActiveTrue();
        
        // Batch Hydration
        if (!profiles.isEmpty()) {
            List<Long> userIds = profiles.stream().map(MentorProfile::getUserId).toList();
            java.util.Map<Long, String> usernames = userRepository.findAllById(userIds).stream()
                    .collect(java.util.stream.Collectors.toMap(com.ras.event_platform.model.User::getId, com.ras.event_platform.model.User::getUsername));
            for (MentorProfile p : profiles) {
                p.setUsername(usernames.get(p.getUserId()));
            }
        }
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/sessions/my-request")
    public ResponseEntity<?> getMyRequest(@RequestAttribute("teamId") UUID teamId) {
        List<MentorSession> sessions = sessionRepository.findByRegistrationIdAndStatusIn(teamId,
                List.of("REQUESTED", "ACTIVE"));
        if (sessions.isEmpty()) {
            return ResponseEntity.ok(null);
        }
        MentorSession s = sessions.get(0);
        userRepository.findById(s.getMentorId()).ifPresent(u -> s.setMentorName(u.getUsername()));
        return ResponseEntity.ok(s);
    }

    @PostMapping("/sessions/request")
    public ResponseEntity<?> requestMentor(@RequestAttribute("teamId") UUID teamId,
            @RequestBody Map<String, Object> payload) {
        // Enforce 1 active request rule
        List<MentorSession> existing = sessionRepository.findByRegistrationIdAndStatusIn(teamId,
                List.of("REQUESTED", "ACTIVE"));
        if (!existing.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("error", "You already have an active or requested session."));
        }

        Long requestedMentorId = Long.valueOf(payload.get("mentorId").toString());

        // Ensure mentor is available
        MentorProfile profile = profileRepository.findById(requestedMentorId).orElse(null);
        if (profile == null || !profile.getIsActive() || !"AVAILABLE".equals(profile.getCurrentStatus())) {
            return ResponseEntity.status(400).body(Map.of("error", "Mentor is no longer available."));
        }

        MentorSession session = new MentorSession();
        session.setRegistrationId(teamId);
        session.setMentorId(requestedMentorId);
        session.setIssueDescription((String) payload.get("issueDescription"));
        session.setStatus("REQUESTED");
        return ResponseEntity.ok(sessionRepository.save(session));
    }

    @DeleteMapping("/sessions/{id}/withdraw")
    public ResponseEntity<?> withdrawRequest(@RequestAttribute("teamId") UUID teamId, @PathVariable Integer id) {
        mentorService.withdrawRequest(teamId, id);
        return ResponseEntity.ok(Map.of("message", "Session withdrawn successfully"));
    }
}
