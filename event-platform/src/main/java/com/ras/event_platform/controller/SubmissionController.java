package com.ras.event_platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.ras.event_platform.dto.SubmissionRequest;
import com.ras.event_platform.dto.TeamStatusResponse;
import java.util.*;
import com.ras.event_platform.model.Submission;
import com.ras.event_platform.service.SubmissionService;

@CrossOrigin(origins = "${cors.allowed.origins}")
@RestController
@RequestMapping("/api")
public class SubmissionController {

  @Autowired
  SubmissionService submissionService;

  @PostMapping("/submit")
  public ResponseEntity<?> createSubmission(@RequestBody SubmissionRequest request,
      @RequestAttribute("teamId") UUID teamId) {
      
    TeamStatusResponse status = submissionService.getTeamStatus(teamId);
    
    if (status.isPending()) {
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body("{\"error\": \"A submission is already being evaluated.\"}");
    }

    if (!status.getAllowedTaskId().equals(request.getTaskId()) || status.getAllowedRound() != request.getRoundNumber()) {
      return ResponseEntity
          .status(HttpStatus.FORBIDDEN)
          .body("{\"error\": \"You are not allowed to submit this feature/round yet!\"}");
    }

    Submission savedSubmission = submissionService.create(teamId, request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(savedSubmission);
  }

  @GetMapping("/status")
  public ResponseEntity<?> getStatus(@RequestAttribute("teamId") UUID teamId) {
    TeamStatusResponse status = submissionService.getTeamStatus(teamId);
    return ResponseEntity.ok(status);
  }
}
