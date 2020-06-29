package net.friedl.fling.model.dto;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactDto {
  @NotNull
  private UUID id;

  @NotNull
  private Path path;
  
  @Builder.Default
  private Instant uploadTime = Instant.now();

  private String archiveId;

  @Builder.Default
  private Boolean archived = false;
}
