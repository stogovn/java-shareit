package ru.practicum.shareit.user.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.GenerateId;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Component
public class InMemoryUserStorage extends GenerateId<User> implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();

    @Override
    public User create(User user) {
        checkEmail(user);
        user.setId(getNextId(users));
        users.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);
        log.info("Создался новый пользователь с id = {}", user.getId());

        return user;
    }

    @Override
    public User update(Long id, User newUser) {
        User oldUser = users.get(id);
        if (newUser.getEmail() != null && !oldUser.getEmail().equals(newUser.getEmail())) {
            checkEmail(newUser);
            usersByEmail.remove(oldUser.getEmail());
            usersByEmail.put(newUser.getEmail(), oldUser);
        }
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        log.info("Обновили пользователя с id = {}", id);
        return oldUser;
    }

    @Override
    public void deleteUserById(Long id) {
        User user = users.remove(id);
        usersByEmail.remove(user.getEmail());
        log.info("Пользователь с id = {} удален", id);
    }

    @Override
    public User getUserById(long id) {
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    protected void checkEmail(User user) {
        if (usersByEmail.containsKey(user.getEmail())) {
            log.error("При попытке обновления пользователя указан существующий email: {}", user.getEmail());
            throw new ConflictException("Этот email уже используется");
        }
    }
}
