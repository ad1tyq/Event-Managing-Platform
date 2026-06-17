package com.ras.event_platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.CrossOrigin;

import com.ras.event_platform.model.User;
import com.ras.event_platform.service.UserService;

import com.ras.event_platform.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "${cors.allowed.origins}")
@RestController
@RequestMapping("api/user")
public class UserController {

  @Autowired
  UserService service;

  @Autowired
  JwtUtil jwtUtil;

  User user;

  @PostMapping("/login")
  public ResponseEntity<?> userLogin(@RequestBody User user) {
    Boolean isValid = service.userLogin(user.getUsername(), user.getPasswordHash());
    if (isValid) {
      String token = jwtUtil.generateToken(user.getUsername());
      Map<String, String> response = new HashMap<>();
      response.put("token", token);
      response.put("message", "Login successful");
      return ResponseEntity.ok(response);
    } else {
      Map<String, String> response = new HashMap<>();
      response.put("error", "Invalid Admin Credentials");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
  }

  @PostMapping("/import")
  public String importData(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      return "File is empty";
    }
    try {
      // Pass the InputStream directly to the service
      service.importTeamsFromStream(file.getInputStream(), 1);
      return "Import Successful";
    } catch (Exception e) {
      return "Import Failed: " + e.getMessage();
    }
  }
}
