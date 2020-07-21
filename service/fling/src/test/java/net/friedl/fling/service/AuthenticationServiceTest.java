package net.friedl.fling.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJws;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import io.jsonwebtoken.security.Keys;
import net.friedl.fling.model.dto.AdminAuthDto;
import net.friedl.fling.model.dto.UserAuthDto;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.entities.TokenEntity;
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.persistence.repositories.TokenRepository;
import net.friedl.fling.security.FlingAuthorities;
import net.friedl.fling.security.authentication.FlingToken;
import net.friedl.fling.security.authentication.authorities.FlingAdminAuthority;
import net.friedl.fling.security.authentication.authorities.FlingUserAuthority;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:/application-test.properties")
@ActiveProfiles("test")
public class AuthenticationServiceTest {
  @Autowired
  public AuthenticationService authenticationService;

  @MockBean
  private FlingRepository flingRepository;

  @MockBean
  private TokenRepository tokenRepository;

  @MockBean
  private JwtParser jwtParser;

  @MockBean
  private PasswordEncoder passwordEncoder;

  @TestConfiguration
  static class FlingServiceTestConfiguration {
    private Key jwtSigningKey = Keys.hmacShaKeyFor(new byte[32]);

    @Bean
    public AuthenticationService authenticationService(JwtParser jwtParser,
        PasswordEncoder passwordEncoder, FlingRepository flingRepository,
        TokenRepository tokenRepository) {

      return new AuthenticationService(jwtParser, jwtSigningKey, passwordEncoder, flingRepository,
          tokenRepository);
    }
  }

  @Test
  public void authenticate_adminNameDiffers_empty() {
    AdminAuthDto adminAuthDto = new AdminAuthDto("wrongadmin", "123");

    assertThat(authenticationService.authenticate(adminAuthDto), equalTo(Optional.empty()));
  }

  @Test
  public void authenticate_passwordDiffers_empty() {
    AdminAuthDto adminAuthDto = new AdminAuthDto("admin", "wrongpassword");

    assertThat(authenticationService.authenticate(adminAuthDto), equalTo(Optional.empty()));
  }

  @Test
  public void authenticate_ok() {
    AdminAuthDto adminAuthDto = new AdminAuthDto("admin", "123");

    assertThat(authenticationService.authenticate(adminAuthDto), not(equalTo(Optional.empty())));
  }

  @Test
  public void authenticate_authCodeDiffers_empty() {
    FlingEntity flingEntity = new FlingEntity();
    flingEntity.setAuthCode("test");
    flingEntity.setId(UUID.randomUUID());

    UserAuthDto userAuthDto = new UserAuthDto("shareId", "wrongCode");

    when(flingRepository.findByShareId(any(String.class))).thenReturn(flingEntity);
    when(passwordEncoder.encode(any(String.class))).thenReturn("wrongCode");

    assertThat(authenticationService.authenticate(userAuthDto), equalTo(Optional.empty()));
  }

  @Test
  public void authenticate_authCodeEquals_ok() {
    FlingEntity flingEntity = new FlingEntity();
    flingEntity.setAuthCode("authCodeHash");
    flingEntity.setId(UUID.randomUUID());

    UserAuthDto userAuthDto = UserAuthDto.builder()
        .authCode("authCode")
        .shareId("shareId").build();

    when(flingRepository.findByShareId(any(String.class))).thenReturn(flingEntity);
    when(passwordEncoder.encode(any(String.class))).thenReturn("authCodeHash");

    assertThat(authenticationService.authenticate(userAuthDto), not(equalTo(Optional.empty())));
  }

  @Test
  public void authenticate_noFlingForShareId_throws() {
    UserAuthDto userAuthDto = UserAuthDto.builder()
        .authCode("authCode")
        .shareId("doesNotExist").build();

    when(flingRepository.findByShareId(any(String.class))).thenReturn(null);
    when(passwordEncoder.encode(any(String.class))).thenReturn("authCodeHash");

    assertThrows(EntityNotFoundException.class,
        () -> authenticationService.authenticate(userAuthDto));
  }

  @Test
  public void parseJwtAuthentication_owner_AdminAuthority() {
    Jws<Claims> jwsClaims = new DefaultJws<>(new DefaultJwsHeader(),
        new DefaultClaims(Map.of("sub", "admin")), "signature");
    when(jwtParser.parseClaimsJws(any(String.class))).thenReturn(jwsClaims);

    FlingToken flingToken = authenticationService.parseJwtAuthentication("any");
    assertThat(flingToken.isAuthenticated(), equalTo(true));
    // authorized for any fling
    assertThat(flingToken.authorizedForFling(UUID.randomUUID()), equalTo(true));
    assertThat(flingToken.getCredentials(), equalTo("any"));
    assertThat(flingToken.getAuthorities(),
        hasItem(org.hamcrest.Matchers.any(FlingAdminAuthority.class)));
  }

  @Test
  public void parseJwtAuthentication_user_UserAuthorityForId() {
    Jws<Claims> jwsClaims = new DefaultJws<>(new DefaultJwsHeader(),
        new DefaultClaims(Map.of("sub", "user", "id", new UUID(0, 0).toString())), "signature");
    when(jwtParser.parseClaimsJws(any(String.class))).thenReturn(jwsClaims);

    FlingToken flingToken = authenticationService.parseJwtAuthentication("any");
    assertThat(flingToken.isAuthenticated(), equalTo(true));
    // authorized for fling in token
    assertThat(flingToken.authorizedForFling(new UUID(0, 0)), equalTo(true));
    // not authorized for fling other flings
    assertThat(flingToken.authorizedForFling(new UUID(0, 1)), equalTo(false));
    assertThat(flingToken.getCredentials(), equalTo("any"));
    assertThat(flingToken.getAuthorities(),
        hasItem(org.hamcrest.Matchers.any(FlingUserAuthority.class)));
  }

  @Test
  public void parseJwtAuthentication_unknownSubject_throws() {
    Jws<Claims> jwsClaims = new DefaultJws<>(new DefaultJwsHeader(),
        new DefaultClaims(Map.of("sub", "unknownSubject")), "signature");
    when(jwtParser.parseClaimsJws(any(String.class))).thenReturn(jwsClaims);

    assertThrows(BadCredentialsException.class,
        () -> authenticationService.parseJwtAuthentication("any"));
  }

  @Test
  public void deriveToken_noAuthenticationInSecurityContext_throws() {
    assertThrows(IllegalStateException.class,
        () -> authenticationService.deriveToken(false));
  }

  @Test
  public void deriveToken_authenticationInSecurityContext_ok() {
    FlingToken flingToken = new FlingToken(List.of(new FlingAdminAuthority()), "token");
    SecurityContextHolder.setContext(new SecurityContextImpl(flingToken));

    String derivedToken = authenticationService.deriveToken(null);

    assertThat(derivedToken, is(not(emptyOrNullString())));
    SecurityContextHolder.clearContext();
  }

  @Test
  public void deriveToken_singleUseNotSet_singleUseIsTrue() {
    FlingToken flingToken = new FlingToken(List.of(new FlingAdminAuthority()), "token");
    SecurityContextHolder.setContext(new SecurityContextImpl(flingToken));

    ArgumentCaptor<TokenEntity> tokenEntityCaptor = ArgumentCaptor.forClass(TokenEntity.class);

    authenticationService.deriveToken(null);

    verify(tokenRepository).save(tokenEntityCaptor.capture());
    assertThat(tokenEntityCaptor.getValue().getSingleUse(), is(true));
  }

  @Test
  public void deriveToken_singleUseFalse_singleUseIsFalse() {
    FlingToken flingToken = new FlingToken(List.of(new FlingAdminAuthority()), "token");
    SecurityContextHolder.setContext(new SecurityContextImpl(flingToken));

    ArgumentCaptor<TokenEntity> tokenEntityCaptor = ArgumentCaptor.forClass(TokenEntity.class);

    authenticationService.deriveToken(false);

    verify(tokenRepository).save(tokenEntityCaptor.capture());
    assertThat(tokenEntityCaptor.getValue().getSingleUse(), is(false));
  }

  @Test
  public void parseDerivedToken_singleUse_deletesToken() {
    String token = UUID.randomUUID().toString();
    Jws<Claims> jwsClaims = new DefaultJws<>(new DefaultJwsHeader(),
        new DefaultClaims(Map.of("sub", "admin")), "signature");
    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setId(UUID.fromString(token));
    tokenEntity.setSingleUse(true);
    tokenEntity.setToken("jwtToken");

    when(jwtParser.parseClaimsJws(any(String.class))).thenReturn(jwsClaims);
    when(tokenRepository.getOne(any(UUID.class))).thenReturn(tokenEntity);

    authenticationService.parseDerivedToken(token);

    verify(tokenRepository).delete(tokenEntity);
  }

  @Test
  public void parseDerivedToken_singleUseFalse_doesNotDeleteToken() {
    String token = UUID.randomUUID().toString();
    Jws<Claims> jwsClaims = new DefaultJws<>(new DefaultJwsHeader(),
        new DefaultClaims(Map.of("sub", "admin")), "signature");
    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setId(UUID.fromString(token));
    tokenEntity.setSingleUse(false);
    tokenEntity.setToken("jwtToken");

    when(jwtParser.parseClaimsJws(any(String.class))).thenReturn(jwsClaims);
    when(tokenRepository.getOne(any(UUID.class))).thenReturn(tokenEntity);

    authenticationService.parseDerivedToken(token);

    verify(tokenRepository, never()).delete(tokenEntity);
  }

  @Test
  public void parseDerivedToken_returnsParentAuthentication() {
    String token = UUID.randomUUID().toString();
    Jws<Claims> jwsClaims = new DefaultJws<>(new DefaultJwsHeader(),
        new DefaultClaims(Map.of("sub", "admin")), "signature");
    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setId(UUID.fromString(token));
    tokenEntity.setSingleUse(true);
    tokenEntity.setToken("jwtToken");

    when(jwtParser.parseClaimsJws(any(String.class))).thenReturn(jwsClaims);
    when(tokenRepository.getOne(any(UUID.class))).thenReturn(tokenEntity);

    FlingToken flingToken = authenticationService.parseDerivedToken(token);

    assertEquals(flingToken.getAuthorities().stream().findFirst().get().getAuthority(),
        FlingAuthorities.FLING_ADMIN.getAuthority());
  }
}
