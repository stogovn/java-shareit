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
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        User user = userMapper.dtoToUser(userDto);
        User savedUser = userRepository.save(user);
        return userMapper.toUserDto(savedUser);
    }

    @Transactional
    @Override
    public UserDto update(Long id, UserDto newUserDto) {
        User existingUser = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (newUserDto.getEmail() != null && !existingUser.getEmail().equals(newUserDto.getEmail())) {
            checkEmail(newUserDto.getEmail());
        }
        if (newUserDto.getName() != null) {
            existingUser.setName(newUserDto.getName());
        }
        existingUser.setEmail(newUserDto.getEmail());

        User updateUser = userRepository.save(existingUser);
        return userMapper.toUserDto(updateUser);
    }

    @Override
    public void deleteUserById(Long id) {
        validateId(id);
        userRepository.deleteById(id);
    }

    @Override
    public UserDto getUserById(long id) {
        User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return userMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserDto)
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
