package net.friedl.fling.model.mapper;

import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.persistence.entities.ArtifactEntity;

@Mapper(componentModel = "spring")
public interface ArtifactMapper {
  ArtifactDto map(ArtifactEntity artifactEntity);

  ArtifactEntity map(ArtifactDto artifactDto);

  List<ArtifactDto> mapEntities(List<ArtifactEntity> artifactEntities);

  Set<ArtifactDto> mapEntities(Set<ArtifactEntity> artifactEntities);

  List<ArtifactEntity> mapDtos(List<ArtifactDto> artifactDtos);
}
