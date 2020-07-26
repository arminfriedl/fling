package net.friedl.fling.security.authentication.authorities;

import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import net.friedl.fling.security.FlingAuthorities;

public class FlingUserAuthority implements GrantedAuthority {
  private static final long serialVersionUID = -1814514234042184275L;

  private UUID flingId;

  public FlingUserAuthority(UUID flingId) {
    this.flingId = flingId;
  }

  @Override
  public String getAuthority() {
    return FlingAuthorities.FLING_USER.getAuthority();
  }

  public UUID getFlingId() {
    return flingId;
  }

}
