package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Map;

public interface ItemStorage {
    Item create(Long userId, Item item);

    Item update(Long userId, Long id, Item item);

    Item getItemById(long id);

    Collection<Item> getItemsByUserId(Long userId);

    Collection<Item> searchItems(String text);

    Map<Long, Item> getItems();
}
