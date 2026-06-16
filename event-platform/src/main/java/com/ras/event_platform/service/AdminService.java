package com.ras.event_platform.service;

import com.opencsv.CSVReader;
import com.ras.event_platform.model.Registration;
import com.ras.event_platform.repo.AdminAuthRepository;
import com.ras.event_platform.repo.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.ras.event_platform.model.*;

@Service
public class AdminService {

  // ------ admin login ------
  @Autowired
  AdminAuthRepository auth_repo;

  public Boolean login(String username, String password) {
    Optional<User> admin = auth_repo.findByNameAndPass(username, password);
    return admin.isPresent();
  }

  // ------ cvs import ------
  @Autowired
  private AuthRepository repo;

  private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom RANDOM = new SecureRandom();

  public void importTeamsFromStream(InputStream inputStream, int eventId) throws Exception {

    try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
      String[] headers = reader.readNext(); // Read header row
      String[] line;
      ObjectMapper mapper = new ObjectMapper(); // Initialize mapper once

      while ((line = reader.readNext()) != null) {
        String unstopTeamId = line[0]; // Assuming Col 0 is ID
        String teamName = line[1]; // Assuming Col 1 is Team Name

        // Map all other CSV columns to a Map for JSONB storage
        Map<String, String> extraData = new HashMap<>();
        for (int i = 2; i < headers.length; i++) {
          extraData.put(headers[i], line[i]);
        }
        String memberDetails = mapper.writeValueAsString(extraData);

        // Check if existing
        java.util.Optional<Registration> existingOpt = repo.findByEventIdAndUnstopTeamId(eventId, unstopTeamId);

        Registration reg;
        if (existingOpt.isPresent()) {
          // Update existing record
          reg = existingOpt.get();
          reg.setTeamName(teamName);
          reg.setMemberDetails(memberDetails);
          // We intentionally don't update the passcode so users aren't locked out
        } else {
          // Create new Registration object
          reg = new Registration();
          reg.setEventId(eventId);
          reg.setUnstopTeamId(unstopTeamId);
          reg.setTeamName(teamName);
          reg.setTeamPasscode(generatePasscode(6));
          reg.setMemberDetails(memberDetails);
        }

        // Save to DB
        repo.save(reg);
      }
    }
  }

  private String generatePasscode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}
