package net.friedl.fling.persistence.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import net.friedl.fling.persistence.entities.ArtifactEntity;

public interface ArtifactRepository extends JpaRepository<ArtifactEntity, Long> {
  Optional<ArtifactEntity> findByDoi(String doi);

  List<ArtifactEntity> deleteByDoi(String doi);

  List<ArtifactEntity> findAllByFlingId(Long flingId);
}
