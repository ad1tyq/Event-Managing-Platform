package com.ras.event_platform.controller;

import com.ras.event_platform.model.Event;
import com.ras.event_platform.model.Submission;
import com.ras.event_platform.service.AdminService;
import com.ras.event_platform.repo.SubmissionRepository;
import com.ras.event_platform.repo.EventRepository;
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
      // NOTE: Here you would ideally check if 'adminId' actually has permission to
      // modify this event
      Event updatedEvent = adminService.updateGlobalRound(eventId, newRound);
      return ResponseEntity.ok(updatedEvent);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }
}
