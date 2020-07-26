package net.friedl.fling.model.dto;

import java.time.Instant;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Fling")
public class FlingDto {
  @Schema(accessMode = AccessMode.READ_ONLY, type = "string")
  private UUID id;

  @Schema(description = "Name of the fling")
  @NotNull
  private String name;

  @Schema(type = "integer", format = "int64", accessMode = AccessMode.READ_ONLY,
      description = "Creation time in milliseconds since the unix epoch 01.01.1970 00:00:00 UTC")
  private Instant creationTime;

  @Schema(description = "Share id of the fling. Used in the share link.")
  private String shareId;

  @Schema(description = "Authentication code for password protecting a fling.")
  private String authCode;

  @Schema(description = "Whether users should be redirected to fling download when accessing the "
      + "fling by share id")
  @Builder.Default
  private Boolean directDownload = false;

  @Schema(description = "Allow uploads to the fling by users")
  @Builder.Default
  private Boolean allowUpload = false;

  @Schema(description = "Whether the fling is accessible by users via the share id")
  @Builder.Default
  private Boolean shared = true;

  @Schema(description = "How many clicks are left until the fling access by share id is disallowed")
  private Integer expirationClicks;

  @Schema(type = "integer", format = "int64",
      description = "Expiration time in milliseconds since the unix epoch 01.01.1970 00:00:00 UTC")
  private Instant expirationTime;
}
