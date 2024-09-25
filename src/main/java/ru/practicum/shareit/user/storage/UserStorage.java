package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserStorage {
    User create(User user);

    User update(Long id, User user);

    void deleteUserById(Long id);

    User getUserById(long id);

    Collection<User> findAll();

    Map<Long, User> getUsers();
}
