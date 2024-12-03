package ru.practicum.shareit.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserServiceImplTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser_Success() {
        //создаем объект UserDto
        UserDto userDto = new UserDto(null, "John Doe", "john.doe@example.com");

        //вызываем метод create
        UserDto savedUser = userService.create(userDto);

        //проверяем, что пользователь сохранен
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo(userDto.getName());
        assertThat(savedUser.getEmail()).isEqualTo(userDto.getEmail());

        // Дополнительно: проверяем, что пользователь сохранен в базе данных
        Optional<User> userFromDb = userRepository.findById(savedUser.getId());
        assertThat(userFromDb).isPresent();
        assertThat(userFromDb.get().getName()).isEqualTo(userDto.getName());
        assertThat(userFromDb.get().getEmail()).isEqualTo(userDto.getEmail());
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        //сохраняем пользователя с email
        userRepository.save(new User(null, "Existing User", "john.doe@example.com"));

        UserDto userDto = new UserDto(null, "John Doe", "john.doe@example.com");

        //ожидаем исключение ConflictException
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.create(userDto)
        );
        assertThat(exception.getMessage()).isEqualTo("Этот email уже используется");
    }

    @Test
    void testGetUserById_Success() {
        // сохраняем пользователя в базе
        User savedUser = userRepository.save(new User(null, "John Doe", "john.doe@example.com"));

        // получаем пользователя по id
        UserDto userDto = userService.getUserById(savedUser.getId());

        // проверяем, что данные корректны
        assertThat(userDto.getId()).isEqualTo(savedUser.getId());
        assertThat(userDto.getName()).isEqualTo(savedUser.getName());
        assertThat(userDto.getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void testGetUserById_UserNotFound() {
        // пытаемся получить пользователя с несуществующим id
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void testUpdateUser_Success() {
        // создаем и сохраняем пользователя в базе
        User existingUser = userRepository.save(new User(null, "John Doe", "john.doe@example.com"));

        // Создаем UserDto с обновленными данными
        UserDto updatedUserDto = new UserDto(existingUser.getId(), "John Smith", "john.smith@example.com");

        // обновляем пользователя
        UserDto updatedUser = userService.update(existingUser.getId(), updatedUserDto);

        // проверяем, что данные пользователя обновились
        assertThat(updatedUser.getId()).isEqualTo(existingUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("John Smith");
        assertThat(updatedUser.getEmail()).isEqualTo("john.smith@example.com");
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // пытаемся обновить пользователя с несуществующим id
        assertThrows(EntityNotFoundException.class, () -> userService.update(999L, new UserDto(999L, "Name", "email@example.com")));
    }

    @Test
    void testUpdateUser_EmailAlreadyExists() {
        // создаем двух пользователей, один из которых уже имеет тот же email
        userRepository.save(new User(null, "Existing User", "existing.user@example.com"));
        User existingUser = userRepository.save(new User(null, "John Doe", "john.doe@example.com"));

        // Создаем UserDto с email, который уже существует в базе данных
        UserDto updatedUserDto = new UserDto(existingUser.getId(), "John Doe", "existing.user@example.com");

        // ожидаем исключение при попытке обновления
        assertThrows(ConflictException.class, () -> userService.update(existingUser.getId(), updatedUserDto));
    }

    @Test
    void testDeleteUserById_Success() {
        // создаем и сохраняем пользователя в базе
        User user = userRepository.save(new User(null, "John Doe", "john.doe@example.com"));

        // удаляем пользователя по ID
        userService.deleteUserById(user.getId());

        // проверяем, что пользователь больше не существует в базе
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void testDeleteUserById_UserNotFound() {
        // пытаемся удалить пользователя с несуществующим ID
        assertThrows(NotFoundException.class, () -> userService.deleteUserById(999L));
    }

    @Test
    void testFindAll_Success() {
        // Arrange: создаем и сохраняем пользователей в базе данных
        userRepository.save(new User(null, "John Doe", "john.doe@example.com"));
        userRepository.save(new User(null, "Jane Smith", "jane.smith@example.com"));

        // Act: получаем всех пользователей через метод findAll
        Collection<UserDto> users = userService.findAll();

        // Assert: проверяем, что в результате запроса содержатся оба пользователя
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserDto::getName).containsExactlyInAnyOrder("John Doe", "Jane Smith");
        assertThat(users).extracting(UserDto::getEmail).containsExactlyInAnyOrder("john.doe@example.com", "jane.smith@example.com");
    }

    @Test
    void testFindAll_NoUsers() {
        // Act: вызываем метод findAll, когда в базе данных нет пользователей
        Collection<UserDto> users = userService.findAll();

        // Assert: проверяем, что метод возвращает пустую коллекцию
        assertThat(users).isEmpty();
    }

}