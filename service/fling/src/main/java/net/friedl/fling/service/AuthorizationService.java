package net.friedl.fling.service;

import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.security.FlingAuthorities;
import net.friedl.fling.security.authentication.FlingToken;

@Slf4j
@Service
public class AuthorizationService {
  private FlingRepository flingRepository;

  @Autowired
  public AuthorizationService(FlingRepository flingRepository) {
    this.flingRepository = flingRepository;
  }

  public boolean allowUpload(UUID flingId, AbstractAuthenticationToken token) {
    if (!(token instanceof FlingToken)) {
      log.debug("Token of type {} not allowed. Authentication denied.", token.getClass());
      return false;
    }

    if (FlingAuthorities.FLING_ADMIN.verify(token)) {
      log.debug("Owner authorized for upload fling[.id={}]", flingId);
      return true;
    }

    if (!flingRepository.getOne(flingId).getAllowUpload()) {
      log.debug("Fling[.id={}] does not not allow uploads");
      return false;
    }

    FlingToken flingToken = (FlingToken) token;
    if (flingToken.authorizedForFling(flingId)) {
      log.debug("User authorized for upload fling[.id={}]", flingId);
      return true;
    }

    log.info("User not authorized for upload fling[.id={}]", flingId);
    return false;
  }

  public boolean allowFlingAccess(UUID flingId, AbstractAuthenticationToken token) {
    if (!(token instanceof FlingToken)) {
      log.debug("Token of type {} not allowed. Authentication denied.", token.getClass());
      return false;
    }

    if (FlingAuthorities.FLING_ADMIN.verify(token)) {
      log.debug("Owner authorized for fling access [id = {}]", flingId);
      return true;
    }

    FlingToken flingToken = (FlingToken) token;
    if (flingToken.authorizedForFling(flingId)) {
      log.debug("User authorized for fling access [id = {}]");
      return true;
    }

    log.info("User not authorized to access fling[.id={}]", flingId);
    return false;
  }

  public boolean allowFlingAccessByShareId(String shareId, AbstractAuthenticationToken token) {
    if (FlingAuthorities.FLING_ADMIN.verify(token)) {
      log.debug("Owner authorized for fling access [shareId = {}]", shareId);
      return true;
    }

    FlingEntity flingEntity = flingRepository.findByShareId(shareId);
    if (flingEntity == null) {
      throw new EntityNotFoundException("No entity for shareId=" + shareId);
    }
    return allowFlingAccess(flingEntity.getId(), token);
  }

  public boolean allowArtifactAccess(UUID artifactId, AbstractAuthenticationToken token) {
    FlingEntity flingEntity = flingRepository.findByArtifactId(artifactId);
    return allowFlingAccess(flingEntity.getId(), token);
  }

  public boolean allowArtifactUpload(UUID artifactId, AbstractAuthenticationToken token) {
    FlingEntity flingEntity = flingRepository.findByArtifactId(artifactId);
    return allowUpload(flingEntity.getId(), token);
  }

}
