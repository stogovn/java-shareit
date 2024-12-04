package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testFindAllUsers_Success() throws Exception {
        // подготовка тестовых данных
        List<UserDto> users = List.of(
                new UserDto(1L, "Alice", "alice@example.com"),
                new UserDto(2L, "Bob", "bob@example.com")
        );

        // Настройка мока
        when(userService.findAll()).thenReturn(users);

        // отправка GET-запроса и проверка результата
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(users.size()))
                .andExpect(jsonPath("$[0].id").value(users.getFirst().getId()))
                .andExpect(jsonPath("$[0].name").value(users.getFirst().getName()))
                .andExpect(jsonPath("$[0].email").value(users.getFirst().getEmail()));
    }

    @Test
    void testFindAllUsers_EmptyList() throws Exception {
        // Настройка мока для возвращения пустого списка
        when(userService.findAll()).thenReturn(Collections.emptyList());

        // отправка GET-запроса и проверка результата
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void testGetUserById_Success() throws Exception {
        // подготовка тестовых данных
        Long userId = 1L;
        UserDto userDto = new UserDto(userId, "Alice", "alice@example.com");

        // Настройка мока для возвращения тестового пользователя
        when(userService.getUserById(userId)).thenReturn(userDto);

        // отправка GET-запроса и проверка результата
        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        // настройка мока для выбрасывания исключения при поиске пользователя
        long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new EntityNotFoundException("User not found"));

        // проверка, что выбрасывается исключение 404 Not Found
        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // подготовка тестовых данных
        UserDto userDto = new UserDto(null, "John Doe", "johndoe@example.com");
        UserDto createdUserDto = new UserDto(1L, "John Doe", "johndoe@example.com");

        // Настройка мока для возвращения созданного пользователя
        when(userService.create(userDto)).thenReturn(createdUserDto);

        // отправка POST-запроса и проверка результата
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdUserDto.getId()))
                .andExpect(jsonPath("$.name").value(createdUserDto.getName()))
                .andExpect(jsonPath("$.email").value(createdUserDto.getEmail()));
    }

    @Test
    void testCreateUser_EmailAlreadyExists() throws Exception {
        UserDto userDto = new UserDto(null, "John Doe", "john.doe@example.com");

        // Симулируем выброс ConflictException в сервисе
        when(userService.create(any(UserDto.class)))
                .thenThrow(new ConflictException("Этот email уже используется"));

        mockMvc.perform(post("/users") // Укажите правильный URL для создания пользователя
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDto)))
                .andExpect(status().isConflict()) // Проверяем, что возвращается 409 CONFLICT
                .andExpect(jsonPath("$.error").value("Этот email уже используется")); // Проверяем тело ответа
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // подготовка тестовых данных
        Long userId = 1L;
        UserDto newUserDto = new UserDto(userId, "John Doe Updated", "john.doe.updated@example.com");
        UserDto updatedUserDto = new UserDto(userId, "John Doe Updated", "john.doe.updated@example.com");

        // Настройка мока для возвращения обновленного пользователя
        when(userService.update(userId, newUserDto)).thenReturn(updatedUserDto);

        // отправка PATCH-запроса и проверка результата
        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedUserDto.getId()))
                .andExpect(jsonPath("$.name").value(updatedUserDto.getName()))
                .andExpect(jsonPath("$.email").value(updatedUserDto.getEmail()));
    }

    @Test
    void testUpdateUser_NotFound() throws Exception {
        // подготовка тестовых данных
        Long userId = 1L;
        UserDto newUserDto = new UserDto(userId, "John Doe Updated", "john.doe.updated@example.com");

        // Настройка мока для выбрасывания исключения, если пользователь не найден
        when(userService.update(userId, newUserDto)).thenThrow(new EntityNotFoundException("User not found"));

        // проверка, что выбрасывается исключение 404 Not Found
        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newUserDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUser_BadRequest() throws Exception {
        // подготовка тестовых данных (например, недопустимый email)
        Long userId = 1L;
        UserDto newUserDto = new UserDto(userId, "John Doe Updated", "invalid-email");

        // Настройка мока для выбрасывания исключения при обновлении пользователя
        when(userService.update(userId, newUserDto)).thenThrow(new IllegalArgumentException("Invalid email format"));

        // проверка, что выбрасывается исключение 400 Bad Request
        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteUserById_Success() throws Exception {
        // подготовка данных
        Long userId = 1L;

        // Настройка мока, чтобы метод deleteUserById не выбрасывал исключений
        Mockito.doNothing().when(userService).deleteUserById(userId);

        // отправка DELETE-запроса и проверка статуса ответа
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteUserById_NotFound() throws Exception {
        // подготовка данных
        Long userId = 1L;

        // Настройка мока для выбрасывания исключения, если пользователь не найден
        Mockito.doThrow(new NotFoundException("User not found")).when(userService).deleteUserById(userId);

        // проверка, что выбрасывается исключение 404 Not Found
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUserById_BadRequest() throws Exception {
        // подготовка данных с неверным ID
        Long invalidUserId = -1L;

        // Настройка мока для выбрасывания исключения при попытке удаления пользователя с неверным ID
        Mockito.doThrow(new IllegalArgumentException("Invalid user ID")).when(userService).deleteUserById(invalidUserId);

        // проверка, что выбрасывается исключение 400 Bad Request
        mockMvc.perform(delete("/users/{id}", invalidUserId))
                .andExpect(status().isBadRequest());
    }

}