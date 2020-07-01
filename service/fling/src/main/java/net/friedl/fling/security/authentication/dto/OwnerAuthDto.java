package net.friedl.fling.security.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "OwnerAuth")
public class OwnerAuthDto {
  private String username;
  private String password;
}
