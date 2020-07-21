package net.friedl.fling.service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.model.mapper.ArtifactMapper;
import net.friedl.fling.model.mapper.FlingMapper;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.service.archive.ArchiveService;

@Slf4j
@Service
@Transactional
public class FlingService {

  private FlingRepository flingRepository;
  private FlingMapper flingMapper;
  private ArtifactMapper artifactMapper;
  private ArchiveService archiveService;
  private PasswordEncoder passwordEncoder;

  @Autowired
  public FlingService(FlingRepository flingRepository, FlingMapper flingMapper,
      ArtifactMapper artifactMapper,
      ArchiveService archiveService, PasswordEncoder passwordEcoder) {

    this.flingRepository = flingRepository;
    this.flingMapper = flingMapper;
    this.artifactMapper = artifactMapper;
    this.archiveService = archiveService;
    this.passwordEncoder = passwordEcoder;
  }

  /**
   * Retrieves a list of all flings
   * 
   * @return A list of all flings
   */
  public List<FlingDto> findAll() {
    return flingMapper.mapEntities(flingRepository.findAll());
  }

  /**
   * Get a fling by id
   * 
   * @param id Id of the fling. Must exist.
   * @return The fling
   */
  public FlingDto getById(UUID id) {
    return flingMapper.map(flingRepository.getOne(id));
  }

  /**
   * Creates a new fling entity from {@code flingDto}
   * 
   * @param flingDto Base data from which the new fling should be created
   * @return The created fling
   */
  public FlingDto create(FlingDto flingDto) {
    log.debug("Creating new fling");
    FlingEntity flingEntity = flingMapper.map(flingDto);

    if (!StringUtils.hasText(flingEntity.getShareId())) {
      log.debug("No share id set. Generating random share id");
      flingEntity.setShareId(generateShareId());
    }

    if (StringUtils.hasText(flingEntity.getAuthCode())) {
      log.debug("Hashing authentication code for {}", flingEntity.getId());
      flingEntity.setAuthCode(hashAuthCode(flingDto.getAuthCode()));
    }

    flingEntity = flingRepository.save(flingEntity);
    log.debug("Created new fling {}", flingEntity.getId());
    return flingMapper.map(flingEntity);
  }

  public FlingDto getByShareId(String shareId) {
    FlingEntity flingEntity = flingRepository.findByShareId(shareId);
    if (flingEntity == null) {
      throw new EntityNotFoundException("No entity for shareId=" + shareId);
    }
    return flingMapper.map(flingEntity);
  }

  public void delete(UUID id) throws IOException {
    archiveService.deleteFling(id);
    flingRepository.deleteById(id);
    log.debug("Deleted fling {}", id);
  }

  public Set<ArtifactDto> getArtifacts(UUID id) {
    FlingEntity flingEntity = flingRepository.getOne(id);
    Set<ArtifactDto> artifactDto = artifactMapper.mapEntities(flingEntity.getArtifacts());
    return artifactDto == null ? Set.of() : artifactDto;
  }

  public boolean validateAuthCode(UUID id, String authCode) {
    FlingEntity flingEntity = flingRepository.getOne(id);
    if (StringUtils.hasText(flingEntity.getAuthCode()) != StringUtils.hasText(authCode)) {
      return false; // only one of them is empty; implicit null safety check
    }

    boolean valid = flingEntity.getAuthCode().equals(hashAuthCode(authCode));
    log.debug("Provided authentication for {} is {} valid", id, valid ? "" : "not");
    return valid;
  }

  private String hashAuthCode(String authCode) {
    String hash = passwordEncoder.encode(authCode);
    log.debug("Hashed authentication code to {}", hash);
    return hash;
  }

  /**
   * Generates a URL safe share id
   * 
   * @return A random URL safe share id
   */
  private String generateShareId() {
    byte[] key = KeyGenerators
        .secureRandom(16)
        .generateKey();

    String shareId = Base64.getUrlEncoder().encodeToString(key)
        // replace all special chars [=-_] in RFC 4648
        // "URL and Filename safe" table with characters from
        // [A-Za-z0-9]. Hence, the generated share url will only consist
        // of [A-Za-z0-9].
        .replace('=', 'q')
        .replace('_', 'u')
        .replace('-', 'd');

    log.debug("Generated share id {}", shareId);
    return shareId;
  }
}
