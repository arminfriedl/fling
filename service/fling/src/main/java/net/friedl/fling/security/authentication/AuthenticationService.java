package net.friedl.fling.security.authentication;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import net.friedl.fling.security.FlingAuthority;
import net.friedl.fling.security.FlingSecurityConfiguration;
import net.friedl.fling.security.authentication.dto.OwnerAuthDto;
import net.friedl.fling.security.authentication.dto.UserAuthDto;
import net.friedl.fling.service.FlingService;

@Service
public class AuthenticationService {
    private FlingService flingService;
    private JwtParser jwtParser;
    private Key signingKey;
    private FlingSecurityConfiguration securityConfig;

    @Autowired
    public AuthenticationService(JwtParser jwtParser, Key signingKey, FlingService flingService,
            FlingSecurityConfiguration securityConfig) {
        this.flingService = flingService;
        this.jwtParser = jwtParser;
        this.signingKey = signingKey;
        this.securityConfig = securityConfig;
    }

    public String authenticate(OwnerAuthDto ownerAuth) {
        if (!securityConfig.getAdminUser().equals(ownerAuth.getUsername())) {
            throw new AccessDeniedException("Wrong credentials");
        }

        if (!securityConfig.getAdminPassword().equals(ownerAuth.getPassword())) {
            throw new AccessDeniedException("Wrong credentials");
        }

        return makeBaseBuilder()
                .setSubject("owner")
                .compact();
    }

    public String authenticate(UserAuthDto userAuth) {
        var fling = flingService.findFlingByShareId(userAuth.getShareId())
                .orElseThrow();
        String authCode = userAuth.getCode();

        if (!flingService.hasAuthCode(fling.getId(), authCode)) {
            throw new AccessDeniedException("Wrong fling code");
        }

        return makeBaseBuilder()
                .setSubject("user")
                .claim("sid", fling.getShareUrl())
                .compact();

    }

    public Authentication parseAuthentication(String token) {
        Claims claims = parseClaims(token);

        FlingAuthority authority;
        Long flingId;

        switch (claims.getSubject()) {
        case "owner":
            authority = FlingAuthority.FLING_OWNER;
            flingId = null;
            break;
        case "user":
            authority = FlingAuthority.FLING_USER;
            var sid = claims.get("sid", String.class);
            flingId = flingService.findFlingByShareId(sid).orElseThrow().getId();
            break;
        default:
            throw new BadCredentialsException("Invalid token");
        }

        return new FlingToken(new GrantedFlingAuthority(authority, flingId));
    }

    private JwtBuilder makeBaseBuilder() {
        return Jwts.builder()
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(securityConfig.getJwtExpiration())))
                .signWith(signingKey);
    }

    private Claims parseClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }
}
