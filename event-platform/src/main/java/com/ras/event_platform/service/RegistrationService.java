package com.ras.event_platform.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

// import com.ras.event_platform.model.*;
// import com.ras.event_platform.dto.*;
import com.ras.event_platform.repo.RegistrationRepository;

@Service
@Component
public class RegistrationService {

  @Autowired
  RegistrationRepository registrationRepository;

  public Optional<com.ras.event_platform.model.Registration> login(String teamName, String teamPasscode) {
    return registrationRepository.findByNameAndPass(teamName, teamPasscode);
  }
}
