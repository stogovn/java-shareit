package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {
    ItemDto create(Long userId, Item item);

    ItemDto update(Long userId, Long id, Item item);

    ItemDto getItemById(long id);

    Collection<ItemDto> getItemsByUserId(Long userId);

    Collection<ItemDto> searchItems(String text);
}
