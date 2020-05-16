package net.friedl.fling.model.mapper;

import org.mapstruct.Mapper;

import net.friedl.fling.model.dto.AuthCodeDto;
import net.friedl.fling.persistence.entities.AuthCodeEntity;

@Mapper(componentModel = "spring")
public interface AuthCodeMapper {
    AuthCodeEntity map(AuthCodeDto authCodeDto);
    AuthCodeDto map(AuthCodeEntity authCodeEntity);
}
