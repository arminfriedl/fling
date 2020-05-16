package net.friedl.fling.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.security.authentication.JwtAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@Getter
@Setter
public class FlingWebSecurityConfigurer extends WebSecurityConfigurerAdapter {
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private AuthorizationService authorizationService;
    private FlingSecurityConfiguration securityConfiguration;

    @Autowired
    public FlingWebSecurityConfigurer(JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthorizationService authorizationService,
            FlingSecurityConfiguration securityConfiguraiton) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authorizationService = authorizationService;
        this.securityConfiguration = securityConfiguraiton;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http
        .csrf().disable()
        .cors(withDefaults())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
            .antMatchers("/api/auth/**")
            .permitAll()
        .and()
        .authorizeRequests()
            .antMatchers("/api/**")
            .hasAuthority(FlingAuthority.FLING_OWNER.name())
        .and()
        .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/api/artifacts/{flingId}/**")
            .access("hasAuthority('"+FlingAuthority.FLING_USER.name()+"') and @authorizationService.allowUpload(#flingId)")
        .and()
        .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/api/artifacts/**")
            .hasAuthority(FlingAuthority.FLING_USER.name());
        //@formatter:on
    }

    private RequestMatcher modificationMethodsAntMatcher(String antPattern) {
        return multiMethodAntMatcher(antPattern,
                HttpMethod.PATCH, HttpMethod.PUT,
                HttpMethod.POST, HttpMethod.DELETE);
    }

    private RequestMatcher multiMethodAntMatcher(String antPattern, HttpMethod... httpMethods) {
        List<RequestMatcher> antMatchers = Arrays.stream(httpMethods)
                .map(m -> new AntPathRequestMatcher(antPattern, m.toString()))
                .collect(Collectors.toList());

        return new OrRequestMatcher(antMatchers);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // see https://stackoverflow.com/a/43559266

        log.info("Allowed origins: {}", securityConfiguration.getAllowedOrigins());

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityConfiguration.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("*"));

        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response
        // must not be the wildcard '*' when the request's credentials mode is
        // 'include'.
        configuration.setAllowCredentials(true);

        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "Origin"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
