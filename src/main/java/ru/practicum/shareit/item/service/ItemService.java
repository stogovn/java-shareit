package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemBookingsDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {
    ItemDto create(Long userId, Item item);

    ItemDto update(Long userId, Long id, Item item);

    ItemInfoDto getItemById(Long userId, Long itemId);

    Collection<ItemDto> searchItems(String text);

    Collection<ItemBookingsDto> getAllItemsForUser(Long userId);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}
