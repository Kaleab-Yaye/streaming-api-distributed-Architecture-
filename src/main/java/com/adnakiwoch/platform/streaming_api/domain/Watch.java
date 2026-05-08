package com.adnakiwoch.platform.streaming_api.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "watches")
public class Watch {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private @Getter @Setter UUID id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private @Getter @Setter User user;

  @ManyToOne
  @JoinColumn(name = "vid_id")
  private @Getter @Setter Vid vid;

  @Column(name = "current_frame")
  private @Getter @Setter Double currentFrame;

  @Column(name = "liked")
  private @Getter @Setter boolean liked;

  @Column(name = "times_rewatch")
  private @Getter @Setter int timesRewatch;
}
