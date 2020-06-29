package net.friedl.fling.persistence.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import net.friedl.fling.persistence.entities.FlingEntity;

public interface FlingRepository extends JpaRepository<FlingEntity, UUID> {
  Optional<FlingEntity> findByName(String name);

  FlingEntity findByShareId(String shareId);

  @Query("SELECT COUNT(*) FROM ArtifactEntity a, FlingEntity f where a.fling=f.id and f.id=:flingId")
  Long countArtifactsById(Long flingId);
}
