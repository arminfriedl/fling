package net.friedl.fling.persistence.entities;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Fling")
@Getter
@Setter
public class FlingEntity {
  @Id
  @GeneratedValue
  private UUID id;

  private String name;

  private Instant expirationTime;

  private Integer expirationClicks;

  @Column(nullable = false)
  private Boolean directDownload = false;

  @Column(nullable = false)
  private Boolean allowUpload = false;

  @Column(nullable = false)
  private Boolean shared = true;

  @Column(unique = true, nullable = false)
  private String shareId;

  private String authCode;

  @OneToMany(mappedBy = "fling", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ArtifactEntity> artifacts;

  @CreationTimestamp
  private Date creationTime;

  @UpdateTimestamp
  private Date updateTime;

  @Version
  private Long version;
}
