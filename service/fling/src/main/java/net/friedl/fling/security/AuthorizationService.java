package net.friedl.fling.security;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.security.authentication.FlingToken;
import net.friedl.fling.security.authentication.dto.UserAuthDto;
import net.friedl.fling.service.FlingService;

@Slf4j
@Service
public class AuthorizationService {
  private FlingService flingService;

  @Autowired
  public AuthorizationService(FlingService flingService) {
    this.flingService = flingService;
  }

  public boolean allowUpload(UUID flingId, AbstractAuthenticationToken token) {
    if (!(token instanceof FlingToken)) {
      log.warn("Authorization attempt without fling token for {}. Authorization denied.", flingId);
      return false;
    }

    FlingToken flingToken = (FlingToken) token;
    if (FlingAuthority.FLING_OWNER.name()
        .equals(flingToken.getGrantedFlingAuthority().getAuthority())) {
      log.debug("Owner authorized for upload [id = {}]", flingId);
      return true;
    }

    boolean uploadAllowed = flingService.getById(flingId).getAllowUpload();
    boolean authorized = uploadAllowed
        && flingToken.getGrantedFlingAuthority().getFlingId().equals(flingId);

    log.debug("User {} authorized for upload [id = {}]", authorized ? "" : "not", flingId);

    return authorized;
  }

  public boolean allowFlingAccess(UUID flingId, AbstractAuthenticationToken token) {
    if (!(token instanceof FlingToken)) {
      log.warn("Authorization attempt without fling token for {}. Authorization denied.", flingId);
      return false;
    }

    FlingToken flingToken = (FlingToken) token;
    if (FlingAuthority.FLING_OWNER.name()
        .equals(flingToken.getGrantedFlingAuthority().getAuthority())) {
      log.debug("Owner authorized for fling access [id = {}]", flingId);
      return true;
    }

    boolean authorized = flingToken.getGrantedFlingAuthority().getFlingId().equals(flingId);
    log.debug("User {} authorized for fling access [id = {}]", authorized ? "" : "not", flingId);

    return authorized;
  }

  public boolean allowFlingAccess(UserAuthDto userAuth, String shareId) {
    boolean authorized = userAuth.getShareId().equals(shareId);
    log.debug("User {} authorized for fling access [shareId = {}]", authorized ? "" : "not",
        shareId);

    return authorized;
  }

}
