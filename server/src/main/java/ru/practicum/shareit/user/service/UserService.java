package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(Long id, UserDto userDto);

    void deleteUserById(Long id);

    UserDto getUserById(long id);

    Collection<UserDto> findAll();
}
