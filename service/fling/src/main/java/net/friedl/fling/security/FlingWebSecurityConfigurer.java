package net.friedl.fling.security;

import static net.friedl.fling.security.FlingAuthorities.FLING_ADMIN;
import static net.friedl.fling.security.FlingAuthorities.FLING_USER;
import static org.springframework.security.config.Customizer.withDefaults;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.security.authentication.filter.BearerAuthenticationFilter;
import net.friedl.fling.security.authentication.filter.TokenAuthenticationFilter;
import net.friedl.fling.service.AuthorizationService;

@Slf4j
@EnableWebSecurity
@ConfigurationProperties(prefix = "fling.security")
@Getter
@Setter
public class FlingWebSecurityConfigurer extends WebSecurityConfigurerAdapter {
  private List<String> allowedOrigins;

  private TokenAuthenticationFilter tokenAuthenticationFilter;
  private BearerAuthenticationFilter bearerAuthenticationFilter;
  private AuthorizationService authorizationService;

  @Autowired
  public FlingWebSecurityConfigurer(
      TokenAuthenticationFilter tokenAuthenticationFilter,
      BearerAuthenticationFilter bearerAuthenticationFilter,
      AuthorizationService authorizationService) {

    this.tokenAuthenticationFilter = tokenAuthenticationFilter;
    this.bearerAuthenticationFilter = bearerAuthenticationFilter;
    this.authorizationService = authorizationService;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    //@formatter:off
         http
        .csrf().disable()
        .cors(withDefaults())
        
        /**********************************************/
        /** Authentication Interceptor Configuration **/
        /**********************************************/
        .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(bearerAuthenticationFilter, TokenAuthenticationFilter.class)
        // Do not keep authorization token in session. This would interfere with bearer authentication
        // in that it is possible to authenticate without a bearer token if the session is kept.
        // Turn off this confusing and non-obvious behavior.
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

        
        /*************************************/
        /** API Authorization Configuration **/
        /*************************************/
        //! Go from most specific to more !//
        //! general, as first hit counts  !//

        /**********************************/
        /** Authorization for: /api/auth **/
        /**********************************/
        .authorizeRequests()
            .antMatchers("/api/auth/derive")
            .hasAnyAuthority(FLING_ADMIN.getAuthority(), FLING_USER.getAuthority())
        .and()
        .authorizeRequests()
            .antMatchers("/api/auth/**")
            .permitAll()
        .and()


        /***********************************/
        /** Authorization for: /api/fling **/
        /***********************************/
        .authorizeRequests()
          .antMatchers(HttpMethod.GET, "/api/fling/share/{shareId}")
          .access("@authorizationService.allowFlingAccessByShareId(#shareId, authentication)")
        .and()
        .authorizeRequests()
          .antMatchers(HttpMethod.GET, "/api/fling/{flingId}/**")
          .access("@authorizationService.allowFlingAccess(#flingId, authentication)")
        .and()
       .authorizeRequests()
          .antMatchers(HttpMethod.POST, "/api/fling/{flingId}/artifact")
          .access("@authorizationService.allowUpload(#flingId, authentication)")
        .and()
        // only admin can create, delete and list flings
        .authorizeRequests()
          .antMatchers(HttpMethod.DELETE, "/api/fling/{flingId}")
          .hasAnyAuthority(FLING_ADMIN.getAuthority())
        .and()
        .authorizeRequests()
          .antMatchers(HttpMethod.POST, "/api/fling")
          .hasAuthority(FLING_ADMIN.getAuthority())
        .and()
        .authorizeRequests()
          .antMatchers(HttpMethod.GET, "/api/fling")
          .hasAuthority(FLING_ADMIN.getAuthority())
        .and()


        /***************************************/
        /** Authorization for: /api/artifacts **/
        /***************************************/
        .authorizeRequests()
          .antMatchers(HttpMethod.GET, "/api/artifacts/{artifactId}/**")
          .access("@authorizationService.allowArtifactAccess(#artifactId, authentication)")
        .and()
        .authorizeRequests()
          .antMatchers(HttpMethod.POST, "/api/artifacts/{artifactId}/data")
          .access("@authorizationService.allowArtifactUpload(#artifactId, authentication)")
        .and()
        .authorizeRequests()
          .antMatchers(HttpMethod.DELETE, "/api/artifacts/{artifactId}")
          .access("@authorizationService.allowArtifactUpload(#artifactId, authentication)");

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
