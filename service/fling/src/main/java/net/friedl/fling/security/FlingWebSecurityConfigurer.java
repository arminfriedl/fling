package net.friedl.fling.security;

import static org.springframework.security.config.Customizer.withDefaults;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.FlingSecurityConfiguration;
import net.friedl.fling.security.authentication.JwtAuthenticationFilter;
import net.friedl.fling.service.AuthorizationService;

@Slf4j
@Configuration
@EnableWebSecurity
@Getter
@Setter
public class FlingWebSecurityConfigurer extends WebSecurityConfigurerAdapter {
  @Value("fling.security.allowedOrigins")
  private List<String> allowedOrigins;

  private JwtAuthenticationFilter jwtAuthenticationFilter;
  private AuthorizationService authorizationService;

  @Autowired
  public FlingWebSecurityConfigurer(JwtAuthenticationFilter jwtAuthenticationFilter,
      AuthorizationService authorizationService,
      FlingSecurityConfiguration securityConfiguraiton) {

    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.authorizationService = authorizationService;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    //@formatter:off
        http
        .csrf().disable()
        .cors(withDefaults())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        // Everybody can try to authenticate
        .authorizeRequests()
            .antMatchers("/api/auth/**")
            .permitAll()
        .and()
        // We need to go from most specific to more general.
        // Hence, first define user permissions
        .authorizeRequests()
            // TODO: This is still insecure since URLs are not encrypted
            // TODO: iframe requests don't send the bearer, use cookie instead
            .antMatchers(HttpMethod.GET, "/api/fling/{flingId}/download/{downloadId}")
            .permitAll()
        .and()
        .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/api/artifacts/{flingId}/**")
            .access("@authorizationService.allowUpload(#flingId, authentication)")
        .and()
        .authorizeRequests()
            .antMatchers(HttpMethod.PATCH, "/api/artifacts/{artifactId}")
            .access("@authorizationService.allowPatchingArtifact(#artifactId, authentication)")
        .and()
        .authorizeRequests()
            // TODO: This is still insecure since URLs are not encrypted
            // TODO: iframe requests don't send the bearer, use cookie instead
            .antMatchers("/api/artifacts/{artifactId}/{downloadId}/download")
            .permitAll()
        .and()
        .authorizeRequests()
            // TODO: Security by request parameters is just not well supported with spring security
            // TODO: Change API
            .regexMatchers(HttpMethod.GET, "\\/api\\/fling\\?(shareId=|flingId=)[a-zA-Z0-9]+")
            .access("@authorizationService.allowFlingAccess(authentication, request)")
        .and()
        .authorizeRequests()
            // TODO: Security by request parameters is just not well supported with spring security
            // TODO: Change API
            .regexMatchers(HttpMethod.GET, "\\/api\\/artifacts\\?(shareId=|flingId=)[a-zA-Z0-9]+")
            .access("@authorizationService.allowFlingAccess(authentication, request)")
        .and()
        .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/api/fling/{flingId}/**")
            .access("@authorizationService.allowFlingAccess(#flingId, authentication)")
        .and()
        // And lastly, the owner is allowed everything
        .authorizeRequests()
            .antMatchers("/api/**")
            .hasAuthority(FlingAuthorities.FLING_ADMIN.getAuthority());

        //@formatter:on
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    // see https://stackoverflow.com/a/43559266

    log.info("Allowed origins: {}", allowedOrigins);

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("*"));

    // setAllowCredentials(true) is important, otherwise:
    // The value of the 'Access-Control-Allow-Origin' header in the response
    // must not be the wildcard '*' when the request's credentials mode is
    // 'include'.
    configuration.setAllowCredentials(true);

    // setAllowedHeaders is important! Without it, OPTIONS preflight request
    // will fail with 403 Invalid CORS request
    configuration
        .setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "Origin"));

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
