package net.friedl.fling.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public enum FlingAuthorities {
  FLING_ADMIN("admin"), FLING_USER("user"), FLING_TOKEN("token");

  String authority;

  FlingAuthorities(String authority) {
    this.authority = authority;
  }

  public boolean verify(String authority) {
    return this.authority.equals(authority);
  }

  public boolean verify(AbstractAuthenticationToken authenticationToken) {
    return authenticationToken.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(this.authority::equals);
  }

  public boolean verify(GrantedAuthority grantedAuthority) {
    return this.authority.equals(grantedAuthority.getAuthority());
  }

  public String getAuthority() {
    return authority;
  }
}
