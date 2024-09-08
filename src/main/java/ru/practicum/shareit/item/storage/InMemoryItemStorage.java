package ru.practicum.shareit.item.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.GenerateId;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Getter
@Component
public class InMemoryItemStorage extends GenerateId<Item> implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Item>> itemsByUserId = new HashMap<>();

    @Override
    public Item create(Long userId, Item item) {
        item.setId(getNextId(items));
        item.setOwner(userId);
        items.put(item.getId(), item);
        itemsByUserId.computeIfAbsent(item.getOwner(), k -> new ArrayList<>()).add(item);
        log.info("Добавилась новая вещь с id = {} пользователя с id = {}", item.getId(), userId);

        return item;
    }

    @Override
    public Item update(Long userId, Long id, Item item) {
        Item oldItem = items.get(id);
        if (!Objects.equals(oldItem.getOwner(), userId)) {
            throw new ValidationException("Данный пользователь не может редактировать эту вещь");
        }
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        List<Item> userItems = itemsByUserId.get(userId);
        int index = userItems.indexOf(oldItem);
        if (index != -1) {
            userItems.set(index, oldItem);
        }
        log.info("Обновили вещь с id = {} пользователя с id = {}", item.getId(), userId);

        return oldItem;
    }

    @Override
    public Item getItemById(long id) {
        return items.get(id);
    }

    @Override
    public Collection<Item> getItemsByUserId(Long userId) {
        return itemsByUserId.getOrDefault(userId, Collections.emptyList());
    }

    @Override
    public Collection<Item> searchItems(String text) {
        String search = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(search) ||
                                item.getDescription().toLowerCase().contains(search))
                .filter(Item::getAvailable)
                .toList();
    }
}
