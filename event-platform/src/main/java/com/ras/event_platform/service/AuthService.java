package com.ras.event_platform.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

// import com.ras.event_platform.model.*;
// import com.ras.event_platform.dto.*;
import com.ras.event_platform.repo.AuthRepository;

@Service
@Component
public class AuthService {

  @Autowired
  AuthRepository repo;

  public Boolean login(String teamName, String teamPasscode) {
    Optional<com.ras.event_platform.model.Registration> team = repo.findByNameAndPass(teamName, teamPasscode);
    return team.isPresent();
  }
}
