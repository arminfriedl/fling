package net.friedl.fling.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
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
  private List<String> allowedOrigins;

  private String adminUser;

  private String adminPassword;

  private String signingKey;

  private Long jwtExpiration;

  @Bean
  public Key jwtSigningKey() {
    byte[] key = signingKey.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(key);
  }

  @Bean
  public JwtParser jwtParser(Key jwtSignigKey) {
    return Jwts.parserBuilder()
        .setSigningKey(jwtSignigKey)
        .build();
  }
}
