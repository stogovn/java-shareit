package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertNull;


class UserMapperTest {

    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void testToUserDto_NullInput() {
        User user = null; // Передаем null

        UserDto result = userMapper.toUserDto(user);

        // Проверяем, что результат тоже null
        assertNull(result, "Метод toUserDto должен вернуть null, если входной параметр null");
    }

    @Test
    void testDtoToUser_NullInput() {
        UserDto userDto = null; // Передаем null

        User result = userMapper.dtoToUser(userDto);

        // Проверяем, что результат тоже null
        assertNull(result, "Метод dtoToUser должен вернуть null, если входной параметр null");
    }
}