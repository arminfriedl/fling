package net.friedl.fling.service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.model.dto.AdminAuthDto;
import net.friedl.fling.model.dto.UserAuthDto;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.entities.TokenEntity;
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.persistence.repositories.TokenRepository;
import net.friedl.fling.security.authentication.FlingToken;
import net.friedl.fling.security.authentication.authorities.FlingAdminAuthority;
import net.friedl.fling.security.authentication.authorities.FlingUserAuthority;

@Slf4j
@Service
public class AuthenticationService {
  private JwtParser jwtParser;
  private Key jwtSigningKey;
  private FlingRepository flingRepository;
  private TokenRepository tokenRepository;
  private PasswordEncoder passwordEncoder;

  @Value("${fling.security.admin-name}")
  private String adminName;
  @Value("${fling.security.admin-password}")
  private String adminPassword;
  @Value("${fling.security.jwt-expiration}")
  private Long jwtExpiration;

  @Autowired
  public AuthenticationService(JwtParser jwtParser, Key jwtSigningKey,
      PasswordEncoder passwordEncoder, FlingRepository flingRepository,
      TokenRepository tokenRepository) {

    this.jwtParser = jwtParser;
    this.jwtSigningKey = jwtSigningKey;
    this.passwordEncoder = passwordEncoder;
    this.flingRepository = flingRepository;
    this.tokenRepository = tokenRepository;
  }

  public Optional<String> authenticate(AdminAuthDto adminAuth) {
    log.info("Authenticating {}", adminAuth.getAdminName());
    if (!adminName.equals(adminAuth.getAdminName())) {
      log.debug("Authentication failed for {}", adminAuth.getAdminName());
      return Optional.empty();
    }

    if (!adminPassword.equals(adminAuth.getAdminPassword())) {
      log.debug("Authentication failed for {}", adminAuth.getAdminName());
      return Optional.empty();
    }

    log.debug("Authentication successful for {}", adminAuth.getAdminName());
    return Optional.of(
        getJwtBuilder()
            .setSubject("admin")
            .compact());
  }

  public Optional<String> authenticate(UserAuthDto userAuth) {
    log.info("Authenticating for fling [.shareId={}]", userAuth.getShareId());
    FlingEntity flingEntity = flingRepository.findByShareId(userAuth.getShareId());
    if (flingEntity == null) {
      throw new EntityNotFoundException("No entity for shareId=" + userAuth.getShareId());
    }

    String providedAuthCode = userAuth.getAuthCode();
    String actualAuthCodeHash = flingEntity.getAuthCode();
    
    Boolean isProtected = StringUtils.hasText(actualAuthCodeHash);

    if(!isProtected) log.debug("No protection set for fling [.shareId={}]");

    if (isProtected && !passwordEncoder.matches(providedAuthCode, actualAuthCodeHash)) {
      log.debug("Authentication failed for fling [.shareId={}]", userAuth.getShareId());
      return Optional.empty();
    }

    log.debug("Authentication successful for fling [.shareId={}]", userAuth.getShareId());
    return Optional.of(
        getJwtBuilder()
            .setSubject("user")
            .claim("shareId", flingEntity.getShareId())
            .claim("id", flingEntity.getId())
            .compact());

  }

  public FlingToken parseJwtAuthentication(String token) {
    Claims claims = jwtParser.parseClaimsJws(token).getBody();

    switch (claims.getSubject()) {
      case "admin":
        return new FlingToken(List.of(new FlingAdminAuthority()), token);
      case "user":
        UUID grantedFlingId = UUID.fromString(claims.get("id", String.class));
        return new FlingToken(List.of(new FlingUserAuthority(grantedFlingId)), token);
      default:
        throw new BadCredentialsException("Invalid token");
    }
  }

  /**
   * Creates a new JwtBuilder. A new builder must be constructed for each JWT creation, because the
   * builder keeps its state.
   * 
   * @return A new JwtBuilder with basic default configuration.
   */
  private JwtBuilder getJwtBuilder() {
    return Jwts.builder()
        .setIssuedAt(Date.from(Instant.now()))
        .setExpiration(Date.from(Instant.now().plusSeconds(jwtExpiration)))
        .signWith(jwtSigningKey);
  }

  /**
   * Creates a derived token with the given settings. Note that the returned string is opaque and
   * should not not be interpreted in any way but only used as is.
   * 
   * @param singleUse Whether this token should be deleted after a single use
   * @return An opaque string representing the token
   */
  @Transactional
  public String deriveToken(Boolean singleUse) {
    UUID id = UUID.randomUUID();
    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setId(id);
    if (singleUse != null) {
      tokenEntity.setSingleUse(singleUse);
    }

    SecurityContext securityContext = SecurityContextHolder.getContext();
    if (securityContext.getAuthentication() instanceof FlingToken) {
      FlingToken flingToken = (FlingToken) securityContext.getAuthentication();
      tokenEntity.setToken(flingToken.getCredentials());
    } else {
      // This should be prevented in FlingWebSecurityConfigurer
      throw new IllegalStateException("Cannot derive token from current authentication");
    }

    tokenRepository.save(tokenEntity);

    return id.toString();
  }

  @Transactional
  public FlingToken parseDerivedToken(String derivedToken) {
    TokenEntity tokenEntity = tokenRepository.getOne(UUID.fromString(derivedToken));

    FlingToken flingToken = parseJwtAuthentication(tokenEntity.getToken());

    if (tokenEntity.getSingleUse()) {
      tokenRepository.delete(tokenEntity);
    }

    return flingToken;
  }
}
