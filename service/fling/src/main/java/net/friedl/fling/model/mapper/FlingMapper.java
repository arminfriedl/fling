package net.friedl.fling.model.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.persistence.entities.FlingEntity;

@Mapper(componentModel = "spring")
public interface FlingMapper {
  FlingDto map(FlingEntity flingEntity);
  FlingEntity map(FlingDto flingDto);

  List<FlingDto> mapEntities(List<FlingEntity> flingEntities);
  List<FlingEntity> mapDtos(List<FlingDto> flingDtos);
}
