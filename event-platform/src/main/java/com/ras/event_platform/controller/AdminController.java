package com.ras.event_platform.controller;

import com.ras.event_platform.model.Event;
import com.ras.event_platform.model.Submission;
import com.ras.event_platform.service.AdminService;
import com.ras.event_platform.repo.SubmissionRepository;
import com.ras.event_platform.repo.EventRepository;
import com.ras.event_platform.repo.DemoCallRepository;
import com.ras.event_platform.model.DemoCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "${cors.allowed.origins}")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  @Autowired
  private AdminService adminService;

  @Autowired
  private SubmissionRepository submissionRepository;

  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private com.ras.event_platform.repo.RegistrationRepository registrationRepository;

  @Autowired
  private DemoCallRepository demoCallRepository;

  @GetMapping("/leaderboard")
  public ResponseEntity<?> getLeaderboard(@RequestAttribute("userId") Long adminId) {
    try {
      List<java.util.Map<String, Object>> leaderboard = registrationRepository.getLeaderboard();
      return ResponseEntity.ok(leaderboard);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @GetMapping("/submissions")
  public ResponseEntity<?> getSubmissionsByStatus(@RequestParam("status") String status,
      @RequestAttribute("userId") Long adminId) {
    try {
      List<Submission> submissions;
      if ("PENDING".equalsIgnoreCase(status)) {
        submissions = submissionRepository.findPendingSubmissionsNotEvaluatedBy(adminId);
      } else if ("GRADED".equalsIgnoreCase(status)) {
        submissions = submissionRepository.findPendingSubmissionsWithEvaluations();
      } else {
        submissions = submissionRepository.findByStatus(status);
      }

      // Populate transient teamName field
      for (Submission s : submissions) {
        registrationRepository.findById(s.getRegistrationId())
            .ifPresent(reg -> s.setTeamName(reg.getTeamName()));
      }

      return ResponseEntity.ok(submissions);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @GetMapping("/teams/{id}")
  public ResponseEntity<?> getTeamDetails(@PathVariable("id") java.util.UUID teamId,
      @RequestAttribute("userId") Long adminId) {
    try {
      com.ras.event_platform.model.Registration team = registrationRepository.findById(teamId)
          .orElseThrow(() -> new RuntimeException("Team not found"));
      List<Submission> submissions = submissionRepository.findByRegistrationId(teamId);

      java.util.Map<String, Object> response = new java.util.HashMap<>();
      response.put("team", team);
      response.put("submissions", submissions);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @GetMapping("/events/{id}")
  public ResponseEntity<?> getEvent(@PathVariable("id") Integer eventId, @RequestAttribute("userId") Long adminId) {
    try {
      Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
      return ResponseEntity.ok(event);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  // The {id} is the specific submission ID you are finalizing
  @PostMapping("/submissions/{id}/finalize")
  public ResponseEntity<?> finalizeSubmission(@PathVariable("id") Integer submissionId,
      @RequestAttribute("userId") Long adminId) {
    try {
      Submission finalizedSubmission = adminService.finalizeSubmission(submissionId);
      return ResponseEntity.ok(finalizedSubmission);

    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @PutMapping("/events/{id}/round/{round}")
  public ResponseEntity<?> updateGlobalRound(@PathVariable("id") Integer eventId,
      @PathVariable("round") Integer newRound,
      @RequestAttribute("userId") Long adminId) {
    try {
      Event updatedEvent = adminService.updateGlobalRound(eventId, newRound);
      return ResponseEntity.ok(updatedEvent);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @PutMapping("/events/{id}/meeting-link")
  public ResponseEntity<?> updateMeetingLink(@PathVariable("id") Integer eventId, @RequestBody java.util.Map<String, String> body) {
    try {
      Event updatedEvent = adminService.updateMeetingLink(eventId, body.get("meetingLink"));
      return ResponseEntity.ok(updatedEvent);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @PutMapping("/events/{id}/active-team/{teamId}")
  public ResponseEntity<?> setActiveMeetingTeam(@PathVariable("id") Integer eventId, @PathVariable("teamId") String teamId) {
    try {
      String tid = "none".equalsIgnoreCase(teamId) ? "" : teamId;
      Event updatedEvent = adminService.setActiveMeetingTeam(eventId, tid);
      return ResponseEntity.ok(updatedEvent);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @PutMapping("/events/{id}/leaderboard-toggle")
  public ResponseEntity<?> toggleLeaderboard(@PathVariable("id") Integer eventId, @RequestBody java.util.Map<String, Boolean> body) {
    try {
      Event updatedEvent = adminService.toggleLeaderboard(eventId, body.get("isPublished"));
      return ResponseEntity.ok(updatedEvent);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @GetMapping("/demo-calls/queue")
  public ResponseEntity<?> getDemoCallsQueue(@RequestAttribute("userId") Long adminId) {
    try {
      List<DemoCall> queue = demoCallRepository.findByStatus("QUEUED");
      
      // Hydrate teamName and enteredQueueAt
      for (DemoCall dc : queue) {
          submissionRepository.findById(dc.getSubmissionId()).ifPresent(sub -> {
              dc.setQueueEnteredAt(sub.getSubmittedAt());
              dc.setRegistrationId(sub.getRegistrationId().toString());
              registrationRepository.findById(sub.getRegistrationId()).ifPresent(reg -> {
                  dc.setTeamName(reg.getTeamName());
              });
          });
      }
      
      // Sort by wait time
      queue.sort((a, b) -> {
          if (a.getQueueEnteredAt() == null || b.getQueueEnteredAt() == null) return 0;
          return a.getQueueEnteredAt().compareTo(b.getQueueEnteredAt());
      });
      
      return ResponseEntity.ok(queue);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  @PutMapping("/demo-calls/{id}/call")
  public ResponseEntity<?> inviteToCall(@PathVariable("id") Integer demoCallId, @RequestBody java.util.Map<String, String> body) {
    try {
      DemoCall dc = demoCallRepository.findById(demoCallId)
          .orElseThrow(() -> new RuntimeException("DemoCall not found"));
      
      dc.setStatus("CALLED");
      dc.setCalledAt(java.time.LocalDateTime.now());
      if (body.containsKey("meetingLink")) {
          dc.setMeetingLink(body.get("meetingLink"));
      }
      demoCallRepository.save(dc);
      
      // Update global active team so their dashboard lights up
      submissionRepository.findById(dc.getSubmissionId()).ifPresent(sub -> {
          registrationRepository.findById(sub.getRegistrationId()).ifPresent(reg -> {
              adminService.setActiveMeetingTeam(reg.getEventId(), reg.getId().toString());
          });
      });
      
      return ResponseEntity.ok(dc);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }
}
