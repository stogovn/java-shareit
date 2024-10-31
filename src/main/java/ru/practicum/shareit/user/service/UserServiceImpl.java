package ru.practicum.shareit.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;


import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto create(User user) {
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserDto update(Long id, User newUser) {
        User existingUser = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (newUser.getEmail() != null && !existingUser.getEmail().equals(newUser.getEmail())) {
            checkEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
        }
        existingUser.setEmail(newUser.getEmail());

        User updateUser = userRepository.save(existingUser);
        return UserMapper.toUserDto(updateUser);
    }

    @Override
    public void deleteUserById(Long id) {
        validateId(id);
        userRepository.deleteById(id);
    }

    @Override
    public UserDto getUserById(long id) {
        User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    private void validateId(Long id) {
        if (!userRepository.existsById(id)) {
            log.error("Указан несуществующий пользователь с id: {}", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    protected void checkEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("При попытке обновления пользователя указан существующий email: {}", email);
            throw new ConflictException("Этот email уже используется");
        }
    }
}
