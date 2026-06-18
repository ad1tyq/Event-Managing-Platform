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
}
