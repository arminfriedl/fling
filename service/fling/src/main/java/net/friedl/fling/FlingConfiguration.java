package net.friedl.fling;

import java.nio.file.Path;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.friedl.fling.model.json.PathDeserializer;
import net.friedl.fling.model.json.PathSerializer;

@Configuration
public class FlingConfiguration {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public ObjectMapper objectMapper() {
    SimpleModule simpleModule = new SimpleModule();
    simpleModule
        .addDeserializer(Path.class, new PathDeserializer())
        .addSerializer(Path.class, new PathSerializer());

    ObjectMapper objectMapper = new ObjectMapper()
        .setSerializationInclusion(Include.NON_ABSENT)
        .registerModule(new JavaTimeModule())
        // Handle instant as milliseconds
        .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .registerModule(simpleModule);

    return objectMapper;
  }
}
