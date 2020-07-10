package net.friedl.fling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.friedl.fling.model.dto.AdminAuthDto;
import net.friedl.fling.model.dto.UserAuthDto;
import net.friedl.fling.service.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth", description = "Operations on /api/auth")
@Validated
public class AuthenticationController {

  private AuthenticationService authenticationService;

  @Autowired
  public AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @Operation(description = "Authenticates the fling admin by username and password")
  @ApiResponse(responseCode = "200",
      description = "JWT Token authenticating the admin of this fling instance")
  @ApiResponse(responseCode = "403",
      description = "Authentication failed, username or password are wrong")
  @PostMapping(path = "/admin")
  public String authenticateOwner(@RequestBody AdminAuthDto adminAuthDto) {
    return authenticationService.authenticate(adminAuthDto)
        .orElseThrow(() -> new AccessDeniedException("Wrong username or password"));
  }

  @Operation(description = "Authenticates a fling user for a fling via code")
  @ApiResponse(responseCode = "200",
      description = "JWT Token authenticating the user for a fling")
  @ApiResponse(responseCode = "403",
      description = "Authentication failed, the provided code for the fling is wrong")
  @ApiResponse(responseCode = "404",
      description = "No fling for the given share id exists")
  @PostMapping("/user")
  public String authenticateUser(@RequestBody UserAuthDto userAuthDto) {
    return authenticationService.authenticate(userAuthDto)
        .orElseThrow(() -> new AccessDeniedException("Wrong username or password"));
  }
}
