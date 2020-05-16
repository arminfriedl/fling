package net.friedl.fling.security.authentication.dto;

import lombok.Data;

@Data
public class UserAuthDto {
    Long flingId;
    String code;
}
