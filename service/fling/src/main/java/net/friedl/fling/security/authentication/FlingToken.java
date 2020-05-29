package net.friedl.fling.security.authentication;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class FlingToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -1112423505610346583L;
    private GrantedFlingAuthority grantedFlingAuthority;

    public FlingToken(GrantedFlingAuthority authority) {
        super(List.of(authority));
        this.grantedFlingAuthority = authority;
    }

    public GrantedFlingAuthority getGrantedFlingAuthority() {
        return this.grantedFlingAuthority;
    }

    @Override
    public Object getCredentials() {
        return null;
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
