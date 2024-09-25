package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto create(Long userId, Item item) {
        checkUserId(userId);
        return ItemMapper.toItemDto(itemStorage.create(userId, item));
    }

    @Override
    public ItemDto update(Long userId, Long id, Item item) {
        checkUserId(userId);
        return ItemMapper.toItemDto(itemStorage.update(userId, id, item));
    }

    @Override
    public ItemDto getItemById(long id) {
        return ItemMapper.toItemDto(itemStorage.getItemById(id));
    }

    @Override
    public Collection<ItemDto> getItemsByUserId(Long userId) {
        checkUserId(userId);
        return itemStorage.getItemsByUserId(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        return itemStorage.searchItems(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private void checkUserId(Long userId) {
        if (!userStorage.getUsers().containsKey(userId)) {
            log.error("Указан несуществующий пользователь с id: {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }
}
