package net.friedl.fling.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.security.Key;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
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
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.security.authentication.FlingAdminAuthority;
import net.friedl.fling.security.authentication.FlingToken;
import net.friedl.fling.security.authentication.FlingUserAuthority;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:/application-test.properties")
@ActiveProfiles("test")
public class AuthenticationServiceTest {
  @Autowired
  public AuthenticationService authenticationService;

  @MockBean
  private FlingRepository flingRepository;

  @MockBean
  private JwtParser jwtParser;

  @MockBean
  private PasswordEncoder passwordEncoder;

  @TestConfiguration
  static class FlingServiceTestConfiguration {
    private Key jwtSigningKey = Keys.hmacShaKeyFor(new byte[32]);

    @Bean
    public AuthenticationService authenticationService(JwtParser jwtParser,
        PasswordEncoder passwordEncoder, FlingRepository flingRepository) {
      return new AuthenticationService(jwtParser, jwtSigningKey, passwordEncoder, flingRepository);
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

    assertThrows(EntityNotFoundException.class, () -> authenticationService.authenticate(userAuthDto));
  }

  @Test
  public void parseAuthentication_owner_AdminAuthority() {
    Jws<Claims> jwsClaims = new DefaultJws<>(new DefaultJwsHeader(),
        new DefaultClaims(Map.of("sub", "admin")), "signature");
    when(jwtParser.parseClaimsJws(any(String.class))).thenReturn(jwsClaims);

    FlingToken flingToken = authenticationService.parseAuthentication("any");
    assertThat(flingToken.isAuthenticated(), equalTo(true));
    // authorized for any fling
    assertThat(flingToken.authorizedForFling(UUID.randomUUID()), equalTo(true));
    assertThat(flingToken.getCredentials(), equalTo("any"));
    assertThat(flingToken.getAuthorities(),
        hasItem(org.hamcrest.Matchers.any(FlingAdminAuthority.class)));
  }

  @Test
  public void parseAuthentication_user_UserAuthorityForId() {
    Jws<Claims> jwsClaims = new DefaultJws<>(new DefaultJwsHeader(),
        new DefaultClaims(Map.of("sub", "user", "id", new UUID(0, 0).toString())), "signature");
    when(jwtParser.parseClaimsJws(any(String.class))).thenReturn(jwsClaims);

    FlingToken flingToken = authenticationService.parseAuthentication("any");
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
  public void parseAuthentication_unknownSubject_throws() {
    Jws<Claims> jwsClaims = new DefaultJws<>(new DefaultJwsHeader(),
        new DefaultClaims(Map.of("sub", "unknownSubject")), "signature");
    when(jwtParser.parseClaimsJws(any(String.class))).thenReturn(jwsClaims);

    assertThrows(BadCredentialsException.class,
        () -> authenticationService.parseAuthentication("any"));
  }
}
