package net.friedl.fling.persistence.entities;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Artifact")
@Getter
@Setter
public class ArtifactEntity {
  @Id
  @GeneratedValue
  private Long id;

  private String name;

  private Integer version;

  private String path;

  @Column(unique = true)
  private String doi;

  private Instant uploadTime;

  private Long size;

  @ManyToOne(optional = false)
  private FlingEntity fling;

  @PrePersist
  private void prePersist() {
    this.uploadTime = Instant.now();

    if (this.version == null)
      this.version = -1;
  }
}
