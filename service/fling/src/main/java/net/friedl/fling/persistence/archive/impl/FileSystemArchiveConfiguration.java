package net.friedl.fling.persistence.archive.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConfigurationProperties("fling.archive.fileystem")
@ConditionalOnBean(FileSystemArchive.class)
@Getter
@Setter
public class FileSystemArchiveConfiguration {
  private String directory;

  @Bean
  public MessageDigest fileStoreDigest() throws NoSuchAlgorithmException {
    return MessageDigest.getInstance("SHA-512");
  }

  @PostConstruct
  public void init() throws IOException {
    if (directory == null) {
      log.info("Directory not configured take temp path");
      Path tmpPath = Files.createTempDirectory("fling");
      this.directory = tmpPath.toAbsolutePath().toString();
    }

    log.info("File store directory: {}", directory);
  }
}
