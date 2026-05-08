package com.adnakiwoch.platform.streaming_api.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription")
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private @Getter @Setter Long id;

  @OneToOne
  @JoinColumn(name = "user_id")
  private @Getter @Setter User user;

  @Column(name = "user_subscription_id")
  private @Getter @Setter UUID userSubscriptionId;

  @Column(name = "last_updated")
  private @Getter @Setter OffsetDateTime lastUpdated;

  @Column(name = "status")
  private @Getter @Setter Boolean status = false;

  @Column(name = "version")
  private @Getter @Setter int version;
}
