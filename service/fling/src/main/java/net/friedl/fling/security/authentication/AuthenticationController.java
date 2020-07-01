package net.friedl.fling.security.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.friedl.fling.security.authentication.dto.OwnerAuthDto;
import net.friedl.fling.security.authentication.dto.UserAuthDto;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth", description = "Operations on /api/auth")
public class AuthenticationController {

  private AuthenticationService authenticationService;

  @Autowired
  public AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping(path = "/owner")
  public String authenticateOwner(@RequestBody OwnerAuthDto ownerAuthDto) {
    return authenticationService.authenticate(ownerAuthDto);
  }

  @PostMapping("/user")
  public String authenticateUser(@RequestBody UserAuthDto userAuthDto) {
    return authenticationService.authenticate(userAuthDto);
  }
}
