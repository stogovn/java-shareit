package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.validation.OnCreate;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserClient userClient;

    @PostMapping
    @Validated({OnCreate.class})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@Valid @RequestBody UserRequestDto userDto) {
        log.info("==> Gateway: Creating user: {}", userDto);

        // Перенаправляем запрос на сервер
        ResponseEntity<Object> response = userClient.createUser(userDto);

        log.info("<== Gateway: User created: {}", response.getBody());
        return response;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") Long id, @Valid @RequestBody UserRequestDto newUserDto) {
        log.info("==>Gateway:  Updating user: {}", newUserDto);
        // Перенаправляем запрос на сервер
        ResponseEntity<Object> response = userClient.updateUser(id, newUserDto);
        log.info("<==Gateway:  Updating user: {}", response.getBody());
        return response;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable("id") Long id) {
        return userClient.getUserById(id);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        return userClient.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUserById(@PathVariable("id") Long id) {
        return userClient.deleteUserById(id);
    }
}
