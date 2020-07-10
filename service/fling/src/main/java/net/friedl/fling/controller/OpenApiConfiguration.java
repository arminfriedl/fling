package net.friedl.fling.controller;

import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("fling.api")
public class OpenApiConfiguration {

  private String version;
  private String serverUrl;
  private String serverDescription;

  @Bean
  public OpenAPI openApi() {
    OpenAPI openApi = new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("bearer", bearerScheme())
            .addSchemas("resource", new Schema<Resource>()
                .type("string").format("binary")))
        .info(apiInfo());

    serverItem().ifPresent(openApi::addServersItem);

    return openApi;
  }

  public SecurityScheme bearerScheme() {
    return new SecurityScheme()
        .name("bearerAuth")
        .type(Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT");
  }

  public Info apiInfo() {
    return new Info()
        .contact(new Contact()
            .name("Armin Friedl")
            .url("https://www.friedl.net")
            .email("dev@friedl.net"))
        .license(new License()
            .name("The MIT License (MIT)"))
        .title("The Fling API")
        .description("Share file collections with expiration, protection and short urls")
        .version(version);
  }

  public Optional<Server> serverItem() {
    if (serverUrl == null) return Optional.empty();

    return Optional.of(
        new Server()
            .description(serverDescription)
            .url(serverUrl));
  }
}