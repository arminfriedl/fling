package net.friedl.fling.service;

import java.util.UUID;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.model.mapper.ArtifactMapper;
import net.friedl.fling.persistence.entities.ArtifactEntity;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.ArtifactRepository;
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.service.archive.ArchiveService;

@Slf4j
@Service
@Transactional
public class ArtifactService {

  private ArtifactRepository artifactRepository;
  private FlingRepository flingRepository;
  private ArtifactMapper artifactMapper;
  private ArchiveService archiveService;

  @Autowired
  public ArtifactService(ArtifactRepository artifactRepository, FlingRepository flingRepository,
      ArtifactMapper artifactMapper, ArchiveService archiveService) {

    this.artifactRepository = artifactRepository;
    this.flingRepository = flingRepository;
    this.artifactMapper = artifactMapper;
    this.archiveService = archiveService;
  }

  /**
   * Fetch an {@link ArtifactDto} by id. Must be called with a valid artifact id, otherwise bails
   * out with a {@link RuntimeException}. Synchronization must be done on client side.
   * 
   * @param id A valid {@link UUID} for an existing entity in the database. Not null.
   * @return The ArtifactDto corresponding to the {@code id}
   */
  @NotNull
  public ArtifactDto getById(@NotNull UUID id) {
    return artifactMapper.map(artifactRepository.getOne(id));
  }

  /**
   * Create a new {@link ArtifactEntity} from {@code artifactDto} for the fling {@code flingId}.
   * 
   * @param flingId Id of an existing {@link FlingEntity}
   * @param artifactDto The data for the new {@link ArtifactEntity}
   * @return The newly created artifact
   */
  public ArtifactDto create(UUID flingId, ArtifactDto artifactDto) {
    FlingEntity flingEntity = flingRepository.getOne(flingId);

    ArtifactEntity artifactEntity = artifactMapper.map(artifactDto);
    artifactEntity.setFling(flingEntity);
    artifactEntity = artifactRepository.save(artifactEntity);
    return artifactMapper.map(artifactEntity);
  }
 
  /**
   * Deletes an artifact identified by {@code id}. NOOP if the artifact cannot be found.
   * 
   * @param id An {@link UUID} that identifies the artifact
   */
  public void delete(UUID id) {
    if (id == null)
      return;

    ArtifactEntity artifactEntity = artifactRepository.findById(id).orElse(null);

    if (artifactEntity == null) {
      log.warn("Cannot delete artifact {}. Artifact not found.", id);
      return;
    }

    archiveService.deleteArtifact(id);
    artifactRepository.delete(artifactEntity);
    log.info("Deleted artifact {}", artifactEntity);
  }
}
