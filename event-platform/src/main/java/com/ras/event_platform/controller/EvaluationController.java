package com.ras.event_platform.controller;

import com.ras.event_platform.dto.EvaluationRequest;
import com.ras.event_platform.model.Evaluation;
import com.ras.event_platform.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "${cors.allowed.origins}")
@RestController
@RequestMapping("/api")
public class EvaluationController {

  @Autowired
  private EvaluationService evaluationService;

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
