package com.adnakiwoch.platform.streaming_api.repository;

import com.adnakiwoch.platform.streaming_api.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

@Service
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByUserName(String userName);

  Optional<User> findByEmail(String useName);

  @Query(value = "SELECT COUNT(u)>0 FROM User u WHERE u.email = :email")
  boolean checkUserExistByEmail(String email);

  User getUserById(UUID id);

  UUID id(UUID id);
}
