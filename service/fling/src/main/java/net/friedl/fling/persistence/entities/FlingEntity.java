package net.friedl.fling.persistence.entities;

import java.time.Instant;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Fling")
@Getter
@Setter
public class FlingEntity {
  @Id
  @GeneratedValue
  private Long id;

  private String name;

  private Instant creationTime;

  private Instant expirationTime;

  private Integer expirationClicks;

  @Column(nullable = false)
  private Boolean directDownload;

  @Column(nullable = false)
  private Boolean allowUpload;

  @Column(nullable = false)
  private Boolean shared;

  @Column(unique = true, nullable = false)
  private String shareUrl;

  private String authCode;

  @OneToMany(mappedBy = "fling", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ArtifactEntity> artifacts;

  @PrePersist
  private void prePersist() {
    if (this.directDownload == null)
      this.directDownload = false;
    if (this.allowUpload == null)
      this.allowUpload = false;
    if (this.shared == null)
      this.shared = true;

    this.creationTime = Instant.now();
  }

  @PostPersist
  private void postPersist() {
    System.out.println("ID: " + this.id);
    System.out.println("Share Url: " + this.shareUrl);

    this.shareUrl = this.id + this.shareUrl;
  }
}
