package com.standardinsurance.intrack.user;

import com.standardinsurance.intrack.user.dto.UserResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toResponse(UserEntity entity);
}
