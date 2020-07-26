package net.friedl.fling.security.authentication.authorities;

import org.springframework.security.core.GrantedAuthority;
import net.friedl.fling.security.FlingAuthorities;

public class FlingAdminAuthority implements GrantedAuthority {

  private static final long serialVersionUID = -4605768612393081070L;

  @Override
  public String getAuthority() {
    return FlingAuthorities.FLING_ADMIN.getAuthority();
  }

}
