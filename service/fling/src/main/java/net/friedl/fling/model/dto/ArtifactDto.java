package net.friedl.fling.model.dto;

import java.time.Instant;
import lombok.Data;

@Data
public class ArtifactDto {
  private String name;

  private Long id;

  private String path;

  private String doi;

  private Long size;

  private Integer version;

  private Instant uploadTime;

  private FlingDto fling;
}
