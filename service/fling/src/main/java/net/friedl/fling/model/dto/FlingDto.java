package net.friedl.fling.model.dto;

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
public class FlingDto {
  @NotNull
  private UUID id;

  @NotNull
  private String name;

  @NotNull
  @Builder.Default
  private Instant creationTime = Instant.now();

  @NotNull
  private String shareId;

  private String authCode;

  @Builder.Default
  private Boolean directDownload = false;

  @Builder.Default
  private Boolean allowUpload = false;

  @Builder.Default
  private Boolean shared = true;

  private Integer expirationClicks;

  private Instant expirationTime;
}
