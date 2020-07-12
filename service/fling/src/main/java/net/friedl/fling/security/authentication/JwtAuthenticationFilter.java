package net.friedl.fling.security.authentication;

import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.service.AuthenticationService;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String TOKEN_PREFIX = "Bearer ";
  private static final String HEADER_STRING = "Authorization";

  private AuthenticationService authenticationService;

  @Autowired
  public JwtAuthenticationFilter(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader(HEADER_STRING);

    if (header == null || !header.startsWith(TOKEN_PREFIX)) {
      log.info("Anonymous request for {} {}{}", request.getMethod(), request.getRequestURL(),
          request.getQueryString() != null ? "?"+request.getQueryString(): "");
      filterChain.doFilter(request, response);
      return;
    }

    String authToken = header.replace(TOKEN_PREFIX, "");

    SecurityContext securityContext = SecurityContextHolder.getContext();

    if (securityContext.getAuthentication() == null) {
      log.info("Authenticating request for {} {}{}", request.getMethod(), request.getRequestURL(),
          request.getQueryString() != null ? "?"+request.getQueryString(): "");
      FlingToken token = authenticationService.parseAuthentication(authToken);
      log.info("Authenticated as {}", token.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")));
      securityContext.setAuthentication(token);
    }

    filterChain.doFilter(request, response);
  }
}
