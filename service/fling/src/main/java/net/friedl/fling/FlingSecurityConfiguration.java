package net.friedl.fling;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("fling.security")
public class FlingSecurityConfiguration {
  private String signingKey;

  @Bean
  public JwtParser jwtParser() {
    return Jwts.parserBuilder()
        .setSigningKey(jwtSigningKey())
        .build();
  }

  @Bean
  public Key jwtSigningKey() {
    byte[] key = signingKey.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(key);
  }
}
