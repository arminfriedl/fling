package net.friedl.fling.model.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.persistence.entities.ArtifactEntity;

@Mapper(componentModel = "spring")
public interface ArtifactMapper {
  ArtifactDto map(ArtifactEntity artifactEntity);
  ArtifactEntity map(ArtifactDto artifactDto);

  List<ArtifactDto> mapEntities(List<ArtifactEntity> artifactEntities);
  List<ArtifactEntity> mapDtos(List<ArtifactDto> artifactDtos);
}
