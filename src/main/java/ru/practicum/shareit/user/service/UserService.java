package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    UserDto create(User user);

    UserDto update(Long id, User user);

    void deleteUserById(Long id);

    UserDto getUserById(long id);

    Collection<UserDto> findAll();
}
