package net.friedl.fling.model.mapper;

import java.util.List;
import java.util.Optional;

import org.mapstruct.Mapper;

import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.persistence.entities.FlingEntity;

@Mapper(componentModel = "spring")
public interface FlingMapper {
    FlingDto map(FlingEntity flingEntity);

    default Optional<FlingDto> map(Optional<FlingEntity> flingEntity) {
        return flingEntity.map(f -> map(f));
    }

    FlingEntity map(FlingDto flingDto);

    List<FlingDto> map(List<FlingEntity> flingEntities);
}
