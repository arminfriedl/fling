package net.friedl.fling.persistence.entities;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Artifact")
@Getter
@Setter
public class ArtifactEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private Path path;

  @Column(unique = true, nullable = true)
  private String archiveId;

  @Column(nullable = false)
  private Boolean archived = false;

  @ManyToOne(optional = false)
  private FlingEntity fling;

  @CreationTimestamp
  private Instant creationTime;

  @UpdateTimestamp
  private Instant updateTime;

  @Version
  private Long version;
}
