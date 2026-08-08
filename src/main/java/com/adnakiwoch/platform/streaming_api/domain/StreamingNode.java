package com.adnakiwoch.platform.streaming_api.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "streaming_node")
public class StreamingNode {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private @Getter @Setter UUID id;

  @Column(name = "ip_addr")
  private @Getter @Setter String ipAddr;

  @Column(name = "port_number")
  private @Getter @Setter int portNumber;

  @Column(name = "up_stat")
  private @Getter @Setter boolean upStat;

  @Column(name = "updated_at")
  private @Getter @Setter OffsetDateTime updatedAt = OffsetDateTime.now();
}
