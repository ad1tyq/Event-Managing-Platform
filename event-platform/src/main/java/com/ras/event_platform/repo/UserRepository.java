package com.ras.event_platform.repo;

import com.ras.event_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  // The Admin Login query
  @Query("SELECT u FROM User u WHERE u.username = :userName AND u.passwordHash = :password")
  Optional<User> findByNameAndPass(String userName, String password);
}
