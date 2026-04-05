package com.tictactoe.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.tictactoe.auth.dto.RegisterRequest;
import com.tictactoe.auth.dto.UserDto;
import com.tictactoe.auth.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "enabled", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User toEntity(RegisterRequest request);

	@Mapping(target = "role", expression = "java(user.getRole().name())")
	UserDto toDto(User user);
}
