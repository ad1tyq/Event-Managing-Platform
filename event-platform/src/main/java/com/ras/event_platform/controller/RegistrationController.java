package com.ras.event_platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.ras.event_platform.model.Registration;
import com.ras.event_platform.dto.*;
import com.ras.event_platform.service.*;
import com.ras.event_platform.util.JwtUtil;

import java.util.Optional;
import java.util.Map;

@CrossOrigin(origins = "${cors.allowed.origins}")
@RestController
@RequestMapping("/api")
public class RegistrationController {

  @Autowired
  RegistrationService service;

  @Autowired
  com.ras.event_platform.repo.RegistrationRepository registrationRepository;

  @Autowired
  com.ras.event_platform.repo.EventRepository eventRepository;

  @Autowired
  JwtUtil jwtUtil;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody RegistrationRequest auth) {
    Optional<Registration> teamOpt = service.login(auth.getTeamName(), auth.getTeamPasscode());
    
    if (teamOpt.isPresent()) {
      Registration team = teamOpt.get();
      String token = jwtUtil.generateToken(team.getId().toString(), "ROLE_TEAM");
      return ResponseEntity.ok(Map.of("token", token));
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Invalid Credentials"));
    }
  }

  @org.springframework.web.bind.annotation.GetMapping("/leaderboard")
  public ResponseEntity<?> getParticipantLeaderboard() {
      // Fetch the first available event dynamically instead of hardcoding ID 1
      java.util.List<com.ras.event_platform.model.Event> events = eventRepository.findAll();
      if (events.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Event not found"));
      com.ras.event_platform.model.Event event = events.get(0);
      try {
          com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
          com.fasterxml.jackson.databind.JsonNode config = mapper.readTree(event.getConfig());
          boolean isPublished = config.has("is_leaderboard_published") && config.get("is_leaderboard_published").asBoolean();
          
          if (!isPublished) {
              return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Leaderboard is currently hidden by admins."));
          }
          
          return ResponseEntity.ok(registrationRepository.getLeaderboard());
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error parsing event config"));
      }
  }
}
