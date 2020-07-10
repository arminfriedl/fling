package net.friedl.fling.security.authentication;

import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class FlingToken extends AbstractAuthenticationToken {

  private static final long serialVersionUID = -1112423505610346583L;
  private String jwtToken;

  public FlingToken(GrantedAuthority authority, String jwtToken) {
    super(List.of(authority));
    this.jwtToken = jwtToken;
  }

  public boolean authorizedForFling(UUID id) {
    for (GrantedAuthority grantedAuthority : getAuthorities()) {
      if (grantedAuthority instanceof FlingAdminAuthority) return true;

      if (!(grantedAuthority instanceof FlingUserAuthority)) continue;

      UUID grantedFlingId = ((FlingUserAuthority) grantedAuthority).getFlingId();
      if (grantedFlingId.equals(id)) return true;
    }

    return false;
  }

  @Override
  public String getCredentials() {
    return this.jwtToken;
  }

  @Override
  public Object getPrincipal() {
    return null;
  }

  @Override
  public boolean isAuthenticated() {
    return true;
  }
}
