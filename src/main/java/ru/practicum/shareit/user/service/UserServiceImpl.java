package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto create(User user) {
        return UserMapper.toUserDto(userStorage.create(user));
    }

    @Override
    public UserDto update(Long id, User newUser) {
        validateId(id);
        return UserMapper.toUserDto(userStorage.update(id, newUser));
    }

    @Override
    public void deleteUserById(Long id) {
        validateId(id);
        userStorage.deleteUserById(id);
    }

    @Override
    public UserDto getUserById(long id) {
        validateId(id);
        return UserMapper.toUserDto(userStorage.getUserById(id));
    }

    @Override
    public Collection<UserDto> findAll() {
        return userStorage.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    private void validateId(Long id) {
        if (!userStorage.getUsers().containsKey(id)) {
            log.error("Указан несуществующий пользователь с id: {}", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }
}
