package com.adnakiwoch.platform.streaming_api.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private @Getter @Setter UUID id;

  @Column(name = "name")
  private @Getter @Setter String userName;

  @Column(name = "email")
  private @Getter @Setter String email;

  @Column(name = "password")
  private @Getter @Setter String password;

  @OneToOne(mappedBy = "user")
  private @Getter @Setter Subscription subscription;

  @Column(name = "is_studio")
  private @Getter @Setter Boolean isStudio = false;

  @Column(name = "created_date")
  private @Getter @Setter OffsetDateTime createdDate = OffsetDateTime.now();
}
