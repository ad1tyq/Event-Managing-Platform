package com.ras.event_platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
// import com.ras.event_platform.model.*;
import com.ras.event_platform.dto.*;
import com.ras.event_platform.service.*;

@CrossOrigin(origins = "${cors.allowed.origins}")
@RestController
@RequestMapping("/api")
public class AuthController {

  @Autowired
  AuthService service;

  @PostMapping("/login")
  public String login(@RequestBody Auth auth) {
    Boolean isValid = service.login(auth.getTeamName(), auth.getTeamPasscode());
    if (isValid) {
      return "Login Successful";
    } else {
      return "Invalid Credentials";
    }
  }
}
