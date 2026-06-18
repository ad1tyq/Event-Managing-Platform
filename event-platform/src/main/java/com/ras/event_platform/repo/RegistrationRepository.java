package com.ras.event_platform.repo;

import com.ras.event_platform.model.*;
// import com.ras.event_platform.dto.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/*import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;*/
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {
  // The "Bouncer" query for our Import logic
  Optional<Registration> findByEventIdAndUnstopTeamId(Integer eventId, String unstopTeamId);

  // The Login query
  @Query("SELECT r FROM Registration r WHERE r.teamName = :teamName AND r.teamPasscode = :passcode")
  Optional<Registration> findByNameAndPass(String teamName, String passcode);

  @Query("SELECT new map(r.id as id, r.teamName as teamName, r.totalScore as totalScore) FROM Registration r ORDER BY r.totalScore DESC")
  java.util.List<java.util.Map<String, Object>> getLeaderboard();
}

/*
 * import javax.sql.DataSource;
 * 
 * public class RegistrationRepository {
 * private final DataSource dataSource;
 * 
 * public RegistrationRepository(DataSource dataSource) {
 * this.dataSource = dataSource;
 * }
 * 
 * public Optional<Registration> findByNameAndPass(String teamName, String
 * passcode) {
 * String sql =
 * "SELECT * FROM registrations WHERE team_name = ? AND team_passcode = ?";
 * 
 * try (Connection conn = dataSource.getConnection();
 * PreparedStatement ps = conn.prepareStatement(sql)) {
 * ps.setString(1, teamName);
 * ps.setString(2, passcode);
 * 
 * try (ResultSet rs = ps.executeQuery()) {
 * if (rs.next()) {
 * Registration reg = new Registration();
 * reg.setId(rs.getObject("id", UUID.class));
 * reg.setTeamName(rs.getString("team_name"));
 * // You manually map every single column here
 * return Optional.of(reg);
 * }
 * }
 * } catch (SQLException e) {
 * throw new RuntimeException("Database error", e);
 * }
 * return Optional.empty();
 * }
 * }
 */
