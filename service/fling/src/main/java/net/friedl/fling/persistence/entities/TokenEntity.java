package net.friedl.fling.persistence.entities;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Token")
@Getter
@Setter
public class TokenEntity {
  @Id
  private UUID id; // Note that this is not generated to ensure randomness independent from the
                   // persistence provider

  @Column(nullable = false)
  private Boolean singleUse = true;

  @Column(nullable = false)
  private String token; // JWT token this token is derived from

  @CreationTimestamp
  private Instant creationTime;

  @UpdateTimestamp
  private Instant updateTime;

  @Version
  private Long version;
}
