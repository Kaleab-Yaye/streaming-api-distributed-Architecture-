package com.adnakiwoch.platform.streaming_api.repository;

import com.adnakiwoch.platform.streaming_api.domain.Subscription;
import com.adnakiwoch.platform.streaming_api.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {

  @Query(value = "SELECT s.status FROM Subscription s WHERE s.user = :user")
  Optional<Boolean> getSubStatByUser(User user);
}
