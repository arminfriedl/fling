package net.friedl.fling.persistence.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import net.friedl.fling.persistence.entities.ArtifactEntity;

public interface ArtifactRepository extends JpaRepository<ArtifactEntity, UUID> {
  List<ArtifactEntity> findAllByFlingId(UUID flingId);
}
