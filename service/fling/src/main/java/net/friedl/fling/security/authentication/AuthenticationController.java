package net.friedl.fling.security.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import net.friedl.fling.security.authentication.dto.OwnerAuthDto;
import net.friedl.fling.security.authentication.dto.UserAuthDto;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

  private AuthenticationService authenticationService;

  @Autowired
  public AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/auth/owner")
  public String authenticateOwner(@RequestBody OwnerAuthDto ownerAuthDto) {
    return authenticationService.authenticate(ownerAuthDto);
  }

  @PostMapping("/auth/user")
  public String authenticateUser(@RequestBody UserAuthDto userAuthDto) {
    return authenticationService.authenticate(userAuthDto);
  }
}
