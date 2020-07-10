package net.friedl.fling.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.friedl.fling.model.dto.AdminAuthDto;
import net.friedl.fling.model.dto.UserAuthDto;
import net.friedl.fling.service.AuthenticationService;
import net.friedl.fling.service.AuthorizationService;

@WebMvcTest(controllers = AuthenticationController.class,
    includeFilters = {@Filter(Configuration.class)})
@ActiveProfiles("local")
public class AuthenticationControllerTest {
  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthenticationService authenticationService;

  @MockBean
  private AuthorizationService authorizationService;

  @Test
  public void authenticateOwner_noToken_403() throws Exception {
    AdminAuthDto adminAuthDto = new AdminAuthDto("admin", "123");
    when(authenticationService.authenticate(any(AdminAuthDto.class))).thenReturn(Optional.empty());

    mvc.perform(post("/api/auth/admin")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(adminAuthDto)))
        .andExpect(status().is(403));
  }

  @Test
  public void authenticateOwner_token_ok() throws Exception {
    AdminAuthDto adminAuthDto = new AdminAuthDto("admin", "123");
    when(authenticationService.authenticate(any(AdminAuthDto.class)))
        .thenReturn(Optional.of("token"));

    mvc.perform(post("/api/auth/admin")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(adminAuthDto)))
        .andExpect(status().is(200))
        .andExpect(content().string("token"));
  }

  @Test
  public void authenticateUser_noToken_403() throws Exception {
    UserAuthDto userAuthDto = new UserAuthDto("shareId", "authCode");
    when(authenticationService.authenticate(any(UserAuthDto.class))).thenReturn(Optional.empty());

    mvc.perform(post("/api/auth/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userAuthDto)))
        .andExpect(status().is(403));
  }

  @Test
  public void authenticateUser_token_ok() throws Exception {
    UserAuthDto userAuthDto = new UserAuthDto("shareId", "authCode");
    when(authenticationService.authenticate(any(UserAuthDto.class)))
        .thenReturn(Optional.of("token"));

    mvc.perform(post("/api/auth/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userAuthDto)))
        .andExpect(status().is(200))
        .andExpect(content().string("token"));
  }
}
