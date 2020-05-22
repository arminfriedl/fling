package net.friedl.fling;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class FlingConfiguration {
    @Bean
    public MessageDigest keyHashDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-512");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_ABSENT);
        return objectMapper;
    }
}
