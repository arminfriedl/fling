package net.friedl.fling.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.security.authentication.FlingAdminAuthority;
import net.friedl.fling.security.authentication.FlingToken;
import net.friedl.fling.security.authentication.FlingUserAuthority;

@ExtendWith(SpringExtension.class)
public class AuthorizationServiceTest {

  @Autowired
  private AuthorizationService authorizationService;

  @MockBean
  private FlingRepository flingRepository;

  @TestConfiguration
  static class FlingServiceTestConfiguration {
    @Bean
    public AuthorizationService authorizationService(FlingRepository flingRepository) {
      return new AuthorizationService(flingRepository);
    }
  }

  @Test
  public void allowUpload_unknownToken_false() {
    var unkownToken = new AnonymousAuthenticationToken("key", "principal",
        List.of(new SimpleGrantedAuthority("role")));

    assertFalse(authorizationService.allowUpload(UUID.randomUUID(), unkownToken));
  }

  @Test
  public void allowUpload_flingAdmin_true() {
    FlingToken flingToken = new FlingToken(new FlingAdminAuthority(), "jwtToken");
    assertTrue(authorizationService.allowUpload(UUID.randomUUID(), flingToken));
  }

  @Test
  public void allowUpload_noAdmin_uploadDisallowed_false() {
    FlingEntity flingEntity = new FlingEntity();
    flingEntity.setAllowUpload(false);

    FlingToken flingToken = new FlingToken(new FlingUserAuthority(new UUID(0, 0)), "jwtToken");

    when(flingRepository.getOne(new UUID(0, 0))).thenReturn(flingEntity);

    assertFalse(authorizationService.allowUpload(new UUID(0, 0), flingToken));
  }

  @Test
  public void allowUpload_noAdmin_uploadAllowed_notAuthorized_false() {
    FlingEntity flingEntity = new FlingEntity();
    flingEntity.setAllowUpload(true);

    FlingToken flingToken = new FlingToken(new FlingUserAuthority(new UUID(0, 0)), "jwtToken");

    when(flingRepository.getOne(new UUID(1, 1))).thenReturn(flingEntity);

    // Token: UUID(0,0), Request: UUID(1,1)
    assertFalse(authorizationService.allowUpload(new UUID(1, 1), flingToken));
  }

  @Test
  public void allowUpload_noAdmin_uploadAllowed_authorized_true() {
    FlingEntity flingEntity = new FlingEntity();
    flingEntity.setAllowUpload(true);

    FlingToken flingToken = new FlingToken(new FlingUserAuthority(new UUID(0, 0)), "jwtToken");

    when(flingRepository.getOne(new UUID(0, 0))).thenReturn(flingEntity);

    // Token: UUID(0,0), Request: UUID(0,0)
    assertTrue(authorizationService.allowUpload(new UUID(0, 0), flingToken));
  }

  @Test
  public void allowFlingAccess_unknownToken_false() {
    var unkownToken = new AnonymousAuthenticationToken("key", "principal",
        List.of(new SimpleGrantedAuthority("role")));
    assertFalse(authorizationService.allowFlingAccess(UUID.randomUUID(), unkownToken));
  }

  @Test
  public void allowFlingAcess_flingAdmin_true() {
    FlingToken flingToken = new FlingToken(new FlingAdminAuthority(), "jwtToken");
    assertTrue(authorizationService.allowFlingAccess(UUID.randomUUID(), flingToken));
  }

  @Test
  public void allowFlingAcess_flingUser_notAuthorizedForId_false() {
    FlingToken flingToken = new FlingToken(new FlingUserAuthority(new UUID(0, 0)), "jwtToken");
    assertFalse(authorizationService.allowFlingAccess(new UUID(1, 1), flingToken));
  }

  @Test
  public void allowFlingAcess_flingUser_authorizedForId_true() {
    FlingToken flingToken = new FlingToken(new FlingUserAuthority(new UUID(0, 0)), "jwtToken");
    assertTrue(authorizationService.allowFlingAccess(new UUID(0, 0), flingToken));
  }
}
