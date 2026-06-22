package com.ras.event_platform.controller;

import com.ras.event_platform.dto.EvaluationRequest;
import com.ras.event_platform.model.Evaluation;
import com.ras.event_platform.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ras.event_platform.repo.EvaluationRepository;
import com.ras.event_platform.repo.SubmissionRepository;
import com.ras.event_platform.repo.RegistrationRepository;
import com.ras.event_platform.model.Submission;
import com.ras.event_platform.model.Registration;
import java.util.List;

@CrossOrigin(origins = "${cors.allowed.origins}")
@RestController
@RequestMapping("/api")
public class EvaluationController {

  @Autowired
  private EvaluationService evaluationService;

  @Autowired
  private EvaluationRepository evaluationRepository;

  @Autowired
  private SubmissionRepository submissionRepository;

  @Autowired
  private RegistrationRepository registrationRepository;

  @GetMapping("/evaluations/me")
  public ResponseEntity<?> getMyEvaluations(@RequestAttribute("userId") Long judgeId) {
      List<Evaluation> evals = evaluationRepository.findByJudgeId(judgeId);
      if (evals.isEmpty()) return ResponseEntity.ok(evals);

      // Batch Fetch Submissions
      List<Integer> subIds = evals.stream().map(Evaluation::getSubmissionId).toList();
      List<Submission> subs = submissionRepository.findAllById(subIds);
      java.util.Map<Integer, Submission> subMap = subs.stream()
              .collect(java.util.stream.Collectors.toMap(Submission::getId, s -> s));

      // Batch Fetch Registrations
      List<java.util.UUID> regIds = subs.stream().map(Submission::getRegistrationId).toList();
      List<Registration> regs = registrationRepository.findAllById(regIds);
      java.util.Map<java.util.UUID, String> teamNames = regs.stream()
              .collect(java.util.stream.Collectors.toMap(Registration::getId, Registration::getTeamName));

      // Hydrate
      for (Evaluation eval : evals) {
          Submission sub = subMap.get(eval.getSubmissionId());
          if (sub != null) {
              eval.setTaskId(sub.getTaskId());
              eval.setPayload(sub.getPayload());
              eval.setTeamName(teamNames.get(sub.getRegistrationId()));
          }
      }
      return ResponseEntity.ok(evals);
  }

  @PostMapping("/evaluate")
  public ResponseEntity<?> submitGrade(@RequestBody EvaluationRequest request,
      @RequestAttribute("userId") Long judgeId) {
    try {
      Evaluation savedEval = evaluationService.gradeSubmission(judgeId, request);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedEval);

    } catch (IllegalStateException e) {
      // Catches the "Not PENDING" error
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (DataIntegrityViolationException e) {
      // Catches the Database Unique Constraint (Judge already graded this)
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body("{\"error\": \"You have already submitted an evaluation for this task.\"}");

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }
}
