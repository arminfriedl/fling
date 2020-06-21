package net.friedl.fling.model.mapper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mapstruct.Mapper;
import lombok.extern.slf4j.Slf4j;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.persistence.entities.ArtifactEntity;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class ArtifactMapper {
  public abstract ArtifactDto map(ArtifactEntity artifactEntity);

  public abstract ArtifactEntity map(ArtifactDto artifactDto);

  public abstract List<ArtifactDto> map(List<ArtifactEntity> artifactEntities);

  public Optional<ArtifactDto> map(Optional<ArtifactEntity> artifactEntity) {
    return artifactEntity.map(a -> map(a));
  }

  public ArtifactDto merge(ArtifactDto originalArtifactDto, Map<String, Object> patch) {
    ArtifactDto mergedArtifactDto = new ArtifactDto();

    for (Field field : ArtifactDto.class.getDeclaredFields()) {
      String fieldName = field.getName();
      field.setAccessible(true);
      try {
        if (patch.containsKey(fieldName)) {
          if (field.getType().equals(Long.class)) {
            field.set(mergedArtifactDto, ((Number) patch.get(fieldName)).longValue());
          }
          field.set(mergedArtifactDto, patch.get(fieldName));
        } else {
          field.set(mergedArtifactDto, field.get(originalArtifactDto));
        }
      } catch (IllegalArgumentException | IllegalAccessException e) {
        log.error("Could not merge {} [value={}] with {}", fieldName, patch.get(fieldName),
            originalArtifactDto,
            e);
      }
    }

    return mergedArtifactDto;
  }
}
