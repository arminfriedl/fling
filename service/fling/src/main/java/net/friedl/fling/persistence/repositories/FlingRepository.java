package net.friedl.fling.persistence.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import net.friedl.fling.persistence.entities.FlingEntity;

public interface FlingRepository extends JpaRepository<FlingEntity, UUID> {
  Optional<FlingEntity> findByName(String name);

  FlingEntity findByShareId(String shareId);

  @Query("SELECT fe FROM FlingEntity fe JOIN ArtifactEntity ae ON fe.id=ae.id WHERE ae.id=:artifactId")
  FlingEntity findByArtifactId(UUID artifactId);
}
