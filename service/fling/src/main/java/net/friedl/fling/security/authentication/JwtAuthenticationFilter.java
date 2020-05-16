package net.friedl.fling.security.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HEADER_STRING);

        if(header == null || !header.startsWith(TOKEN_PREFIX)) {
            log.warn("Could not find bearer token. No JWT authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        String authToken = header.replace(TOKEN_PREFIX, "");

        SecurityContext securityContext = SecurityContextHolder.getContext();

        if(securityContext.getAuthentication() == null) {
            Authentication authentication = authenticationService.parseAuthentication(authToken);
            securityContext.setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
