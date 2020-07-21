package net.friedl.fling.security.authentication;

import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import net.friedl.fling.security.authentication.authorities.FlingAdminAuthority;
import net.friedl.fling.security.authentication.authorities.FlingUserAuthority;

public class FlingToken extends AbstractAuthenticationToken {

  private static final long serialVersionUID = -1112423505610346583L;
  private String token;

  public FlingToken(List<GrantedAuthority> authorities, String token) {
    super(authorities);
    this.token = token;
  }

  public boolean authorizedForFling(UUID id) {
    for (GrantedAuthority grantedAuthority : getAuthorities()) {
      if (grantedAuthority instanceof FlingAdminAuthority) {
        return true;
      }

      if (grantedAuthority instanceof FlingUserAuthority) {
        UUID grantedFlingId = ((FlingUserAuthority) grantedAuthority).getFlingId();
        if (grantedFlingId.equals(id)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public String getCredentials() {
    return this.token;
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
