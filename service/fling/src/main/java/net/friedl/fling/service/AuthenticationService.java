package net.friedl.fling.service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.model.dto.AdminAuthDto;
import net.friedl.fling.model.dto.UserAuthDto;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.security.authentication.FlingAdminAuthority;
import net.friedl.fling.security.authentication.FlingToken;
import net.friedl.fling.security.authentication.FlingUserAuthority;

@Slf4j
@Service
public class AuthenticationService {
  private JwtParser jwtParser;
  private Key jwtSigningKey;
  private FlingRepository flingRepository;
  private PasswordEncoder passwordEncoder;

  @Value("${fling.security.admin-name}")
  private String adminName;
  @Value("${fling.security.admin-password}")
  private String adminPassword;
  @Value("${fling.security.jwt-expiration}")
  private Long jwtExpiration;

  @Autowired
  public AuthenticationService(JwtParser jwtParser, Key jwtSigningKey,
      PasswordEncoder passwordEncoder, FlingRepository flingRepository) {

    this.jwtParser = jwtParser;
    this.jwtSigningKey = jwtSigningKey;
    this.passwordEncoder = passwordEncoder;
    this.flingRepository = flingRepository;
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
    String providedAuthCodeHash = passwordEncoder.encode(userAuth.getAuthCode());
    String actualAuthCodeHash = flingEntity.getAuthCode();

    if (!actualAuthCodeHash.equals(providedAuthCodeHash)) {
      log.debug("Authentication failed for fling [.shareId={}]", userAuth.getShareId());
      return Optional.empty();
    }

    log.debug("Authentication successful for fling [.shareId={}]", userAuth.getShareId());
    return Optional.of(
        getJwtBuilder()
            .setSubject("user")
            .claim("id", flingEntity.getId())
            .compact());

  }

  public FlingToken parseAuthentication(String token) {
    Claims claims = jwtParser.parseClaimsJws(token).getBody();

    switch (claims.getSubject()) {
      case "admin":
        return new FlingToken(new FlingAdminAuthority(), token);
      case "user":
        UUID grantedFlingId = UUID.fromString(claims.get("id", String.class));
        return new FlingToken(new FlingUserAuthority(grantedFlingId), token);
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
}
