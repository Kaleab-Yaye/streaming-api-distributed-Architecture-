package com.adnakiwoch.platform.streaming_api.domain;

import enums.VidStat;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "vid")
public class Vid {
  @Id
  @GeneratedValue(
      strategy =
          GenerationType
              .UUID) // without this thing, hibernate wont know to submit a null of this type,
  // and the data abse can handel it
  @Getter
  @Setter
  private UUID id;

  @Column(name = "name")
  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  @Column(name = "about")
  private String about;

  @Getter
  @Setter
  @Column(name = "uploaded_at")
  private OffsetDateTime uploadedAt = OffsetDateTime.now();

  @Column(name = "vid_stat")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Getter
  @Setter
  private VidStat vidStat = VidStat.UPLOADREQ;

  @Column(name = "upload_location")
  private @Getter @Setter String uploadLocation = "NOT_SPECIFIED";

  @Column(name = "encoded_location")
  private @Getter @Setter String encodedLocation = "NOT_SPECIFIED";

  @Column(name = "length")
  private @Getter @Setter Double length;

  @Column(name = "size")
  private @Getter @Setter Long size;

  @ManyToOne()
  @JoinColumn(name = "uploaded_by")
  private @Getter @Setter User user;

  @Column(name = "present")
  private @Getter @Setter boolean present;
}
