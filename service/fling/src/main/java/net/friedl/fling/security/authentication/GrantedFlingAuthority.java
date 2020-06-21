package net.friedl.fling.security.authentication;

import org.springframework.security.core.GrantedAuthority;
import net.friedl.fling.security.FlingAuthority;

/**
 * Authority granting access to a fling
 *
 * @author Armin Friedl <dev@friedl.net>
 */
public class GrantedFlingAuthority implements GrantedAuthority {

  private static final long serialVersionUID = -1552301479158714777L;

  private FlingAuthority authority;
  private Long flingId;

  public GrantedFlingAuthority(FlingAuthority authority, Long flingId) {
    this.authority = authority;
    this.flingId = flingId;
  }

  public Long getFlingId() {
    return this.flingId;
  }

  @Override
  public String getAuthority() {
    return authority.name();
  }

}
