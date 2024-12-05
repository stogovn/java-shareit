package ru.practicum.shareit.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Named("toUserDto")
    UserDto toUserDto(User user);

    @Mapping(target = "id", source = "id")
    User dtoToUser(UserDto userDto);

}
