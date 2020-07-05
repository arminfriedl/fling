package net.friedl.fling.model.dto;

import java.nio.file.Path;
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
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Artifact")
public class ArtifactDto {
  @Schema(accessMode = AccessMode.READ_ONLY, type = "string")
  @NotNull
  private UUID id;

  @Schema(type = "string",
      description = "Path of the artifact")
  @NotNull
  private Path path;

  @Schema(type = "integer", format = "int64",
      description = "Upload time in milliseconds since the unix epoch 01.01.1970 00:00:00 UTC")
  @Builder.Default
  private Instant uploadTime = Instant.now();

  @Schema(accessMode = AccessMode.READ_ONLY, type = "boolean",
      description = "Whether the artifact was successfully persisted in the archive.")
  @Builder.Default
  private Boolean archived = false;
}
