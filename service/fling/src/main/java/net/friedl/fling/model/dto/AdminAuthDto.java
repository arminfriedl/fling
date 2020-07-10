package net.friedl.fling.model.dto;

import javax.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "AdminAuth")
public class AdminAuthDto {
  @NotNull
  private String adminName;

  @NotNull
  private String adminPassword;
}
